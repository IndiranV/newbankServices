package org.in.com.aggregator.cargo;

import org.in.com.dto.AuthDTO;

import net.sf.json.JSONArray;

public interface CargoService {

	public JSONArray getCargoTransitDetails(AuthDTO authDTO, String tripDate, String registrationNumber);
}
