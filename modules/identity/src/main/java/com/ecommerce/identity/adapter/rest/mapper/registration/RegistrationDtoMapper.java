package com.ecommerce.identity.adapter.rest.mapper.registration;

import com.ecommerce.identity.adapter.rest.dto.auth.AuthTokenResponse;
import com.ecommerce.identity.adapter.rest.dto.registration.*;
import com.ecommerce.identity.application.dto.registration.command.CompleteRegistrationCommand;
import com.ecommerce.identity.application.dto.registration.command.InitiateRegistrationCommand;
import com.ecommerce.identity.application.dto.registration.command.ResendRegistrationOtpCommand;
import com.ecommerce.identity.application.dto.registration.command.VerifyRegistrationOtpCommand;
import com.ecommerce.identity.application.dto.registration.result.CompleteRegistrationResult;
import com.ecommerce.identity.application.dto.registration.result.InitiateRegistrationResult;
import com.ecommerce.identity.application.dto.registration.result.ResendRegistrationOtpResult;
import com.ecommerce.identity.application.dto.registration.result.VerifyRegistrationOtpResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RegistrationDtoMapper {

	@Mapping(target = "identity", source = "request.phoneNumber")
	InitiateRegistrationCommand toInitiateCommand(InitiateRegistrationRequest request, String ipAddress);

	RegistrationSessionResponse toInitiateResponse(InitiateRegistrationResult result);

	VerifyRegistrationOtpCommand toVerifyOtpCommand(String regId, VerifyOtpRequest request);

	VerifyOtpResponse toVerifyOtpResponse(VerifyRegistrationOtpResult result);

	CompleteRegistrationCommand toCompleteCommand(
			String regId,
			CompleteRegistrationRequest request,
			String ipAddress,
			String userAgent);

	AuthTokenResponse toAuthTokenResponse(CompleteRegistrationResult result);

	ResendRegistrationOtpCommand toResendOtpCommand(String regId, String ipAddress);

	RegistrationSessionResponse toResendOtpResponse(ResendRegistrationOtpResult result);
}
