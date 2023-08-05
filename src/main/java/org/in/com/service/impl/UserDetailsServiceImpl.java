package org.in.com.service.impl;

import java.util.List;

import org.in.com.dao.UserDetailsDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.UserDetailsDTO;
import org.in.com.service.StateService;
import org.in.com.service.StationService;
import org.in.com.service.UserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	@Autowired
	StationService stationService;
	@Autowired
	StateService stateService;

	public List<UserDetailsDTO> get(AuthDTO authDTO, UserDetailsDTO dto) {
		return null;
	}

	public UserDetailsDTO getUserDetails(AuthDTO authDTO, UserDTO userDTO) {
		UserDetailsDAO userDetailsDAO = new UserDetailsDAO();
		UserDetailsDTO userDetailsDTO = userDetailsDAO.getUserDetails(authDTO, userDTO);
		if (userDetailsDTO.getStation() != null) {
			userDetailsDTO.setStation(stationService.getStation(userDetailsDTO.getStation()));
		}
		if (userDetailsDTO.getState() != null) {
			userDetailsDTO.setState(stateService.getState(userDetailsDTO.getState()));
		}
		if (userDetailsDTO.getStationArea() != null) {
			stationService.getStationArea(userDetailsDTO.getStationArea());
		}
		return userDetailsDTO;
	}

	public UserDetailsDTO getUserDetailsV2(AuthDTO authDTO, UserDTO userDTO) {
		UserDetailsDAO userDetailsDAO = new UserDetailsDAO();
		UserDetailsDTO userDetailsDTO = userDetailsDAO.getUserDetails(authDTO, userDTO);
		if (userDetailsDTO.getStationArea() != null) {
			stationService.getStationArea(userDetailsDTO.getStationArea());
		}
		return userDetailsDTO;
	}

	public UserDetailsDTO Update(AuthDTO authDTO, UserDetailsDTO userDetailsDTO) {
		UserDetailsDAO userDetailsDAO = new UserDetailsDAO();

		if (userDetailsDTO.getStation() != null) {
			userDetailsDTO.setStation(stationService.getStation(userDetailsDTO.getStation()));
		}
		if (userDetailsDTO.getState() != null) {
			userDetailsDTO.setState(stateService.getState(userDetailsDTO.getState()));
		}
		UserDetailsDTO userdetails = userDetailsDAO.updateUserDetails(authDTO, userDetailsDTO);

		return userdetails;
	}

	public List<UserDetailsDTO> getAll(AuthDTO authDTO) {
		return null;
	}

}
