package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserProfileNotificationIO extends BaseIO {
	private String message;
	private String activeFrom;
	private String activeTo;
	private int commentFlag;
	private UserIO user;
	private GroupIO group;
	private String comments;
}
