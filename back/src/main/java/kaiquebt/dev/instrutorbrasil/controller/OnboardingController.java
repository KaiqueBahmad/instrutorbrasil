package kaiquebt.dev.instrutorbrasil.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kaiquebt.dev.instrutorbrasil.dto.request.DocumentRequest;
import kaiquebt.dev.instrutorbrasil.dto.request.OnboardingRequest;
import kaiquebt.dev.instrutorbrasil.dto.response.OnboardingResponse;
import kaiquebt.dev.instrutorbrasil.model.User;
import kaiquebt.dev.instrutorbrasil.service.OnboardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/instructor/onboarding")
@RequiredArgsConstructor
@Tag(name = "Instructor Onboarding", description = "Instructor onboarding management APIs")
public class OnboardingController {

	private final OnboardingService onboardingService;

	@PostMapping
	@PreAuthorize("isAuthenticated()")
	@Operation(
		summary = "Start instructor onboarding",
		description = "Initialize the onboarding process to become an instructor",
		security = @SecurityRequirement(name = "bearer-jwt")
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Onboarding started successfully"),
		@ApiResponse(responseCode = "400", description = "Invalid request or validation failed"),
		@ApiResponse(responseCode = "401", description = "Unauthorized"),
		@ApiResponse(responseCode = "409", description = "Already has active onboarding or cooldown active")
	})
	public ResponseEntity<OnboardingResponse> startOnboarding(
			@AuthenticationPrincipal User user,
			@Valid @RequestBody OnboardingRequest request) {
		OnboardingResponse response = onboardingService.startOnboarding(user, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	@Operation(
		summary = "Get user's onboarding",
		description = "Retrieve the current user's latest onboarding information",
		security = @SecurityRequirement(name = "bearer-jwt")
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Onboarding retrieved successfully"),
		@ApiResponse(responseCode = "401", description = "Unauthorized"),
		@ApiResponse(responseCode = "404", description = "No onboarding found")
	})
	public ResponseEntity<OnboardingResponse> getOnboarding(@AuthenticationPrincipal User user) {
		OnboardingResponse response = onboardingService.getOnboarding(user);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/documents")
	@PreAuthorize("isAuthenticated()")
	@Operation(
		summary = "Add document to onboarding",
		description = "Add a document to the user's active onboarding",
		security = @SecurityRequirement(name = "bearer-jwt")
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Document added successfully"),
		@ApiResponse(responseCode = "400", description = "Invalid request or document conflict"),
		@ApiResponse(responseCode = "401", description = "Unauthorized"),
		@ApiResponse(responseCode = "404", description = "No active onboarding found")
	})
	public ResponseEntity<OnboardingResponse> addDocument(
			@AuthenticationPrincipal User user,
			@Valid @RequestBody DocumentRequest request) {
		OnboardingResponse response = onboardingService.addDocument(user, request);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/documents/{documentId}")
	@PreAuthorize("isAuthenticated()")
	@Operation(
		summary = "Remove document from onboarding",
		description = "Remove a document from the user's active onboarding",
		security = @SecurityRequirement(name = "bearer-jwt")
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Document removed successfully"),
		@ApiResponse(responseCode = "401", description = "Unauthorized"),
		@ApiResponse(responseCode = "404", description = "Document or onboarding not found")
	})
	public ResponseEntity<OnboardingResponse> removeDocument(
			@AuthenticationPrincipal User user,
			@PathVariable Long documentId) {
		OnboardingResponse response = onboardingService.removeDocument(user, documentId);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/submit")
	@PreAuthorize("isAuthenticated()")
	@Operation(
		summary = "Submit onboarding for review",
		description = "Submit the user's onboarding for admin review",
		security = @SecurityRequirement(name = "bearer-jwt")
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Onboarding submitted successfully"),
		@ApiResponse(responseCode = "400", description = "Invalid state or missing required documents"),
		@ApiResponse(responseCode = "401", description = "Unauthorized"),
		@ApiResponse(responseCode = "404", description = "No active onboarding found")
	})
	public ResponseEntity<OnboardingResponse> submitOnboarding(@AuthenticationPrincipal User user) {
		OnboardingResponse response = onboardingService.submitOnboarding(user);
		return ResponseEntity.ok(response);
	}
}
