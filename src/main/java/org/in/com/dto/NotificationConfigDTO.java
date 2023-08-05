package org.in.com.dto;

import org.in.com.dto.enumeration.NotificationMediumEM;
import org.in.com.utils.StringUtil;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotificationConfigDTO extends BaseDTO<NotificationConfigDTO> {
	private String entityCode;
	private String headerDltCode;
	private String header;
	private NotificationMediumEM notificationMode;
	
	// If Numeric values in Header - Promotional (P)
	public boolean isPromotionalSMSType() {
		boolean isNumericValue = StringUtil.isNumeric(header);
		return isNumericValue;
	}
}
