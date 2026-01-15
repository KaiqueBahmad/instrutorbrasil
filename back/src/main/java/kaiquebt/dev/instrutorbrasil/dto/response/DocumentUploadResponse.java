package kaiquebt.dev.instrutorbrasil.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentUploadResponse {

	private Long documentId;
	private String uploadUrl;
	private Map<String, String> formFields;
}
