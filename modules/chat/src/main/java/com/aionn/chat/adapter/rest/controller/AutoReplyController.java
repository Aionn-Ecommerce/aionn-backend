package com.aionn.chat.adapter.rest.controller;

import com.aionn.chat.adapter.rest.dto.UpdateAutoReplyRequest;
import com.aionn.chat.application.dto.autoreply.command.AutoReplyCommands;
import com.aionn.chat.application.dto.autoreply.result.AutoReplyResult;
import com.aionn.chat.application.service.AutoReplyService;
import com.aionn.sharedkernel.adapter.web.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat/merchants/{merchantId}/auto-reply")
@RequiredArgsConstructor
public class AutoReplyController {

    private final AutoReplyService autoReplyService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AutoReplyResult>> get(@PathVariable String merchantId) {
        return ResponseEntity.ok(ApiResponse.success(
                autoReplyService.get(merchantId), "Auto-reply config fetched"));
    }

    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AutoReplyResult>> update(
            @PathVariable String merchantId,
            @RequestBody UpdateAutoReplyRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                autoReplyService.update(new AutoReplyCommands.UpdateAutoReply(
                        merchantId, request.enabled(), request.greeting(), request.awayMessage(),
                        request.workingHourStart(), request.workingHourEnd(), request.workingDays())),
                "Auto-reply config saved"));
    }
}

