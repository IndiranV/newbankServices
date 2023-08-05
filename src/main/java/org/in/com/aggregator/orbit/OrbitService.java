package org.in.com.aggregator.orbit;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.StationAreaDTO;

public interface OrbitService {

	public List<StationAreaDTO> syncStationArea(AuthDTO authDTO, String syncDate);

}
