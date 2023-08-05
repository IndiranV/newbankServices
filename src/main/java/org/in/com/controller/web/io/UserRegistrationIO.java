package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserRegistrationIO extends BaseIO {
	private String organization;
	private String city;
	private String address;
	private String State;
	private String email;
	private String mobile;
	private String requestDate;
	private String comments;
}
