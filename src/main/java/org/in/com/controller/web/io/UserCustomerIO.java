package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserCustomerIO extends BaseIO {
	private String email;
	private String mobile;
	private String lastname;
}
