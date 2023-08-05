package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EventNotificationConfigIO extends BaseIO {
	private int startMinitues;
	private int endMinitues;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private BaseIO notificationType;
	private List<String> deviceMedium;
	private List<String> notificationMedium;
	private List<GroupIO> groupList;
	private List<ScheduleIO> schedule;
	private List<RouteIO> route;
	private List<BaseIO> events;
	private NotificationTemplateConfigIO templateConfig;
}
