package org.in.com.controller.api_v3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.in.com.config.ApplicationConfig;
import org.in.com.controller.api_v3.io.ResponseIO;
import org.in.com.controller.api_v3.io.StateIO;
import org.in.com.controller.api_v3.io.StationIO;
import org.in.com.controller.web.BaseController;
import org.in.com.dto.StationDTO;
import org.in.com.dto.enumeration.NamespaceZoneEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.StationService;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/3.0/json/{vendorCode}/{accessToken}")
public class ApiV3MasterController extends BaseController {
	public static Map<String, Integer> ConcurrentRequests = new ConcurrentHashMap<String, Integer>();

	@Autowired
	StationService stationService;

	@RequestMapping(value = "/station", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<StationIO>> getAllStations(@PathVariable("vendorCode") String vendorCode, @PathVariable("accessToken") String accessToken) throws Exception {
		validateMandatory(accessToken, vendorCode);

		List<StationIO> stations = new ArrayList<StationIO>();
		List<StationDTO> list = stationService.getAllStations();
		for (StationDTO stationDTO : list) {
			if (stationDTO.getApiFlag() != 1) {
				continue;
			}
			StationIO stationIO = new StationIO();
			stationIO.setCode(stationDTO.getCode());
			stationIO.setName(stationDTO.getName());

			StateIO state = new StateIO();
			state.setCode(stationDTO.getState().getCode());
			state.setName(stationDTO.getState().getName());
			stationIO.setState(state);
			stations.add(stationIO);
		}
		return ResponseIO.success(stations);
	}

	private void validateMandatory(String accessToken, String vendorCode) {
		NamespaceZoneEM namespaceZone = NamespaceZoneEM.getNamespaceZoneEM(ApplicationConfig.getServerZoneCode());
		if (StringUtil.isNull(accessToken) || namespaceZone == null || !accessToken.equals(namespaceZone.getToken())) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED);
		}
		if (StringUtil.isNull(vendorCode)) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED);
		}
	}
}
