package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AuditEventIO extends BaseIO {
	private BaseIO namespaceEventType;
	private String mobileNumber;
	private String emailId;
}
