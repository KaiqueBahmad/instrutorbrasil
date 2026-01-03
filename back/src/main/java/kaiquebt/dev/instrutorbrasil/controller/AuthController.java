package kaiquebt.dev.instrutorbrasil.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import kaiquebt.dev.instrutorbrasil.dto.request.RefreshTokenRequest;
import kaiquebt.dev.instrutorbrasil.dto.response.AuthResponse;
import kaiquebt.dev.instrutorbrasil.dto.response.UserResponse;
import kaiquebt.dev.instrutorbrasil.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/refresh-token")
	@Operation(
		summary = "Refresh access token",
		description = "Generate a new access token using a valid refresh token"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
		@ApiResponse(responseCode = "400", description = "Invalid refresh token"),
		@ApiResponse(responseCode = "401", description = "Expired or invalid token")
	})
	public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
		AuthResponse response = authService.refreshToken(request);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/me")
	@PreAuthorize("isAuthenticated()")
	@Operation(
		summary = "Get current user",
		description = "Retrieve the authenticated user's information",
		security = @SecurityRequirement(name = "bearer-jwt")
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "User information retrieved successfully"),
		@ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
		@ApiResponse(responseCode = "404", description = "User not found")
	})
	public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
		UserResponse response = authService.getCurrentUser(userDetails.getUsername());
		return ResponseEntity.ok(response);
	}
}
