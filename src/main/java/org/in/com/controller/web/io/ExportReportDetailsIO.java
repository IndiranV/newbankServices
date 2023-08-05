package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExportReportDetailsIO extends BaseIO {

	public String serviceName;
	public String parameter;
	public String status;
	public String requestedTime;
	public String encryptData;
	public int activeFlag;
}
