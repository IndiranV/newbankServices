package org.in.com.controller.web.io;

import lombok.Data;

@Data
public class PasswordIO {
	private String userCode;
	private String oldAuthPassword;
	private String newAuthPassword;

}
