package org.in.com.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserProfileNotificationDTO extends BaseDTO<UserProfileNotificationDTO> {
	private String message;
	private String activeFrom;
	private String activeTo;
	private int commentFlag;
	private UserDTO user;
	private GroupDTO group;
	private String comments;
}
