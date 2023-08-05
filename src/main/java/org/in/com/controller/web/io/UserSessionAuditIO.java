package org.in.com.controller.web.io;

import lombok.Data;

@Data
public class UserSessionAuditIO {

	private String latitude;
	private String longitude;
	private String sessionStartAt;
	private String sessionEndAt;
	private BaseIO sessionStatus;
	private BaseIO deviceMedium;
	private String ipAddress;
}
