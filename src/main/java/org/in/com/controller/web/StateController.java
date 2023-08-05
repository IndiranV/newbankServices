package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.StateIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.StateDTO;
import org.in.com.dto.enumeration.CountryEM;
import org.in.com.dto.enumeration.StateEM;
import org.in.com.service.StateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/{authtoken}/states")
public class StateController extends BaseController {
	@Autowired
	StateService stateService;

	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<StateIO>> getListAllStations(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "-1") int activeFlag) throws Exception {
		List<StateIO> stations = new ArrayList<StateIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<StateDTO> list = (List<StateDTO>) stateService.getAll();
			for (StateDTO stateDTO : list) {
				if (activeFlag != -1 && activeFlag != stateDTO.getActiveFlag()) {
					continue;
				}
				StateIO stateIO = new StateIO();
				stateIO.setCode(stateDTO.getCode());
				stateIO.setName(stateDTO.getName());
				stateIO.setActiveFlag(stateDTO.getActiveFlag());
				
				StateEM state = StateEM.getStateEM(stateDTO.getCode());
				BaseIO country = new StateIO();
				if (state != null) {
					CountryEM countryEM = state.getCountry();
					country.setCode(countryEM.getCode());
					country.setName(countryEM.getName());
				}
				stateIO.setCountry(country);
				
				stations.add(stateIO);
			}
		}
		return ResponseIO.success(stations);
	}
}
