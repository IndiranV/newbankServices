package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PhoneNumberBlockIO extends BaseIO {
	private String mobile;
	private String remarks;
}
