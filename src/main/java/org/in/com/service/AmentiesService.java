package org.in.com.service;

import java.util.List;

import org.in.com.dto.AmenitiesDTO;
import org.in.com.dto.AuthDTO;

public interface AmentiesService extends BaseService<AmenitiesDTO> {
	public void reloadAmenties();

	public List<AmenitiesDTO> getAllforZoneSync(AuthDTO authDTO, String syncDate);

}
