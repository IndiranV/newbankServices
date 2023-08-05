package org.in.com.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleControlDTO extends BaseDTO<ScheduleControlDTO> {
	private ScheduleDTO schedule;
	private GroupDTO group;
	private int openMinitues;
	private int closeMinitues;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private int allowBookingFlag;
	private StationDTO fromStation;
	private StationDTO toStation;
	private String lookupCode;
	private List<ScheduleControlDTO> overrideList = new ArrayList<ScheduleControlDTO>();

}