package kaiquebt.dev.instrutorbrasil.service;

import kaiquebt.dev.instrutorbrasil.repository.PasswordResetTokenRepository;
import kaiquebt.dev.instrutorbrasil.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupService {

	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordResetTokenRepository passwordResetTokenRepository;

	@Scheduled(cron = "0 0 2 * * ?")
	@Transactional
	public void cleanupExpiredTokens() {
		Instant now = Instant.now();

		log.info("Starting cleanup of expired tokens");

		refreshTokenRepository.deleteByExpiryDateBefore(now);
		passwordResetTokenRepository.deleteByExpiryDateBefore(now);

		log.info("Completed cleanup of expired tokens");
	}
}
