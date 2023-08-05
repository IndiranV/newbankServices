package org.in.com.controller.web.io;

import lombok.Data;

@Data
public class CollaborationIO {

	private UserIO user;
	private GroupIO group;
	private String authToken;
	private RoleIO role;

}
