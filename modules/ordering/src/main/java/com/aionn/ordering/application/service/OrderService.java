package com.aionn.ordering.application.service;

import com.aionn.ordering.application.dto.order.command.OrderCommands;
import com.aionn.ordering.application.dto.order.result.OrderResult;
import com.aionn.ordering.application.mapper.OrderingResultMapper;
import com.aionn.ordering.application.port.out.CartRepository;
import com.aionn.ordering.application.port.out.CatalogPricingGateway;
import com.aionn.sharedkernel.application.port.EventPublisher;
import com.aionn.ordering.application.port.out.OrderRepository;
import com.aionn.ordering.application.port.out.PaymentGateway;
import com.aionn.ordering.application.port.out.ShippingGateway;
import com.aionn.ordering.application.port.out.StockReservationGateway;
import com.aionn.ordering.application.port.out.VoucherGateway;
import com.aionn.ordering.domain.exception.OrderingErrorCode;
import com.aionn.ordering.domain.exception.OrderingException;
import com.aionn.ordering.domain.model.Cart;
import com.aionn.ordering.domain.model.Order;
import com.aionn.ordering.domain.model.OrderItem;
import com.aionn.sharedkernel.domain.vo.Money;
import com.aionn.ordering.domain.valueobject.OrderStatus;
import com.aionn.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private static final int RESERVATION_TTL_SECONDS = 15 * 60;

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderingResultMapper mapper;
    private final EventPublisher eventPublisher;
    private final StockReservationGateway stockReservationGateway;
    private final PaymentGateway paymentGateway;
    private final ShippingGateway shippingGateway;
    private final CatalogPricingGateway catalogPricingGateway;
    private final VoucherGateway voucherGateway;
    private final CartService cartService;

    public OrderResult placeOrder(OrderCommands.PlaceOrder command) {
        Cart cart = cartService.loadOwned(command.userId());
        if (cart.isEmpty()) {
            throw new OrderingException(OrderingErrorCode.CART_EMPTY);
        }

        // 1. Resolve current SKU prices/availability from Catalog (UC5.7)
        List<String> skuIds = cart.snapshot().stream().map(Map.Entry::getKey).toList();
        Map<String, CatalogPricingGateway.SkuPricing> pricing = catalogPricingGateway.resolve(skuIds);

        for (Map.Entry<String, Integer> line : cart.snapshot()) {
            CatalogPricingGateway.SkuPricing skuInfo = pricing.get(line.getKey());
            if (skuInfo == null || !skuInfo.active()) {
                throw new OrderingException(OrderingErrorCode.ORDER_INVALID_STATE,
                        "SKU " + line.getKey() + " is not available for sale");
            }
        }

        // Single-merchant check: all items must belong to the same merchant for
        // this iteration. Multi-merchant carts trigger UC5.12 split which is
        // owned by a separate orchestrator path; here we throw if mixed.
        String merchantId = pricing.values().iterator().next().merchantId();
        for (CatalogPricingGateway.SkuPricing p : pricing.values()) {
            if (!merchantId.equals(p.merchantId())) {
                throw new OrderingException(OrderingErrorCode.ORDER_INVALID_STATE,
                        "Cart contains SKUs from multiple merchants - split flow not implemented");
            }
        }

        String currency = command.currency() != null ? command.currency() : "VND";

        // 2. Reserve all lines via Inventory.
        List<StockReservationGateway.ReservationLine> reservationLines = cart.snapshot().stream()
                .map(line -> {
                    CatalogPricingGateway.SkuPricing p = pricing.get(line.getKey());
                    return new StockReservationGateway.ReservationLine(
                            line.getKey(), p.warehouseId(), line.getValue(),
                            p.price(), currency);
                }).toList();
        List<StockReservationGateway.Reservation> reservations;
        try {
            reservations = stockReservationGateway.reserveAll(IdGenerator.ulid(), reservationLines,
                    RESERVATION_TTL_SECONDS);
        } catch (StockReservationGateway.ReservationException ex) {
            throw new OrderingException(OrderingErrorCode.ORDER_RESERVATION_FAILED,
                    "Reservation failed for SKU " + ex.getSkuId() + ": " + ex.getMessage());
        }

        // 3. Build order line items
        List<OrderItem> items = new ArrayList<>(reservations.size());
        Money lineSubtotal = Money.zero(currency);
        for (StockReservationGateway.Reservation r : reservations) {
            Money unit = Money.of(r.unitPrice(), currency);
            items.add(new OrderItem(r.skuId(), r.qty(), unit, r.warehouseId(), r.reservationId()));
            lineSubtotal = lineSubtotal.add(unit.multiply(r.qty()));
        }

        // 4. Apply voucher discount if present
        if (cart.getVoucherCode() != null) {
            VoucherGateway.Discount discount = voucherGateway.apply(
                    command.userId(), cart.getVoucherCode(), lineSubtotal.amount(), currency);
            if (!discount.valid()) {
                releaseReservations(reservations, "voucher-invalid");
                throw new OrderingException(OrderingErrorCode.ORDER_INVALID_STATE,
                        "Voucher invalid: " + discount.reason());
            }
            // discount is informational here (reflected in totalAmount through subtraction)
            BigDecimal newSubtotal = lineSubtotal.amount().subtract(discount.amount()).max(BigDecimal.ZERO);
            lineSubtotal = Money.of(newSubtotal, currency);
        }

        Money shippingFee = command.shippingFee() == null
                ? Money.zero(currency)
                : Money.of(command.shippingFee(), currency);
        Money totalAmount = lineSubtotal.add(shippingFee);

        // 5. Authorize payment
        PaymentGateway.PaymentAuthorization auth;
        try {
            auth = paymentGateway.authorize(IdGenerator.ulid(), command.userId(),
                    command.paymentMethodId(), totalAmount.amount(), currency);
        } catch (Exception ex) {
            releaseReservations(reservations, "payment-error");
            throw new OrderingException(OrderingErrorCode.ORDER_PAYMENT_FAILED,
                    "Payment authorization error: " + ex.getMessage());
        }
        if (!auth.approved()) {
            releaseReservations(reservations, "payment-declined");
            throw new OrderingException(OrderingErrorCode.ORDER_PAYMENT_FAILED,
                    "Payment declined: " + auth.declineReason());
        }

        // 6. Persist Order, transition to APPROVED immediately (synchronous saga
        // happy path).
        String orderId = IdGenerator.ulid();
        String proposalId = "prop-" + System.nanoTime();
        Order order = Order.place(orderId, command.userId(), merchantId, proposalId,
                command.paymentMethodId(), currency, items, command.shippingAddressSnapshot(), shippingFee);
        order.approve(auth.paymentId());

        // After commit, reservations are held; later integration with payment
        // success event will trigger commit/release. For now we keep them
        // RESERVED until merchant prepares + ships (or until auto-cancel).
        Order saved = orderRepository.save(order);
        eventPublisher.publish(order.pullEvents());

        // 7. Clear the cart
        Cart freshCart = cartService.loadOwned(command.userId());
        freshCart.clear("order-placed");
        cartRepository.save(freshCart);
        eventPublisher.publish(freshCart.pullEvents());

        return mapper.toResult(saved);
    }

    public OrderResult confirmPreparation(OrderCommands.ConfirmPreparation command) {
        Order order = ownedByMerchant(command.orderId(), command.merchantId());
        order.confirmPreparation();
        Order saved = orderRepository.save(order);
        eventPublisher.publish(order.pullEvents());
        return mapper.toResult(saved);
    }

    public OrderResult cancel(OrderCommands.CancelOrder command) {
        Order order = ownedByUser(command.orderId(), command.userId());
        if (order.getStatus().isPickedUpByCarrier()) {
            throw new OrderingException(OrderingErrorCode.ORDER_ALREADY_PICKED_UP);
        }
        order.cancel("USER_CANCELLED", command.reason());
        for (OrderItem item : order.items()) {
            stockReservationGateway.release(item.reservationId(), "order-cancelled");
        }
        if (order.getPaymentId() != null) {
            paymentGateway.refund(order.getPaymentId(), order.getTotalAmount().amount(),
                    order.getCurrency(), "user cancellation");
        }
        Order saved = orderRepository.save(order);
        eventPublisher.publish(order.pullEvents());
        return mapper.toResult(saved);
    }

    public OrderResult rejectByMerchant(OrderCommands.RejectOrder command) {
        Order order = ownedByMerchant(command.orderId(), command.merchantId());
        order.rejectByMerchant(command.merchantId(), command.reason());
        for (OrderItem item : order.items()) {
            stockReservationGateway.release(item.reservationId(), "merchant-rejected");
        }
        if (order.getPaymentId() != null) {
            paymentGateway.refund(order.getPaymentId(), order.getTotalAmount().amount(),
                    order.getCurrency(), "merchant rejected");
        }
        Order saved = orderRepository.save(order);
        eventPublisher.publish(order.pullEvents());
        return mapper.toResult(saved);
    }

    public OrderResult changeShippingInfo(OrderCommands.ChangeShippingInfo command) {
        Order order = ownedByUser(command.orderId(), command.userId());
        BigDecimal feeOverride = command.newShippingFee();
        Money newFee;
        if (feeOverride == null) {
            ShippingGateway.ShippingQuote quote = shippingGateway.quote(order.getOrderId(), order.getMerchantId(),
                    command.newAddress(), order.getCurrency());
            newFee = Money.of(quote.fee(), quote.currency());
        } else {
            newFee = Money.of(feeOverride, order.getCurrency());
        }
        order.changeShippingInfo(command.newAddress(), newFee);
        Order saved = orderRepository.save(order);
        eventPublisher.publish(order.pullEvents());
        return mapper.toResult(saved);
    }

    public OrderResult markShipped(OrderCommands.ConfirmShipped command) {
        Order order = required(command.orderId());
        order.markShipped(command.shipmentId());
        // Commit reservations now that the goods are out the door.
        for (OrderItem item : order.items()) {
            stockReservationGateway.commit(item.reservationId());
        }
        Order saved = orderRepository.save(order);
        eventPublisher.publish(order.pullEvents());
        return mapper.toResult(saved);
    }

    public OrderResult complete(OrderCommands.ConfirmDelivered command) {
        Order order = required(command.orderId());
        order.complete();
        Order saved = orderRepository.save(order);
        eventPublisher.publish(order.pullEvents());
        return mapper.toResult(saved);
    }

    
    public int autoCancelExpired(Instant cutoff, int batchSize) {
        List<Order> expired = orderRepository.findPendingOlderThan(cutoff, batchSize);
        int cancelled = 0;
        for (Order order : expired) {
            try {
                order.autoCancel("PAYMENT_TIMEOUT");
                for (OrderItem item : order.items()) {
                    stockReservationGateway.release(item.reservationId(), "auto-cancel");
                }
                orderRepository.save(order);
                eventPublisher.publish(order.pullEvents());
                cancelled++;
            } catch (OrderingException ex) {
                log.warn("Skip auto-cancel for {}: {}", order.getOrderId(), ex.getMessage());
            }
        }
        return cancelled;
    }

    @Transactional(readOnly = true)
    public OrderResult get(String orderId) {
        return mapper.toResult(required(orderId));
    }

    @Transactional(readOnly = true)
    public List<OrderResult> listByUser(String userId, int limit) {
        return orderRepository.findByUser(userId, limit).stream().map(mapper::toResult).toList();
    }

    Order required(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderingException(OrderingErrorCode.ORDER_NOT_FOUND));
    }

    Order ownedByUser(String orderId, String userId) {
        Order order = required(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new OrderingException(OrderingErrorCode.ORDER_FORBIDDEN);
        }
        return order;
    }

    Order ownedByMerchant(String orderId, String merchantId) {
        Order order = required(orderId);
        if (!order.getMerchantId().equals(merchantId)) {
            throw new OrderingException(OrderingErrorCode.ORDER_NOT_OWNED_BY_MERCHANT);
        }
        return order;
    }

    private void releaseReservations(List<StockReservationGateway.Reservation> reservations, String reason) {
        for (StockReservationGateway.Reservation r : reservations) {
            try {
                stockReservationGateway.release(r.reservationId(), reason);
            } catch (Exception ex) {
                log.warn("Failed to release reservation {} during compensation: {}", r.reservationId(),
                        ex.getMessage());
            }
        }
    }

    
    public OrderStatus statusOf(String orderId) {
        return required(orderId).getStatus();
    }
}

