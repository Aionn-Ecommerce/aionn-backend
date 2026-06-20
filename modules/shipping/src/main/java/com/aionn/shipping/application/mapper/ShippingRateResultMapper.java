package com.aionn.shipping.application.mapper;

import com.aionn.shipping.application.dto.rate.result.ShippingRateResult;
import com.aionn.shipping.domain.model.ShippingRate;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShippingRateResultMapper {

    ShippingRateResult toResult(ShippingRate rate);
}
