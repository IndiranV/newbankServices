package org.in.com.controller.web.io;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CommissionIO extends BaseIO {
	private BigDecimal value;
	private BaseIO valueType;
	private String commissionType;
	private String createdDateTime;
	private BigDecimal creditLimit;
	private BigDecimal serviceTax;
	private AuditIO audit;
}
