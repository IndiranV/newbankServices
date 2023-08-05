package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.TripSeatQuotaDTO;

public interface TripSeatQuotaService {
	public void updateTripSeatQuota(AuthDTO authDTO, TripSeatQuotaDTO tripSeatQuotaDTO);

	public List<TripSeatQuotaDTO> getAllTripSeatQuota(AuthDTO authDTO, TripDTO tripDTO);

	public List<TripSeatQuotaDTO> getAllTripSeatQuotaV2(AuthDTO authDTO, TripDTO tripDTO);
}
