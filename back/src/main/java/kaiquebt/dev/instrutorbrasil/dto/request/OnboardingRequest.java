package kaiquebt.dev.instrutorbrasil.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import kaiquebt.dev.instrutorbrasil.model.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingRequest {

	// Personal Information
	@NotBlank(message = "Full name is required")
	private String fullName;

	@NotNull(message = "Birth date is required")
	@Past(message = "Birth date must be in the past")
	private Instant birthDate;

	@NotBlank(message = "Phone is required")
	private String phone;

	// Address Information
	@NotBlank(message = "Street is required")
	private String addressStreet;

	@NotBlank(message = "Number is required")
	private String addressNumber;

	private String addressComplement;

	@NotBlank(message = "Neighborhood is required")
	private String addressNeighborhood;

	@NotBlank(message = "City is required")
	private String addressCity;

	@NotBlank(message = "State is required")
	private String addressState;

	@NotBlank(message = "ZIP code is required")
	private String addressZipCode;

	// Professional Information
	@NotEmpty(message = "At least one expertise area is required")
	private List<VehicleType> expertiseAreas;

	@NotNull(message = "Years of experience is required")
	@Positive(message = "Years of experience must be positive")
	private Integer yearsOfExperience;

	@NotBlank(message = "Bio is required")
	private String bio;
}
