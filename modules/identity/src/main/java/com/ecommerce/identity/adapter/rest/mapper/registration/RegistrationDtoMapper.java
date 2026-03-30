package com.ecommerce.identity.adapter.rest.mapper.registration;

import com.ecommerce.identity.application.dto.registration.CompleteRegistrationCommand;
import com.ecommerce.identity.application.dto.registration.CompleteRegistrationResult;
import com.ecommerce.identity.application.dto.registration.InitiateRegistrationCommand;
import com.ecommerce.identity.application.dto.registration.InitiateRegistrationResult;
import com.ecommerce.identity.application.dto.registration.VerifyRegistrationOtpCommand;
import com.ecommerce.identity.application.dto.registration.VerifyRegistrationOtpResult;
import com.ecommerce.identity.adapter.rest.dto.registration.CompleteRegistrationRequest;
import com.ecommerce.identity.adapter.rest.dto.auth.AuthTokenResponse;
import com.ecommerce.identity.adapter.rest.dto.registration.InitiateRegistrationRequest;
import com.ecommerce.identity.adapter.rest.dto.registration.InitiateRegistrationResponse;
import com.ecommerce.identity.adapter.rest.dto.registration.VerifyOtpRequest;
import com.ecommerce.identity.adapter.rest.dto.registration.VerifyOtpResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RegistrationDtoMapper {

    @Mapping(target = "identity", source = "request.phoneNumber")
    @Mapping(target = "ipAddress", source = "ipAddress")
    InitiateRegistrationCommand toInitiateCommand(InitiateRegistrationRequest request, String ipAddress);

    InitiateRegistrationResponse toInitiateResponse(InitiateRegistrationResult result);

    VerifyRegistrationOtpCommand toVerifyOtpCommand(String regId, VerifyOtpRequest request);

    VerifyOtpResponse toVerifyOtpResponse(VerifyRegistrationOtpResult result);

    CompleteRegistrationCommand toCompleteCommand(String regId, CompleteRegistrationRequest request);

    AuthTokenResponse toAuthTokenResponse(CompleteRegistrationResult result);
}
