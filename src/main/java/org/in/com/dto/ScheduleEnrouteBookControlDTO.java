package org.in.com.dto;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.enumeration.EnRouteTypeEM;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleEnrouteBookControlDTO extends BaseDTO<ScheduleEnrouteBookControlDTO> {
	private List<StageDTO> stageList;
	private int releaseMinutes;
	private String dayOfWeek;
	private EnRouteTypeEM enRouteType;
	private ScheduleDTO schedule;

	public String getStages() {
		StringBuilder stageCodes = new StringBuilder();
		for (StageDTO stageDTO : stageList) {
			StationDTO fromStationDTO = stageDTO.getFromStation().getStation();
			StationDTO toStationDTO = stageDTO.getToStation().getStation();

			if (fromStationDTO.getId() == Numeric.ZERO_INT || toStationDTO.getId() == Numeric.ZERO_INT) {
				continue;
			}
			stageCodes.append(fromStationDTO.getId());
			stageCodes.append(Text.HYPHEN);
			stageCodes.append(toStationDTO.getId());
			stageCodes.append(Text.COMMA);
		}
		return stageCodes.toString();
	}
}
