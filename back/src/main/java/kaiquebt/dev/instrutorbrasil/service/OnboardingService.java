package kaiquebt.dev.instrutorbrasil.service;

import kaiquebt.dev.instrutorbrasil.dto.request.ConfirmUploadRequest;
import kaiquebt.dev.instrutorbrasil.dto.request.DocumentRequest;
import kaiquebt.dev.instrutorbrasil.dto.request.OnboardingRequest;
import kaiquebt.dev.instrutorbrasil.dto.request.ReviewOnboardingRequest;
import kaiquebt.dev.instrutorbrasil.dto.request.DocumentReviewRequest;
import kaiquebt.dev.instrutorbrasil.dto.response.DocumentDownloadUrlResponse;
import kaiquebt.dev.instrutorbrasil.dto.response.DocumentResponse;
import kaiquebt.dev.instrutorbrasil.dto.response.DocumentUploadResponse;
import kaiquebt.dev.instrutorbrasil.dto.response.MessageResponse;
import kaiquebt.dev.instrutorbrasil.dto.response.OnboardingResponse;
import kaiquebt.dev.instrutorbrasil.dto.response.OnboardingSummaryResponse;
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
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OnboardingService {

	private final UserOnboardingRepository onboardingRepository;
	private final OnboardingDocumentRepository documentRepository;
	private final UserRepository userRepository;
	private final S3Service s3Service;

	// @Value("${app.onboarding.retry-cooldown-days:30}")
	// private int retryCooldownDays;

	@Value("${app.aws.s3.bucket}")
	private String s3Bucket;

	@Transactional
	public OnboardingResponse startOnboarding(User user, OnboardingRequest request) {
		validateCanStartOnboarding(user);

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
	public MessageResponse confirmUpload(User user, Long documentId, ConfirmUploadRequest request) {
		OnboardingDocument document = documentRepository.findById(documentId)
				.orElseThrow(() -> new IllegalArgumentException("Document not found"));

		if (!document.getOnboarding().getUser().getId().equals(user.getId())) {
			throw new IllegalStateException("Document does not belong to user");
		}

		if (document.getStatus() != DocumentStatus.PENDING_UPLOAD) {
			throw new IllegalStateException("Document is not in PENDING_UPLOAD status");
		}

		// Query S3 to verify file exists and get size
		S3Service.S3FileMetadata s3Metadata = s3Service.getFileMetadata(document.getS3Key());

		document.setOriginalFilename(request.getOriginalFilename());
		document.setFileSize(s3Metadata.fileSize());
		document.setMimeType(request.getMimeType());
		document.setUploadedAt(Instant.now());
		document.setStatus(DocumentStatus.UPLOADED);

		documentRepository.save(document);

		return new MessageResponse("Upload confirmed successfully");
	}

	@Transactional
	public DocumentUploadResponse addDocument(User user, DocumentRequest request) {
		UserOnboarding onboarding = getActiveOnboarding(user);

		validateDocumentAddition(onboarding, request.getPurpose(), request.getSide());

		String s3Key = String.format("onboarding/%d/%d/%s/%s/%s",
			user.getId(),
			onboarding.getId(),
			request.getPurpose().name().toLowerCase(),
			request.getSide().name().toLowerCase(),
			UUID.randomUUID()
		);

		OnboardingDocument document = OnboardingDocument.builder()
			.onboarding(onboarding)
			.purpose(request.getPurpose())
			.side(request.getSide())
			.s3Key(s3Key)
			.s3Bucket(s3Bucket)
			.status(DocumentStatus.PENDING_UPLOAD)
		.build();

		document = documentRepository.save(document);
		onboarding.getDocuments().add(document);

		S3Service.PresignedPostData presignedPost = s3Service.generatePresignedPost(s3Key);

		return DocumentUploadResponse.builder()
				.documentId(document.getId())
				.uploadUrl(presignedPost.url())
				.formFields(presignedPost.formFields())
				.build();
	}

	@Transactional(readOnly = true)
	public List<DocumentDownloadUrlResponse> getOnboardingDocumentsWithUrls(Long onboardingId) {
		UserOnboarding onboarding = onboardingRepository.findById(onboardingId)
				.orElseThrow(() -> new IllegalArgumentException("Onboarding not found"));

		return onboarding.getDocuments().stream()
				.filter(doc -> doc.getStatus() == DocumentStatus.UPLOADED || doc.getStatus() == DocumentStatus.VERIFIED)
				.map(doc -> {
					// Generate presigned GET URL with correct Content-Type
					String downloadUrl = doc.getStatus() != DocumentStatus.PENDING_UPLOAD ? s3Service.generatePresignedGetUrl(doc.getS3Key(), doc.getMimeType()) : null;

					return DocumentDownloadUrlResponse.builder()
							.documentId(doc.getId())
							.purpose(doc.getPurpose())
							.side(doc.getSide())
							.originalFilename(doc.getOriginalFilename())
							.mimeType(doc.getMimeType())
							.status(doc.getStatus())
							.uploadedAt(doc.getUploadedAt())
							.downloadUrl(downloadUrl)
							.build();
				})
				.collect(Collectors.toList());
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

			User user = onboarding.getUser();
			user.getRoles().add(Role.INSTRUCTOR);
			userRepository.save(user);
		} else {
			if (request.getRejectionType() == RejectionType.PERMANENT) {
				onboarding.setStatus(OnboardingStatus.PERMANENTLY_REJECTED);
			} else {
				onboarding.setStatus(OnboardingStatus.REJECTED);
				onboarding.setCanRetryAfter(Instant.now());
				// onboarding.setCanRetryAfter(Instant.now().plus(retryCooldownDays, ChronoUnit.DAYS));
			}
			onboarding.setRejectionReason(request.getRejectionReason());
			onboarding.setRejectionType(request.getRejectionType());
		}

		onboarding = onboardingRepository.save(onboarding);
		return mapToResponse(onboarding);
	}

	@Transactional
	public void reviewDocument(Long documentId, User reviewer, DocumentReviewRequest request) {
		OnboardingDocument document = documentRepository.findById(documentId)
				.orElseThrow(() -> new IllegalArgumentException("Document not found"));

		if (document.getStatus() == DocumentStatus.PENDING_UPLOAD) {
			throw new IllegalStateException("Cannot judge a document that is still pending upload");
		}

		UserOnboarding onboarding = document.getOnboarding();
		if (onboarding.getStatus() != OnboardingStatus.IN_REVIEW) {
			throw new IllegalStateException("Cannot judge a document when onboarding is not under review");
		}

		if (request.getApproved()) {
			document.setStatus(DocumentStatus.VERIFIED);
		} else {
			document.setStatus(DocumentStatus.REJECTED);
		}

		documentRepository.save(document);
	}

	@Transactional(readOnly = true)
	public OnboardingResponse getOnboarding(User user) {
		UserOnboarding onboarding = onboardingRepository
				.findFirstByUserOrderByCreatedAtDesc(user)
				.orElseThrow(() -> new IllegalArgumentException("No onboarding found for user"));

		return mapToResponse(onboarding);
	}

	@Transactional(readOnly = true)
	public List<OnboardingSummaryResponse> getPendingOnboardingsSummary() {
		return onboardingRepository.findByStatusOrderByCreatedAtDesc(OnboardingStatus.IN_REVIEW)
				.stream()
				.map(this::mapToSummaryResponse)
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public OnboardingResponse getOnboardingById(Long onboardingId) {
		UserOnboarding onboarding = onboardingRepository.findById(onboardingId)
				.orElseThrow(() -> new IllegalArgumentException("Onboarding not found"));
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
				OnboardingStatus.IN_REVIEW);

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
						"You must wait until " + lastOnboarding.getCanRetryAfter() + " before retrying");
			}
		}
	}

	private void validateDocumentAddition(UserOnboarding onboarding, DocumentPurpose purpose, DocumentSide side) {
		if (onboarding.getStatus() != OnboardingStatus.PENDING) {
			throw new IllegalStateException("Cannot add documents to submitted onboarding");
		}

		documentRepository.findByOnboardingAndPurposeAndSide(
				onboarding, purpose, side).ifPresent(doc -> {
					throw new IllegalStateException(
							"A document with this purpose and side already exists. Please remove it first.");
				});

		List<OnboardingDocument> existingDocs = documentRepository.findByOnboardingAndPurpose(
				onboarding, purpose);

		if (!existingDocs.isEmpty()) {
			OnboardingDocument firstDoc = existingDocs.get(0);

			if (firstDoc.getSide() == DocumentSide.SINGLE) {
				throw new IllegalStateException(
						"A SINGLE document already exists for this purpose. Cannot add FRONT or BACK.");
			}

			if (side == DocumentSide.SINGLE) {
				throw new IllegalStateException(
						"Cannot add SINGLE document when FRONT or BACK already exists.");
			}
		}
	}

	private void validateRequiredDocuments(UserOnboarding onboarding) {
		List<OnboardingDocument> documents = onboarding.getDocuments();

		boolean hasIdentification = hasCompleteDocument(documents, DocumentPurpose.IDENTIFICATION);
		boolean hasInstructorLicense = hasCompleteDocument(documents, DocumentPurpose.INSTRUCTOR_LICENSE);
		boolean hasProofOfResidency = hasCompleteDocument(documents, DocumentPurpose.PROOF_OF_RESIDENCY);

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

	private boolean hasCompleteDocument(List<OnboardingDocument> documents, DocumentPurpose purpose) {
		EnumSet<DocumentSide> foundSides = documents.stream()
				.filter(doc -> doc.getPurpose() == purpose)
				.map(OnboardingDocument::getSide)
				.collect(Collectors.toCollection(() -> EnumSet.noneOf(DocumentSide.class)));

		return foundSides.contains(DocumentSide.SINGLE)
				|| (foundSides.contains(DocumentSide.FRONT) && foundSides.contains(DocumentSide.BACK));
	}

	private UserOnboarding getActiveOnboarding(User user) {
		List<OnboardingStatus> activeStatuses = Arrays.asList(
				OnboardingStatus.PENDING,
				OnboardingStatus.IN_REVIEW);

		return onboardingRepository.findFirstByUserAndStatusInOrderByCreatedAtDesc(user, activeStatuses)
				.orElseThrow(() -> new IllegalStateException("No active onboarding found"));
	}

	private OnboardingSummaryResponse mapToSummaryResponse(UserOnboarding onboarding) {
		return OnboardingSummaryResponse.builder()
				.id(onboarding.getId())
				.userId(onboarding.getUser().getId())
				.userFullName(onboarding.getFullName())
				.userEmail(onboarding.getUser().getEmail())
				.status(onboarding.getStatus())
				.submittedAt(onboarding.getSubmittedAt())
				.createdAt(onboarding.getCreatedAt())
				.build();
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

}
