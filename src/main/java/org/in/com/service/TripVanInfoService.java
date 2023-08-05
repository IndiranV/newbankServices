package org.in.com.service;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.TripVanExceptionDTO;
import org.in.com.dto.TripVanInfoDTO;

public interface TripVanInfoService {

	public void updateTripVanInfo(AuthDTO authDTO, TripVanInfoDTO tripVanInfoDTO);

	public TripVanInfoDTO getTripVanInfo(AuthDTO authDTO, TripVanInfoDTO tripVanInfoDTO);

	public TripVanInfoDTO getTripVanInfoByCode(AuthDTO authDTO, TripVanInfoDTO tripVanInfoDTO);

	public void updateNotitficationStatus(AuthDTO authDTO, TripVanInfoDTO tripVanInfoDTO);
	
	// Van pickup chart Exception
	public void updateTripVanException(AuthDTO authDTO, TripVanExceptionDTO tripVanExceptionDTO);
	
	public TripVanInfoDTO getTripVanInfoV2(AuthDTO authDTO, TripVanInfoDTO tripVanInfoDTO);
}
