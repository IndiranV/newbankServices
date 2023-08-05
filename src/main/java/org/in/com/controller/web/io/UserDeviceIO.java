package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserDeviceIO extends BaseIO {
	private String deviceCode;
	private String uniqueCode;
	private BaseIO deviceMedium;
	private String version;
}
