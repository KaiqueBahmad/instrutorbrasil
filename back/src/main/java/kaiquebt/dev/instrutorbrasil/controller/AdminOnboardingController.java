package kaiquebt.dev.instrutorbrasil.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kaiquebt.dev.instrutorbrasil.dto.request.ReviewOnboardingRequest;
import kaiquebt.dev.instrutorbrasil.dto.response.OnboardingResponse;
import kaiquebt.dev.instrutorbrasil.model.User;
import kaiquebt.dev.instrutorbrasil.model.enums.OnboardingStatus;
import kaiquebt.dev.instrutorbrasil.service.OnboardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/onboardings")
@RequiredArgsConstructor
@Tag(name = "Admin - Onboarding Management", description = "Admin APIs for managing instructor onboardings")
public class AdminOnboardingController {

	private final OnboardingService onboardingService;

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
		summary = "List all onboardings",
		description = "Retrieve all onboarding requests (admin only)",
		security = @SecurityRequirement(name = "bearer-jwt")
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Onboardings retrieved successfully"),
		@ApiResponse(responseCode = "401", description = "Unauthorized"),
		@ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
	})
	public ResponseEntity<List<OnboardingResponse>> getAllOnboardings() {
		List<OnboardingResponse> onboardings = onboardingService.getAllOnboardings();
		return ResponseEntity.ok(onboardings);
	}

	@GetMapping("/status/{status}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
		summary = "List onboardings by status",
		description = "Retrieve onboarding requests filtered by status (admin only)",
		security = @SecurityRequirement(name = "bearer-jwt")
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Onboardings retrieved successfully"),
		@ApiResponse(responseCode = "401", description = "Unauthorized"),
		@ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
	})
	public ResponseEntity<List<OnboardingResponse>> getOnboardingsByStatus(
			@PathVariable OnboardingStatus status) {
		List<OnboardingResponse> onboardings = onboardingService.getOnboardingsByStatus(status);
		return ResponseEntity.ok(onboardings);
	}

	@PostMapping("/{onboardingId}/review")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
		summary = "Review an onboarding",
		description = "Approve or reject an instructor onboarding request (admin only)",
		security = @SecurityRequirement(name = "bearer-jwt")
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Onboarding reviewed successfully"),
		@ApiResponse(responseCode = "400", description = "Invalid request or onboarding not in review state"),
		@ApiResponse(responseCode = "401", description = "Unauthorized"),
		@ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
		@ApiResponse(responseCode = "404", description = "Onboarding not found")
	})
	public ResponseEntity<OnboardingResponse> reviewOnboarding(
			@PathVariable Long onboardingId,
			@AuthenticationPrincipal User reviewer,
			@Valid @RequestBody ReviewOnboardingRequest request) {
		OnboardingResponse response = onboardingService.reviewOnboarding(onboardingId, reviewer, request);
		return ResponseEntity.ok(response);
	}
}
