package kaiquebt.dev.instrutorbrasil.dto.response;

import kaiquebt.dev.instrutorbrasil.model.enums.OnboardingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingSummaryResponse {

	private Long id;
	private Long userId;
	private String userFullName;
	private String userEmail;
	private OnboardingStatus status;
	private Instant submittedAt;
	private Instant createdAt;
}
