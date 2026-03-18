package com.ecommerce.identity.presentation.mapper.registration;

import com.ecommerce.identity.application.dto.registration.CompleteRegistrationCommand;
import com.ecommerce.identity.application.dto.registration.CompleteRegistrationResult;
import com.ecommerce.identity.application.dto.registration.InitiateRegistrationCommand;
import com.ecommerce.identity.application.dto.registration.InitiateRegistrationResult;
import com.ecommerce.identity.application.dto.registration.VerifyRegistrationOtpCommand;
import com.ecommerce.identity.application.dto.registration.VerifyRegistrationOtpResult;
import com.ecommerce.identity.presentation.dto.registration.CompleteRegistrationRequest;
import com.ecommerce.identity.presentation.dto.registration.CompleteRegistrationResponse;
import com.ecommerce.identity.presentation.dto.registration.InitiateRegistrationRequest;
import com.ecommerce.identity.presentation.dto.registration.InitiateRegistrationResponse;
import com.ecommerce.identity.presentation.dto.registration.VerifyOtpRequest;
import com.ecommerce.identity.presentation.dto.registration.VerifyOtpResponse;
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

    CompleteRegistrationResponse toCompleteResponse(CompleteRegistrationResult result);
}
