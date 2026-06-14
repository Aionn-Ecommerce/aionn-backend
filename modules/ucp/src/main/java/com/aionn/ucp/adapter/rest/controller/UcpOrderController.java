package com.aionn.ucp.adapter.rest.controller;

import com.aionn.ucp.application.dto.order.OrderDtos;
import com.aionn.ucp.application.service.UcpOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/ucp/v1/orders", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "UCP - Order", description = "UCP order capability (read-only)")
public class UcpOrderController {

    private final UcpOrderService orderService;

    @GetMapping("/{id}")
    @Operation(summary = "Get order (UCP)")
    public ResponseEntity<OrderDtos.OrderResponse> getOrder(@PathVariable String id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }
}
