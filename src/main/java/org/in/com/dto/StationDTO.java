package org.in.com.dto;

import java.util.List;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StationDTO extends BaseDTO<StationDTO> {
	private StateDTO state;
	private int apiFlag = Numeric.ONE_INT;
	private List<StationPointDTO> stationPoints;
	private String latitude;
	private String longitude;
	private int radius;

	public String getRelatedStationIds(List<StationDTO> relatedStations) {
		StringBuilder relatedStationIds = new StringBuilder();
		for (StationDTO relatedStation : relatedStations) {
			if (relatedStation.getId() != 0) {
				relatedStationIds.append(relatedStation.getId()).append(Text.COMMA);
			}
		}
		return relatedStationIds.toString();
	}
}
