package org.in.com.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ReportSummaryDTO {
	private String keyword;
	private BigDecimal value;
	private int column;

}
