package org.in.com.utils;

import java.util.List;

import org.in.com.dto.StationPointDTO;

public class CollectionsUtil {

	public static StationPointDTO getStationPoint(List<StationPointDTO> stationPointList, StationPointDTO stationPointDTO) {
		StationPointDTO stationPoint = null;
		for (StationPointDTO pointDTO : stationPointList) {
			if (pointDTO.getId() == stationPointDTO.getId()) {
				stationPoint = pointDTO;
				break;
			}
		}
		return stationPoint;
	}
}
