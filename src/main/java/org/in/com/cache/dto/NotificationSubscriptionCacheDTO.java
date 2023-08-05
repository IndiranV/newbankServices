package org.in.com.cache.dto;

import java.util.List;

public class NotificationSubscriptionCacheDTO {
	private String code;
	private List<String> notificationMedium;
	private List<Integer> user;
	private List<Integer> group;
	private int activeFlag;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public List<String> getNotificationMedium() {
		return notificationMedium;
	}

	public void setNotificationMedium(List<String> notificationMedium) {
		this.notificationMedium = notificationMedium;
	}

	public List<Integer> getUser() {
		return user;
	}

	public void setUser(List<Integer> user) {
		this.user = user;
	}

	public List<Integer> getGroup() {
		return group;
	}

	public void setGroup(List<Integer> group) {
		this.group = group;
	}

	public int getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(int activeFlag) {
		this.activeFlag = activeFlag;
	}

}
