package kaiquebt.dev.instrutorbrasil.repository;

import kaiquebt.dev.instrutorbrasil.model.OnboardingDocument;
import kaiquebt.dev.instrutorbrasil.model.UserOnboarding;
import kaiquebt.dev.instrutorbrasil.model.enums.DocumentPurpose;
import kaiquebt.dev.instrutorbrasil.model.enums.DocumentSide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OnboardingDocumentRepository extends JpaRepository<OnboardingDocument, Long> {

	List<OnboardingDocument> findByOnboarding(UserOnboarding onboarding);

	Optional<OnboardingDocument> findByOnboardingAndPurposeAndSide(UserOnboarding onboarding, DocumentPurpose purpose, DocumentSide side);

	List<OnboardingDocument> findByOnboardingAndPurpose(UserOnboarding onboarding, DocumentPurpose purpose);
}
