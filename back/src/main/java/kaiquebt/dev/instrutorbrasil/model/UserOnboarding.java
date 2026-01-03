package kaiquebt.dev.instrutorbrasil.model;

import jakarta.persistence.*;
import kaiquebt.dev.instrutorbrasil.model.enums.OnboardingStatus;
import kaiquebt.dev.instrutorbrasil.model.enums.RejectionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_onboarding")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserOnboarding {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OnboardingStatus status;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at")
	private Instant updatedAt;

	@Column(name = "submitted_at")
	private Instant submittedAt;

	@Column(name = "reviewed_at")
	private Instant reviewedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reviewer_id")
	private User reviewer;

	@Column(name = "rejection_reason", columnDefinition = "TEXT")
	private String rejectionReason;

	@Enumerated(EnumType.STRING)
	@Column(name = "rejection_type")
	private RejectionType rejectionType;

	@Column(name = "can_retry_after")
	private Instant canRetryAfter;

	// Personal Information
	@Column(name = "full_name")
	private String fullName;

	@Column(name = "birth_date")
	private Instant birthDate;

	@Column(name = "phone")
	private String phone;

	// Address Information
	@Column(name = "address_street")
	private String addressStreet;

	@Column(name = "address_number")
	private String addressNumber;

	@Column(name = "address_complement")
	private String addressComplement;

	@Column(name = "address_neighborhood")
	private String addressNeighborhood;

	@Column(name = "address_city")
	private String addressCity;

	@Column(name = "address_state")
	private String addressState;

	@Column(name = "address_zip_code")
	private String addressZipCode;

	// Professional Information
	@Column(name = "expertise_areas", columnDefinition = "TEXT")
	private String expertiseAreas;

	@Column(name = "years_of_experience")
	private Integer yearsOfExperience;

	@Column(name = "bio", columnDefinition = "TEXT")
	private String bio;

	@OneToMany(mappedBy = "onboarding", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<OnboardingDocument> documents = new ArrayList<>();

	@PrePersist
	protected void onCreate() {
		createdAt = Instant.now();
		updatedAt = Instant.now();
		if (status == null) {
			status = OnboardingStatus.PENDING;
		}
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = Instant.now();
	}
}
