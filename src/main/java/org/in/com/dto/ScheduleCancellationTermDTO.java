package org.in.com.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleCancellationTermDTO extends BaseDTO<ScheduleCancellationTermDTO> {
	private ScheduleDTO schedule;
	private CancellationTermDTO cancellationTerm;
	private GroupDTO group;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private String lookupCode;
	private List<ScheduleCancellationTermDTO> overrideList = new ArrayList<ScheduleCancellationTermDTO>();
}