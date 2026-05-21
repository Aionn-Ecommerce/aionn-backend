package com.aionn.ordering.application.port.out;

import com.aionn.ordering.domain.model.OrderReturn;

import java.util.Optional;

public interface OrderReturnRepository {

    OrderReturn save(OrderReturn orderReturn);

    Optional<OrderReturn> findById(String returnId);
}

