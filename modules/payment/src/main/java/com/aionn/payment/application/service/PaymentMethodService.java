package com.aionn.payment.application.service;

import com.aionn.payment.application.dto.method.command.LinkMethodCommand;
import com.aionn.payment.application.dto.method.command.RemoveMethodCommand;
import com.aionn.payment.application.dto.method.command.VerifyMethodCommand;
import com.aionn.payment.application.dto.method.result.PaymentMethodResult;
import com.aionn.payment.application.mapper.PaymentResultMapper;
import com.aionn.payment.application.port.out.PaymentMethodRepository;
import com.aionn.payment.domain.exception.PaymentErrorCode;
import com.aionn.payment.domain.exception.PaymentException;
import com.aionn.payment.domain.model.PaymentMethod;
import com.aionn.sharedkernel.application.port.EventPublisher;
import com.aionn.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentMethodService {

    private final PaymentMethodRepository repository;
    private final PaymentResultMapper mapper;
    private final EventPublisher eventPublisher;

    public PaymentMethodResult link(LinkMethodCommand command) {
        PaymentMethod method = PaymentMethod.link(IdGenerator.ulid(),
                command.userId(), command.provider(), command.last4Digits(), command.gatewayToken());
        PaymentMethod saved = repository.save(method);
        eventPublisher.publish(method.pullEvents());
        return mapper.toResult(saved);
    }

    public PaymentMethodResult verify(VerifyMethodCommand command) {
        PaymentMethod method = ownedBy(command.methodId(), command.userId());
        method.verify();
        PaymentMethod saved = repository.save(method);
        eventPublisher.publish(method.pullEvents());
        return mapper.toResult(saved);
    }

    public void remove(RemoveMethodCommand command) {
        PaymentMethod method = ownedBy(command.methodId(), command.userId());
        method.remove();
        repository.save(method);
        eventPublisher.publish(method.pullEvents());
    }

    @Transactional(readOnly = true)
    public List<PaymentMethodResult> listMine(String userId) {
        return repository.findActiveByUserId(userId).stream().map(mapper::toResult).toList();
    }

    @Transactional(readOnly = true)
    public PaymentMethodResult get(String userId, String methodId) {
        return mapper.toResult(ownedBy(methodId, userId));
    }

    private PaymentMethod ownedBy(String methodId, String userId) {
        PaymentMethod method = repository.findById(methodId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.METHOD_NOT_FOUND));
        method.ensureOwnedBy(userId);
        return method;
    }
}
