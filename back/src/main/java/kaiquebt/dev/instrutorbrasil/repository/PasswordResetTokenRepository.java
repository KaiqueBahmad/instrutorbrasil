package kaiquebt.dev.instrutorbrasil.repository;

import kaiquebt.dev.instrutorbrasil.model.PasswordResetToken;
import kaiquebt.dev.instrutorbrasil.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

	Optional<PasswordResetToken> findByToken(String token);

	void deleteByUser(User user);

	@Modifying
	@Query("DELETE FROM PasswordResetToken prt WHERE prt.expiryDate < :date")
	void deleteByExpiryDateBefore(Instant date);
}
