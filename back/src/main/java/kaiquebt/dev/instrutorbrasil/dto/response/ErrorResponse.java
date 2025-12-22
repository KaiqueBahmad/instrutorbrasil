package kaiquebt.dev.instrutorbrasil.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

	private Instant timestamp;
	private Integer status;
	private String error;
	private String message;
	private String path;
	private List<String> errors;

	public ErrorResponse(Integer status, String error, String message, String path) {
		this.timestamp = Instant.now();
		this.status = status;
		this.error = error;
		this.message = message;
		this.path = path;
	}
}
