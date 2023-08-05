package org.in.com.dto;

import lombok.Data;

import org.in.com.dto.enumeration.FareTypeEM;

@Data
public class DiscountCriteriaSlabDTO {
	private int slabFromValue;
	private int slabToValue;
	private int slabValue;
	private FareTypeEM slabValueType;

}
