package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.in.com.controller.web.io.AmenitiesIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.dto.AmenitiesDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.service.AmentiesService;
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
@RequestMapping("/{authtoken}/amenties")
public class AmentiesController extends BaseController {
	@Autowired
	AmentiesService amentiesService;

	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<AmenitiesIO>> getAllAmenties(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "-1") int activeFlag) throws Exception {
		List<AmenitiesIO> amenties = new ArrayList<AmenitiesIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<AmenitiesDTO> list = (List<AmenitiesDTO>) amentiesService.getAll(authDTO);
			for (AmenitiesDTO amentiesDTO : list) {
				if (activeFlag != -1 && activeFlag != amentiesDTO.getActiveFlag()) {
					continue;
				}
				AmenitiesIO amentiesio = new AmenitiesIO();
				amentiesio.setCode(amentiesDTO.getCode());
				amentiesio.setName(amentiesDTO.getName());
				amentiesio.setActiveFlag(amentiesDTO.getActiveFlag());
				amentiesio.setCode(amentiesDTO.getCode());
				amenties.add(amentiesio);
			}
		}
		return ResponseIO.success(amenties);
	}

	@RequestMapping(value = "/zonesync", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<AmenitiesIO>> getAllforZoneSync(@PathVariable("authtoken") String authtoken, String syncDate) throws Exception {
		List<AmenitiesIO> amenties = new ArrayList<AmenitiesIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<AmenitiesDTO> list = (List<AmenitiesDTO>) amentiesService.getAllforZoneSync(authDTO, syncDate);
			for (AmenitiesDTO amentiesDTO : list) {
				AmenitiesIO amentiesio = new AmenitiesIO();
				amentiesio.setCode(amentiesDTO.getCode());
				amentiesio.setName(amentiesDTO.getName());
				amentiesio.setActiveFlag(amentiesDTO.getActiveFlag());
				amentiesio.setCode(amentiesDTO.getCode());
				amenties.add(amentiesio);
			}
		}
		return ResponseIO.success(amenties);
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<AmenitiesIO> getNamespaceStationUID(@PathVariable("authtoken") String authtoken, @RequestBody AmenitiesIO amenties) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		AmenitiesIO amentiesIO = new AmenitiesIO();
		if (authDTO != null) {
			AmenitiesDTO amentiesDTO = new AmenitiesDTO();
			amentiesDTO.setCode(amenties.getCode());
			amentiesDTO.setName(amenties.getName());
			amentiesDTO.setActiveFlag(amenties.getActiveFlag());
			amentiesService.Update(authDTO, amentiesDTO);
			amentiesIO.setName(amentiesDTO.getName());
			amentiesIO.setCode(amentiesDTO.getCode());
			amentiesIO.setActiveFlag(amentiesDTO.getActiveFlag());
		}
		return ResponseIO.success(amentiesIO);

	}

}
