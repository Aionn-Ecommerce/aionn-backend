package com.aionn.ucp.application.dto.envelope;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UcpMessage(
        String type,
        String code,
        String content,
        String severity,
        String path) {

    public static UcpMessage info(String code, String content) {
        return new UcpMessage("info", code, content, null, null);
    }

    public static UcpMessage error(String code, String content, String severity) {
        return new UcpMessage("error", code, content, severity, null);
    }

    public static UcpMessage warning(String code, String content) {
        return new UcpMessage("warning", code, content, "recoverable", null);
    }
}
