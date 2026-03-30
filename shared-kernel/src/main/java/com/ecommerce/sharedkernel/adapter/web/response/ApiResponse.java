package com.ecommerce.sharedkernel.adapter.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
		String statusCode,
		String message,
		T data,
		Instant timestamp,
		Object paging
) {

	// SUCCESS RESPONSE

	public static <T> ApiResponse<T> success(T data, String message) {
		return new ApiResponse<>("200", message, data, Instant.now(), null);
	}

	public static ApiResponse<Void> success(String message) {
		return new ApiResponse<>("200", message, null, Instant.now(), null);
	}

	public static <T> ApiResponse<T> created(String message, T data) {
		return new ApiResponse<>(
				String.valueOf(HttpStatus.CREATED.value()),
				message,
				data,
				Instant.now(),
				null
		);
	}

	public static <T> ResponseEntity<ApiResponse<T>> createdResponse(String message, T data) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(created(message, data));
	}

	public static <T> ApiResponse<T> successWithPaging(
			T data,
			Object paging,
			String message
	) {
		return new ApiResponse<>("200", message, data, Instant.now(), paging);
	}

	// ERROR RESPONSE

	public static <T> ApiResponse<T> error(String statusCode, String message) {
		return new ApiResponse<>(statusCode, message, null, Instant.now(), null);
	}

	public static <T> ApiResponse<T> error(HttpStatus status, String message) {
		return new ApiResponse<>(
				String.valueOf(status.value()),
				message,
				null,
				Instant.now(),
				null
		);
	}

	public static <T> ApiResponse<T> error(String statusCode, String message, T data) {
		return new ApiResponse<>(statusCode, message, data, Instant.now(), null);
	}
}