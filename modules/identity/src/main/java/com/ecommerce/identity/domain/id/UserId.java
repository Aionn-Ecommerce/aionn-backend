package com.ecommerce.identity.domain.id;

import com.ecommerce.sharedkernel.domain.id.BaseId;

public class UserId extends BaseId {

	protected UserId(String value) {
		super(value);
	}

	public static UserId of(String value) {
		return new UserId(value);
	}
}

