package com.ecommerce.identity.application.port.out.auth;

public interface AuthClientPolicy {
	String getClientTypeHeader();

	String getMobileClientValue();
}
