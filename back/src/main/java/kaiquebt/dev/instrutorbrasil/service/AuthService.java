package kaiquebt.dev.instrutorbrasil.service;

import kaiquebt.dev.instrutorbrasil.dto.request.RefreshTokenRequest;
import kaiquebt.dev.instrutorbrasil.dto.response.AuthResponse;
import kaiquebt.dev.instrutorbrasil.dto.response.UserResponse;
import kaiquebt.dev.instrutorbrasil.model.RefreshToken;
import kaiquebt.dev.instrutorbrasil.model.User;
import kaiquebt.dev.instrutorbrasil.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserService userService;
	private final JwtService jwtService;
	private final RefreshTokenService refreshTokenService;

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
