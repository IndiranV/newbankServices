package org.in.com.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserRegistrationDTO extends BaseDTO<UserRegistrationDTO> {
	private String organization;
	private String city;
	private String address;
	private String state;
	private String email;
	private String mobile;
	private String requestDate;
	private String comments;
}
