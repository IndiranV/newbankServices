package org.in.com.dto;

import java.util.List;

import org.in.com.constants.Text;
import org.in.com.dto.enumeration.NotificationSubscriptionTypeEM;
import org.in.com.dto.enumeration.NotificationMediumEM;
import org.in.com.utils.StringUtil;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotificationSubscriptionDTO extends BaseDTO<NotificationSubscriptionDTO> {
	private NotificationSubscriptionTypeEM subscriptionType;
	private List<GroupDTO> groupList;
	private List<UserDTO> userList;
	private List<NotificationMediumEM> notificationMediumList;
	
	public String getUsers() {
		StringBuilder userIds = new StringBuilder();
		if (userList != null) {
			for (UserDTO userDTO : userList) {
				if (userDTO.getId() == 0) {
					continue;
				}
				userIds.append(userDTO.getId());
				userIds.append(Text.COMMA);
			}
		}
		return StringUtil.isNotNull(userIds.toString()) ? userIds.toString() : Text.NA;
	}
	
	public String getGroups() {
		StringBuilder groupIds = new StringBuilder();
		if (groupList != null) {
			for (GroupDTO groupDTO : groupList) {
				if (groupDTO.getId() == 0) {
					continue;
				}
				groupIds.append(groupDTO.getId());
				groupIds.append(Text.COMMA);
			}
		}
		return StringUtil.isNotNull(groupIds.toString()) ? groupIds.toString() : Text.NA;
	}
	
	public String getNotificationMediums() {
		StringBuilder mediumIds = new StringBuilder();
		if (notificationMediumList != null) {
			for (NotificationMediumEM notification : notificationMediumList) {
				if (notification.getId() == 0) {
					continue;
				}
				mediumIds.append(notification.getId());
				mediumIds.append(Text.COMMA);
			}
		}
		return StringUtil.isNotNull(mediumIds.toString()) ? mediumIds.toString() : Text.NA;
	}
}
