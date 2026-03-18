package com.ecommerce.identity.domain.id;

import com.ecommerce.sharedkernel.domain.id.BaseId;

public class RegistrationId extends BaseId {
    
	private RegistrationId(String value) {
		super(value);
	}

	public static RegistrationId of(String value) {
		return new RegistrationId(value);
	}
}