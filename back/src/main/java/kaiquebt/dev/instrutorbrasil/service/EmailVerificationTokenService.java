package kaiquebt.dev.instrutorbrasil.service;

import kaiquebt.dev.instrutorbrasil.exception.InvalidTokenException;
import kaiquebt.dev.instrutorbrasil.exception.TokenExpiredException;
import kaiquebt.dev.instrutorbrasil.exception.TokenNotFoundException;
import kaiquebt.dev.instrutorbrasil.model.EmailVerificationToken;
import kaiquebt.dev.instrutorbrasil.model.User;
import kaiquebt.dev.instrutorbrasil.repository.EmailVerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationTokenService {

	@Value("${app.token.email-verification-expiration:86400000}")
	private Long emailVerificationTokenExpiration;

	private final EmailVerificationTokenRepository emailVerificationTokenRepository;

	@Transactional
	public EmailVerificationToken createEmailVerificationToken(User user) {
		// Invalidar tokens anteriores do mesmo usuário
		emailVerificationTokenRepository.deleteByUser(user);

		EmailVerificationToken token = EmailVerificationToken.builder()
				.user(user)
				.token(UUID.randomUUID().toString())
				.expiryDate(Instant.now().plusMillis(emailVerificationTokenExpiration))
				.used(false)
				.build();

		return emailVerificationTokenRepository.save(token);
	}

	public EmailVerificationToken findByToken(String token) {
		return emailVerificationTokenRepository.findByToken(token)
				.orElseThrow(() -> new TokenNotFoundException("Email verification token not found"));
	}

	public EmailVerificationToken verifyToken(EmailVerificationToken token) {
		if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
			emailVerificationTokenRepository.delete(token);
			throw new TokenExpiredException("Email verification token has expired");
		}
		if (token.getUsed()) {
			throw new InvalidTokenException("Email verification token has already been used");
		}
		return token;
	}

	@Transactional
	public void markTokenAsUsed(EmailVerificationToken token) {
		token.setUsed(true);
		emailVerificationTokenRepository.save(token);
	}

	@Transactional
	public void deleteByUser(User user) {
		emailVerificationTokenRepository.deleteByUser(user);
	}
}
