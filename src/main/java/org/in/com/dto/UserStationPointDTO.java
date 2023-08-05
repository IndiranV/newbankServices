package org.in.com.dto;

import java.math.BigDecimal;
import java.util.List;

import org.in.com.constants.Text;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserStationPointDTO extends BaseDTO<UserStationPointDTO>{
	private UserDTO user;
	private StationDTO station;
	private List<GroupDTO> groupList;
	private BigDecimal boardingCommission;
	
	public String getStationPointIds() {
		StringBuilder stationPointCodes = new StringBuilder();
		if (station != null && station.getStationPoints() != null) {
			for (StationPointDTO stationPointDTO : station.getStationPoints()) {
				if (stationPointDTO.getId() == 0) {
					continue;
				}
				stationPointCodes.append(stationPointDTO.getId());
				stationPointCodes.append(Text.COMMA);
			}
		}
		return stationPointCodes.toString();
		
	}
}
