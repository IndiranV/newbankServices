package org.in.com.dto;

import lombok.Data;

import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.SessionStatusEM;

@Data
public class UserSessionAuditDTO {

	private String latitude;
	private String longitude;
	private String sessionStartAt;
	private String sessionEndAt;
	private SessionStatusEM sessionStatus;
	private DeviceMediumEM deviceMedium;
	private String ipAddress;
}
