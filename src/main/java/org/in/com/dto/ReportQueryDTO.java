package org.in.com.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReportQueryDTO extends BaseDTO<ReportQueryDTO> {
	private String description;
	private String query;
	private int daysLimit;
}
