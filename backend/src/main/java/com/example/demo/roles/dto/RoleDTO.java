package com.example.demo.roles.dto;

import com.example.demo.roles.validation.CreateRole;
import com.example.demo.roles.validation.RoleUniqueConstraint;
import com.example.demo.roles.validation.SecondOrder;
import com.example.demo.roles.validation.UpdateRole;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.GroupSequence;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@Data
@With
@AllArgsConstructor
@NoArgsConstructor
@Builder
@RoleUniqueConstraint(groups = SecondOrder.class)
@GroupSequence({RoleDTO.class, SecondOrder.class})
public class RoleDTO {
	@NotNull(groups = UpdateRole.class)
	@Null(groups = CreateRole.class)
	@JsonProperty("id")
	private Long id;
	
	@NotEmpty
	@Length(min = 2, max = 50)
	@JsonProperty("name")
	private String name;
	
	@NotNull
	@Length(max = 250)
	@JsonProperty("description")
	private String description;
}
