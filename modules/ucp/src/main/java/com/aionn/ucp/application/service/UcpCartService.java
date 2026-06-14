package com.aionn.ucp.application.service;

import com.aionn.sharedkernel.util.IdGenerator;
import com.aionn.ucp.application.dto.cart.CartDtos;
import com.aionn.ucp.application.dto.catalog.CatalogProductDto;
import com.aionn.ucp.application.dto.checkout.CheckoutDtos;
import com.aionn.ucp.application.dto.envelope.UcpMessage;
import com.aionn.ucp.application.port.out.CartSessionPersistencePort;
import com.aionn.ucp.application.port.out.CartSessionPersistencePort.CartSession;
import com.aionn.ucp.application.port.out.CatalogQueryPort;
import com.aionn.ucp.application.port.out.CheckoutSessionPersistencePort.LineCodec;
import com.aionn.ucp.application.port.out.CheckoutSessionPersistencePort.LineItemSnapshot;
import com.aionn.ucp.domain.exception.UcpErrorCode;
import com.aionn.ucp.domain.exception.UcpException;
import com.aionn.ucp.domain.model.CapabilityName;
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
public class UcpCartService {

    private final CatalogQueryPort catalogQueryPort;
    private final CartSessionPersistencePort cartRepository;
    private final LineCodec lineCodec;
    private final UcpEnvelopeFactory envelopeFactory;
    private final UcpProperties properties;

    public CartDtos.CartResponse create(CartDtos.CreateRequest request, String userId) {
        if (request == null || request.line_items() == null || request.line_items().isEmpty()) {
            throw new UcpException(UcpErrorCode.INVALID_ARGUMENT, "line_items required");
        }

        List<String> ids = request.line_items().stream().map(li -> li.item().id()).toList();
        CatalogQueryPort.LookupResult lookup = catalogQueryPort.lookup(ids);
        if (lookup.products().isEmpty()) {
            return CartDtos.CartResponse.error(
                    envelopeFactory.error(CapabilityName.CART),
                    List.of(UcpMessage.error("out_of_stock",
                            "All requested items are unavailable",
                            "unrecoverable")));
        }

        List<LineItemSnapshot> snapshots = new ArrayList<>();
        List<CheckoutDtos.CheckoutLineItem> outLines = new ArrayList<>();
        long subtotal = 0;
        String currency = "VND";
        List<UcpMessage> messages = new ArrayList<>();

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
            return CartDtos.CartResponse.error(
                    envelopeFactory.error(CapabilityName.CART),
                    List.of(UcpMessage.error("out_of_stock",
                            "None of the requested items are purchasable",
                            "unrecoverable")));
        }

        String cartId = "cart_" + IdGenerator.ulid();
        Instant now = Instant.now();
        String expiresAt = now.plus(7, ChronoUnit.DAYS).toString();
        String continueUrl = buildContinueUrl(cartId);

        CartSession session = new CartSession(
                cartId, userId, currency,
                lineCodec.encode(snapshots),
                totalsJson(subtotal, subtotal),
                continueUrl,
                now, now);
        cartRepository.save(session);

        List<CheckoutDtos.Total> totals = buildTotals(subtotal, subtotal);

