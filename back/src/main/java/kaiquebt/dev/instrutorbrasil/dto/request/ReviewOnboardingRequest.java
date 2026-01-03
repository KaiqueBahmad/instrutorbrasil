package kaiquebt.dev.instrutorbrasil.dto.request;

import jakarta.validation.constraints.NotNull;
import kaiquebt.dev.instrutorbrasil.model.enums.RejectionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewOnboardingRequest {

	@NotNull(message = "Approval decision is required")
	private Boolean approved;

	private String rejectionReason;

	private RejectionType rejectionType;
}
