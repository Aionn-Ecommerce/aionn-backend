package com.aionn.shipping.application.service;

import com.aionn.shipping.application.dto.rate.command.ConfigureRateCommand;
import com.aionn.shipping.application.dto.rate.command.UpdateRateCommand;
import com.aionn.shipping.application.dto.rate.result.ShippingRateResult;
import com.aionn.shipping.application.mapper.ShippingResultMapper;
import com.aionn.shipping.application.port.out.ShippingRatePersistencePort;
import com.aionn.shipping.domain.exception.ShippingErrorCode;
import com.aionn.shipping.domain.exception.ShippingException;
import com.aionn.shipping.domain.model.ShippingRate;
import com.aionn.sharedkernel.application.port.EventPublisher;
import com.aionn.sharedkernel.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ShippingRateService {

    private final ShippingRatePersistencePort repository;
    private final ShippingResultMapper mapper;
    private final EventPublisher eventPublisher;

    public ShippingRateResult configure(ConfigureRateCommand command) {
        if (repository.findByZoneCode(command.zoneCode()).isPresent()) {
            throw new ShippingException(ShippingErrorCode.RATE_DUPLICATE);
        }
        ShippingRate rate = ShippingRate.configure(IdGenerator.ulid(),
                command.zoneCode(), command.baseFee(),
                command.currency() == null ? "VND" : command.currency(), command.condition());
        ShippingRate saved = repository.save(rate);
        eventPublisher.publish(rate.pullEvents());
        return mapper.toResult(saved);
    }

    public ShippingRateResult update(UpdateRateCommand command) {
        ShippingRate rate = repository.findById(command.rateId())
                .orElseThrow(() -> new ShippingException(ShippingErrorCode.RATE_NOT_FOUND));
        rate.update(command.baseFee(), command.condition());
        ShippingRate saved = repository.save(rate);
        eventPublisher.publish(rate.pullEvents());
        return mapper.toResult(saved);
    }

    @Transactional(readOnly = true)
    public ShippingRateResult get(String rateId) {
        return mapper.toResult(repository.findById(rateId)
                .orElseThrow(() -> new ShippingException(ShippingErrorCode.RATE_NOT_FOUND)));
    }
}
