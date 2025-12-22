package kaiquebt.dev.instrutorbrasil.exception;

import jakarta.servlet.http.HttpServletRequest;
import kaiquebt.dev.instrutorbrasil.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(UserAlreadyExistsException.class)
	public ResponseEntity<ErrorResponse> handleUserAlreadyExists(
			UserAlreadyExistsException ex,
			HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.CONFLICT.value(),
				"Conflict",
				ex.getMessage(),
				request.getRequestURI()
		);
		return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleInvalidCredentials(
			InvalidCredentialsException ex,
			HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.UNAUTHORIZED.value(),
				"Unauthorized",
				ex.getMessage(),
				request.getRequestURI()
		);
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
	}

	@ExceptionHandler(TokenExpiredException.class)
	public ResponseEntity<ErrorResponse> handleTokenExpired(
			TokenExpiredException ex,
			HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.UNAUTHORIZED.value(),
				"Unauthorized",
				ex.getMessage(),
				request.getRequestURI()
		);
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
	}

	@ExceptionHandler(TokenNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleTokenNotFound(
			TokenNotFoundException ex,
			HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.NOT_FOUND.value(),
				"Not Found",
				ex.getMessage(),
				request.getRequestURI()
		);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	@ExceptionHandler(InvalidTokenException.class)
	public ResponseEntity<ErrorResponse> handleInvalidToken(
			InvalidTokenException ex,
			HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.UNAUTHORIZED.value(),
				"Unauthorized",
				ex.getMessage(),
				request.getRequestURI()
		);
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleUserNotFound(
			UserNotFoundException ex,
			HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.NOT_FOUND.value(),
				"Not Found",
				ex.getMessage(),
				request.getRequestURI()
		);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationErrors(
			MethodArgumentNotValidException ex,
			HttpServletRequest request) {
		List<String> errors = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(FieldError::getDefaultMessage)
				.collect(Collectors.toList());

		ErrorResponse error = ErrorResponse.builder()
				.timestamp(java.time.Instant.now())
				.status(HttpStatus.BAD_REQUEST.value())
				.error("Bad Request")
				.message("Validation failed")
				.path(request.getRequestURI())
				.errors(errors)
				.build();

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGenericException(
			Exception ex,
			HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"Internal Server Error",
				ex.getMessage(),
				request.getRequestURI()
		);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	}
}
