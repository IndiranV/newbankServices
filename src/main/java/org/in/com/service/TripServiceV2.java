package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.TripDTO;

public interface TripServiceV2 {

	public List<TripDTO> saveTrip(AuthDTO authDTO, List<TripDTO> tripList);

}
