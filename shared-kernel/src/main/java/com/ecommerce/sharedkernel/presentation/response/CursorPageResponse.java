package com.ecommerce.sharedkernel.presentation.response;

import java.util.List;
import java.util.function.Function;

public record CursorPageResponse<T>(
        List<T> content,
        String nextCursor,
        boolean hasNext,
        int size) {

    public static <T> CursorPageResponse<T> of(List<T> content, int size, Function<T, String> cursorExtractor) {

        boolean hasNext = content.size() > size;
        List<T> data = hasNext ? content.subList(0, size) : content;

        String nextCursor = null;
        if (hasNext && !data.isEmpty()) {
            T lastItem = data.get(data.size() - 1);
            nextCursor = cursorExtractor.apply(lastItem);
        }

        return new CursorPageResponse<>(data, nextCursor, hasNext, size);
    }

    public boolean isEmpty() {
        return content == null || content.isEmpty();
    }
}