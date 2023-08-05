package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.RouteIO;
import org.in.com.controller.web.io.StationIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.StationDTO;
import org.in.com.service.StationService;
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
@RequestMapping("/{authtoken}/routes")
public class RouteController extends BaseController {
	@Autowired
	StationService stationService;

	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<RouteIO>> getRoutes(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "-1") int activeFlag) throws Exception {
		List<RouteIO> routeIOs = new ArrayList<RouteIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<RouteDTO> list = stationService.getRoute(authDTO);
			// Sorting
			Collections.sort(list, new Comparator<RouteDTO>() {
				@Override
				public int compare(RouteDTO r1, RouteDTO r2) {
					return new CompareToBuilder()
							.append(r2.getActiveFlag(), r1.getActiveFlag())
							.append(r1.getFromStation().getName(), r2.getFromStation().getName())
							.append(r1.getToStation().getName(), r2.getToStation().getName()).toComparison();
				}
			});
			
			for (RouteDTO routeDTO : list) {
				if (activeFlag != -1 && activeFlag != routeDTO.getActiveFlag()) {
					continue;
				}
				RouteIO routeIO = new RouteIO();
				StationIO FromStationIO = new StationIO();
				StationIO ToStationIO = new StationIO();
				FromStationIO.setCode(routeDTO.getFromStation().getCode());
				ToStationIO.setCode(routeDTO.getToStation().getCode());
				FromStationIO.setName(routeDTO.getFromStation().getName());
				ToStationIO.setName(routeDTO.getToStation().getName());
				routeIO.setFromStation(FromStationIO);
				routeIO.setToStation(ToStationIO);
				routeIO.setActiveFlag(routeDTO.getActiveFlag());
				routeIO.setCode(routeDTO.getCode());
				routeIO.setMinFare(routeDTO.getMinFare());
				routeIO.setMaxFare(routeDTO.getMaxFare());
				routeIO.setName(routeDTO.getFromStation().getName() + " - " + routeDTO.getToStation().getName());
				routeIOs.add(routeIO);
			}
		}
		return ResponseIO.success(routeIOs);
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<RouteIO> getStationUID(@PathVariable("authtoken") String authtoken, @RequestBody RouteIO routeIO) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		RouteIO routeIO2 = new RouteIO();
		if (authDTO != null) {
			StationDTO fromStationDTO = new StationDTO();
			StationDTO toStationDTO = new StationDTO();
			fromStationDTO.setCode(routeIO.getFromStation() != null ? routeIO.getFromStation().getCode() : null);
			toStationDTO.setCode(routeIO.getToStation() != null ? routeIO.getToStation().getCode() : null);
			RouteDTO routeDTO = new RouteDTO();
			routeDTO.setFromStation(fromStationDTO);
			routeDTO.setToStation(toStationDTO);
			routeDTO.setCode(routeIO.getCode());
			routeDTO.setMinFare(routeIO.getMinFare());
			routeDTO.setMaxFare(routeIO.getMaxFare());
			routeDTO.setActiveFlag(routeIO.getActiveFlag());
			stationService.updateRoute(authDTO, routeDTO);
			routeIO2.setActiveFlag(routeDTO.getActiveFlag());
			routeIO2.setCode(routeDTO.getCode());
		}
		return ResponseIO.success(routeIO2);
	}
}
