package kaiquebt.dev.instrutorbrasil.controller;

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
public class AuthController {

	private final AuthService authService;

	@PostMapping("/refresh-token")
	public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
		AuthResponse response = authService.refreshToken(request);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/me")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
		UserResponse response = authService.getCurrentUser(userDetails.getUsername());
		return ResponseEntity.ok(response);
	}
}
