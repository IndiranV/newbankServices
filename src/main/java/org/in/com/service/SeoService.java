package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;

public interface SeoService {
	public List<StationPointDTO> getScheduleStationPoint(AuthDTO authDTO, StationDTO station);
}
