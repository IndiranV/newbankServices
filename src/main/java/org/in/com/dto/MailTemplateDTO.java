package org.in.com.dto;

import java.io.InputStream;

import lombok.Data;
@Data
public class MailTemplateDTO {
	private String subject = null;
	private String body = null;
	private String signature = null;
	private boolean htmlContent = false;
	private boolean hasAttachment = false;
	private InputStream inputStreamData = null;
	private String attachmentContentType = null;
	private String attachFileName = null;
}
