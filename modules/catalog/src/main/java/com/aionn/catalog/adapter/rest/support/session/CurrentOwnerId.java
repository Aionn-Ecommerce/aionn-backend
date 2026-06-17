package com.aionn.catalog.adapter.rest.support.session;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Resolves to the authenticated user's principal id (the userId / ownerId).
 *
 * <p>
 * Use this in merchant management endpoints where the action targets the
 * merchant entity owned by the user (register, update profile, close).
 * </p>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentOwnerId {
}
