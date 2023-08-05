package org.in.com.dto;

import java.util.List;

import org.in.com.constants.Text;
import org.in.com.dto.enumeration.OTAPartnerEM;
import org.in.com.utils.StringUtil;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StationOtaPartnerDTO extends BaseDTO<StationOtaPartnerDTO> {
	private String otaStationCode;
	private String otaStationName;
	private StateDTO state;
	private List<StationDTO> stations;
	private List<StationOtaPartnerDTO> otaStations;
	private OTAPartnerEM otaPartner;

	public String getStationCode() {
		String stationIds = Text.NA;
		if (stations != null) {
			StringBuilder station = new StringBuilder();
			for (StationDTO stationDTO : stations) {
				if (StringUtil.isNull(stationDTO.getCode())) {
					continue;
				}
				station.append(stationDTO.getCode());
				station.append(Text.COMMA);
			}

			if (StringUtil.isNotNull(station.toString())) {
				stationIds = station.toString();
			}
		}
		return stationIds;
	}
	
	public String getOTAStationCodes() {
		String otaStationCodes = Text.NA;
		if (otaStations != null) {
			StringBuilder otaStation = new StringBuilder();
			for (StationOtaPartnerDTO otaStationDTO : otaStations) {
				if (StringUtil.isNull(otaStationDTO.getOtaStationCode())) {
					continue;
				}
				otaStation.append(otaStationDTO.getOtaStationCode());
				otaStation.append(Text.COMMA);
			}

			if (StringUtil.isNotNull(otaStation.toString())) {
				otaStationCodes = otaStation.toString();
			}
		}
		return otaStationCodes;
	}
}
