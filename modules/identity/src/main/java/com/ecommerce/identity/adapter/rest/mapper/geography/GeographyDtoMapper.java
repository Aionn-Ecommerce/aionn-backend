package com.ecommerce.identity.adapter.rest.mapper.geography;

import com.ecommerce.identity.adapter.rest.dto.geography.GeographyResponse;
import com.ecommerce.identity.application.dto.geography.result.GeographyResult;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GeographyDtoMapper {

    GeographyResponse toResponse(GeographyResult result);

    List<GeographyResponse> toResponses(List<GeographyResult> results);
}
