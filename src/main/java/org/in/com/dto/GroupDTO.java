package org.in.com.dto;

import org.in.com.dto.enumeration.UserRoleEM;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GroupDTO extends BaseDTO<GroupDTO> {
	private String decription;
	private String color;
	private UserRoleEM role;
	private int userCount;
	private int level;

}
