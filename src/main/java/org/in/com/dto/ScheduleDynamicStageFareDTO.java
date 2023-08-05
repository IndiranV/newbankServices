package org.in.com.dto;

import java.util.ArrayList;
import java.util.List;

import org.in.com.dto.enumeration.DynamicPriceProviderEM;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleDynamicStageFareDTO extends BaseDTO<ScheduleDynamicStageFareDTO> {
	private ScheduleDTO schedule;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private List<ScheduleDynamicStageFareDetailsDTO> stageFare;
	private String lookupCode;
	private List<ScheduleDynamicStageFareDTO> overrideList = new ArrayList<ScheduleDynamicStageFareDTO>();
	private int status;
	private DynamicPriceProviderEM dynamicPriceProvider;
	
	
	public ScheduleDynamicStageFareDetailsDTO getScheduleDynamicStageFareDTO(){
		return stageFare.get(0);
	}
}