package com.ecommerce.identity.adapter.rest.dto.admin;

import com.ecommerce.identity.domain.valueobject.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(
                @NotNull(message = "Status is required") UserStatus status) {
}
