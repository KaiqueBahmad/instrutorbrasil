package kaiquebt.dev.instrutorbrasil.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kaiquebt.dev.instrutorbrasil.dto.request.ReviewOnboardingRequest;
import kaiquebt.dev.instrutorbrasil.dto.request.DocumentReviewRequest;
import kaiquebt.dev.instrutorbrasil.dto.response.DocumentDownloadUrlResponse;
import kaiquebt.dev.instrutorbrasil.dto.response.DocumentResponse;
import kaiquebt.dev.instrutorbrasil.dto.response.MessageResponse;
import kaiquebt.dev.instrutorbrasil.dto.response.OnboardingResponse;
import kaiquebt.dev.instrutorbrasil.dto.response.OnboardingSummaryResponse;
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
@RequestMapping("/admin/onboarding")
@RequiredArgsConstructor
@Tag(name = "Admin - Onboarding Management", description = "Admin APIs for managing instructor onboardings")
public class AdminOnboardingController {

	private final OnboardingService onboardingService;

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
		summary = "List pending onboardings",
		description = "Retrieve a summary of all onboarding requests awaiting review (IN_REVIEW status only). " +
				"Returns minimal data for listing purposes. Use GET /{id} to get full details of a specific onboarding.",
		security = @SecurityRequirement(name = "bearer-jwt")
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Pending onboardings retrieved successfully"),
		@ApiResponse(responseCode = "401", description = "Unauthorized"),
		@ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
	})
	public ResponseEntity<List<OnboardingSummaryResponse>> getPendingOnboardings() {
		List<OnboardingSummaryResponse> onboardings = onboardingService.getPendingOnboardingsSummary();
		return ResponseEntity.ok(onboardings);
	}

	@GetMapping("/{onboardingId}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
		summary = "Get onboarding details",
		description = "Retrieve complete details of a specific onboarding request, including all documents and personal information.",
		security = @SecurityRequirement(name = "bearer-jwt")
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Onboarding details retrieved successfully"),
		@ApiResponse(responseCode = "401", description = "Unauthorized"),
		@ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
		@ApiResponse(responseCode = "404", description = "Onboarding not found")
	})
	public ResponseEntity<OnboardingResponse> getOnboardingById(@PathVariable Long onboardingId) {
		OnboardingResponse response = onboardingService.getOnboardingById(onboardingId);
		return ResponseEntity.ok(response);
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

	@GetMapping("/{onboardingId}/documents/download-urls")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
		summary = "Get all document download URLs",
		description = "Generate presigned URLs for all documents in an onboarding. " +
				"Returns document metadata (purpose, side, filename, size, etc.) along with presigned download URLs. " +
				"The URLs include correct Content-Type header override, ensuring browsers display files correctly. " +
				"URLs expire after the configured time (default 15 minutes). " +
				"Only returns documents with UPLOADED or VERIFIED status.",
		security = @SecurityRequirement(name = "bearer-jwt")
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Download URLs generated successfully"),
		@ApiResponse(responseCode = "401", description = "Unauthorized"),
		@ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
		@ApiResponse(responseCode = "404", description = "Onboarding not found")
	})
	public ResponseEntity<List<DocumentDownloadUrlResponse>> getOnboardingDocumentsDownloadUrls(
			@PathVariable Long onboardingId) {
		List<DocumentDownloadUrlResponse> response = onboardingService.getOnboardingDocumentsWithUrls(onboardingId);
		return ResponseEntity.ok(response);
	}
	@PostMapping("/documents/{documentId}/review")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
		summary = "Review a document",
		description = "Approve (VERIFIED) or reject (REJECTED) a specific document. " +
				"Only documents with status UPLOADED can be reviewed. " +
				"Document must belong to an onboarding that is currently IN_REVIEW.",
		security = @SecurityRequirement(name = "bearer-jwt")
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Document reviewed successfully"),
		@ApiResponse(responseCode = "400", description = "Invalid request or document not eligible for judgment"),
		@ApiResponse(responseCode = "401", description = "Unauthorized"),
		@ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
		@ApiResponse(responseCode = "404", description = "Document not found")
	})
	public ResponseEntity<MessageResponse> reviewDocument(
			@PathVariable Long documentId,
			@AuthenticationPrincipal User reviewer,
			@Valid @RequestBody DocumentReviewRequest request) {
		onboardingService.reviewDocument(documentId, reviewer, request);
		return ResponseEntity.ok(new MessageResponse("Document reviewed successfully"));
	}
}
