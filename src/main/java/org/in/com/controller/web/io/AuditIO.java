package org.in.com.controller.web.io;

import lombok.Data;


@Data
public class AuditIO {
	private String event;
	private String updatedAt;
	private UserIO user;
}
