package kaiquebt.dev.instrutorbrasil.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentReviewRequest {

    @NotNull(message = "Approval decision is required")
    private Boolean approved;

    private String rejectionReason;
}