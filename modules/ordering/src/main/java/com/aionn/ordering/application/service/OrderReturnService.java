package com.aionn.ordering.application.service;

import com.aionn.ordering.application.dto.returns.command.ReturnCommands;
import com.aionn.ordering.application.dto.returns.result.ReturnResult;
import com.aionn.ordering.application.mapper.OrderingResultMapper;
import com.aionn.sharedkernel.application.port.EventPublisher;
import com.aionn.ordering.application.port.out.OrderRepository;
import com.aionn.ordering.application.port.out.OrderReturnRepository;
import com.aionn.ordering.domain.exception.OrderingErrorCode;
import com.aionn.ordering.domain.exception.OrderingException;
import com.aionn.ordering.domain.model.Order;
import com.aionn.ordering.domain.model.OrderReturn;
import com.aionn.sharedkernel.domain.vo.Money;
import com.aionn.ordering.domain.valueobject.OrderStatus;
import com.aionn.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderReturnService {

    private static final Duration RETURN_WINDOW = Duration.ofDays(7);

    private final OrderRepository orderRepository;
    private final OrderReturnRepository returnRepository;
    private final OrderingResultMapper mapper;
    private final EventPublisher eventPublisher;

    public ReturnResult requestReturn(ReturnCommands.RequestReturn command) {
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new OrderingException(OrderingErrorCode.ORDER_NOT_FOUND));
        if (!order.getUserId().equals(command.userId())) {
            throw new OrderingException(OrderingErrorCode.ORDER_FORBIDDEN);
        }
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new OrderingException(OrderingErrorCode.ORDER_INVALID_STATE,
                    "Returns are only allowed on COMPLETED orders");
        }
        Instant deadline = order.getCompletedAt().plus(RETURN_WINDOW);
        if (Instant.now().isAfter(deadline)) {
            throw new OrderingException(OrderingErrorCode.ORDER_RETURN_WINDOW_EXPIRED);
        }
        OrderReturn r = OrderReturn.request(IdGenerator.ulid(), order.getOrderId(),
                order.getUserId(), order.getMerchantId(), command.reason(), command.evidenceUrl());
        OrderReturn saved = returnRepository.save(r);
        eventPublisher.publish(r.pullEvents());
        return mapper.toResult(saved);
    }

    public ReturnResult approve(ReturnCommands.ApproveReturn command) {
        OrderReturn r = ownedByMerchant(command.returnId(), command.merchantId());
        Money refundAmount = command.refundAmount() == null
                ? null
                : Money.of(command.refundAmount(), command.currency() == null ? "VND" : command.currency());
        r.approve(refundAmount, command.returnWarehouseId());
        OrderReturn saved = returnRepository.save(r);
        eventPublisher.publish(r.pullEvents());
        return mapper.toResult(saved);
    }

    public ReturnResult reject(ReturnCommands.RejectReturn command) {
        OrderReturn r = ownedByMerchant(command.returnId(), command.merchantId());
        r.reject(command.reason());
        OrderReturn saved = returnRepository.save(r);
        eventPublisher.publish(r.pullEvents());
        return mapper.toResult(saved);
    }

    public ReturnResult confirmItemReceived(ReturnCommands.ConfirmItemReceived command) {
        OrderReturn r = ownedByMerchant(command.returnId(), command.merchantId());
        r.confirmReceived(command.itemCondition());
        OrderReturn saved = returnRepository.save(r);
        eventPublisher.publish(r.pullEvents());
        return mapper.toResult(saved);
    }

    @Transactional(readOnly = true)
    public ReturnResult get(String returnId) {
        return mapper.toResult(returnRepository.findById(returnId)
                .orElseThrow(() -> new OrderingException(OrderingErrorCode.RETURN_NOT_FOUND)));
    }

    private OrderReturn ownedByMerchant(String returnId, String merchantId) {
        OrderReturn r = returnRepository.findById(returnId)
                .orElseThrow(() -> new OrderingException(OrderingErrorCode.RETURN_NOT_FOUND));
        if (!r.getMerchantId().equals(merchantId)) {
            throw new OrderingException(OrderingErrorCode.ORDER_NOT_OWNED_BY_MERCHANT);
        }
        return r;
    }
}

