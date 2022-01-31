package com.example.demo.users;

import com.example.demo.roles.RoleDTO;
import com.example.demo.users.validation.CreateUser;
import com.example.demo.users.validation.UpdateUser;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
	@NotNull(groups = UpdateUser.class)
	@Null(groups = CreateUser.class)
	@JsonProperty("id")
	private Long id;
	
	@Length(min = 4, max = 40)
	@JsonProperty("username")
	private String username;
	
	@JsonProperty("banned")
	private boolean banned;
	
	//@EqualsAndHashCode.Exclude
	@JsonProperty(value = "role", access = JsonProperty.Access.READ_ONLY)
	private RoleDTO role;
}