package kaiquebt.dev.instrutorbrasil.dto.response;

import kaiquebt.dev.instrutorbrasil.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

	private Long id;
	private String email;
	private String name;
	private Set<Role> roles;
	private Boolean emailVerified;
}
