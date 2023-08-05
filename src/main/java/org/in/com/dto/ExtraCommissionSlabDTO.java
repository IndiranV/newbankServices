package org.in.com.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.dto.enumeration.SlabCalenderModeEM;
import org.in.com.dto.enumeration.SlabCalenderTypeEM;
import org.in.com.dto.enumeration.SlabModeEM;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExtraCommissionSlabDTO extends BaseDTO<ExtraCommissionSlabDTO> {
	private SlabCalenderTypeEM slabCalenderType;
	private SlabCalenderModeEM slabCalenderMode;
	private SlabModeEM slabMode;
	private int slabFromValue;
	private int slabToValue;
}
