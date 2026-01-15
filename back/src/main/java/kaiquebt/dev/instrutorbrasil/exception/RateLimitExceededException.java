package kaiquebt.dev.instrutorbrasil.exception;

public class RateLimitExceededException extends RuntimeException {
	public RateLimitExceededException(String message) {
		super(message);
	}
}
