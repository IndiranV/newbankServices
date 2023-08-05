package org.in.com.service.impl;

import java.util.List;

import org.in.com.dao.ScheduleStationPointDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.service.SeoService;
import org.in.com.service.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SeoServiceImpl implements SeoService {
	@Autowired
	StationService stationService;

	/*
	 * All station point in schedule active for station
	 * Used for SEO
	 */
	public List<StationPointDTO> getScheduleStationPoint(AuthDTO authDTO, StationDTO station) {
		StationDTO stationDTO = stationService.getStation(station);
		ScheduleStationPointDAO pointDAO = new ScheduleStationPointDAO();
		List<StationPointDTO> list = pointDAO.getScheduleStationPoint(authDTO, stationDTO);
		return list;
	}

}
