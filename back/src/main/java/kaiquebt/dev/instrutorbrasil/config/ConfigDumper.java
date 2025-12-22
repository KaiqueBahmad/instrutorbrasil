package kaiquebt.dev.instrutorbrasil.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dump-config")
public class ConfigDumper implements CommandLineRunner {

	@Value("${spring.application.name}")
	private String applicationName;

	@Value("${server.port}")
	private String serverPort;

	@Value("${spring.datasource.url}")
	private String datasourceUrl;

	@Value("${spring.datasource.username}")
	private String datasourceUsername;

	@Value("${spring.datasource.password}")
	private String datasourcePassword;

	@Value("${jwt.secret}")
	private String jwtSecret;

	@Value("${jwt.access-token-expiration}")
	private String accessTokenExpiration;

	@Value("${jwt.refresh-token-expiration}")
	private String refreshTokenExpiration;

	@Value("${spring.mail.host}")
	private String mailHost;

	@Value("${spring.mail.port}")
	private String mailPort;

	@Value("${spring.mail.username}")
	private String mailUsername;

	@Value("${spring.mail.password}")
	private String mailPassword;

	@Value("${app.email.from}")
	private String emailFrom;

	@Value("${app.email.from-name}")
	private String emailFromName;

	@Value("${app.frontend.url}")
	private String frontendUrl;

	@Value("${spring.security.oauth2.client.registration.google.client-id}")
	private String googleClientId;

	@Value("${spring.security.oauth2.client.registration.google.client-secret}")
	private String googleClientSecret;

	@Override
	public void run(String... args) {
		System.out.println("\n" + "=".repeat(80));
		System.out.println("APPLICATION.PROPERTIES VALUES");
		System.out.println("=".repeat(80));

		System.out.println("\nspring.application.name=" + applicationName);
		System.out.println("server.port=" + serverPort);
		System.out.println();
		System.out.println("spring.datasource.url=" + datasourceUrl);
		System.out.println("spring.datasource.username=" + datasourceUsername);
		System.out.println("spring.datasource.password=" + datasourcePassword);
		System.out.println();
		System.out.println("jwt.secret=" + jwtSecret);
		System.out.println("jwt.access-token-expiration=" + accessTokenExpiration);
		System.out.println("jwt.refresh-token-expiration=" + refreshTokenExpiration);
		System.out.println();
		System.out.println("spring.mail.host=" + mailHost);
		System.out.println("spring.mail.port=" + mailPort);
		System.out.println("spring.mail.username=" + mailUsername);
		System.out.println("spring.mail.password=" + mailPassword);
		System.out.println();
		System.out.println("app.email.from=" + emailFrom);
		System.out.println("app.email.from-name=" + emailFromName);
		System.out.println("app.frontend.url=" + frontendUrl);
		System.out.println();
		System.out.println("spring.security.oauth2.client.registration.google.client-id=" + googleClientId);
		System.out.println("spring.security.oauth2.client.registration.google.client-secret=" + googleClientSecret);

		System.out.println("\n" + "=".repeat(80) + "\n");
		System.exit(0); // Exit immediately after dumping
	}
}
