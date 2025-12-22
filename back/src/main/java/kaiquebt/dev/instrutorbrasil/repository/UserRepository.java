package kaiquebt.dev.instrutorbrasil.repository;

import kaiquebt.dev.instrutorbrasil.model.User;
import kaiquebt.dev.instrutorbrasil.model.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	Boolean existsByEmail(String email);

	Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);
}
