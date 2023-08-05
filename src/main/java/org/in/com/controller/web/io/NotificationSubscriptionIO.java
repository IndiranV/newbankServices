package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotificationSubscriptionIO extends BaseIO {
	private BaseIO subscriptionType;
	private List<GroupIO> groupList;
	private List<UserIO> userList;
	private List<BaseIO> mediumList;
}
