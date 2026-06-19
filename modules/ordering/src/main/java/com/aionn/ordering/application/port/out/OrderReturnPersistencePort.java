package com.aionn.ordering.application.port.out;

import com.aionn.ordering.domain.model.OrderReturn;
import com.aionn.ordering.domain.valueobject.ReturnStatus;

import java.util.List;
import java.util.Optional;

public interface OrderReturnPersistencePort {

    OrderReturn save(OrderReturn orderReturn);

    Optional<OrderReturn> findById(String returnId);

    List<OrderReturn> findByStatus(ReturnStatus status, int limit);

    List<OrderReturn> findByUserId(String userId, int limit);

    List<OrderReturn> findByMerchantId(String merchantId, int limit);
}

