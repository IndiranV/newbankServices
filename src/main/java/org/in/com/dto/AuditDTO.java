package org.in.com.dto;

import lombok.Data;

@Data
public class AuditDTO {
	private String event;
	private UserDTO user;
	private String updatedAt;
}
