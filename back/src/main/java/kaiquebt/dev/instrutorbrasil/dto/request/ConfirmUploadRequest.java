package kaiquebt.dev.instrutorbrasil.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmUploadRequest {

	@NotBlank(message = "Original filename is required")
	private String originalFilename;
}
