package kaiquebt.dev.instrutorbrasil.service;

import kaiquebt.dev.instrutorbrasil.dto.request.*;
import kaiquebt.dev.instrutorbrasil.dto.response.AuthResponse;
import kaiquebt.dev.instrutorbrasil.dto.response.MessageResponse;
import kaiquebt.dev.instrutorbrasil.dto.response.UserResponse;
import kaiquebt.dev.instrutorbrasil.exception.EmailNotVerifiedException;
import kaiquebt.dev.instrutorbrasil.exception.InvalidCredentialsException;
import kaiquebt.dev.instrutorbrasil.exception.InvalidProviderException;
import kaiquebt.dev.instrutorbrasil.exception.UserAlreadyExistsException;
import kaiquebt.dev.instrutorbrasil.model.*;
import kaiquebt.dev.instrutorbrasil.model.enums.AuthProvider;
import kaiquebt.dev.instrutorbrasil.model.enums.Role;
import kaiquebt.dev.instrutorbrasil.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserService userService;
	private final JwtService jwtService;
	private final RefreshTokenService refreshTokenService;
	private final PasswordResetTokenService passwordResetTokenService;
	private final EmailVerificationTokenService emailVerificationTokenService;
	private final EmailService emailService;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;

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
				.roles(user.getRoles())
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

	public UserResponse getCurrentUser(String email) {
		User user = userService.findByEmail(email);

		return UserResponse.builder()
				.id(user.getId())
				.email(user.getEmail())
				.name(user.getName())
				.roles(user.getRoles())
				.emailVerified(user.getEmailVerified())
				.build();
	}

	@Transactional
	public MessageResponse register(RegisterRequest request) {
		// Verificar se email já existe
		if (userService.existsByEmail(request.getEmail())) {
			throw new UserAlreadyExistsException("Email already registered");
		}

		// Criar usuário
		User user = User.builder()
				.email(request.getEmail())
				.password(passwordEncoder.encode(request.getPassword()))
				.name(request.getName())
				.provider(AuthProvider.LOCAL)
				.roles(Set.of(Role.USER))
				.emailVerified(false)
				.enabled(true)
				.build();

		user = userService.createUser(user);

		// Criar token de verificação e enviar email
		EmailVerificationToken verificationToken =
				emailVerificationTokenService.createEmailVerificationToken(user);
		emailService.sendEmailVerificationEmail(
				user.getEmail(),
				user.getName(),
				verificationToken.getToken()
		);

		return new MessageResponse("Registration successful. Please check your email to verify your account.");
	}

	@Transactional
	public AuthResponse login(LoginRequest request) {
		// Autenticar credenciais
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(
							request.getEmail(),
							request.getPassword()
					)
			);
		} catch (BadCredentialsException e) {
			throw new InvalidCredentialsException("Invalid email or password");
		}

		// Buscar usuário
		User user = userService.findByEmail(request.getEmail());

		// Verificar se email foi verificado (apenas para LOCAL)
		if (user.getProvider() == AuthProvider.LOCAL && !user.getEmailVerified()) {
			throw new EmailNotVerifiedException(
					"Please verify your email before logging in"
			);
		}

		// Verificar se conta está habilitada
		if (!user.getEnabled()) {
			throw new InvalidCredentialsException("Account is disabled");
		}

		// Gerar tokens
		String accessToken = jwtService.generateAccessToken(user);
		RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

		// Construir resposta
		UserResponse userResponse = UserResponse.builder()
				.id(user.getId())
				.email(user.getEmail())
				.name(user.getName())
				.roles(user.getRoles())
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
	public MessageResponse verifyEmail(VerifyEmailRequest request) {
		// Buscar token
		EmailVerificationToken verificationToken =
				emailVerificationTokenService.findByToken(request.getToken());

		// Verificar validade
		emailVerificationTokenService.verifyToken(verificationToken);

		// Atualizar usuário
		User user = verificationToken.getUser();
		user.setEmailVerified(true);
		userService.updateUser(user);

		// Marcar token como usado
		emailVerificationTokenService.markTokenAsUsed(verificationToken);

		// Enviar email de boas-vindas
		emailService.sendWelcomeEmail(user.getEmail(), user.getName());

		return new MessageResponse("Email verified successfully. You can now log in.");
	}

	@Transactional
	public MessageResponse forgotPassword(ForgotPasswordRequest request) {
		// Buscar usuário
		User user = userService.findByEmailOptional(request.getEmail()).orElse(null);

		if (user != null) {
			// Verificar se é conta LOCAL
			if (user.getProvider() != AuthProvider.LOCAL) {
				throw new InvalidProviderException(
						"Password reset is only available for accounts created with email/password. " +
								"Please use " + user.getProvider() + " to sign in."
				);
			}

			// Criar token e enviar email
			PasswordResetToken resetToken =
					passwordResetTokenService.createPasswordResetToken(user);
			emailService.sendPasswordResetEmail(
					user.getEmail(),
					user.getName(),
					resetToken.getToken()
			);
		}

		// Retornar mensagem genérica (segurança)
		return new MessageResponse("If this email exists, a password reset link has been sent.");
	}

	@Transactional
	public MessageResponse resetPassword(ResetPasswordRequest request) {
		// Buscar token
		PasswordResetToken resetToken =
				passwordResetTokenService.findByToken(request.getToken());

		// Verificar validade
		passwordResetTokenService.verifyToken(resetToken);

		// Verificar se é conta LOCAL (extra safety check)
		User user = resetToken.getUser();
		if (user.getProvider() != AuthProvider.LOCAL) {
			throw new InvalidProviderException(
					"Password reset is only available for local accounts"
			);
		}

		// Atualizar senha
		user.setPassword(passwordEncoder.encode(request.getNewPassword()));
		userService.updateUser(user);

		// Marcar token como usado
		passwordResetTokenService.markTokenAsUsed(resetToken);

		// Invalidar todos os refresh tokens (logout forçado)
		refreshTokenService.deleteByUser(user);

		// Enviar email de confirmação
		emailService.sendPasswordResetConfirmationEmail(user.getEmail(), user.getName());

		return new MessageResponse("Password reset successfully. You can now log in with your new password.");
	}

	@Transactional
	public MessageResponse resendVerificationEmail(ResendVerificationRequest request) {
		// Buscar usuário
		User user = userService.findByEmailOptional(request.getEmail()).orElse(null);

		if (user != null) {
			// Verificar se é conta LOCAL
			if (user.getProvider() != AuthProvider.LOCAL) {
				throw new InvalidProviderException(
						"Email verification is only available for accounts created with email/password. " +
								"Accounts created with " + user.getProvider() + " are automatically verified."
				);
			}

			// Verificar se o email já foi verificado
			if (user.getEmailVerified()) {
				return new MessageResponse("Email is already verified. You can log in now.");
			}

			// Invalidar tokens antigos e criar novo
			EmailVerificationToken verificationToken =
					emailVerificationTokenService.createEmailVerificationToken(user);

			// Enviar email
			emailService.sendEmailVerificationEmail(
					user.getEmail(),
					user.getName(),
					verificationToken.getToken()
			);
		}

		// Retornar mensagem genérica (segurança - não revelar se email existe)
		return new MessageResponse("If this email is registered and not verified, a new verification link has been sent.");
	}
}
