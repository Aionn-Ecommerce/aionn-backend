package com.aionn.identity.application.dto.security.result;

/**
 * Result returned after initiating a password reset. Always reports
 * {@code accepted=true} so that callers cannot distinguish between a real and
 * non-existent identity.
 */
public record PasswordResetResult(boolean accepted) {

    public static PasswordResetResult acceptedResult() {
        return new PasswordResetResult(true);
    }
}

