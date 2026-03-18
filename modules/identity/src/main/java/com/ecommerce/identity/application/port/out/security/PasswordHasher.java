package com.ecommerce.identity.application.port.out.security;

public interface PasswordHasher {

    String hash(String rawPassword);
}