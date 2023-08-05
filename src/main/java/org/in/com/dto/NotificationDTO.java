package org.in.com.dto;

import org.in.com.constants.Text;
import org.in.com.dto.enumeration.NotificationMediumEM;
import org.in.com.dto.enumeration.NotificationTypeEM;
import org.in.com.utils.StringUtil;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotificationDTO extends BaseDTO<NotificationDTO> {
	private String refferenceCode;
	private String participantAddress;
	private int transactionCount;
	private String requestLog;
	private String responseLog;
	private NotificationMediumEM notificationMode;
	private NotificationTypeEM notificationType;

	public String getRequestLog1() {
		return StringUtil.substring(requestLog, 250);
	}

	public String getRequestLog2() {
		String response = Text.EMPTY;
		if (requestLog.length() > 250) {
			response = requestLog.substring(250 - 1, requestLog.length());
		}
		return StringUtil.substring(response, 250);
	}

	public String getRequestLog() {
		return StringUtil.substring(requestLog, 250);
	}

	public String getResponseLog() {
		return StringUtil.substring(responseLog, 250);
	}
}
