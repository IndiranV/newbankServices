package org.in.com.controller.web.io;

import lombok.Data;

@Data
public class AuthIO {
	private String username;
	private String password;
	private String authToken;
	private String namespaceCode;
	private RoleIO role;
	private String deviceMediumCode;
	// Bus Buddy Tab Device
	private String deviceCode;
	private String driverName;
	private String deviceToken;
	// Customer APP,Browser local
	private String sessionToken;

}
