package com.ecommerce.sharedkernel.adapter.web.response;

import com.ecommerce.sharedkernel.domain.vo.OffsetPagination;

import java.util.List;

public record OffsetPageResponse<T>(
		List<T> content,
		int page,
		int size,
		long totalElements,
		int totalPages
) {

	public static <T> OffsetPageResponse<T> of(
			List<T> content,
			OffsetPagination pagination,
			long totalElements
	) {
		int totalPages = (int) Math.ceil((double) totalElements / pagination.size());

		return new OffsetPageResponse<>(
				content,
				pagination.page(),
				pagination.size(),
				totalElements,
				totalPages
		);
	}

	public boolean isEmpty() {
		return content == null || content.isEmpty();
	}
}