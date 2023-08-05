package org.in.com.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleStationDTO extends BaseDTO<ScheduleStationDTO> {
	private ScheduleDTO schedule;
	private StationDTO station;
	private int minitues;
	private int stationSequence;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private String lookupCode;
	private String mobileNumber;
	private List<ScheduleStationDTO> overrideList = new ArrayList<ScheduleStationDTO>();

}