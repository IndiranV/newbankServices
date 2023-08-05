package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.StationIO;
import org.in.com.controller.web.io.StationPointIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.enumeration.StationPointAmenitiesEM;
import org.in.com.service.StationPointService;
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
@RequestMapping("/{authtoken}/stationpoints")
public class StationPointController extends BaseController {
	@Autowired
	StationPointService pointService;

	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<StationPointIO>> getListAllStations(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "-1") int activeFlag) throws Exception {
		List<StationPointIO> stations = new ArrayList<StationPointIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<StationPointDTO> pointDTOs = (List<StationPointDTO>) pointService.getAll(authDTO);
			for (StationPointDTO stationPointDTO : pointDTOs) {
				if (activeFlag != -1 && activeFlag != stationPointDTO.getActiveFlag()) {
					continue;
				}
				StationPointIO stationPointIO = new StationPointIO();
				StationIO stationIO = new StationIO();
				stationIO.setName(stationPointDTO.getStation().getName());
				stationIO.setCode(stationPointDTO.getStation().getCode());
				stationPointIO.setLatitude(stationPointDTO.getLatitude() == null ? "" : stationPointDTO.getLatitude());
				stationPointIO.setLongitude(stationPointDTO.getLongitude() == null ? "" : stationPointDTO.getLongitude());
				stationPointIO.setCode(stationPointDTO.getCode());
				stationPointIO.setName(stationPointDTO.getName());
				stationPointIO.setLandmark(stationPointDTO.getLandmark());
				stationPointIO.setAddress(stationPointDTO.getAddress());
				stationPointIO.setNumber(stationPointDTO.getNumber());
				stationPointIO.setMapUrl(stationPointDTO.getMapUrl());

				List<BaseIO> amentiesList = new ArrayList<BaseIO>();
				for (StationPointAmenitiesEM amentiesEM : StationPointAmenitiesEM.getStationPointAmenitiesFromCodes(stationPointDTO.getAmenities())) {
					BaseIO amenitiesIO = new BaseIO();
					amenitiesIO.setCode(amentiesEM.getCode());
					amenitiesIO.setName(amentiesEM.getName());
					amentiesList.add(amenitiesIO);
				}
				stationPointIO.setAmenities(amentiesList);
				stationPointIO.setActiveFlag(stationPointDTO.getActiveFlag());
				stationPointIO.setStation(stationIO);
				stations.add(stationPointIO);
			}

		}
		return ResponseIO.success(stations);
	}

	@RequestMapping(value = "/{stationCode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<StationPointIO>> getStations(@PathVariable("authtoken") String authtoken, @PathVariable("stationCode") String stationCode) throws Exception {
		List<StationPointIO> stations = new ArrayList<StationPointIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			StationPointDTO pointDTO = new StationPointDTO();
			StationDTO stationDTO = new StationDTO();
			stationDTO.setCode(stationCode);
			pointDTO.setStation(stationDTO);
			List<StationPointDTO> list = (List<StationPointDTO>) pointService.get(authDTO, pointDTO);
			for (StationPointDTO stationPointDTO : list) {
				StationPointIO stationPointIO = new StationPointIO();
				StationIO stationIO = new StationIO();
				stationIO.setName(stationPointDTO.getStation().getName());
				stationIO.setCode(stationPointDTO.getStation().getCode());
				stationPointIO.setLatitude(stationPointDTO.getLatitude() == null ? "" : stationPointDTO.getLatitude());
				stationPointIO.setLongitude(stationPointDTO.getLongitude() == null ? "" : stationPointDTO.getLongitude());
				stationPointIO.setCode(stationPointDTO.getCode());
				stationPointIO.setName(stationPointDTO.getName());
				stationPointIO.setLandmark(stationPointDTO.getLandmark());
				stationPointIO.setAddress(stationPointDTO.getAddress());
				stationPointIO.setNumber(stationPointDTO.getNumber());
				stationPointIO.setMapUrl(stationPointDTO.getMapUrl());
				List<BaseIO> amentiesList = new ArrayList<BaseIO>();
				for (StationPointAmenitiesEM amentiesEM : StationPointAmenitiesEM.getStationPointAmenitiesFromCodes(stationPointDTO.getAmenities())) {
					BaseIO amenitiesIO = new BaseIO();
					amenitiesIO.setCode(amentiesEM.getCode());
					amenitiesIO.setName(amentiesEM.getName());
					amentiesList.add(amenitiesIO);
				}
				stationPointIO.setAmenities(amentiesList);
				stationPointIO.setActiveFlag(stationPointDTO.getActiveFlag());
				stationPointIO.setStation(stationIO);
				stations.add(stationPointIO);
			}
		}
		return ResponseIO.success(stations);
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<StationPointIO> getStationPointUID(@PathVariable("authtoken") String authtoken, @RequestBody StationPointIO stationPointIO) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		StationPointIO pointIO = new StationPointIO();
		if (authDTO != null) {
			StationPointDTO stationPointDTO = new StationPointDTO();
			StationDTO stationDTO = new StationDTO();
			stationDTO.setCode(stationPointIO.getStation() != null ? stationPointIO.getStation().getCode() : null);
			stationPointDTO.setStation(stationDTO);
			stationPointDTO.setCode(stationPointIO.getCode());
			stationPointDTO.setName(stationPointIO.getName());
			stationPointDTO.setLatitude(stationPointIO.getLatitude());
			stationPointDTO.setLongitude(stationPointIO.getLongitude());
			stationPointDTO.setAddress(stationPointIO.getAddress());
			stationPointDTO.setLandmark(stationPointIO.getLandmark());
			stationPointDTO.setNumber(stationPointIO.getNumber());
			stationPointDTO.setMapUrl(stationPointIO.getMapUrl());

			List<StationPointAmenitiesEM> amentiesList = new ArrayList<StationPointAmenitiesEM>();
			if (stationPointIO.getAmenities() != null) {
				for (BaseIO amenitiesIO : stationPointIO.getAmenities()) {
					StationPointAmenitiesEM amentiesEM = StationPointAmenitiesEM.getStationPointAmenitiesEM(amenitiesIO.getCode());
					amentiesList.add(amentiesEM);
				}
			}
			stationPointDTO.setAmenities(StationPointAmenitiesEM.getStationPointAmenitiesCodes(amentiesList));
			stationPointDTO.setActiveFlag(stationPointIO.getActiveFlag());
			pointService.Update(authDTO, stationPointDTO);
			pointIO.setCode(stationPointDTO.getCode());
			pointIO.setActiveFlag(stationPointDTO.getActiveFlag());
		}
		return ResponseIO.success(pointIO);
	}

	@RequestMapping(value = "/amenities", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<BaseIO>> getStationPointAmenities(@PathVariable("authtoken") String authtoken) throws Exception {
		List<BaseIO> amenitiesList = new ArrayList<>();
		authService.getAuthDTO(authtoken);
		for (StationPointAmenitiesEM amenitiesEM : StationPointAmenitiesEM.values()) {
			if (amenitiesEM.getCode().equals(StationPointAmenitiesEM.NOT_AVAILABLE.getCode())) {
				continue;
			}
			BaseIO amenities = new BaseIO();
			amenities.setCode(amenitiesEM.getCode());
			amenities.setName(amenitiesEM.getName());
			amenitiesList.add(amenities);
		}
		return ResponseIO.success(amenitiesList);
	}
}
