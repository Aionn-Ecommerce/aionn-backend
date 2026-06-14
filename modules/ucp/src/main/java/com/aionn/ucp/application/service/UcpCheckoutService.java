package com.aionn.ucp.application.service;

import com.aionn.sharedkernel.util.IdGenerator;
import com.aionn.ucp.application.dto.catalog.CatalogProductDto;
import com.aionn.ucp.application.dto.checkout.CheckoutDtos;
import com.aionn.ucp.application.dto.discount.DiscountDtos;
import com.aionn.ucp.application.dto.envelope.UcpMessage;
import com.aionn.ucp.application.dto.fulfillment.FulfillmentDtos;
import com.aionn.ucp.application.port.out.CatalogQueryPort;
import com.aionn.ucp.application.port.out.CartSessionPersistencePort;
import com.aionn.ucp.application.port.out.CheckoutSessionPersistencePort;
import com.aionn.ucp.application.port.out.CheckoutSessionPersistencePort.LineCodec;
import com.aionn.ucp.application.port.out.CheckoutSessionPersistencePort.LineItemSnapshot;
import com.aionn.ucp.application.port.out.CheckoutSessionPersistencePort.Session;
import com.aionn.ucp.application.port.out.OrderPlacementPort;
import com.aionn.ucp.application.port.out.PromotionQueryPort;
import com.aionn.ucp.application.port.out.ShippingQueryPort;
import com.aionn.ucp.domain.exception.UcpErrorCode;
import com.aionn.ucp.domain.exception.UcpException;
import com.aionn.ucp.domain.model.CapabilityName;
import com.aionn.ucp.domain.model.CheckoutSessionStatus;
import com.aionn.ucp.infrastructure.config.UcpProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UcpCheckoutService {

    private final CatalogQueryPort catalogQueryPort;
    private final CheckoutSessionPersistencePort sessionRepository;
    private final LineCodec lineCodec;
    private final UcpEnvelopeFactory envelopeFactory;
    private final UcpProperties properties;
    private final OrderPlacementPort orderPlacementPort;
    private final ShippingQueryPort shippingQueryPort;
    private final PromotionQueryPort promotionQueryPort;
    private final CartSessionPersistencePort cartRepository;

    public CheckoutDtos.CheckoutResponse create(CheckoutDtos.CreateRequest request, String userId) {
        if (request.cart_id() != null && !request.cart_id().isBlank()) {
            return createFromCart(request.cart_id(), userId, request);
        }

        if (request == null || request.line_items() == null || request.line_items().isEmpty()) {
            throw new UcpException(UcpErrorCode.CHECKOUT_LINE_ITEMS_REQUIRED);
        }

        List<String> ids = request.line_items().stream().map(li -> li.item().id()).toList();
        CatalogQueryPort.LookupResult lookup = catalogQueryPort.lookup(ids);
        if (lookup.products().isEmpty()) {
            return CheckoutDtos.CheckoutResponse.error(
                    envelopeFactory.error(CapabilityName.CHECKOUT),
                    List.of(UcpMessage.error("out_of_stock",
                            "All requested items are unavailable",
                            "unrecoverable")),
                    properties.getEndpointBaseUrl());
        }

        List<LineItemSnapshot> snapshots = new ArrayList<>(request.line_items().size());
        long subtotal = 0;
        String currency = "VND";
        List<UcpMessage> messages = new ArrayList<>();
        List<CheckoutDtos.CheckoutLineItem> outLines = new ArrayList<>();

        for (CheckoutDtos.CreateLineItem requested : request.line_items()) {
            String itemId = requested.item().id();
            CatalogProductDto.Variant variant = findVariantById(lookup.products(), itemId);
            if (variant == null || variant.price() == null) {
                messages.add(UcpMessage.warning("item_unavailable",
                        "Item " + itemId + " is not available"));
                continue;
            }
            currency = variant.price().currency();
            long unitMinor = variant.price().amount();
            long lineTotal = unitMinor * requested.quantity();
            subtotal += lineTotal;
            snapshots.add(new LineItemSnapshot(itemId, requested.quantity(), unitMinor, variant.title()));
            outLines.add(new CheckoutDtos.CheckoutLineItem(
                    "li_" + (outLines.size() + 1),
                    new CheckoutDtos.CheckoutItem(itemId, variant.title(), unitMinor, null),
                    requested.quantity(),
                    List.of(
                            new CheckoutDtos.Total("subtotal", lineTotal, null),
                            new CheckoutDtos.Total("total", lineTotal, null))));
        }

        if (snapshots.isEmpty()) {
            return CheckoutDtos.CheckoutResponse.error(
                    envelopeFactory.error(CapabilityName.CHECKOUT),
                    List.of(UcpMessage.error("out_of_stock",
                            "None of the requested items are purchasable",
                            "unrecoverable")),
                    properties.getEndpointBaseUrl());
        }

        List<String> lineItemIds = outLines.stream().map(CheckoutDtos.CheckoutLineItem::id).toList();
        FulfillmentDtos.Fulfillment fulfillment = buildFulfillment(lineItemIds, null, null, subtotal, currency);

        long discountTotal = 0;
        DiscountDtos.DiscountResponse discountResponse = null;
        if (request.discounts() != null && request.discounts().codes() != null
                && !request.discounts().codes().isEmpty()) {
            DiscountResult dr = processDiscountCodes(request.discounts().codes(), userId, subtotal, currency, messages);
            discountTotal = dr.totalDiscount();
            discountResponse = dr.response();
        }

        long shippingFee = 0;
        if (fulfillment != null && fulfillment.methods() != null && !fulfillment.methods().isEmpty()) {
            var method = fulfillment.methods().get(0);
            if (method.groups() != null && !method.groups().isEmpty()) {
                var group = method.groups().get(0);
                if (group.options() != null && !group.options().isEmpty()) {
                    var option = group.options().get(0);
                    if (option.totals() != null && !option.totals().isEmpty()) {
                        shippingFee = option.totals().get(0).amount();
                    }
                }
            }
        }

        long totalAmount = subtotal - discountTotal + shippingFee;
        List<CheckoutDtos.Total> totals = buildTotals(subtotal, shippingFee, discountTotal, totalAmount);

        CheckoutSessionStatus status = determineStatus(request.buyer(), request.fulfillment());

        String sessionId = "chk_" + IdGenerator.ulid();
        Instant now = Instant.now();
        String expiresAt = now.plus(30, ChronoUnit.MINUTES).toString();
        Session session = new Session(
                sessionId, userId, null, request.webhook_url(),
                status, currency,
                lineCodec.encode(snapshots),
                totalsJson(subtotal, totalAmount),
                null, null, buildContinueUrl(sessionId),
                now, now);
        sessionRepository.save(session);

        return new CheckoutDtos.CheckoutResponse(
                envelopeFactory.ok(CapabilityName.CHECKOUT),
                sessionId,
                status.toWireFormat(),
                currency,
                outLines,
                request.buyer(),
                fulfillment,
                discountResponse,
                totals,
                buildLinks(),
                messages.isEmpty() ? null : messages,
                buildContinueUrl(sessionId),
                null,
                expiresAt);
    }

    @Transactional(readOnly = true)
    public CheckoutDtos.CheckoutResponse get(String sessionId) {
        Session s = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new UcpException(UcpErrorCode.CHECKOUT_NOT_FOUND));
        return toResponse(s);
    }

    public CheckoutDtos.CheckoutResponse update(String sessionId, String userId,
            CheckoutDtos.UpdateRequest request) {
        Session s = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new UcpException(UcpErrorCode.CHECKOUT_NOT_FOUND));
        if (s.status().isTerminal()) {
            throw new UcpException(UcpErrorCode.CHECKOUT_INVALID_STATE,
                    "Session " + sessionId + " is in terminal state " + s.status());
        }
        if (s.userId() != null && !s.userId().equals(userId)) {
            throw new UcpException(UcpErrorCode.CHECKOUT_FORBIDDEN);
        }

        List<LineItemSnapshot> snapshots;
        List<CheckoutDtos.CheckoutLineItem> outLines;
        long subtotal;
        String currency = s.currency();
        List<UcpMessage> messages = new ArrayList<>();

        if (request.line_items() != null && !request.line_items().isEmpty()) {
            List<String> ids = request.line_items().stream().map(li -> li.item().id()).toList();
            CatalogQueryPort.LookupResult lookup = catalogQueryPort.lookup(ids);
            snapshots = new ArrayList<>();
            outLines = new ArrayList<>();
            subtotal = 0;
            for (CheckoutDtos.CreateLineItem li : request.line_items()) {
                CatalogProductDto.Variant variant = findVariantById(lookup.products(), li.item().id());
                if (variant == null || variant.price() == null) {
                    messages.add(UcpMessage.warning("item_unavailable",
                            "Item " + li.item().id() + " is not available"));
                    continue;
                }
                currency = variant.price().currency();
                long unit = variant.price().amount();
                long lineTotal = unit * li.quantity();
                subtotal += lineTotal;
                snapshots.add(new LineItemSnapshot(li.item().id(), li.quantity(), unit, variant.title()));
                outLines.add(new CheckoutDtos.CheckoutLineItem(
                        "li_" + (outLines.size() + 1),
                        new CheckoutDtos.CheckoutItem(li.item().id(), variant.title(), unit, null),
                        li.quantity(),
                        List.of(new CheckoutDtos.Total("subtotal", lineTotal, null),
                                new CheckoutDtos.Total("total", lineTotal, null))));
            }
        } else {
            snapshots = lineCodec.decode(s.lineItemsJson());
            outLines = rebuildLineItems(snapshots);
            subtotal = snapshots.stream().mapToLong(sn -> sn.unitPriceMinor() * sn.quantity()).sum();
        }

        List<String> lineItemIds = outLines.stream().map(CheckoutDtos.CheckoutLineItem::id).toList();
        FulfillmentDtos.Fulfillment fulfillment = buildFulfillment(lineItemIds,
                request.fulfillment() != null ? "VN" : null, "VN", subtotal, currency);

        long discountTotal = 0;
        DiscountDtos.DiscountResponse discountResponse = null;
        if (request.discounts() != null && request.discounts().codes() != null) {
            DiscountResult dr = processDiscountCodes(request.discounts().codes(), userId, subtotal, currency, messages);
            discountTotal = dr.totalDiscount();
            discountResponse = dr.response();
        }

        long shippingFee = getDefaultShippingFee(fulfillment);
        long totalAmount = subtotal - discountTotal + shippingFee;
        List<CheckoutDtos.Total> totals = buildTotals(subtotal, shippingFee, discountTotal, totalAmount);

        CheckoutSessionStatus newStatus = determineStatus(request.buyer(), null);
        Instant now = Instant.now();
        Session updated = new Session(
                s.sessionId(), s.userId(), s.platformProfileUrl(), s.webhookUrl(),
                newStatus, currency,
                lineCodec.encode(snapshots),
                totalsJson(subtotal, totalAmount),
                s.orderId(), s.cartId(), s.continueUrl(),
                s.createdAt(), now);
        sessionRepository.save(updated);

        return new CheckoutDtos.CheckoutResponse(
                envelopeFactory.ok(CapabilityName.CHECKOUT),
                s.sessionId(),
                newStatus.toWireFormat(),
                currency,
                outLines,
                request.buyer(),
                fulfillment,
                discountResponse,
                totals,
                buildLinks(),
                messages.isEmpty() ? null : messages,
                s.continueUrl(),
                s.orderId(),
                now.plus(30, ChronoUnit.MINUTES).toString());
    }

    public CheckoutDtos.CheckoutResponse complete(String sessionId, String userId,
            CheckoutDtos.CompleteRequest request) {
        Session s = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new UcpException(UcpErrorCode.CHECKOUT_NOT_FOUND));
        if (s.status().isTerminal()) {
            throw new UcpException(UcpErrorCode.CHECKOUT_INVALID_STATE,
                    "Session " + sessionId + " is in state " + s.status());
        }
        if (s.userId() != null && !s.userId().equals(userId)) {
            throw new UcpException(UcpErrorCode.CHECKOUT_FORBIDDEN);
        }
        if (request == null
                || request.payment_method_id() == null || request.payment_method_id().isBlank()
                || request.address_id() == null || request.address_id().isBlank()) {
            throw new UcpException(UcpErrorCode.INVALID_ARGUMENT,
                    "complete requires payment_method_id and address_id");
        }

        Session inProgress = new Session(
                s.sessionId(), s.userId(), s.platformProfileUrl(), s.webhookUrl(),
                CheckoutSessionStatus.COMPLETE_IN_PROGRESS,
                s.currency(), s.lineItemsJson(), s.totalsJson(),
                s.orderId(), s.cartId(), s.continueUrl(),
                s.createdAt(), Instant.now());
        sessionRepository.save(inProgress);

        List<LineItemSnapshot> snapshots = lineCodec.decode(s.lineItemsJson());
        List<OrderPlacementPort.PlaceCommand.Line> lines = snapshots.stream()
                .map(snap -> new OrderPlacementPort.PlaceCommand.Line(snap.skuId(), snap.quantity()))
                .toList();
        OrderPlacementPort.PlacedOrder placed = orderPlacementPort.place(
                new OrderPlacementPort.PlaceCommand(
                        userId, lines, request.payment_method_id(),
                        request.address_id(), s.currency(), null));

        Session completed = new Session(
                s.sessionId(), s.userId(), s.platformProfileUrl(), s.webhookUrl(),
                CheckoutSessionStatus.COMPLETED,
                s.currency(), s.lineItemsJson(), s.totalsJson(),
                placed.orderId(), s.cartId(), s.continueUrl(),
                s.createdAt(), Instant.now());
        sessionRepository.save(completed);

        CheckoutDtos.CheckoutResponse base = toResponse(completed);
        return new CheckoutDtos.CheckoutResponse(
                base.ucp(), base.id(), "completed", base.currency(),
                base.line_items(), base.buyer(), base.fulfillment(),
                base.discounts(), base.totals(), base.links(),
                List.of(UcpMessage.info("order_placed", "Order " + placed.orderId() + " placed.")),
                null, placed.orderId(), null);
    }

    public CheckoutDtos.CheckoutResponse cancel(String sessionId, String userId) {
        Session s = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new UcpException(UcpErrorCode.CHECKOUT_NOT_FOUND));
        if (s.status().isTerminal()) {
            throw new UcpException(UcpErrorCode.CHECKOUT_INVALID_STATE,
                    "Session " + sessionId + " cannot be canceled in state " + s.status());
        }
        if (s.userId() != null && !s.userId().equals(userId)) {
            throw new UcpException(UcpErrorCode.CHECKOUT_FORBIDDEN);
        }

        Session canceled = new Session(
                s.sessionId(), s.userId(), s.platformProfileUrl(), s.webhookUrl(),
                CheckoutSessionStatus.CANCELED,
                s.currency(), s.lineItemsJson(), s.totalsJson(),
                s.orderId(), s.cartId(), null,
                s.createdAt(), Instant.now());
        sessionRepository.save(canceled);

        return new CheckoutDtos.CheckoutResponse(
                envelopeFactory.ok(CapabilityName.CHECKOUT),
                s.sessionId(),
                "canceled",
                s.currency(),
                null, null, null, null, null, null,
                List.of(UcpMessage.info("checkout_canceled", "Checkout session has been canceled.")),
                null, null, null);
    }

    private FulfillmentDtos.Fulfillment buildFulfillment(List<String> lineItemIds,
            String addressRegion, String addressCountry,
            long orderTotalMinor, String currency) {
        List<ShippingQueryPort.ShippingOption> options = shippingQueryPort.getShippingOptions(
                addressRegion != null ? addressRegion : "VN",
                addressCountry != null ? addressCountry : "VN",
                orderTotalMinor, currency);

        if (options.isEmpty()) {
            return null;
        }

        List<FulfillmentDtos.FulfillmentOption> fulfillmentOptions = options.stream()
                .map(o -> new FulfillmentDtos.FulfillmentOption(
                        o.id(), o.title(), o.description(),
                        List.of(new FulfillmentDtos.Total("total", o.feeMinor()))))
                .toList();

        FulfillmentDtos.FulfillmentGroup group = new FulfillmentDtos.FulfillmentGroup(
                "package_1", lineItemIds,
                options.get(0).id(),
                fulfillmentOptions);

        FulfillmentDtos.FulfillmentMethod method = new FulfillmentDtos.FulfillmentMethod(
                "method_1", "shipping", lineItemIds,
                null, null, List.of(group));

        return new FulfillmentDtos.Fulfillment(List.of(method), null);
    }

    private record DiscountResult(long totalDiscount, DiscountDtos.DiscountResponse response) {
    }

    private DiscountResult processDiscountCodes(List<String> codes, String userId,
            long subtotal, String currency, List<UcpMessage> messages) {
        List<DiscountDtos.AppliedDiscount> applied = new ArrayList<>();
        long totalDiscount = 0;

        for (int i = 0; i < codes.size(); i++) {
            String code = codes.get(i);
            var info = promotionQueryPort.validateCode(code, userId, subtotal, currency);
            if (info.isEmpty() || !info.get().isValid()) {
                String reason = info.map(PromotionQueryPort.DiscountInfo::rejectionReason)
                        .orElse("discount_code_invalid");
                messages.add(UcpMessage.warning(reason,
                        "Code '" + code + "' could not be applied"));
                continue;
            }
            PromotionQueryPort.DiscountInfo di = info.get();
            totalDiscount += di.discountAmountMinor();
            applied.add(new DiscountDtos.AppliedDiscount(
                    di.code(), di.title(), di.discountAmountMinor(),
                    null, null, null, applied.size() + 1, null, null));
        }

        return new DiscountResult(totalDiscount,
                new DiscountDtos.DiscountResponse(codes, applied.isEmpty() ? null : applied));
    }

    private CheckoutSessionStatus determineStatus(CheckoutDtos.Buyer buyer,
            FulfillmentDtos.FulfillmentRequest fulfillment) {
        if (buyer != null && buyer.email() != null && !buyer.email().isBlank()) {
            return CheckoutSessionStatus.READY_FOR_COMPLETE;
        }
        return CheckoutSessionStatus.INCOMPLETE;
    }

    private CheckoutDtos.CheckoutResponse toResponse(Session s) {
        List<LineItemSnapshot> snapshots = lineCodec.decode(s.lineItemsJson());
        List<CheckoutDtos.CheckoutLineItem> outLines = rebuildLineItems(snapshots);
        long subtotal = snapshots.stream().mapToLong(sn -> sn.unitPriceMinor() * sn.quantity()).sum();

        List<String> lineItemIds = outLines.stream().map(CheckoutDtos.CheckoutLineItem::id).toList();
        FulfillmentDtos.Fulfillment fulfillment = buildFulfillment(lineItemIds, null, "VN", subtotal, s.currency());
        long shippingFee = getDefaultShippingFee(fulfillment);
        long totalAmount = subtotal + shippingFee;

        return new CheckoutDtos.CheckoutResponse(
                envelopeFactory.ok(CapabilityName.CHECKOUT),
                s.sessionId(),
                s.status().toWireFormat(),
                s.currency(),
                outLines,
                null,
                fulfillment,
                null,
                buildTotals(subtotal, shippingFee, 0, totalAmount),
                buildLinks(),
                null,
                s.status().isTerminal() ? null : s.continueUrl(),
                s.orderId(),
                s.status().isTerminal() ? null : Instant.now().plus(30, ChronoUnit.MINUTES).toString());
    }

    private List<CheckoutDtos.CheckoutLineItem> rebuildLineItems(List<LineItemSnapshot> snapshots) {
        List<CheckoutDtos.CheckoutLineItem> lines = new ArrayList<>(snapshots.size());
        for (int i = 0; i < snapshots.size(); i++) {
            LineItemSnapshot snap = snapshots.get(i);
            long lineTotal = snap.unitPriceMinor() * snap.quantity();
            lines.add(new CheckoutDtos.CheckoutLineItem(
                    "li_" + (i + 1),
                    new CheckoutDtos.CheckoutItem(snap.skuId(), snap.title(), snap.unitPriceMinor(), null),
                    snap.quantity(),
                    List.of(new CheckoutDtos.Total("subtotal", lineTotal, null),
                            new CheckoutDtos.Total("total", lineTotal, null))));
        }
        return lines;
    }

    private long getDefaultShippingFee(FulfillmentDtos.Fulfillment fulfillment) {
        if (fulfillment == null || fulfillment.methods() == null || fulfillment.methods().isEmpty())
            return 0;
        var method = fulfillment.methods().get(0);
        if (method.groups() == null || method.groups().isEmpty())
            return 0;
        var group = method.groups().get(0);
        if (group.selected_option_id() == null || group.options() == null)
            return 0;
        return group.options().stream()
                .filter(o -> o.id().equals(group.selected_option_id()))
                .findFirst()
                .map(o -> o.totals() != null && !o.totals().isEmpty() ? o.totals().get(0).amount() : 0L)
                .orElse(0L);
    }

    private List<CheckoutDtos.Total> buildTotals(long subtotal, long shippingFee, long discount, long total) {
        List<CheckoutDtos.Total> totals = new ArrayList<>();
        totals.add(new CheckoutDtos.Total("subtotal", subtotal, "Subtotal"));
        if (shippingFee > 0) {
            totals.add(new CheckoutDtos.Total("fulfillment", shippingFee, "Shipping"));
        }
        if (discount > 0) {
            totals.add(new CheckoutDtos.Total("items_discount", -discount, "Discounts"));
        }
        totals.add(new CheckoutDtos.Total("total", total, "Total"));
        return totals;
    }

    private List<CheckoutDtos.Link> buildLinks() {
        String base = properties.getEndpointBaseUrl();
        if (base == null || base.isBlank())
            return List.of();
        return List.of(
                new CheckoutDtos.Link("terms_of_service", base + "/terms", "Terms of Service"),
                new CheckoutDtos.Link("privacy_policy", base + "/privacy", "Privacy Policy"),
                new CheckoutDtos.Link("refund_policy", base + "/refund-policy", "Refund Policy"));
    }

    private static CatalogProductDto.Variant findVariantById(List<CatalogProductDto> products, String id) {
        for (CatalogProductDto p : products) {
            if (p.variants() == null)
                continue;
            for (CatalogProductDto.Variant v : p.variants()) {
                if (id.equals(v.id()) || id.equals(v.sku())) {
                    return v;
                }
            }
        }
        return null;
    }

    private String buildContinueUrl(String sessionId) {
        String base = properties.getEndpointBaseUrl();
        if (base == null || base.isBlank())
            return null;
        if (base.endsWith("/"))
            base = base.substring(0, base.length() - 1);
        return base + "/checkout?session=" + sessionId;
    }

    private static String totalsJson(long subtotal, long total) {
        return "{\"subtotal\":" + subtotal + ",\"total\":" + total + "}";
    }

    /**
     * Cart-to-checkout conversion per UCP spec.
     * Idempotent: if incomplete checkout already exists for cart_id, returns
     * existing session.
     */
    private CheckoutDtos.CheckoutResponse createFromCart(String cartId, String userId,
            CheckoutDtos.CreateRequest request) {
        java.util.Optional<Session> existing = sessionRepository.findByCartId(cartId);
        if (existing.isPresent() && !existing.get().status().isTerminal()) {
            log.info("Reusing existing checkout session {} for cart {}", existing.get().sessionId(), cartId);
            return toResponse(existing.get());
        }

        CartSessionPersistencePort.CartSession cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new UcpException(UcpErrorCode.CART_NOT_FOUND));
        if (cart.userId() != null && !cart.userId().equals(userId)) {
            throw new UcpException(UcpErrorCode.CART_FORBIDDEN);
        }

        List<LineItemSnapshot> snapshots = lineCodec.decode(cart.lineItemsJson());
        List<CheckoutDtos.CheckoutLineItem> outLines = rebuildLineItems(snapshots);
        long subtotal = snapshots.stream().mapToLong(sn -> sn.unitPriceMinor() * sn.quantity()).sum();

        List<String> lineItemIds = outLines.stream().map(CheckoutDtos.CheckoutLineItem::id).toList();
        FulfillmentDtos.Fulfillment fulfillment = buildFulfillment(lineItemIds, null, null, subtotal, cart.currency());

        long shippingFee = getDefaultShippingFee(fulfillment);
        long totalAmount = subtotal + shippingFee;
        List<CheckoutDtos.Total> totals = buildTotals(subtotal, shippingFee, 0, totalAmount);

        CheckoutSessionStatus status = CheckoutSessionStatus.INCOMPLETE;

        String sessionId = "chk_" + IdGenerator.ulid();
        Instant now = Instant.now();
        String expiresAt = now.plus(30, ChronoUnit.MINUTES).toString();
        Session session = new Session(
                sessionId, userId, null, request.webhook_url(),
                status, cart.currency(),
                cart.lineItemsJson(),
                totalsJson(subtotal, totalAmount),
                null, cartId, buildContinueUrl(sessionId),
                now, now);
        sessionRepository.save(session);

        log.info("Created checkout session {} from cart {}", sessionId, cartId);

        return new CheckoutDtos.CheckoutResponse(
                envelopeFactory.ok(CapabilityName.CHECKOUT),
                sessionId,
                status.toWireFormat(),
                cart.currency(),
                outLines,
                null,
                fulfillment,
                null,
                totals,
                buildLinks(),
                null,
                buildContinueUrl(sessionId),
                null,
                expiresAt);
    }
}
