package com.aionn.identity.adapter.rest.support;

import com.aionn.sharedkernel.adapter.web.response.ApiResponse;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class NoStoreResponseFactory {

    public <T> ResponseEntity<ApiResponse<T>> ok(ApiResponse<T> body) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore().mustRevalidate().cachePrivate())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0")
                .body(body);
    }
}
