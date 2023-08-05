package org.in.com.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class FareRuleDetailsDTO {
	private int id;
	private StationDTO fromStation;
	private StationDTO toStation;
	private int distance;
	private int activeFlag;
	private BigDecimal nonAcSeaterMinFare = BigDecimal.ZERO;
	private BigDecimal nonAcSeaterMaxFare = BigDecimal.ZERO;
	private BigDecimal acSeaterMinFare = BigDecimal.ZERO;
	private BigDecimal acSeaterMaxFare = BigDecimal.ZERO;
	private BigDecimal multiAxleSeaterMinFare = BigDecimal.ZERO;
	private BigDecimal multiAxleSeaterMaxFare = BigDecimal.ZERO;
	private BigDecimal nonAcSleeperLowerMinFare = BigDecimal.ZERO;
	private BigDecimal nonAcSleeperLowerMaxFare = BigDecimal.ZERO;
	private BigDecimal nonAcSleeperUpperMinFare = BigDecimal.ZERO;
	private BigDecimal nonAcSleeperUpperMaxFare = BigDecimal.ZERO;
	private BigDecimal acSleeperLowerMinFare = BigDecimal.ZERO;
	private BigDecimal acSleeperLowerMaxFare = BigDecimal.ZERO;
	private BigDecimal acSleeperUpperMinFare = BigDecimal.ZERO;
	private BigDecimal acSleeperUpperMaxFare = BigDecimal.ZERO;
	private BigDecimal brandedAcSleeperMinFare = BigDecimal.ZERO;
	private BigDecimal brandedAcSleeperMaxFare = BigDecimal.ZERO;
	private BigDecimal singleAxleAcSeaterMinFare = BigDecimal.ZERO;
	private BigDecimal singleAxleAcSeaterMaxFare = BigDecimal.ZERO;
	private BigDecimal multiAxleAcSleeperMinFare = BigDecimal.ZERO;
	private BigDecimal multiAxleAcSleeperMaxFare = BigDecimal.ZERO;
	private String updatedAt;

	public boolean isValid() {
		boolean isValid = true;
		if (nonAcSeaterMinFare.compareTo(BigDecimal.ZERO) == 0 && nonAcSeaterMaxFare.compareTo(BigDecimal.ZERO) == 0 && acSeaterMinFare.compareTo(BigDecimal.ZERO) == 0 && acSeaterMaxFare.compareTo(BigDecimal.ZERO) == 0 && multiAxleSeaterMinFare.compareTo(BigDecimal.ZERO) == 0 && multiAxleSeaterMaxFare.compareTo(BigDecimal.ZERO) == 0 && nonAcSleeperLowerMinFare.compareTo(BigDecimal.ZERO) == 0 && nonAcSleeperLowerMaxFare.compareTo(BigDecimal.ZERO) == 0 && nonAcSleeperUpperMinFare.compareTo(BigDecimal.ZERO) == 0 && nonAcSleeperUpperMaxFare.compareTo(BigDecimal.ZERO) == 0 && acSleeperLowerMinFare.compareTo(BigDecimal.ZERO) == 0 && acSleeperLowerMaxFare.compareTo(BigDecimal.ZERO) == 0 && acSleeperUpperMinFare.compareTo(BigDecimal.ZERO) == 0 && acSleeperUpperMaxFare.compareTo(BigDecimal.ZERO) == 0 && brandedAcSleeperMinFare.compareTo(BigDecimal.ZERO) == 0 && brandedAcSleeperMaxFare.compareTo(BigDecimal.ZERO) == 0 && singleAxleAcSeaterMinFare.compareTo(BigDecimal.ZERO) == 0 && singleAxleAcSeaterMaxFare.compareTo(BigDecimal.ZERO) == 0 && multiAxleAcSleeperMinFare.compareTo(BigDecimal.ZERO) == 0 && multiAxleAcSleeperMaxFare.compareTo(BigDecimal.ZERO) == 0) {
			isValid = false;
		}
		return isValid;
	}
}