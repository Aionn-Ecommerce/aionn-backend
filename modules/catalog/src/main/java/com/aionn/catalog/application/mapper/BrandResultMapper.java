package com.aionn.catalog.application.mapper;

import com.aionn.catalog.application.dto.brand.result.BrandResult;
import com.aionn.catalog.domain.model.Brand;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BrandResultMapper {

    BrandResult toResult(Brand brand);
}
