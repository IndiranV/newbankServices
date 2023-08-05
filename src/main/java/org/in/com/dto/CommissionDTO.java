package org.in.com.dto;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.dto.enumeration.CommissionTypeEM;
import org.in.com.dto.enumeration.FareTypeEM;

@Data
@EqualsAndHashCode(callSuper = true)
public class CommissionDTO extends BaseDTO<CommissionDTO> {
	private BigDecimal commissionValue = BigDecimal.ZERO;
	private BigDecimal tdsTaxValue = BigDecimal.ZERO;
	private BigDecimal serviceTax = BigDecimal.ZERO;
	private FareTypeEM commissionValueType;
	private BigDecimal creditlimit = BigDecimal.ZERO;
	// booking/Cancel commission
	private CommissionTypeEM commissionType;
	private String createdDateTime;
	private AuditDTO audit;
}
