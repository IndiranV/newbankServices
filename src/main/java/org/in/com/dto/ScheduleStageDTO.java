package org.in.com.dto;

import java.util.ArrayList;
import java.util.List;

import org.in.com.constants.Text;
import org.in.com.dto.enumeration.BusSeatTypeEM;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleStageDTO extends BaseDTO<ScheduleStageDTO> implements Cloneable {
	private ScheduleDTO schedule;
	private StationDTO fromStation;
	private StationDTO toStation;
	private int fromStationSequence;
	private int toStationSequence;
	private BusSeatTypeEM busSeatType;
	private GroupDTO group;
	private List<BusSeatTypeFareDTO> busSeatTypeFare;
	private double fare;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private String lookupCode;
	private List<ScheduleStageDTO> overrideList = new ArrayList<ScheduleStageDTO>();

	public ScheduleStageDTO clone() throws CloneNotSupportedException {
		return (ScheduleStageDTO) super.clone();
	}
	
	public String getBusSeatTypeFareDetails() {
		StringBuilder seatTypeFareBuilder = new StringBuilder();
		if (busSeatTypeFare != null && !busSeatTypeFare.isEmpty()) {
			for (BusSeatTypeFareDTO seatTypeFare : busSeatTypeFare) {
				seatTypeFareBuilder.append(seatTypeFare.getBusSeatType().getId());
				seatTypeFareBuilder.append(Text.COLON);
				seatTypeFareBuilder.append(seatTypeFare.getFare());
				seatTypeFareBuilder.append(Text.COMMA);
			}
		}
		else {
			seatTypeFareBuilder.append(Text.NA);
		}
		return seatTypeFareBuilder.toString();
	}
}