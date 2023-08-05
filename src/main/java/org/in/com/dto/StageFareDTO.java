package org.in.com.dto;

import java.math.BigDecimal;

import lombok.Data;

import org.in.com.dto.enumeration.BusSeatTypeEM;

@Data
public class StageFareDTO {
	private BigDecimal fare;
	private BigDecimal minFare = BigDecimal.ZERO;
	private BigDecimal maxFare = BigDecimal.ZERO;
	private BigDecimal discountFare;
	private BusSeatTypeEM busSeatType;
	private GroupDTO group;
}