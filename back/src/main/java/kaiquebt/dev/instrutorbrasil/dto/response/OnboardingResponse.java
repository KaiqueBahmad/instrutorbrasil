package kaiquebt.dev.instrutorbrasil.dto.response;

import kaiquebt.dev.instrutorbrasil.model.enums.OnboardingStatus;
import kaiquebt.dev.instrutorbrasil.model.enums.RejectionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingResponse {

	private Long id;
	private Long userId;
	private OnboardingStatus status;
	private Instant createdAt;
	private Instant updatedAt;
	private Instant submittedAt;
	private Instant reviewedAt;
	private Long reviewerId;
	private String rejectionReason;
	private RejectionType rejectionType;
	private Instant canRetryAfter;

	// Personal Information
	private String fullName;
	private Instant birthDate;
	private String phone;

	// Address Information
	private String addressStreet;
	private String addressNumber;
	private String addressComplement;
	private String addressNeighborhood;
	private String addressCity;
	private String addressState;
	private String addressZipCode;

	// Professional Information
	private String expertiseAreas;
	private Integer yearsOfExperience;
	private String bio;

	private List<DocumentResponse> documents;
}
