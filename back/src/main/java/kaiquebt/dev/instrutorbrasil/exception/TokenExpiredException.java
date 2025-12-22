package kaiquebt.dev.instrutorbrasil.exception;

public class TokenExpiredException extends RuntimeException {
	public TokenExpiredException(String message) {
		super(message);
	}
}
