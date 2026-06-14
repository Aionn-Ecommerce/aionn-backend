package com.aionn.ordering.application.service;

import com.aionn.ordering.application.dto.returns.command.ApproveReturnCommand;
import com.aionn.ordering.application.dto.returns.command.ConfirmItemReceivedCommand;
import com.aionn.ordering.application.dto.returns.command.RejectReturnCommand;
import com.aionn.ordering.application.dto.returns.command.RequestReturnCommand;
import com.aionn.ordering.application.dto.returns.result.ReturnResult;
import com.aionn.ordering.application.mapper.OrderingResultMapper;
import com.aionn.ordering.application.port.out.OrderPersistencePort;
import com.aionn.ordering.application.port.out.OrderReturnPersistencePort;
import com.aionn.ordering.domain.exception.OrderingErrorCode;
import com.aionn.ordering.domain.exception.OrderingException;
import com.aionn.ordering.domain.model.Order;
import com.aionn.ordering.domain.model.OrderReturn;
import com.aionn.ordering.domain.valueobject.OrderStatus;
import com.aionn.sharedkernel.application.port.EventPublisher;
import com.aionn.sharedkernel.domain.vo.Money;
import com.aionn.sharedkernel.integration.port.catalog.MerchantQueryPort;
import com.aionn.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderReturnService {

    private static final Duration RETURN_WINDOW = Duration.ofDays(7);

    private final OrderPersistencePort orderRepository;
    private final OrderReturnPersistencePort returnRepository;
    private final OrderingResultMapper mapper;
    private final EventPublisher eventPublisher;
    private final MerchantQueryPort merchantQueryPort;

    public ReturnResult requestReturn(RequestReturnCommand command) {
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new OrderingException(OrderingErrorCode.ORDER_NOT_FOUND));
        if (!order.getUserId().equals(command.userId())) {
            throw new OrderingException(OrderingErrorCode.ORDER_FORBIDDEN);
        }
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new OrderingException(OrderingErrorCode.ORDER_INVALID_STATE,
                    "Returns are only allowed on COMPLETED orders");
        }
        Instant completedAt = Objects.requireNonNull(order.getCompletedAt(),
                "completed order must have a completedAt timestamp");
        if (Instant.now().isAfter(completedAt.plus(RETURN_WINDOW))) {
            throw new OrderingException(OrderingErrorCode.ORDER_RETURN_WINDOW_EXPIRED);
        }
        OrderReturn r = OrderReturn.request(IdGenerator.ulid(), order.getOrderId(),
                order.getUserId(), order.getMerchantId(), command.reason(), command.evidenceUrl());
        OrderReturn saved = returnRepository.save(r);
        eventPublisher.publish(r.pullEvents());
        return mapper.toResult(saved);
    }

    public ReturnResult approve(ApproveReturnCommand command) {
        OrderReturn r = ownedByOwner(command.returnId(), command.ownerId());
        Money refundAmount = command.refundAmount() == null
                ? null
                : Money.of(command.refundAmount(), command.currency() == null ? "VND" : command.currency());
        r.approve(refundAmount, command.returnWarehouseId());
        OrderReturn saved = returnRepository.save(r);
        eventPublisher.publish(r.pullEvents());
        return mapper.toResult(saved);
    }

    public ReturnResult reject(RejectReturnCommand command) {
        OrderReturn r = ownedByOwner(command.returnId(), command.ownerId());
        r.reject(command.reason());
        OrderReturn saved = returnRepository.save(r);
        eventPublisher.publish(r.pullEvents());
        return mapper.toResult(saved);
    }

    public ReturnResult confirmItemReceived(ConfirmItemReceivedCommand command) {
        OrderReturn r = ownedByOwner(command.returnId(), command.ownerId());
        r.confirmReceived(command.itemCondition());
        OrderReturn saved = returnRepository.save(r);
        eventPublisher.publish(r.pullEvents());
        return mapper.toResult(saved);
    }

    @Transactional(readOnly = true)
    public ReturnResult getForRequester(String returnId, String requesterUserId) {
        OrderReturn r = returnRepository.findById(returnId)
                .orElseThrow(() -> new OrderingException(OrderingErrorCode.RETURN_NOT_FOUND));
        if (r.getUserId().equals(requesterUserId)) {
            return mapper.toResult(r);
        }
        String requesterMerchantId = merchantQueryPort.findMerchantIdByOwnerId(requesterUserId).orElse(null);
        if (requesterMerchantId != null && requesterMerchantId.equals(r.getMerchantId())) {
            return mapper.toResult(r);
        }
        throw new OrderingException(OrderingErrorCode.ORDER_FORBIDDEN);
    }

    private OrderReturn ownedByOwner(String returnId, String ownerId) {
        String merchantId = merchantQueryPort.findMerchantIdByOwnerId(ownerId)
                .orElseThrow(() -> new OrderingException(OrderingErrorCode.ORDER_NOT_OWNED_BY_MERCHANT,
                        "No merchant registered for the authenticated user"));
        OrderReturn r = returnRepository.findById(returnId)
                .orElseThrow(() -> new OrderingException(OrderingErrorCode.RETURN_NOT_FOUND));
        if (!r.getMerchantId().equals(merchantId)) {
            throw new OrderingException(OrderingErrorCode.ORDER_NOT_OWNED_BY_MERCHANT);
        }
        return r;
    }
}
