package kaiquebt.dev.instrutorbrasil.model.enums;

public enum Role {
	USER("ROLE_USER"),
	INSTRUCTOR("ROLE_INSTRUCTOR"),
	ADMIN("ROLE_ADMIN");

	private final String authority;

	Role(String authority) {
		this.authority = authority;
	}

	public String getAuthority() {
		return authority;
	}
}
