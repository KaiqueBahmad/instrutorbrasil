package kaiquebt.dev.instrutorbrasil.dto.response;

import kaiquebt.dev.instrutorbrasil.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

	private Long id;
	private String email;
	private String name;
	private Role role;
	private Boolean emailVerified;
}
