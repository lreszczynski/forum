package com.example.demo.users.dto;

import com.example.demo.roles.dto.RoleDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBasicDTO {
	private Long id;
	
	private String username;
	
	private boolean banned;
	
	private RoleDTO role;
}
