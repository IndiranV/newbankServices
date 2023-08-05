package org.in.com.aggregator.orbit;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.StationAreaDTO;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import net.sf.json.JSONObject;

@Service
@EnableAsync
public class OrbitServiceImpl implements OrbitService {

	@Override
	public List<StationAreaDTO> syncStationArea(AuthDTO authDTO, String syncDate) {
		OrbitCommunicator communicator = new OrbitCommunicator();
		JSONObject jsonData = communicator.getStationAreas(authDTO, syncDate);
		OrbitDataConvertor convertor = new OrbitDataConvertor();
		return convertor.getStationArea(jsonData);
	}
}
