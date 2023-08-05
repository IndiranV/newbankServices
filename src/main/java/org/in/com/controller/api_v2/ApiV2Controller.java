package org.in.com.controller.api_v2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.in.com.controller.api_v2.io.ResponseIO;
import org.in.com.controller.api_v2.io.StationIO;
import org.in.com.controller.web.BaseController;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.StationDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.AuthService;
import org.in.com.service.ScheduleFareOverrideService;
import org.in.com.service.StationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/2.0/json/{apiToken}")
public class ApiV2Controller extends BaseController {
	public static Map<String, Integer> ConcurrentRequests = new ConcurrentHashMap<String, Integer>();

	@Autowired
	AuthService authService;
	@Autowired
	StationService stationService;
	@Autowired
	ScheduleFareOverrideService fareOverrideService;
	private static final Logger loggerapi = LoggerFactory.getLogger("org.in.com.controller.api_v2");

	@RequestMapping(value = "/station", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<StationIO>> getStations(@PathVariable("apiToken") String apiToken) throws Exception {
		loggerapi.info("Request for getStations with apiToken " + apiToken + "--" + ConcurrentRequests.get(apiToken));
		checkConcurrentRequests(apiToken);
		List<StationDTO> list = null;
		List<StationIO> IOlist = new ArrayList<StationIO>();
		try {
			AuthDTO authDTO = authService.APIAuthendtication(apiToken);
			list = stationService.getCommerceStation(authDTO);
			for (StationDTO stationDTO : list) {
				StationIO stationIO = new StationIO();
				stationIO.setCode(stationDTO.getCode());
				stationIO.setName(stationDTO.getName());
				IOlist.add(stationIO);
			}
		}
		catch (ServiceException e) {
			throw new ServiceException(e.getErrorCode());
		}
		finally {
			releaseConcurrentRequests(apiToken);
		}
		loggerapi.info("Response for getStations done-" + apiToken + "--" + ConcurrentRequests.get(apiToken));
		return ResponseIO.success(IOlist);
	}

	@RequestMapping(value = "/route", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<Map<String, List<String>>> getRoutes(@PathVariable("apiToken") String apiToken) throws Exception {
		loggerapi.info("Request for getRoutes with apiToken " + apiToken + "--" + ConcurrentRequests.get(apiToken));
		checkConcurrentRequests(apiToken);
		AuthDTO authDTO = authService.APIAuthendtication(apiToken);
		Map<String, List<String>> MapList = null;
		try {
			if (authDTO != null) {
				MapList = stationService.getCommerceRoutes(authDTO);
			}
		}
		catch (ServiceException e) {
			throw new ServiceException(e.getErrorCode());
		}
		finally {
			releaseConcurrentRequests(apiToken);
		}
		loggerapi.info("Response for getRoutes done-" + apiToken + "--" + ConcurrentRequests.get(apiToken));
		return ResponseIO.success(MapList);
	}

	public static synchronized boolean checkConcurrentRequests(String apiToken) {
		System.out.println("V2 API Version: " + apiToken);
		if (ConcurrentRequests.get(apiToken) != null && ConcurrentRequests.get(apiToken) > 5) {
			loggerapi.error("Error reached Max Concurrent Request CC800:" + apiToken + "-->" + ConcurrentRequests.get(apiToken));
			throw new ServiceException(ErrorCode.REACHED_MAX_CONCURRENT_REQUESTS);
		}
		if (ConcurrentRequests.get(apiToken) != null) {
			ConcurrentRequests.put(apiToken, ConcurrentRequests.get(apiToken) + 1);
		}
		else {
			ConcurrentRequests.put(apiToken, 1);
		}
		return true;
	}

	public static synchronized boolean releaseConcurrentRequests(String apiToken) {
		if (ConcurrentRequests.get(apiToken) != null) {
			if (ConcurrentRequests.get(apiToken) > 0) {
				ConcurrentRequests.put(apiToken, ConcurrentRequests.get(apiToken) - 1);
			}
		}
		return true;
	}

}
