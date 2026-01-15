package kaiquebt.dev.instrutorbrasil.exception;

import jakarta.servlet.http.HttpServletRequest;
import kaiquebt.dev.instrutorbrasil.dto.response.ErrorResponse;
import kaiquebt.dev.instrutorbrasil.model.enums.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
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
				request.getRequestURI(),
				ErrorCode.EMAIL_ALREADY_EXISTS
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
				request.getRequestURI(),
				ErrorCode.INVALID_CREDENTIALS
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
				request.getRequestURI(),
				ErrorCode.TOKEN_EXPIRED
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
				request.getRequestURI(),
				ErrorCode.RESOURCE_NOT_FOUND
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
				request.getRequestURI(),
				ErrorCode.TOKEN_INVALID
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
				request.getRequestURI(),
				ErrorCode.USER_NOT_FOUND
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
				.errorCode(ErrorCode.VALIDATION_ERROR)
				.errors(errors)
				.build();

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ErrorResponse> handleIllegalState(
			IllegalStateException ex,
			HttpServletRequest request) {
		// Determine specific error code based on message
		ErrorCode errorCode = determineOnboardingErrorCode(ex.getMessage());

		ErrorResponse error = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				"Bad Request",
				ex.getMessage(),
				request.getRequestURI(),
				errorCode
		);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgument(
			IllegalArgumentException ex,
			HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				"Bad Request",
				ex.getMessage(),
				request.getRequestURI(),
				ErrorCode.INVALID_REQUEST
		);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(EmailNotVerifiedException.class)
	public ResponseEntity<ErrorResponse> handleEmailNotVerified(
			EmailNotVerifiedException ex,
			HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.UNAUTHORIZED.value(),
				"Unauthorized",
				ex.getMessage(),
				request.getRequestURI(),
				ErrorCode.EMAIL_NOT_VERIFIED
		);
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
	}

	@ExceptionHandler(InvalidProviderException.class)
	public ResponseEntity<ErrorResponse> handleInvalidProvider(
			InvalidProviderException ex,
			HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				"Bad Request",
				ex.getMessage(),
				request.getRequestURI(),
				ErrorCode.INVALID_PROVIDER
		);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleBadCredentials(
			BadCredentialsException ex,
			HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.UNAUTHORIZED.value(),
				"Unauthorized",
				"Invalid email or password",
				request.getRequestURI(),
				ErrorCode.INVALID_CREDENTIALS
		);
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
	}

	@ExceptionHandler(RateLimitExceededException.class)
	public ResponseEntity<ErrorResponse> handleRateLimitExceeded(
			RateLimitExceededException ex,
			HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.TOO_MANY_REQUESTS.value(),
				"Too Many Requests",
				ex.getMessage(),
				request.getRequestURI(),
				ErrorCode.RATE_LIMIT_EXCEEDED
		);
		return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGenericException(
			Exception ex,
			HttpServletRequest request) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"Internal Server Error",
				ex.getMessage(),
				request.getRequestURI(),
				ErrorCode.INTERNAL_SERVER_ERROR
		);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	}

	private ErrorCode determineOnboardingErrorCode(String message) {
		if (message.contains("already have an onboarding in progress")) {
			return ErrorCode.ONBOARDING_ALREADY_IN_PROGRESS;
		} else if (message.contains("permanently rejected")) {
			return ErrorCode.ONBOARDING_PERMANENTLY_REJECTED;
		} else if (message.contains("must wait until")) {
			return ErrorCode.ONBOARDING_RETRY_COOLDOWN;
		} else if (message.contains("already submitted")) {
			return ErrorCode.ONBOARDING_ALREADY_SUBMITTED;
		} else if (message.contains("not in review")) {
			return ErrorCode.ONBOARDING_NOT_IN_REVIEW;
		} else if (message.contains("Cannot add documents")) {
			return ErrorCode.ONBOARDING_CANNOT_ADD_DOCUMENTS;
		} else if (message.contains("document")) {
			return ErrorCode.DOCUMENT_CONFLICT;
		} else if (message.contains("required")) {
			return ErrorCode.REQUIRED_DOCUMENTS_MISSING;
		}
		return ErrorCode.INVALID_REQUEST;
	}
}
