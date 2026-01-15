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
public class DocumentDownloadUrlResponse {

	private Long documentId;
	private DocumentPurpose purpose;
	private DocumentSide side;
	private String originalFilename;
	private String mimeType;
	private DocumentStatus status;
	private Instant uploadedAt;
	private String downloadUrl;
}
