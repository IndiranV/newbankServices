package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.TravelStopsIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.TravelStopsDTO;
import org.in.com.service.TravelStopsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/{authtoken}/travel/stops")
public class TravelStopsController extends BaseController {

	@Autowired
	TravelStopsService stopService;

	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<TravelStopsIO>> getAllStops(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "1") int activeFlag) throws Exception {
		List<TravelStopsIO> stopsList = new ArrayList<TravelStopsIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<TravelStopsDTO> list = stopService.getAll(authDTO);
			for (TravelStopsDTO dto : list) {
				if (activeFlag != -1 && activeFlag != dto.getActiveFlag()) {
					continue;
				}
				TravelStopsIO stops = new TravelStopsIO();
				stops.setCode(dto.getCode());
				stops.setName(dto.getName());
				stops.setAmenities(dto.getAmenities());
				stops.setRestRoom(dto.getRestRoom());
				stops.setMinutes(dto.getMinutes());
				stops.setLandmark(dto.getLandmark());
				stops.setLatitude(dto.getLatitude());
				stops.setLongitude(dto.getLongitude());
				stops.setActiveFlag(dto.getActiveFlag());
				stopsList.add(stops);
			}
		}
		return ResponseIO.success(stopsList);
	}

	@RequestMapping(value = "/{code}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<TravelStopsIO>> getStop(@PathVariable("authtoken") String authtoken, @PathVariable("code") String code) throws Exception {
		List<TravelStopsIO> stopsList = new ArrayList<TravelStopsIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			TravelStopsDTO stopDTO = new TravelStopsDTO();
			stopDTO.setCode(code);
			List<TravelStopsDTO> list = stopService.get(authDTO, stopDTO);
			for (TravelStopsDTO dto : list) {
				TravelStopsIO stops = new TravelStopsIO();
				stops.setCode(dto.getCode());
				stops.setName(dto.getName());
				stops.setAmenities(dto.getAmenities());
				stops.setRestRoom(dto.getRestRoom());
				stops.setMinutes(dto.getMinutes());
				stops.setLandmark(dto.getLandmark());
				stops.setLatitude(dto.getLatitude());
				stops.setLongitude(dto.getLongitude());
				stops.setActiveFlag(dto.getActiveFlag());
				stopsList.add(stops);
			}
		}
		return ResponseIO.success(stopsList);
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<TravelStopsIO> updateStop(@PathVariable("authtoken") String authtoken, @RequestBody TravelStopsIO stops) throws Exception {
		TravelStopsIO stopIO = new TravelStopsIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			TravelStopsDTO dto = new TravelStopsDTO();
			dto.setCode(stops.getCode());
			dto.setName(stops.getName());
			dto.setAmenities(stops.getAmenities());
			dto.setRestRoom(stops.getRestRoom());
			dto.setMinutes(stops.getMinutes());
			dto.setLandmark(stops.getLandmark());
			dto.setLatitude(stops.getLatitude());
			dto.setLongitude(stops.getLongitude());
			dto.setActiveFlag(stops.getActiveFlag());
			stopService.Update(authDTO, dto);
			stopIO.setCode(dto.getCode());
			stopIO.setName(dto.getName());
			stopIO.setMinutes(dto.getMinutes());
			stopIO.setActiveFlag(dto.getActiveFlag());
		}
		return ResponseIO.success(stopIO);
	}

}
