package com.aionn.ucp.infrastructure.gateway;

import com.aionn.sharedkernel.integration.port.identity.UserAddressLookupPort;
import com.aionn.sharedkernel.integration.port.ordering.OrderPlacementPort;
import com.aionn.ucp.domain.exception.UcpErrorCode;
import com.aionn.ucp.domain.exception.UcpException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderPlacementGateway implements com.aionn.ucp.application.port.out.OrderPlacementPort {

    private final OrderPlacementPort orderPlacementPort;
    private final UserAddressLookupPort userAddressLookupPort;

    @Override
    public com.aionn.ucp.application.port.out.OrderPlacementPort.PlacedOrder place(
            com.aionn.ucp.application.port.out.OrderPlacementPort.PlaceCommand command) {
        UserAddressLookupPort.UserAddress addr = userAddressLookupPort
                .findOwned(command.addressId(), command.userId())
                .orElseThrow(() -> new UcpException(UcpErrorCode.INVALID_ARGUMENT,
                        "Address not found for user: " + command.addressId()));

        var lines = command.lines().stream()
                .map(l -> new OrderPlacementPort.PlaceCommand.Line(l.skuId(), l.qty()))
                .toList();

        OrderPlacementPort.PlacedOrder result = orderPlacementPort.placeHeadless(
                new OrderPlacementPort.PlaceCommand(
                        command.userId(),
                        lines,
                        command.voucherCode(),
                        command.paymentMethodId(),
                        command.currency(),
                        command.shippingFee(),
                        new OrderPlacementPort.PlaceCommand.ShippingAddress(
                                addr.addressId(),
                                addr.contactName(),
                                addr.phone(),
                                addr.detailAddress(),
                                addr.wardCode(),
                                addr.districtCode(),
                                addr.provinceCode(),
                                addr.countryCode())));

        return new com.aionn.ucp.application.port.out.OrderPlacementPort.PlacedOrder(
                result.orderId(), result.totalAmountMinor(), result.currency());
    }
}
