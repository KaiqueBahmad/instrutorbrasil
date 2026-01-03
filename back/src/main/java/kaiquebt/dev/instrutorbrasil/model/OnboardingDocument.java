package kaiquebt.dev.instrutorbrasil.model;

import jakarta.persistence.*;
import kaiquebt.dev.instrutorbrasil.model.enums.DocumentPurpose;
import kaiquebt.dev.instrutorbrasil.model.enums.DocumentSide;
import kaiquebt.dev.instrutorbrasil.model.enums.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "onboarding_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingDocument {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "onboarding_id", nullable = false)
	private UserOnboarding onboarding;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DocumentPurpose purpose;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DocumentSide side;

	@Column(name = "s3_key")
	private String s3Key;

	@Column(name = "s3_bucket")
	private String s3Bucket;

	@Column(name = "original_filename")
	private String originalFilename;

	@Column(name = "file_size")
	private Long fileSize;

	@Column(name = "mime_type")
	private String mimeType;

	@Column(name = "uploaded_at")
	private Instant uploadedAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DocumentStatus status;

	@PrePersist
	protected void onCreate() {
		if (status == null) {
			status = DocumentStatus.PENDING_UPLOAD;
		}
	}
}