        return new CartDtos.CartResponse(
                envelopeFactory.ok(CapabilityName.CART),
                cartId,
                currency,
                outLines,
                request.buyer(),
                request.context(),
                totals,
                buildLinks(),
                messages.isEmpty() ? null : messages,
                continueUrl,
                expiresAt);
    }

    @Transactional(readOnly = true)
    public CartDtos.CartResponse get(String cartId) {
        CartSession session = cartRepository.findById(cartId)
                .orElseThrow(() -> new UcpException(UcpErrorCode.CART_NOT_FOUND));
        return toResponse(session);
    }

    public CartDtos.CartResponse update(String cartId, String userId, CartDtos.UpdateRequest request) {
        CartSession existing = cartRepository.findById(cartId)
                .orElseThrow(() -> new UcpException(UcpErrorCode.CART_NOT_FOUND));
        if (existing.userId() != null && !existing.userId().equals(userId)) {
            throw new UcpException(UcpErrorCode.CART_FORBIDDEN);
        }

        List<LineItemSnapshot> snapshots;
        List<CheckoutDtos.CheckoutLineItem> outLines;
        long subtotal;
        String currency = existing.currency();
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
                        List.of(
                                new CheckoutDtos.Total("subtotal", lineTotal, null),
                                new CheckoutDtos.Total("total", lineTotal, null))));
            }
        } else {
            snapshots = lineCodec.decode(existing.lineItemsJson());
            outLines = rebuildLineItems(snapshots);
            subtotal = snapshots.stream().mapToLong(sn -> sn.unitPriceMinor() * sn.quantity()).sum();
        }

        Instant now = Instant.now();
        String expiresAt = now.plus(7, ChronoUnit.DAYS).toString();
        CartSession updated = new CartSession(
                existing.cartId(), existing.userId(), currency,
                lineCodec.encode(snapshots),
                totalsJson(subtotal, subtotal),
                existing.continueUrl(),
                existing.createdAt(), now);
        cartRepository.save(updated);

        return new CartDtos.CartResponse(
                envelopeFactory.ok(CapabilityName.CART),
                cartId,
                currency,
                outLines,
                request.buyer(),
                request.context(),
                buildTotals(subtotal, subtotal),
                buildLinks(),
                messages.isEmpty() ? null : messages,
                existing.continueUrl(),
                expiresAt);
    }

    public CartDtos.CartResponse cancel(String cartId, String userId) {
        CartSession session = cartRepository.findById(cartId)
                .orElseThrow(() -> new UcpException(UcpErrorCode.CART_NOT_FOUND));
        if (session.userId() != null && !session.userId().equals(userId)) {
            throw new UcpException(UcpErrorCode.CART_FORBIDDEN);
        }

        CartDtos.CartResponse response = toResponse(session);
        cartRepository.deleteById(cartId);

        return new CartDtos.CartResponse(
                envelopeFactory.ok(CapabilityName.CART),
                cartId,
                response.currency(),
                response.line_items(),
                null, null,
                response.totals(),
                null,
                List.of(UcpMessage.info("cart_canceled", "Cart session has been canceled.")),
                null, null);
    }

    private CartDtos.CartResponse toResponse(CartSession session) {
        List<LineItemSnapshot> snapshots = lineCodec.decode(session.lineItemsJson());
        List<CheckoutDtos.CheckoutLineItem> outLines = rebuildLineItems(snapshots);
        long subtotal = snapshots.stream().mapToLong(sn -> sn.unitPriceMinor() * sn.quantity()).sum();
        String expiresAt = session.createdAt().plus(7, ChronoUnit.DAYS).toString();

        return new CartDtos.CartResponse(
                envelopeFactory.ok(CapabilityName.CART),
                session.cartId(),
                session.currency(),
                outLines,
                null, null,
                buildTotals(subtotal, subtotal),
                buildLinks(),
                null,
                session.continueUrl(),
                expiresAt);
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
                    List.of(
                            new CheckoutDtos.Total("subtotal", lineTotal, null),
                            new CheckoutDtos.Total("total", lineTotal, null))));
        }
        return lines;
    }

    private List<CheckoutDtos.Total> buildTotals(long subtotal, long total) {
        List<CheckoutDtos.Total> totals = new ArrayList<>();
        totals.add(new CheckoutDtos.Total("subtotal", subtotal, "Subtotal"));
        totals.add(new CheckoutDtos.Total("total", total,
                "Estimated total (taxes calculated at checkout)"));
        return totals;
    }

    private List<CheckoutDtos.Link> buildLinks() {
        String base = properties.getEndpointBaseUrl();
        if (base == null || base.isBlank())
            return List.of();
        return List.of(
                new CheckoutDtos.Link("terms_of_service", base + "/terms", "Terms of Service"),
                new CheckoutDtos.Link("privacy_policy", base + "/privacy", "Privacy Policy"));
    }

    private String buildContinueUrl(String cartId) {
        String base = properties.getEndpointBaseUrl();
        if (base == null || base.isBlank())
            return null;
        if (base.endsWith("/"))
            base = base.substring(0, base.length() - 1);
        return base + "/cart?session=" + cartId;
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

    private static String totalsJson(long subtotal, long total) {
        return "{\"subtotal\":" + subtotal + ",\"total\":" + total + "}";
    }
}
