package kaiquebt.dev.instrutorbrasil.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kaiquebt.dev.instrutorbrasil.dto.request.DocumentRequest;
import kaiquebt.dev.instrutorbrasil.dto.request.OnboardingRequest;
import kaiquebt.dev.instrutorbrasil.dto.response.DocumentUploadResponse;
import kaiquebt.dev.instrutorbrasil.dto.response.MessageResponse;
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
		summary = "Initiate document upload",
		description = "Create a document record and get a presigned URL to upload the file to S3. After upload, call POST /documents/{id}/confirm.",
		security = @SecurityRequirement(name = "bearer-jwt")
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Document created and upload URL generated"),
		@ApiResponse(responseCode = "400", description = "Invalid request or document conflict"),
		@ApiResponse(responseCode = "401", description = "Unauthorized"),
		@ApiResponse(responseCode = "404", description = "No active onboarding found")
	})
	public ResponseEntity<DocumentUploadResponse> addDocument(
			@AuthenticationPrincipal User user,
			@Valid @RequestBody DocumentRequest request) {
		DocumentUploadResponse response = onboardingService.addDocument(user, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/documents/{documentId}/confirm")
	@PreAuthorize("isAuthenticated()")
	@Operation(
		summary = "Confirm document upload",
		description = "Confirm that a document was successfully uploaded to S3. This will query S3 for file metadata and mark the document as uploaded.",
		security = @SecurityRequirement(name = "bearer-jwt")
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Upload confirmed successfully"),
		@ApiResponse(responseCode = "400", description = "Document not in PENDING_UPLOAD status"),
		@ApiResponse(responseCode = "401", description = "Unauthorized"),
		@ApiResponse(responseCode = "404", description = "Document not found")
	})
	public ResponseEntity<MessageResponse> confirmUpload(
			@AuthenticationPrincipal User user,
			@PathVariable Long documentId) {
		MessageResponse response = onboardingService.confirmUpload(user, documentId);
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
		@ApiResponse(responseCode = "204", description = "Document removed successfully"),
		@ApiResponse(responseCode = "401", description = "Unauthorized"),
		@ApiResponse(responseCode = "404", description = "Document or onboarding not found")
	})
	public ResponseEntity<Void> removeDocument(
			@AuthenticationPrincipal User user,
			@PathVariable Long documentId) {
		onboardingService.removeDocument(user, documentId);
		return ResponseEntity.noContent().build();
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
	public ResponseEntity<MessageResponse> submitOnboarding(@AuthenticationPrincipal User user) {
		MessageResponse response = onboardingService.submitOnboarding(user);
		return ResponseEntity.ok(response);
	}
}
