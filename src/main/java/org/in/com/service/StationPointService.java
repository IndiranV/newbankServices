package org.in.com.service;

import java.util.List;
import java.util.Map;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.UserStationPointDTO;

public interface StationPointService extends BaseService<StationPointDTO> {
	public List<UserStationPointDTO> getUserSpecificStationPoint(AuthDTO authDTO, UserDTO userDTO, StationDTO stationDTO);

	public void updateUserSpecificStationPoint(AuthDTO authDTO, UserStationPointDTO userStationPointDTO);

	public Map<String, Map<String, StationPointDTO>> getUserSpecificStationPointV2(AuthDTO authDTO, UserDTO userDTO, StationDTO station);
	
	public StationPointDTO getStationPoint(AuthDTO auth, StationPointDTO stationPoint);
}
