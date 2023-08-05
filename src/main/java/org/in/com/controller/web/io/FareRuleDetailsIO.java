package org.in.com.controller.web.io;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FareRuleDetailsIO extends BaseIO {
	private StationIO fromStation;
	private StationIO toStation;
	private int distance;
	private BigDecimal nonAcSeaterMinFare;
	private BigDecimal nonAcSeaterMaxFare;
	private BigDecimal acSeaterMinFare;
	private BigDecimal acSeaterMaxFare;
	private BigDecimal multiAxleSeaterMinFare;
	private BigDecimal multiAxleSeaterMaxFare;
	private BigDecimal nonAcSleeperLowerMinFare;
	private BigDecimal nonAcSleeperLowerMaxFare;
	private BigDecimal nonAcSleeperUpperMinFare;
	private BigDecimal nonAcSleeperUpperMaxFare;
	private BigDecimal acSleeperLowerMinFare;
	private BigDecimal acSleeperLowerMaxFare;
	private BigDecimal acSleeperUpperMinFare;
	private BigDecimal acSleeperUpperMaxFare;
	private BigDecimal brandedAcSleeperMinFare;
	private BigDecimal brandedAcSleeperMaxFare;
	private BigDecimal singleAxleAcSeaterMinFare;
	private BigDecimal singleAxleAcSeaterMaxFare;
	private BigDecimal multiAxleAcSleeperMinFare;
	private BigDecimal multiAxleAcSleeperMaxFare;
	private String updatedAt;
}