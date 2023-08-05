package org.in.com.cache.dto;

import java.io.Serializable;

public class NotificationTemplateConfigCacheDTO implements Serializable {
	private static final long serialVersionUID = 7415123223890504312L;
	private int id;
	private String code;
	private String name;
	private String entityCode;
	private String headerDltCode;
	private String header;
	private String templateDltCode;
	private String notificationType;
	private String content;
	private String notificationMode;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEntityCode() {
		return entityCode;
	}

	public void setEntityCode(String entityCode) {
		this.entityCode = entityCode;
	}

	public String getHeaderDltCode() {
		return headerDltCode;
	}

	public void setHeaderDltCode(String headerDltCode) {
		this.headerDltCode = headerDltCode;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getTemplateDltCode() {
		return templateDltCode;
	}

	public void setTemplateDltCode(String templateDltCode) {
		this.templateDltCode = templateDltCode;
	}

	public String getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(String notificationType) {
		this.notificationType = notificationType;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getNotificationMode() {
		return notificationMode;
	}

	public void setNotificationMode(String notificationMode) {
		this.notificationMode = notificationMode;
	}

}
