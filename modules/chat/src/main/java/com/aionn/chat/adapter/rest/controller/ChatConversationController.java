package com.aionn.chat.adapter.rest.controller;

import com.aionn.chat.adapter.rest.dto.JoinSupportRequest;
import com.aionn.chat.adapter.rest.dto.StartConversationRequest;
import com.aionn.chat.application.dto.conversation.command.ConversationCommands;
import com.aionn.chat.application.dto.conversation.result.ConversationResult;
import com.aionn.chat.application.service.ConversationService;
import com.aionn.sharedkernel.adapter.web.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat/conversations")
@RequiredArgsConstructor
public class ChatConversationController {

        private final ConversationService conversationService;

        @PostMapping
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ApiResponse<ConversationResult>> start(
                        Authentication auth,
                        @Valid @RequestBody StartConversationRequest request) {
                ConversationResult result = conversationService.startOrGet(new ConversationCommands.StartConversation(
                                auth.getName(),
                                request.buyerDisplayName(), request.buyerAvatarUrl(),
                                request.merchantId(),
                                request.merchantDisplayName(), request.merchantAvatarUrl(),
                                auth.getName()));
                return ResponseEntity.ok(ApiResponse.success(result, "Conversation ready"));
        }

        @GetMapping
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ApiResponse<List<ConversationResult>>> listMine(
                        Authentication auth,
                        @RequestParam(defaultValue = "false") boolean includeArchived,
                        @RequestParam(defaultValue = "50") int limit) {
                int safeLimit = Math.min(Math.max(limit, 1), 100);
                return ResponseEntity.ok(ApiResponse.success(
                                conversationService.listForUser(auth.getName(), includeArchived, safeLimit),
                                "Conversations fetched"));
        }

        @GetMapping("/{conversationId}")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ApiResponse<ConversationResult>> get(
                        Authentication auth,
                        @PathVariable String conversationId) {
                return ResponseEntity.ok(ApiResponse.success(
                                conversationService.getForUser(auth.getName(), conversationId),
                                "Conversation fetched"));
        }

        @PostMapping("/{conversationId}/read")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ApiResponse<ConversationResult>> markRead(
                        Authentication auth,
                        @PathVariable String conversationId) {
                return ResponseEntity.ok(ApiResponse.success(
                                conversationService.markRead(
                                                new ConversationCommands.MarkRead(auth.getName(), conversationId)),
                                "Conversation marked read"));
        }

        @PostMapping("/{conversationId}/archive")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ApiResponse<ConversationResult>> archive(
                        Authentication auth,
                        @PathVariable String conversationId) {
                return ResponseEntity.ok(ApiResponse.success(
                                conversationService.archive(
                                                new ConversationCommands.Archive(auth.getName(), conversationId)),
                                "Conversation archived"));
        }

        @PostMapping("/{conversationId}/unarchive")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ApiResponse<ConversationResult>> unarchive(
                        Authentication auth,
                        @PathVariable String conversationId) {
                return ResponseEntity.ok(ApiResponse.success(
                                conversationService.unarchive(
                                                new ConversationCommands.Unarchive(auth.getName(), conversationId)),
                                "Conversation unarchived"));
        }

        @PostMapping("/{conversationId}/support")
        @PreAuthorize("hasAuthority('ROLE_CS_ADMIN')")
        public ResponseEntity<ApiResponse<ConversationResult>> joinSupport(
                        Authentication auth,
                        @PathVariable String conversationId,
                        @RequestBody(required = false) JoinSupportRequest request) {
                String displayName = request == null ? null : request.displayName();
                String avatarUrl = request == null ? null : request.avatarUrl();
                return ResponseEntity.ok(ApiResponse.success(
                                conversationService.joinSupport(new ConversationCommands.JoinSupport(
                                                auth.getName(), conversationId, displayName, avatarUrl)),
                                "Support joined"));
        }
}
