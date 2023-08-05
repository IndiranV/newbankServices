package org.in.com.dto;

import org.in.com.dto.enumeration.AuditEventTypeEM;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AuditEventDTO extends BaseDTO<AuditEventDTO> {
	private AuditEventTypeEM namespaceEventType;
	private String mobileNumber;
	private String emailId;
}
