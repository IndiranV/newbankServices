package org.in.com.dto;

import hirondelle.date4j.DateTime;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.dto.enumeration.BusSeatTypeEM;
import org.in.com.dto.enumeration.TripStatusEM;

@Data
@EqualsAndHashCode(callSuper = true)
public class StageDTO extends BaseDTO<StageDTO> {
	private StageStationDTO fromStation;
	private StageStationDTO toStation;
	private int stageSequence;
	private int distance;
	private List<StageFareDTO> stageFare;
	private String tripStageCode;
	private List<StageDTO> overrideList;
	private DateTime travelDate;
	private TripStatusEM stageStatus;
	private BusDTO bus;

	public BigDecimal getSeatFare(BusSeatTypeEM busSeatTypeDTO) {
		BigDecimal seatFare = BigDecimal.ZERO;
		if (stageFare != null && !stageFare.isEmpty()) {
			for (StageFareDTO fareDTO : stageFare) {
				if (fareDTO.getBusSeatType().getId() == busSeatTypeDTO.getId()) {
					seatFare = fareDTO.getFare();
				}
			}
		}
		return seatFare;
	}

}