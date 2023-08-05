package org.in.com.dto;

import hirondelle.date4j.DateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExportReportDetailsDTO extends BaseDTO<ExportReportDetailsDTO> {

	public String serviceName;
	public String parameter;
	public String status;
	public DateTime requestedTime;
	public String encryptData;
	public int activeFlag;
}
