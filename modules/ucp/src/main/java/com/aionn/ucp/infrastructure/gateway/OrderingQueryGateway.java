package com.aionn.ucp.infrastructure.gateway;

import com.aionn.ordering.application.port.out.OrderPersistencePort;
import com.aionn.ordering.domain.model.Order;
import com.aionn.ordering.domain.model.OrderItem;
import com.aionn.ucp.application.port.out.OrderingQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderingQueryGateway implements OrderingQueryPort {

    @org.springframework.beans.factory.annotation.Value("${ucp.endpoint-base-url:http://localhost:8080}")
    private String endpointBaseUrl;

    private final OrderPersistencePort orderRepository;

    @Override
    public Optional<OrderSnapshot> findOrderById(String orderId) {
        return orderRepository.findById(orderId).map(this::toSnapshot);
    }

    private OrderSnapshot toSnapshot(Order order) {
        List<OrderSnapshot.Line> lines = new ArrayList<>(order.items().size());
        long subtotalMinor = 0;
        for (OrderItem item : order.items()) {
            long unit = item.unitPrice().amount().longValue();
            long lineTotal = unit * item.qty();
            subtotalMinor += lineTotal;
            lines.add(new OrderSnapshot.Line(item.skuId(), item.qty(), unit, lineTotal));
        }
        long shippingMinor = order.getShippingFee() == null ? 0L : order.getShippingFee().amount().longValue();
        long totalMinor = order.getTotalAmount() == null
                ? subtotalMinor + shippingMinor
                : order.getTotalAmount().amount().longValue();

        return new OrderSnapshot(
                order.getOrderId(),
                order.getUserId(),
                order.getMerchantId(),
                order.getCurrency(),
                order.getStatus().name(),
                subtotalMinor,
                shippingMinor,
                totalMinor,
                lines,
                trim(endpointBaseUrl) + "/orders/" + order.getOrderId());
    }

    private static String trim(String url) {
        if (url == null || url.isBlank())
            return "";
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
