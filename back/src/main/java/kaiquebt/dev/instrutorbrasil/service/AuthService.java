package kaiquebt.dev.instrutorbrasil.service;

import kaiquebt.dev.instrutorbrasil.dto.request.*;
import kaiquebt.dev.instrutorbrasil.dto.response.AuthResponse;
import kaiquebt.dev.instrutorbrasil.dto.response.MessageResponse;
import kaiquebt.dev.instrutorbrasil.dto.response.UserResponse;
import kaiquebt.dev.instrutorbrasil.exception.InvalidCredentialsException;
import kaiquebt.dev.instrutorbrasil.exception.TokenExpiredException;
import kaiquebt.dev.instrutorbrasil.exception.TokenNotFoundException;
import kaiquebt.dev.instrutorbrasil.exception.UserAlreadyExistsException;
import kaiquebt.dev.instrutorbrasil.model.PasswordResetToken;
import kaiquebt.dev.instrutorbrasil.model.RefreshToken;
import kaiquebt.dev.instrutorbrasil.model.User;
import kaiquebt.dev.instrutorbrasil.model.enums.AuthProvider;
import kaiquebt.dev.instrutorbrasil.model.enums.Role;
import kaiquebt.dev.instrutorbrasil.repository.PasswordResetTokenRepository;
import kaiquebt.dev.instrutorbrasil.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserService userService;
	private final JwtService jwtService;
	private final RefreshTokenService refreshTokenService;
	private final EmailService emailService;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final PasswordResetTokenRepository passwordResetTokenRepository;

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		if (userService.existsByEmail(request.getEmail())) {
			throw new UserAlreadyExistsException("Email is already registered");
		}

		User user = User.builder()
				.email(request.getEmail())
				.password(passwordEncoder.encode(request.getPassword()))
				.name(request.getName())
				.role(Role.USER)
				.provider(AuthProvider.LOCAL)
				.emailVerified(false)
				.enabled(true)
				.build();

		user = userService.createUser(user);

		String accessToken = jwtService.generateAccessToken(user);
		RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

		emailService.sendWelcomeEmail(user.getEmail(), user.getName());

		UserResponse userResponse = UserResponse.builder()
				.id(user.getId())
				.email(user.getEmail())
				.name(user.getName())
				.role(user.getRole())
				.emailVerified(user.getEmailVerified())
				.build();

		return AuthResponse.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken.getToken())
				.tokenType("Bearer")
				.expiresIn(jwtService.getAccessTokenExpiration() / 1000)
				.user(userResponse)
				.build();
	}

	public AuthResponse login(LoginRequest request) {
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(
							request.getEmail(),
							request.getPassword()
					)
			);
		} catch (AuthenticationException e) {
			throw new InvalidCredentialsException("Invalid email or password");
		}

		User user = userService.findByEmail(request.getEmail());

		String accessToken = jwtService.generateAccessToken(user);
		RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

		UserResponse userResponse = UserResponse.builder()
				.id(user.getId())
				.email(user.getEmail())
				.name(user.getName())
				.role(user.getRole())
				.emailVerified(user.getEmailVerified())
				.build();

		return AuthResponse.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken.getToken())
				.tokenType("Bearer")
				.expiresIn(jwtService.getAccessTokenExpiration() / 1000)
				.user(userResponse)
				.build();
	}

	@Transactional
	public AuthResponse refreshToken(RefreshTokenRequest request) {
		RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken());
		refreshTokenService.verifyExpiration(refreshToken);

		User user = refreshToken.getUser();

		String accessToken = jwtService.generateAccessToken(user);

		refreshTokenService.deleteByToken(request.getRefreshToken());
		RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

		UserResponse userResponse = UserResponse.builder()
				.id(user.getId())
				.email(user.getEmail())
				.name(user.getName())
				.role(user.getRole())
				.emailVerified(user.getEmailVerified())
				.build();

		return AuthResponse.builder()
				.accessToken(accessToken)
				.refreshToken(newRefreshToken.getToken())
				.tokenType("Bearer")
				.expiresIn(jwtService.getAccessTokenExpiration() / 1000)
				.user(userResponse)
				.build();
	}

	@Transactional
	public MessageResponse forgotPassword(ForgotPasswordRequest request) {
		User user = userService.findByEmail(request.getEmail());

		String token = UUID.randomUUID().toString();
		PasswordResetToken resetToken = PasswordResetToken.builder()
				.token(token)
				.user(user)
				.expiryDate(Instant.now().plusSeconds(3600))
				.used(false)
				.build();

		passwordResetTokenRepository.save(resetToken);

		emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), token);

		return new MessageResponse("Password reset email sent successfully");
	}

	@Transactional
	public MessageResponse resetPassword(ResetPasswordRequest request) {
		PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
				.orElseThrow(() -> new TokenNotFoundException("Invalid password reset token"));

		if (resetToken.getUsed()) {
			throw new TokenExpiredException("Password reset token has already been used");
		}

		if (resetToken.getExpiryDate().isBefore(Instant.now())) {
			throw new TokenExpiredException("Password reset token has expired");
		}

		User user = resetToken.getUser();
		user.setPassword(passwordEncoder.encode(request.getNewPassword()));
		userService.updateUser(user);

		resetToken.setUsed(true);
		passwordResetTokenRepository.save(resetToken);

		refreshTokenService.deleteByUser(user);

		emailService.sendPasswordResetConfirmationEmail(user.getEmail(), user.getName());

		return new MessageResponse("Password reset successfully");
	}

	public UserResponse getCurrentUser(String email) {
		User user = userService.findByEmail(email);

		return UserResponse.builder()
				.id(user.getId())
				.email(user.getEmail())
				.name(user.getName())
				.role(user.getRole())
				.emailVerified(user.getEmailVerified())
				.build();
	}
}
