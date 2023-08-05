package org.in.com.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LoginDTO extends BaseDTO<LoginDTO>{
	private int id;
	private int namespaceId;
	private String token;
	private int userGroupId;
	private int organizationId;

}
