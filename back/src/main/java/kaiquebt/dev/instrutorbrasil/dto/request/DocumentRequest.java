package kaiquebt.dev.instrutorbrasil.dto.request;

import jakarta.validation.constraints.NotNull;
import kaiquebt.dev.instrutorbrasil.model.enums.DocumentPurpose;
import kaiquebt.dev.instrutorbrasil.model.enums.DocumentSide;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRequest {

	@NotNull(message = "Document purpose is required")
	private DocumentPurpose purpose;

	@NotNull(message = "Document side is required")
	private DocumentSide side;

	private String s3Key;
	private String s3Bucket;
	private String originalFilename;
	private Long fileSize;
	private String mimeType;
}
