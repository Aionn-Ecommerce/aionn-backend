package com.aionn.ucp.infrastructure.gateway;

import com.aionn.sharedkernel.integration.port.ordering.OrderSnapshotQueryPort;
import com.aionn.ucp.application.port.out.OrderingQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * UCP-side adapter for ordering queries. Pulls the flat snapshot through the
 * shared-kernel port, then attaches the storefront permalink — that base URL
 * is a UCP concern (the agent rendering the response decides where users go),
 * not ordering's, so the port intentionally omits it.
 */
@Component
@RequiredArgsConstructor
public class OrderingQueryGateway implements OrderingQueryPort {

    @Value("${ucp.endpoint-base-url:http://localhost:8080}")
    private String endpointBaseUrl;

    private final OrderSnapshotQueryPort orderSnapshotQuery;

    @Override
    public Optional<OrderSnapshot> findOrderById(String orderId) {
        return orderSnapshotQuery.findOrderById(orderId).map(this::toSnapshot);
    }

    private OrderSnapshot toSnapshot(OrderSnapshotQueryPort.OrderSnapshot src) {
        java.util.List<OrderSnapshot.Line> lines = src.lines().stream()
                .map(l -> new OrderSnapshot.Line(l.skuId(), l.qty(), l.unitPriceMinor(), l.lineTotalMinor()))
                .toList();
        return new OrderSnapshot(
                src.orderId(),
                src.userId(),
                src.merchantId(),
                src.currency(),
                src.status(),
                src.subtotalMinor(),
                src.shippingMinor(),
                src.totalMinor(),
                lines,
                trim(endpointBaseUrl) + "/orders/" + src.orderId());
    }

    private static String trim(String url) {
        if (url == null || url.isBlank()) return "";
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
