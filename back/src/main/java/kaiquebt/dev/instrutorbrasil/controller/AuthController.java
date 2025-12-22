package kaiquebt.dev.instrutorbrasil.controller;

import jakarta.validation.Valid;
import kaiquebt.dev.instrutorbrasil.dto.request.*;
import kaiquebt.dev.instrutorbrasil.dto.response.AuthResponse;
import kaiquebt.dev.instrutorbrasil.dto.response.MessageResponse;
import kaiquebt.dev.instrutorbrasil.dto.response.UserResponse;
import kaiquebt.dev.instrutorbrasil.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
		AuthResponse response = authService.register(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		AuthResponse response = authService.login(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/refresh-token")
	public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
		AuthResponse response = authService.refreshToken(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
		MessageResponse response = authService.forgotPassword(request);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/reset-password")
	public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
		MessageResponse response = authService.resetPassword(request);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/me")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
		UserResponse response = authService.getCurrentUser(userDetails.getUsername());
		return ResponseEntity.ok(response);
	}
}
