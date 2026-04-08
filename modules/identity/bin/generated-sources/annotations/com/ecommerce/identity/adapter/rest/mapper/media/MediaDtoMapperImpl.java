package com.ecommerce.identity.adapter.rest.mapper.media;

import com.ecommerce.identity.adapter.rest.dto.media.UploadSignatureResponse;
import com.ecommerce.identity.application.dto.media.result.UploadSignatureResult;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-05T19:28:09+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class MediaDtoMapperImpl implements MediaDtoMapper {

    @Override
    public UploadSignatureResponse toUploadSignatureResponse(UploadSignatureResult result) {
        if ( result == null ) {
            return null;
        }

        String signature = null;
        String timestamp = null;
        String apiKey = null;
        String cloudName = null;
        String uploadUrl = null;
        String folder = null;

        signature = result.signature();
        timestamp = result.timestamp();
        apiKey = result.apiKey();
        cloudName = result.cloudName();
        uploadUrl = result.uploadUrl();
        folder = result.folder();

        UploadSignatureResponse uploadSignatureResponse = new UploadSignatureResponse( signature, timestamp, apiKey, cloudName, uploadUrl, folder );

        return uploadSignatureResponse;
    }
}
