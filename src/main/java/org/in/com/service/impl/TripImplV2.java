package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.in.com.cache.CacheCentral;
import org.in.com.cache.TripCache;
import org.in.com.dao.TripDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.enumeration.TripStatusEM;
import org.in.com.service.TripServiceV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@Service
@EnableAsync
public class TripImplV2 extends CacheCentral implements TripServiceV2 {
	public static Logger TRIP_INFO_LOGGER = LoggerFactory.getLogger("org.in.com.service.impl.TripImpl");

	@Async
	public List<TripDTO> saveTrip(AuthDTO authDTO, List<TripDTO> tripList) {
		List<TripDTO> activeList = new ArrayList<TripDTO>();

		TripDAO tripDAO = new TripDAO();
		TripCache tripCache = new TripCache();
		List<TripDTO> saveList = new ArrayList<TripDTO>();
		tripCache.CheckAndGetTripDTO(authDTO, tripList);

		for (TripDTO tripDTO : tripList) {
			if (tripDTO.getId() == 0 && tripDTO.getTripStatus().getId() == TripStatusEM.TRIP_OPEN.getId()) {
				saveList.add(tripDTO);
			}
			else if (tripDTO.getId() != 0) {
				activeList.add(tripDTO);
			}
		}
		if (!saveList.isEmpty()) {
			// Save in DB
			tripDAO.saveTripDTO(authDTO, saveList);
			// Update in Cache
			tripCache.putAllTripDTO(authDTO, saveList);
			activeList.addAll(saveList);
		}
		return activeList;
	}

}
