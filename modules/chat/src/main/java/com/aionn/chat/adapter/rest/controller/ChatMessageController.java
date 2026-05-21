package com.aionn.chat.adapter.rest.controller;

import com.aionn.chat.adapter.rest.dto.SendMessageRequest;
import com.aionn.chat.adapter.rest.dto.SetTypingRequest;
import com.aionn.chat.application.dto.message.command.MessageCommands;
import com.aionn.chat.application.dto.message.result.MessageResult;
import com.aionn.chat.application.service.MessageService;
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

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatMessageController {

        private final MessageService messageService;

        @PostMapping("/conversations/{conversationId}/messages")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ApiResponse<MessageResult>> send(
                        Authentication auth,
                        @PathVariable String conversationId,
                        @Valid @RequestBody SendMessageRequest request) {
                MessageResult result = messageService.send(new MessageCommands.SendMessage(
                                auth.getName(), conversationId, request.type(), request.body(), request.metadata()));
                return ResponseEntity.ok(ApiResponse.success(result, "Message sent"));
        }

        @GetMapping("/conversations/{conversationId}/messages")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ApiResponse<List<MessageResult>>> list(
                        Authentication auth,
                        @PathVariable String conversationId,
                        @RequestParam(required = false) Instant before,
                        @RequestParam(defaultValue = "30") int limit) {
                int safeLimit = Math.min(Math.max(limit, 1), 100);
                List<MessageResult> results = before == null
                                ? messageService.listLatest(auth.getName(), conversationId, safeLimit)
                                : messageService.listBefore(auth.getName(), conversationId, before, safeLimit);
                return ResponseEntity.ok(ApiResponse.success(results, "Messages fetched"));
        }

        @PostMapping("/messages/{messageId}/delivered")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ApiResponse<MessageResult>> markDelivered(
                        Authentication auth,
                        @PathVariable String messageId) {
                return ResponseEntity.ok(ApiResponse.success(
                                messageService.markDelivered(
                                                new MessageCommands.DeliverMessage(auth.getName(), messageId)),
                                "Message delivered"));
        }

        @PostMapping("/messages/{messageId}/read")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ApiResponse<MessageResult>> markRead(
                        Authentication auth,
                        @PathVariable String messageId) {
                return ResponseEntity.ok(ApiResponse.success(
                                messageService.markRead(new MessageCommands.ReadMessage(auth.getName(), messageId)),
                                "Message read"));
        }

        @PostMapping("/messages/{messageId}/recall")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ApiResponse<MessageResult>> recall(
                        Authentication auth,
                        @PathVariable String messageId) {
                return ResponseEntity.ok(ApiResponse.success(
                                messageService.recall(new MessageCommands.RecallMessage(auth.getName(), messageId)),
                                "Message recalled"));
        }

        @PostMapping("/conversations/{conversationId}/typing")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<ApiResponse<Void>> setTyping(
                        Authentication auth,
                        @PathVariable String conversationId,
                        @RequestBody SetTypingRequest request) {
                messageService.setTyping(new MessageCommands.SetTyping(
                                auth.getName(), conversationId, request.typing()));
                return ResponseEntity.ok(ApiResponse.success("Typing state updated"));
        }
}
