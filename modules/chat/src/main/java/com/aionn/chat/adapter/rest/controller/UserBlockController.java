package com.aionn.chat.adapter.rest.controller;

import com.aionn.chat.adapter.rest.dto.BlockUserRequest;
import com.aionn.chat.application.dto.block.command.BlockCommands;
import com.aionn.chat.application.dto.block.result.BlockResult;
import com.aionn.chat.application.service.UserBlockService;
import com.aionn.sharedkernel.adapter.web.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat/blocks")
@RequiredArgsConstructor
public class UserBlockController {

    private final UserBlockService blockService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BlockResult>> block(
            Authentication auth,
            @Valid @RequestBody BlockUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                blockService.block(new BlockCommands.BlockUser(
                        auth.getName(), request.blockedId(), request.reason())),
                "User blocked"));
    }

    @DeleteMapping("/{blockedId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BlockResult>> unblock(
            Authentication auth,
            @PathVariable String blockedId) {
        return ResponseEntity.ok(ApiResponse.success(
                blockService.unblock(new BlockCommands.UnblockUser(auth.getName(), blockedId)),
                "User unblocked"));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<BlockResult>>> listMyBlocks(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                blockService.listMyBlocks(auth.getName()), "Blocks fetched"));
    }
}

