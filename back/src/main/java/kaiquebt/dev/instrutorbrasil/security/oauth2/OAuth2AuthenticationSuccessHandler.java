package kaiquebt.dev.instrutorbrasil.security.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kaiquebt.dev.instrutorbrasil.dto.response.AuthResponse;
import kaiquebt.dev.instrutorbrasil.dto.response.UserResponse;
import kaiquebt.dev.instrutorbrasil.model.RefreshToken;
import kaiquebt.dev.instrutorbrasil.model.User;
import kaiquebt.dev.instrutorbrasil.model.enums.AuthProvider;
import kaiquebt.dev.instrutorbrasil.security.jwt.JwtService;
import kaiquebt.dev.instrutorbrasil.service.RefreshTokenService;
import kaiquebt.dev.instrutorbrasil.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final JwtService jwtService;
	private final UserService userService;
	private final RefreshTokenService refreshTokenService;
	private final ObjectMapper objectMapper;

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication
	) throws IOException, ServletException {
		OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

		User user = userService.processOAuth2User(oAuth2User, AuthProvider.GOOGLE);

		String accessToken = jwtService.generateAccessToken(user);
		RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

		UserResponse userResponse = UserResponse.builder()
				.id(user.getId())
				.email(user.getEmail())
				.name(user.getName())
				.role(user.getRole())
				.emailVerified(user.getEmailVerified())
				.build();

		AuthResponse authResponse = AuthResponse.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken.getToken())
				.tokenType("Bearer")
				.expiresIn(jwtService.getAccessTokenExpiration() / 1000)
				.user(userResponse)
				.build();

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(objectMapper.writeValueAsString(authResponse));
	}
}
