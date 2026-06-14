package com.aionn.ucp.infrastructure.gateway;

import com.aionn.identity.application.port.out.address.AddressPersistencePort;
import com.aionn.identity.domain.model.Address;
import com.aionn.ordering.application.dto.order.command.PlaceOrderHeadlessCommand;
import com.aionn.ordering.application.dto.order.result.OrderResult;
import com.aionn.ordering.application.service.OrderService;
import com.aionn.ordering.domain.valueobject.ShippingAddress;
import com.aionn.ucp.application.port.out.OrderPlacementPort;
import com.aionn.ucp.domain.exception.UcpErrorCode;
import com.aionn.ucp.domain.exception.UcpException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderPlacementGateway implements OrderPlacementPort {

        private final OrderService orderService;
        private final AddressPersistencePort addressPersistencePort;

        @Override
        public PlacedOrder place(PlaceCommand command) {
                ShippingAddress addressSnapshot = resolveAddress(command.userId(), command.addressId());

                List<PlaceOrderHeadlessCommand.Line> lines = command.lines().stream()
                                .map(l -> new PlaceOrderHeadlessCommand.Line(l.skuId(), l.qty()))
                                .toList();
                PlaceOrderHeadlessCommand cmd = new PlaceOrderHeadlessCommand(
                                command.userId(),
                                lines,
                                null,
                                command.paymentMethodId(),
                                command.currency(),
                                command.shippingFee(),
                                addressSnapshot);

                OrderResult result = orderService.placeOrderHeadless(cmd);
                return new PlacedOrder(result.orderId(),
                                result.totalAmount() == null ? 0L : result.totalAmount().longValue(),
                                result.currency());
        }

        private ShippingAddress resolveAddress(String userId, String addressId) {
                Address address = addressPersistencePort.findByAddressIdAndUserId(addressId, userId)
                                .orElseThrow(() -> new UcpException(UcpErrorCode.INVALID_ARGUMENT,
                                                "Address not found for user: " + addressId));
                return new ShippingAddress(
                                address.addressId(),
                                address.contactName(),
                                address.phone(),
                                address.detailAddress(),
                                address.wardCode(),
                                address.districtCode(),
                                address.provinceCode(),
                                "VN");
        }
}
