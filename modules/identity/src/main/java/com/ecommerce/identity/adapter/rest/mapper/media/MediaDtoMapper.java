package com.ecommerce.identity.adapter.rest.mapper.media;

import com.ecommerce.identity.adapter.rest.dto.media.UploadSignatureResponse;
import com.ecommerce.identity.application.dto.media.UploadSignatureResult;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MediaDtoMapper {

    UploadSignatureResponse toUploadSignatureResponse(UploadSignatureResult result);
}
