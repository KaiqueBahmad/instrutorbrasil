package kaiquebt.dev.instrutorbrasil.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RateLimits.class)
public @interface RateLimit {

	/**
	 * Maximum number of requests allowed within the time window
	 */
	int maxAttempts() default 5;

	/**
	 * Time window in seconds
	 */
	int timeWindowSeconds() default 3600;

	/**
	 * Rate limit key type (IP or EMAIL)
	 */
	KeyType keyType() default KeyType.IP;

	enum KeyType {
		IP,      // Rate limit by IP address
		EMAIL    // Rate limit by email from request body
	}
}
