package org.in.com.cache.dto;

import java.io.Serializable;
import java.util.List;

public class EventNotificationConfigCacheDTO implements Serializable {
	private static final long serialVersionUID = 7415123223890504312L;
	private String code;
	private List<Integer> notificationEventIds;
	private String NotificationTypeCode;
	private List<Integer> scheduleIds;
	private List<String> routes;
	private List<Integer> groupIds;
	private List<String> deviceMedium;
	private List<String> notificationMedium;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private int startMinitues;
	private int endMinitues;
	private int templateId;
	private int activeFlag;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public List<Integer> getNotificationEventIds() {
		return notificationEventIds;
	}

	public void setNotificationEventIds(List<Integer> ticketStatusId) {
		this.notificationEventIds = ticketStatusId;
	}

	public String getNotificationTypeCode() {
		return NotificationTypeCode;
	}

	public void setNotificationTypeCode(String notificationTypeCode) {
		NotificationTypeCode = notificationTypeCode;
	}

	public List<Integer> getScheduleIds() {
		return scheduleIds;
	}

	public void setScheduleIds(List<Integer> scheduleIds) {
		this.scheduleIds = scheduleIds;
	}

	public List<String> getRoutes() {
		return routes;
	}

	public void setRoutes(List<String> routes) {
		this.routes = routes;
	}

	public List<Integer> getGroupIds() {
		return groupIds;
	}

	public void setGroupIds(List<Integer> groupIds) {
		this.groupIds = groupIds;
	}

	public List<String> getDeviceMedium() {
		return deviceMedium;
	}

	public void setDeviceMedium(List<String> deviceMedium) {
		this.deviceMedium = deviceMedium;
	}

	public List<String> getNotificationMedium() {
		return notificationMedium;
	}

	public void setNotificationMedium(List<String> notificationMedium) {
		this.notificationMedium = notificationMedium;
	}

	public String getActiveFrom() {
		return activeFrom;
	}

	public void setActiveFrom(String activeFrom) {
		this.activeFrom = activeFrom;
	}

	public String getActiveTo() {
		return activeTo;
	}

	public void setActiveTo(String activeTo) {
		this.activeTo = activeTo;
	}

	public String getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(String dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public int getStartMinitues() {
		return startMinitues;
	}

	public void setStartMinitues(int startMinitues) {
		this.startMinitues = startMinitues;
	}

	public int getEndMinitues() {
		return endMinitues;
	}

	public void setEndMinitues(int endMinitues) {
		this.endMinitues = endMinitues;
	}

	public int getTemplateId() {
		return templateId;
	}

	public void setTemplateId(int templateId) {
		this.templateId = templateId;
	}

	public int getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(int activeFlag) {
		this.activeFlag = activeFlag;
	}

}
