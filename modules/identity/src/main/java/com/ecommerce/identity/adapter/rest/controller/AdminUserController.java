package com.ecommerce.identity.adapter.rest.controller;

import com.ecommerce.identity.adapter.rest.dto.admin.*;
import com.ecommerce.identity.adapter.rest.mapper.admin.AdminUserDtoMapper;
import com.ecommerce.identity.application.port.in.admin.*;
import com.ecommerce.sharedkernel.adapter.web.response.ApiResponse;
import com.ecommerce.sharedkernel.adapter.web.response.PageMetadata;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_SYSTEM_ADMIN','ROLE_CS_ADMIN')")
@Tag(name = "Identity - Admin User", description = "Identity module: admin endpoints for user role and status management")
public class AdminUserController {

	private final ListUsersQueryPort listUsersQueryPort;
	private final GetUserByIdQueryPort getUserByIdQueryPort;
	private final UpdateUserRolesInputPort updateUserRolesInputPort;
	private final RemoveUserRolesInputPort removeUserRolesInputPort;
	private final UpdateUserStatusInputPort updateUserStatusInputPort;
	private final AdminUserDtoMapper adminUserDtoMapper;

	@GetMapping
	@Operation(summary = "List users", description = "Get paginated users for admin management with optional status and role filters")
	public ResponseEntity<ApiResponse<List<UserSummaryResponse>>> listUsers(
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String role,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		var result = listUsersQueryPort.execute(adminUserDtoMapper.toListUsersQuery(status, role, page, size));
		List<UserSummaryResponse> users = adminUserDtoMapper.toUserSummaryResponses(result);
		PageMetadata paging = adminUserDtoMapper.toUserListPaging(result);
		return ResponseEntity.ok(ApiResponse.successWithPaging(users, paging, "Users fetched"));
	}

	@GetMapping("/{userId}")
	@Operation(summary = "Get user detail", description = "Get full user profile details by user ID for admin management")
	public ResponseEntity<ApiResponse<UserDetailResponse>> getUserById(@PathVariable String userId) {
		var result = getUserByIdQueryPort.execute(adminUserDtoMapper.toGetUserQuery(userId));
		var response = adminUserDtoMapper.toUserDetailResponse(result);
		return ResponseEntity.ok(ApiResponse.success(response, "User fetched"));
	}

	@PutMapping("/{userId}/roles")
	@Operation(summary = "Update user roles", description = "Replace user roles with the provided role set")
	public ResponseEntity<ApiResponse<UserRolesResponse>> updateRoles(
			@PathVariable String userId,
			@Valid @RequestBody UpdateRolesRequest request) {
		var result = updateUserRolesInputPort.execute(adminUserDtoMapper.toUpdateRolesCommand(userId, request));
		var response = adminUserDtoMapper.toRolesResponse(result);
		return ResponseEntity.ok(ApiResponse.success(response, "Roles updated"));
	}

	@DeleteMapping("/{userId}/roles")
	@Operation(summary = "Remove user roles", description = "Remove specific roles from a user account")
	public ResponseEntity<Void> removeRoles(
			@PathVariable String userId,
			@Valid @RequestBody UpdateRolesRequest request) {
		removeUserRolesInputPort.execute(adminUserDtoMapper.toRemoveRolesCommand(userId, request));
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{userId}/status")
	@Operation(summary = "Update user status", description = "Change account status for a user")
	public ResponseEntity<ApiResponse<UserStatusResponse>> updateStatus(
			@PathVariable String userId,
			@Valid @RequestBody UpdateUserStatusRequest request) {
		var result = updateUserStatusInputPort.execute(adminUserDtoMapper.toUpdateStatusCommand(userId, request));
		var response = adminUserDtoMapper.toStatusResponse(result);
		return ResponseEntity.ok(ApiResponse.success(response, "Status updated"));
	}
}


