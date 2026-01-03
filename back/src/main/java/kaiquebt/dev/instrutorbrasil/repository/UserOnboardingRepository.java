package kaiquebt.dev.instrutorbrasil.repository;

import kaiquebt.dev.instrutorbrasil.model.User;
import kaiquebt.dev.instrutorbrasil.model.UserOnboarding;
import kaiquebt.dev.instrutorbrasil.model.enums.OnboardingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserOnboardingRepository extends JpaRepository<UserOnboarding, Long> {

	Optional<UserOnboarding> findFirstByUserOrderByCreatedAtDesc(User user);

	Optional<UserOnboarding> findFirstByUserAndStatusInOrderByCreatedAtDesc(User user, List<OnboardingStatus> statuses);

	List<UserOnboarding> findByStatusOrderByCreatedAtDesc(OnboardingStatus status);

	List<UserOnboarding> findAllByOrderByCreatedAtDesc();
}
