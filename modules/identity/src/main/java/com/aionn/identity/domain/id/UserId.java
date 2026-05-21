package com.aionn.identity.domain.id;

import com.aionn.sharedkernel.domain.id.BaseId;

public class UserId extends BaseId {

	protected UserId(String value) {
		super(value);
	}

	public static UserId of(String value) {
		return new UserId(value);
	}
}


