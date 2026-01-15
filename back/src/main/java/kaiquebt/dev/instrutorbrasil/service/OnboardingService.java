package kaiquebt.dev.instrutorbrasil.service;

import kaiquebt.dev.instrutorbrasil.dto.request.DocumentRequest;
import kaiquebt.dev.instrutorbrasil.dto.request.OnboardingRequest;
import kaiquebt.dev.instrutorbrasil.dto.request.ReviewOnboardingRequest;
import kaiquebt.dev.instrutorbrasil.dto.response.DocumentResponse;
import kaiquebt.dev.instrutorbrasil.dto.response.DocumentUploadResponse;
import kaiquebt.dev.instrutorbrasil.dto.response.MessageResponse;
import kaiquebt.dev.instrutorbrasil.dto.response.OnboardingResponse;
import kaiquebt.dev.instrutorbrasil.model.OnboardingDocument;
import kaiquebt.dev.instrutorbrasil.model.User;
import kaiquebt.dev.instrutorbrasil.model.UserOnboarding;
import kaiquebt.dev.instrutorbrasil.model.enums.*;
import kaiquebt.dev.instrutorbrasil.repository.OnboardingDocumentRepository;
import kaiquebt.dev.instrutorbrasil.repository.UserOnboardingRepository;
import kaiquebt.dev.instrutorbrasil.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OnboardingService {

	private final UserOnboardingRepository onboardingRepository;
	private final OnboardingDocumentRepository documentRepository;
	private final UserRepository userRepository;

	@Value("${app.onboarding.retry-cooldown-days:30}")
	private int retryCooldownDays;

	@Transactional
	public OnboardingResponse startOnboarding(User user, OnboardingRequest request) {
		validateCanStartOnboarding(user);

		// Convert list of VehicleType enums to comma-separated string
		String expertiseAreasStr = request.getExpertiseAreas().stream()
				.map(Enum::name)
				.collect(Collectors.joining(","));

		UserOnboarding onboarding = UserOnboarding.builder()
				.user(user)
				.status(OnboardingStatus.PENDING)
				.fullName(request.getFullName())
				.birthDate(request.getBirthDate())
				.phone(request.getPhone())
				.addressStreet(request.getAddressStreet())
				.addressNumber(request.getAddressNumber())
				.addressComplement(request.getAddressComplement())
				.addressNeighborhood(request.getAddressNeighborhood())
				.addressCity(request.getAddressCity())
				.addressState(request.getAddressState())
				.addressZipCode(request.getAddressZipCode())
				.expertiseAreas(expertiseAreasStr)
				.yearsOfExperience(request.getYearsOfExperience())
				.bio(request.getBio())
				.build();

		onboarding = onboardingRepository.save(onboarding);
		return mapToResponse(onboarding);
	}

	@Transactional
	public MessageResponse confirmUpload(User user, Long documentId) {
		OnboardingDocument document = documentRepository.findById(documentId)
				.orElseThrow(() -> new IllegalArgumentException("Document not found"));

		// Verify document belongs to user's onboarding
		if (!document.getOnboarding().getUser().getId().equals(user.getId())) {
			throw new IllegalStateException("Document does not belong to user");
		}

		// Verify document is in PENDING_UPLOAD status
		if (document.getStatus() != DocumentStatus.PENDING_UPLOAD) {
			throw new IllegalStateException("Document is not in PENDING_UPLOAD status");
		}

		// TODO: When S3 integration is implemented, query S3 to get file metadata
		// For now, return mock data
		S3FileMetadata s3Metadata = queryS3FileMetadata(document.getS3Bucket(), document.getS3Key());

		// Update document with S3 metadata
		document.setFileSize(s3Metadata.fileSize);
		document.setMimeType(s3Metadata.mimeType);
		document.setUploadedAt(Instant.now());
		document.setStatus(DocumentStatus.UPLOADED);

		documentRepository.save(document);

		return new MessageResponse("Upload confirmed successfully");
	}

	private S3FileMetadata queryS3FileMetadata(String bucket, String s3Key) {
		// TODO: Replace with actual S3 SDK call when integration is implemented
		// Example: s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(s3Key).build())

		// Mock data for now
		return new S3FileMetadata(
				1048576L, // 1MB mock size
				"image/jpeg" // mock MIME type
		);
	}

	@Transactional
	public DocumentUploadResponse addDocument(User user, DocumentRequest request) {
		UserOnboarding onboarding = getActiveOnboarding(user);

		// Validate document can be added
		validateDocumentAddition(onboarding, request.getPurpose(), request.getSide());

		// Generate S3 key
		String fileExtension = getFileExtension(request.getOriginalFilename());
		String s3Key = String.format("onboarding/%d/%s/%s/%s%s",
				user.getId(),
				request.getPurpose().name().toLowerCase(),
				request.getSide().name().toLowerCase(),
				UUID.randomUUID(),
				fileExtension);

		// TODO: Get bucket from configuration when S3 integration is implemented
		String s3Bucket = "instrutorbrasil-documents";

		// Create document with PENDING_UPLOAD status
		OnboardingDocument document = OnboardingDocument.builder()
				.onboarding(onboarding)
				.purpose(request.getPurpose())
				.side(request.getSide())
				.s3Key(s3Key)
				.s3Bucket(s3Bucket)
				.originalFilename(request.getOriginalFilename())
				.status(DocumentStatus.PENDING_UPLOAD)
				.build();

		document = documentRepository.save(document);
		onboarding.getDocuments().add(document);

		// Generate presigned URL (mocked for now)
		String presignedUrl = String.format("https://%s.s3.amazonaws.com/%s?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=MOCK",
				s3Bucket, s3Key);

		return DocumentUploadResponse.builder()
				.documentId(document.getId())
				.uploadUrl(presignedUrl)
				.build();
	}

	@Transactional
	public void removeDocument(User user, Long documentId) {
		UserOnboarding onboarding = getActiveOnboarding(user);

		OnboardingDocument document = documentRepository.findById(documentId)
				.orElseThrow(() -> new IllegalArgumentException("Document not found"));

		if (!document.getOnboarding().getId().equals(onboarding.getId())) {
			throw new IllegalStateException("Document does not belong to user's onboarding");
		}

		documentRepository.delete(document);
		onboarding.getDocuments().remove(document);
	}

	@Transactional
	public MessageResponse submitOnboarding(User user) {
		UserOnboarding onboarding = getActiveOnboarding(user);

		if (onboarding.getStatus() != OnboardingStatus.PENDING) {
			throw new IllegalStateException("Onboarding already submitted");
		}

		// Validate that all required documents are present
		validateRequiredDocuments(onboarding);

		onboarding.setStatus(OnboardingStatus.IN_REVIEW);
		onboarding.setSubmittedAt(Instant.now());
		onboardingRepository.save(onboarding);

		return new MessageResponse("Onboarding submitted successfully for review");
	}

	@Transactional
	public OnboardingResponse reviewOnboarding(Long onboardingId, User reviewer, ReviewOnboardingRequest request) {
		UserOnboarding onboarding = onboardingRepository.findById(onboardingId)
				.orElseThrow(() -> new IllegalArgumentException("Onboarding not found"));

		if (onboarding.getStatus() != OnboardingStatus.IN_REVIEW) {
			throw new IllegalStateException("Onboarding is not in review state");
		}

		onboarding.setReviewer(reviewer);
		onboarding.setReviewedAt(Instant.now());

		if (request.getApproved()) {
			onboarding.setStatus(OnboardingStatus.APPROVED);

			// Add INSTRUCTOR role to user
			User user = onboarding.getUser();
			user.getRoles().add(Role.INSTRUCTOR);
			userRepository.save(user);
		} else {
			if (request.getRejectionType() == RejectionType.PERMANENT) {
				onboarding.setStatus(OnboardingStatus.PERMANENTLY_REJECTED);
			} else {
				onboarding.setStatus(OnboardingStatus.REJECTED);
				onboarding.setCanRetryAfter(Instant.now().plus(retryCooldownDays, ChronoUnit.DAYS));
			}
			onboarding.setRejectionReason(request.getRejectionReason());
			onboarding.setRejectionType(request.getRejectionType());
		}

		onboarding = onboardingRepository.save(onboarding);
		return mapToResponse(onboarding);
	}

	@Transactional(readOnly = true)
	public OnboardingResponse getOnboarding(User user) {
		UserOnboarding onboarding = onboardingRepository
				.findFirstByUserOrderByCreatedAtDesc(user)
				.orElseThrow(() -> new IllegalArgumentException("No onboarding found for user"));

		return mapToResponse(onboarding);
	}

	@Transactional(readOnly = true)
	public List<OnboardingResponse> getAllOnboardings() {
		return onboardingRepository.findAllByOrderByCreatedAtDesc()
				.stream()
				.map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<OnboardingResponse> getOnboardingsByStatus(OnboardingStatus status) {
		return onboardingRepository.findByStatusOrderByCreatedAtDesc(status)
				.stream()
				.map(this::mapToResponse)
				.collect(Collectors.toList());
	}

	private void validateCanStartOnboarding(User user) {
		List<OnboardingStatus> activeStatuses = Arrays.asList(
				OnboardingStatus.PENDING,
				OnboardingStatus.IN_REVIEW
		);

		onboardingRepository.findFirstByUserAndStatusInOrderByCreatedAtDesc(user, activeStatuses)
				.ifPresent(onboarding -> {
					throw new IllegalStateException("You already have an onboarding in progress");
				});

		UserOnboarding lastOnboarding = onboardingRepository
				.findFirstByUserOrderByCreatedAtDesc(user)
				.orElse(null);

		if (lastOnboarding != null) {
			if (lastOnboarding.getStatus() == OnboardingStatus.PERMANENTLY_REJECTED) {
				throw new IllegalStateException("Your onboarding was permanently rejected. You cannot retry.");
			}

			if (lastOnboarding.getStatus() == OnboardingStatus.REJECTED &&
					lastOnboarding.getCanRetryAfter() != null &&
					Instant.now().isBefore(lastOnboarding.getCanRetryAfter())) {
				throw new IllegalStateException(
						"You must wait until " + lastOnboarding.getCanRetryAfter() + " before retrying"
				);
			}
		}
	}

	private void validateDocumentAddition(UserOnboarding onboarding, DocumentPurpose purpose, DocumentSide side) {
		if (onboarding.getStatus() != OnboardingStatus.PENDING) {
			throw new IllegalStateException("Cannot add documents to submitted onboarding");
		}

		// Check if there's already a document with this purpose and side
		documentRepository.findByOnboardingAndPurposeAndSide(
				onboarding, purpose, side
		).ifPresent(doc -> {
			throw new IllegalStateException(
					"A document with this purpose and side already exists. Please remove it first."
			);
		});

		// Check for conflicting document types (e.g., SINGLE vs FRONT/BACK)
		List<OnboardingDocument> existingDocs = documentRepository.findByOnboardingAndPurpose(
				onboarding, purpose
		);

		if (!existingDocs.isEmpty()) {
			OnboardingDocument firstDoc = existingDocs.get(0);

			// If existing doc is SINGLE, cannot add any other doc with same purpose
			if (firstDoc.getSide() == DocumentSide.SINGLE) {
				throw new IllegalStateException(
						"A SINGLE document already exists for this purpose. Cannot add FRONT or BACK."
				);
			}

			// If trying to add SINGLE but FRONT or BACK exists
			if (side == DocumentSide.SINGLE) {
				throw new IllegalStateException(
						"Cannot add SINGLE document when FRONT or BACK already exists."
				);
			}
		}
	}

	private void validateRequiredDocuments(UserOnboarding onboarding) {
		List<OnboardingDocument> documents = onboarding.getDocuments();

		boolean hasIdentification = documents.stream()
				.anyMatch(doc -> doc.getPurpose() == DocumentPurpose.IDENTIFICATION);

		boolean hasInstructorLicense = documents.stream()
				.anyMatch(doc -> doc.getPurpose() == DocumentPurpose.INSTRUCTOR_LICENSE);

		boolean hasProofOfResidency = documents.stream()
				.anyMatch(doc -> doc.getPurpose() == DocumentPurpose.PROOF_OF_RESIDENCY);

		if (!hasIdentification) {
			throw new IllegalStateException("Identification document is required");
		}
		if (!hasInstructorLicense) {
			throw new IllegalStateException("Instructor license document is required");
		}
		if (!hasProofOfResidency) {
			throw new IllegalStateException("Proof of residency document is required");
		}
	}

	private UserOnboarding getActiveOnboarding(User user) {
		List<OnboardingStatus> activeStatuses = Arrays.asList(
				OnboardingStatus.PENDING,
				OnboardingStatus.IN_REVIEW
		);

		return onboardingRepository.findFirstByUserAndStatusInOrderByCreatedAtDesc(user, activeStatuses)
				.orElseThrow(() -> new IllegalStateException("No active onboarding found"));
	}

	private OnboardingResponse mapToResponse(UserOnboarding onboarding) {
		List<DocumentResponse> documentResponses = onboarding.getDocuments().stream()
				.map(doc -> DocumentResponse.builder()
						.id(doc.getId())
						.purpose(doc.getPurpose())
						.side(doc.getSide())
						.s3Key(doc.getS3Key())
						.s3Bucket(doc.getS3Bucket())
						.originalFilename(doc.getOriginalFilename())
						.fileSize(doc.getFileSize())
						.mimeType(doc.getMimeType())
						.uploadedAt(doc.getUploadedAt())
						.status(doc.getStatus())
						.build())
				.collect(Collectors.toList());

		return OnboardingResponse.builder()
				.id(onboarding.getId())
				.userId(onboarding.getUser().getId())
				.status(onboarding.getStatus())
				.createdAt(onboarding.getCreatedAt())
				.updatedAt(onboarding.getUpdatedAt())
				.submittedAt(onboarding.getSubmittedAt())
				.reviewedAt(onboarding.getReviewedAt())
				.reviewerId(onboarding.getReviewer() != null ? onboarding.getReviewer().getId() : null)
				.rejectionReason(onboarding.getRejectionReason())
				.rejectionType(onboarding.getRejectionType())
				.canRetryAfter(onboarding.getCanRetryAfter())
				.fullName(onboarding.getFullName())
				.birthDate(onboarding.getBirthDate())
				.phone(onboarding.getPhone())
				.addressStreet(onboarding.getAddressStreet())
				.addressNumber(onboarding.getAddressNumber())
				.addressComplement(onboarding.getAddressComplement())
				.addressNeighborhood(onboarding.getAddressNeighborhood())
				.addressCity(onboarding.getAddressCity())
				.addressState(onboarding.getAddressState())
				.addressZipCode(onboarding.getAddressZipCode())
				.expertiseAreas(onboarding.getExpertiseAreas())
				.yearsOfExperience(onboarding.getYearsOfExperience())
				.bio(onboarding.getBio())
				.documents(documentResponses)
				.build();
	}

	private String getFileExtension(String filename) {
		if (filename == null || !filename.contains(".")) {
			return "";
		}
		return filename.substring(filename.lastIndexOf("."));
	}

	private record S3FileMetadata(Long fileSize, String mimeType) {}
}
