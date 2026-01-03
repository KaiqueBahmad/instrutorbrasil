package kaiquebt.dev.instrutorbrasil.dto.response;

import kaiquebt.dev.instrutorbrasil.model.enums.DocumentPurpose;
import kaiquebt.dev.instrutorbrasil.model.enums.DocumentSide;
import kaiquebt.dev.instrutorbrasil.model.enums.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentResponse {

	private Long id;
	private DocumentPurpose purpose;
	private DocumentSide side;
	private String s3Key;
	private String s3Bucket;
	private String originalFilename;
	private Long fileSize;
	private String mimeType;
	private Instant uploadedAt;
	private DocumentStatus status;
}
