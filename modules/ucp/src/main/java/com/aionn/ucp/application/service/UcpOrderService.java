package com.aionn.ucp.application.service;

import com.aionn.ucp.application.dto.envelope.UcpMessage;
import com.aionn.ucp.application.dto.order.OrderDtos;
import com.aionn.ucp.application.port.out.OrderingQueryPort;
import com.aionn.ucp.domain.model.CapabilityName;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UcpOrderService {

        private final OrderingQueryPort orderingQueryPort;
        private final UcpEnvelopeFactory envelopeFactory;

        public OrderDtos.OrderResponse getOrder(String orderId) {
                Optional<OrderingQueryPort.OrderSnapshot> opt = orderingQueryPort.findOrderById(orderId);
                if (opt.isEmpty()) {
                        return OrderDtos.OrderResponse.error(
                                        envelopeFactory.error(CapabilityName.ORDER),
                                        List.of(UcpMessage.error("not_found", "Order not found.", "unrecoverable")));
                }
                OrderingQueryPort.OrderSnapshot snap = opt.get();
                List<OrderDtos.OrderLineItem> lines = new ArrayList<>(snap.lines().size());
                for (OrderingQueryPort.OrderSnapshot.Line l : snap.lines()) {
                        lines.add(new OrderDtos.OrderLineItem(
                                        "li_" + l.skuId(),
                                        new OrderDtos.OrderItem(l.skuId(), null, l.unitPriceMinor()),
                                        new OrderDtos.Quantity(l.qty(), null),
                                        List.of(
                                                        new OrderDtos.Total("subtotal", l.lineTotalMinor()),
                                                        new OrderDtos.Total("total", l.lineTotalMinor())),
                                        snap.status()));
                }
                List<OrderDtos.Total> totals = List.of(
                                new OrderDtos.Total("subtotal", snap.subtotalMinor()),
                                new OrderDtos.Total("fulfillment", snap.shippingMinor()),
                                new OrderDtos.Total("total", snap.totalMinor()));

                return new OrderDtos.OrderResponse(
                                envelopeFactory.ok(CapabilityName.ORDER),
                                snap.id(),
                                null,
                                snap.permalinkUrl(),
                                snap.currency(),
                                snap.status(),
                                lines,
                                null,
                                totals,
                                null);
        }
}
