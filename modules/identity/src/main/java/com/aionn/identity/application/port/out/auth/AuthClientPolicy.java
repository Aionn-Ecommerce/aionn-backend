package com.aionn.identity.application.port.out.auth;

public interface AuthClientPolicy {
	String getClientTypeHeader();

	String getMobileClientValue();
}

