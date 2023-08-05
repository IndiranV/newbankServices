package org.in.com.aggregator.cargo;

import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.IntegrationDTO;
import org.in.com.dto.enumeration.IntegrationTypeEM;
import org.in.com.service.IntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class CargoServiceImpl implements CargoService {

	@Autowired
	IntegrationService integrationService;

	@Override
	public JSONArray getCargoTransitDetails(AuthDTO authDTO, String tripDate, String registrationNumber) {
		IntegrationDTO integration = integrationService.getIntegration(authDTO, IntegrationTypeEM.CARGO, Text.NA);
		CargoCommunicator commnicator = new CargoCommunicator();
		JSONObject json = commnicator.getCargoTransitDetails(authDTO, integration, tripDate, registrationNumber);
		CargoDataConvertor convertor = new CargoDataConvertor();
		return convertor.getCargoTransitDetails(json);
	}

}
