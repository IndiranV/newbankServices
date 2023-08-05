package org.in.com.dto;

import java.math.BigDecimal;

import org.in.com.dto.enumeration.BusSeatTypeEM;

import lombok.Data;

@Data
public class BusSeatTypeFareDTO {
	private BigDecimal fare;
	private BusSeatTypeEM busSeatType;

}