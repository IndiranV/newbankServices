package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.in.com.constants.Text;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.StateIO;
import org.in.com.controller.web.io.StationAreaIO;
import org.in.com.controller.web.io.StationIO;
import org.in.com.controller.web.io.StationOtaPartnerIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.StateDTO;
import org.in.com.dto.StationAreaDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationOtaPartnerDTO;
import org.in.com.dto.enumeration.OTAPartnerEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.StationService;
import org.in.com.service.ZoneSyncService;
import org.in.com.utils.StringUtil;
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
@RequestMapping("/{authtoken}/stations")
public class StationController extends BaseController {
	@Autowired
	StationService stationService;
	@Autowired
	ZoneSyncService zoneSyncService;

	@RequestMapping(value = "/{statecode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<StationIO>> getStations(@PathVariable("authtoken") String authtoken, @PathVariable("statecode") String statecode, @RequestParam(required = false, defaultValue = "-1") int activeFlag) throws Exception {
		List<StationIO> stations = new ArrayList<StationIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			StationDTO dto = new StationDTO();
			StateDTO stateDTO = new StateDTO();
			stateDTO.setCode(statecode);
			dto.setState(stateDTO);
			List<StationDTO> list = (List<StationDTO>) stationService.getAll(authDTO, stateDTO);
			for (StationDTO stationDTO : list) {
				if (activeFlag != -1 && activeFlag != stationDTO.getActiveFlag()) {
					continue;
				}
				StationIO stationio = new StationIO();
				stationio.setCode(stationDTO.getCode());
				stationio.setName(stationDTO.getName());
				stationio.setLatitude(StringUtil.isNull(stationDTO.getLatitude(), Text.EMPTY));
				stationio.setLongitude(StringUtil.isNull(stationDTO.getLongitude(), Text.EMPTY));
				stationio.setRadius(stationDTO.getRadius());
				stationio.setApiFlag(stationDTO.getApiFlag());
				stationio.setActiveFlag(stationDTO.getActiveFlag());
				stations.add(stationio);
			}

		}
		return ResponseIO.success(stations);
	}

	@RequestMapping(value = "/zonesync", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<StationIO>> getAllforZoneSync(@PathVariable("authtoken") String authtoken, String syncDate) throws Exception {
		List<StationIO> stations = new ArrayList<StationIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<StationDTO> list = (List<StationDTO>) stationService.getAllforZoneSync(authDTO, syncDate);
			for (StationDTO stationDTO : list) {
				StationIO stationio = new StationIO();
				stationio.setCode(stationDTO.getCode());
				stationio.setName(stationDTO.getName());
				stationio.setLatitude(StringUtil.isNull(stationDTO.getLatitude(), Text.EMPTY));
				stationio.setLongitude(StringUtil.isNull(stationDTO.getLongitude(), Text.EMPTY));
				stationio.setRadius(stationDTO.getRadius());
				stationio.setApiFlag(stationDTO.getApiFlag());
				stationio.setActiveFlag(stationDTO.getActiveFlag());
				StateIO state = new StateIO();
				state.setCode(stationDTO.getState().getCode());
				state.setName(stationDTO.getState().getName());
				stationio.setState(state);
				stations.add(stationio);
			}

		}
		return ResponseIO.success(stations);
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<StationIO> getStationUID(@PathVariable("authtoken") String authtoken, @RequestBody StationIO station) throws Exception {
		StationIO stationIO = new StationIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			StationDTO stationDTO = new StationDTO();
			StateDTO stateDTO = new StateDTO();
			stateDTO.setCode(station.getState().getCode());
			stationDTO.setCode(station.getCode());
			stationDTO.setName(station.getName());
			stationDTO.setLatitude(station.getLatitude());
			stationDTO.setLongitude(station.getLongitude());
			stationDTO.setRadius(station.getRadius());
			stationDTO.setApiFlag(station.getApiFlag());
			stationDTO.setActiveFlag(station.getActiveFlag());
			stationDTO.setState(stateDTO);
			stationService.Update(authDTO, stationDTO);
			stationIO.setCode(stationDTO.getCode());
			stationIO.setActiveFlag(stationDTO.getActiveFlag());
		}
		return ResponseIO.success(stationIO);
	}

	@RequestMapping(value = "/namespace", method = RequestMethod.POST)
	@ResponseBody
	public ResponseIO<List<StationIO>> getAllStations(@PathVariable("authtoken") String authtoken) throws Exception {
		List<StationIO> stations = new ArrayList<StationIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<StationDTO> list = stationService.getAll(authDTO);
			for (StationDTO stationDTO : list) {
				StationIO stationio = new StationIO();
				stationio.setCode(stationDTO.getCode());
				stationio.setName(stationDTO.getName());
				stationio.setActiveFlag(stationDTO.getActiveFlag());
				stationio.setLatitude(StringUtil.isNull(stationDTO.getLatitude(), Text.EMPTY));
				stationio.setLongitude(StringUtil.isNull(stationDTO.getLongitude(), Text.EMPTY));
				stationio.setRadius(stationDTO.getRadius());
				StateIO state = new StateIO();
				state.setCode(stationDTO.getState().getCode());
				state.setName(stationDTO.getState().getName());
				stationio.setState(state);

				List<BaseIO> relatedStationList = new ArrayList<>();
				for (StationDTO relatedStationDTO : stationDTO.getList()) {
					BaseIO relatedStation = new BaseIO();
					relatedStation.setCode(relatedStationDTO.getCode());
					relatedStation.setName(relatedStationDTO.getName());
					relatedStationList.add(relatedStation);
				}
				stationio.setRelatedStations(relatedStationList);

				stations.add(stationio);
			}
		}
		return ResponseIO.success(stations);
	}

	@RequestMapping(value = "/namespace/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<StationIO> getNamespaceStationUID(@PathVariable("authtoken") String authtoken, @RequestBody StationIO station) throws Exception {
		StationIO stationIO = new StationIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			StationDTO stationDTO = new StationDTO();
			stationDTO.setCode(station.getCode());
			stationDTO.setActiveFlag(station.getActiveFlag());

			List<StationDTO> relatedStations = new ArrayList<>();
			if (station.getRelatedStations() != null) {
				for (BaseIO relatedStationObj : station.getRelatedStations()) {
					StationDTO relatedStation = new StationDTO();
					relatedStation.setCode(relatedStationObj.getCode());
					relatedStation.setActiveFlag(1);
					relatedStations.add(relatedStation);
				}
			}
			stationDTO.setList(relatedStations);

			stationService.updateNamespace(authDTO, stationDTO);
			stationIO.setCode(stationDTO.getCode());
			stationIO.setActiveFlag(stationDTO.getActiveFlag());
		}
		return ResponseIO.success(stationIO);
	}

	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<StationIO>> getAllStation(@PathVariable("authtoken") String authtoken) throws Exception {
		List<StationIO> stationList = new ArrayList<StationIO>();
		authService.getAuthDTO(authtoken);
		List<StationDTO> list = stationService.getAllStations();
		for (StationDTO stationDTO : list) {
			StationIO stationIO = new StationIO();
			stationIO.setCode(stationDTO.getCode());
			stationIO.setName(stationDTO.getName());
			stationIO.setLatitude(StringUtil.isNull(stationDTO.getLatitude(), Text.EMPTY));
			stationIO.setLongitude(StringUtil.isNull(stationDTO.getLongitude(), Text.EMPTY));
			stationIO.setRadius(stationDTO.getRadius());
			stationIO.setActiveFlag(stationDTO.getActiveFlag());

			StateIO state = new StateIO();
			state.setCode(stationDTO.getState().getCode());
			state.setName(stationDTO.getState().getName());
			stationIO.setState(state);
			stationList.add(stationIO);
		}
		return ResponseIO.success(stationList);
	}

	@RequestMapping(value = "/ota/{partnerCode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<StationOtaPartnerIO>> getOTAStation(@PathVariable("authtoken") String authtoken, @PathVariable("partnerCode") String partnerCode, String stateCode) throws Exception {
		List<StationOtaPartnerIO> stationOtaPartners = new ArrayList<>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		StationOtaPartnerDTO stationOtaPartnerDTO = new StationOtaPartnerDTO();
		StateDTO stateDTO = new StateDTO();
		stateDTO.setCode(stateCode);
		stationOtaPartnerDTO.setState(stateDTO);
		stationOtaPartnerDTO.setOtaPartner(OTAPartnerEM.getOtaPartnerEM(partnerCode));

		if (stationOtaPartnerDTO.getOtaPartner() == null) {
			throw new ServiceException(ErrorCode.INVALID_CODE, "Parter Not found");
		}

		List<StationOtaPartnerDTO> list = stationService.getStationOtaPartners(authDTO, stationOtaPartnerDTO);
		for (StationOtaPartnerDTO stationOtaPartnerDTO2 : list) {

			List<BaseIO> stations = new ArrayList<>();
			for (StationDTO stationDTO : stationOtaPartnerDTO2.getStations()) {
				BaseIO stationio = new BaseIO();
				stationio.setCode(stationDTO.getCode());
				stationio.setName(stationDTO.getName());
				stationio.setActiveFlag(stationDTO.getActiveFlag());
				stations.add(stationio);
			}

			StationOtaPartnerIO stationOtaPartnerIO = new StationOtaPartnerIO();
			stationOtaPartnerIO.setCode(stationOtaPartnerDTO2.getCode());
			stationOtaPartnerIO.setOtaStationCode(stationOtaPartnerDTO2.getOtaStationCode());
			stationOtaPartnerIO.setOtaStationName(stationOtaPartnerDTO2.getOtaStationName());

			stationOtaPartnerIO.setActiveFlag(stationOtaPartnerDTO2.getActiveFlag());
			stationOtaPartnerIO.setStations(stations);
			stationOtaPartners.add(stationOtaPartnerIO);
		}

		return ResponseIO.success(stationOtaPartners);
	}

	@RequestMapping(value = "/ota/v2/{partnerCode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<StationOtaPartnerIO>> getOTAStationV2(@PathVariable("authtoken") String authtoken, @PathVariable("partnerCode") String partnerCode, String stateCode) throws Exception {
		List<StationOtaPartnerIO> stationOtaPartners = new ArrayList<StationOtaPartnerIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		StationOtaPartnerDTO stationOtaPartnerDTO = new StationOtaPartnerDTO();
		StateDTO stateDTO = new StateDTO();
		stateDTO.setCode(stateCode);
		stationOtaPartnerDTO.setState(stateDTO);
		stationOtaPartnerDTO.setOtaPartner(OTAPartnerEM.getOtaPartnerEM(partnerCode));

		if (stationOtaPartnerDTO.getOtaPartner() == null) {
			throw new ServiceException(ErrorCode.INVALID_CODE, "Parter Not found");
		}

		List<StationOtaPartnerDTO> list = stationService.getStationOtaPartnersV2(authDTO, stationOtaPartnerDTO);
		for (StationOtaPartnerDTO stationOtaPartnerDTO2 : list) {

			List<StationOtaPartnerIO> otaStations = new ArrayList<StationOtaPartnerIO>();
			for (StationDTO stationDTO : stationOtaPartnerDTO2.getStations()) {
				StationOtaPartnerIO otaStationIO = new StationOtaPartnerIO();
				otaStationIO.setCode(stationDTO.getCode().split(Text.COMMA)[0]);
				otaStationIO.setOtaStationCode(stationDTO.getCode().split(Text.COMMA)[1]);
				otaStationIO.setOtaStationName(stationDTO.getName());
				otaStationIO.setActiveFlag(stationDTO.getActiveFlag());
				otaStations.add(otaStationIO);
			}

			StationOtaPartnerIO stationOtaPartnerIO = new StationOtaPartnerIO();
			stationOtaPartnerIO.setCode(stationOtaPartnerDTO2.getOtaStationCode());
			stationOtaPartnerIO.setName(stationOtaPartnerDTO2.getOtaStationName());
			stationOtaPartnerIO.setActiveFlag(stationOtaPartnerDTO2.getActiveFlag());
			stationOtaPartnerIO.setOtaStations(otaStations);
			stationOtaPartners.add(stationOtaPartnerIO);
		}

		return ResponseIO.success(stationOtaPartners);
	}

	@RequestMapping(value = "/ota/{partnerCode}/station", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<StationOtaPartnerIO>> getOTAStationByStation(@PathVariable("authtoken") String authtoken, @PathVariable("partnerCode") String partnerCode, String stationCodes) throws Exception {
		List<StationOtaPartnerIO> stationOtaPartners = new ArrayList<>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		StationOtaPartnerDTO stationOtaPartnerDTO = new StationOtaPartnerDTO();
		stationOtaPartnerDTO.setOtaPartner(OTAPartnerEM.getOtaPartnerEM(partnerCode));

		List<StationDTO> stations = new ArrayList<>();
		if (StringUtil.isNotNull(stationCodes)) {
			for (String stationCode : Arrays.asList(stationCodes.split(Text.COMMA))) {
				if (StringUtil.isNull(stationCode)) {
					continue;
				}
				StationDTO stationDTO = new StationDTO();
				stationDTO.setCode(stationCode);
				stations.add(stationDTO);
			}
		}
		stationOtaPartnerDTO.setStations(stations);

		if (stationOtaPartnerDTO.getOtaPartner() == null) {
			throw new ServiceException(ErrorCode.INVALID_CODE, "Parter Not found");
		}

		if (stationOtaPartnerDTO.getStations().isEmpty()) {
			throw new ServiceException(ErrorCode.INVALID_STATION, "station code Not found");
		}

		List<StationOtaPartnerDTO> list = stationService.getOtaStation(authDTO, stationOtaPartnerDTO);
		for (StationOtaPartnerDTO stationOtaPartnerDTO2 : list) {
			StationOtaPartnerIO stationOtaPartnerIO = new StationOtaPartnerIO();
			stationOtaPartnerIO.setCode(stationOtaPartnerDTO2.getCode());
			stationOtaPartnerIO.setOtaStationCode(stationOtaPartnerDTO2.getOtaStationCode());
			stationOtaPartnerIO.setOtaStationName(stationOtaPartnerDTO2.getOtaStationName());

			List<BaseIO> stationList = new ArrayList<>();
			for (StationDTO stationDTO : stationOtaPartnerDTO2.getStations()) {
				BaseIO stationIO = new BaseIO();
				stationIO.setCode(stationDTO.getCode());
				stationIO.setName(stationDTO.getName());
				stationIO.setActiveFlag(stationDTO.getActiveFlag());
				stationList.add(stationIO);
			}
			StateIO state = new StateIO();
			state.setCode(stationOtaPartnerDTO2.getState().getCode());
			state.setName(stationOtaPartnerDTO2.getState().getName());
			stationOtaPartnerIO.setState(state);
			stationOtaPartnerIO.setStations(stationList);
			stationOtaPartnerIO.setActiveFlag(stationOtaPartnerDTO2.getActiveFlag());
			stationOtaPartners.add(stationOtaPartnerIO);
		}

		return ResponseIO.success(stationOtaPartners);
	}

	@RequestMapping(value = "/ota/{partnerCode}/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> updateStationOtaPartner(@PathVariable("authtoken") String authtoken, @PathVariable("partnerCode") String partnerCode, @RequestBody StationOtaPartnerIO stationOtaPartner) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		StationOtaPartnerDTO stationOtaPartnerDTO = new StationOtaPartnerDTO();
		stationOtaPartnerDTO.setCode(stationOtaPartner.getCode());

		List<StationDTO> stations = new ArrayList<>();
		for (BaseIO stationIO : stationOtaPartner.getStations()) {
			if (StringUtil.isNull(stationIO.getCode())) {
				continue;
			}
			StationDTO stationDTO = new StationDTO();
			stationDTO.setCode(stationIO.getCode());
			stations.add(stationDTO);
		}
		stationOtaPartnerDTO.setStations(stations);

		stationService.updateStationOtaPartner(authDTO, stationOtaPartnerDTO);
		return ResponseIO.success();
	}

	@RequestMapping(value = "/ota/{partnerCode}/update/v2", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> updateStationOtaPartnerV2(@PathVariable("authtoken") String authtoken, @PathVariable("partnerCode") String partnerCode, @RequestBody StationOtaPartnerIO stationOtaPartner) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		StationOtaPartnerDTO stationOtaPartnerDTO = new StationOtaPartnerDTO();
		stationOtaPartnerDTO.setCode(stationOtaPartner.getCode());

		List<StationOtaPartnerDTO> stations = new ArrayList<StationOtaPartnerDTO>();
		for (StationOtaPartnerIO otaStationIO : stationOtaPartner.getOtaStations()) {
			if (StringUtil.isNull(otaStationIO.getCode())) {
				continue;
			}
			StationOtaPartnerDTO otaStationDTO = new StationOtaPartnerDTO();
			otaStationDTO.setCode(otaStationIO.getCode());
			otaStationDTO.setActiveFlag(otaStationIO.getActiveFlag());
			stations.add(otaStationDTO);
		}
		stationOtaPartnerDTO.setOtaStations(stations);

		stationService.updateStationOtaPartnerV2(authDTO, stationOtaPartnerDTO);
		return ResponseIO.success();
	}

	@RequestMapping(value = "/ota/station/add", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> addStationOtaPartner(@PathVariable("authtoken") String authtoken, @RequestBody StationOtaPartnerIO stationOtaPartner) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		StationOtaPartnerDTO stationOta = new StationOtaPartnerDTO();

		stationOta.setOtaPartner(OTAPartnerEM.getOtaPartnerEM(stationOtaPartner.getOtaPartner().getCode()));
		stationOta.setOtaStationCode(stationOtaPartner.getOtaStationCode());
		stationOta.setOtaStationName(stationOtaPartner.getOtaStationName());

		StateDTO state = new StateDTO();
		state.setCode(stationOtaPartner.getState().getCode());
		stationOta.setState(state);
		if (stationOta.getOtaPartner() == null || StringUtil.isNull(stationOta.getOtaStationCode()) || StringUtil.isNull(stationOta.getOtaStationName())) {
			throw new ServiceException(ErrorCode.REQURIED_FIELD_SHOULD_NOT_NULL);
		}
		stationService.addStationOtaPartner(authDTO, stationOta);
		BaseIO base = new BaseIO();
		base.setCode(stationOta.getCode());
		return ResponseIO.success(base);
	}

	@RequestMapping(value = "/ota/zonesync", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<StationOtaPartnerIO>> getZoneSync(@PathVariable("authtoken") String authtoken, String syncDate) throws Exception {
		List<StationOtaPartnerIO> stationOtaPartners = new ArrayList<>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		List<StationOtaPartnerDTO> list = stationService.getAllStationOtaforZoneSync(authDTO, syncDate);
		for (StationOtaPartnerDTO stationOtaPartnerDTO2 : list) {

			List<BaseIO> stations = new ArrayList<>();
			for (StationDTO stationDTO : stationOtaPartnerDTO2.getStations()) {
				BaseIO stationio = new BaseIO();
				stationio.setCode(stationDTO.getCode());
				stationio.setName(stationDTO.getName());
				stationio.setActiveFlag(stationDTO.getActiveFlag());
				stations.add(stationio);
			}

			StationOtaPartnerIO stationOtaPartnerIO = new StationOtaPartnerIO();
			stationOtaPartnerIO.setCode(stationOtaPartnerDTO2.getCode());
			stationOtaPartnerIO.setOtaStationCode(stationOtaPartnerDTO2.getOtaStationCode());
			stationOtaPartnerIO.setOtaStationName(stationOtaPartnerDTO2.getOtaStationName());

			BaseIO state = new BaseIO();
			state.setCode(stationOtaPartnerDTO2.getState().getCode());
			state.setName(stationOtaPartnerDTO2.getState().getName());
			stationOtaPartnerIO.setState(state);

			stationOtaPartnerIO.setActiveFlag(stationOtaPartnerDTO2.getActiveFlag());
			stationOtaPartnerIO.setStations(stations);

			BaseIO otaPartner = new BaseIO();
			otaPartner.setCode(stationOtaPartnerDTO2.getOtaPartner().getCode());
			stationOtaPartnerIO.setOtaPartner(otaPartner);

			stationOtaPartners.add(stationOtaPartnerIO);
		}

		return ResponseIO.success(stationOtaPartners);
	}

	@RequestMapping(value = "/area/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<StationIO> updateStationArea(@PathVariable("authtoken") String authtoken, @RequestBody StationAreaIO stationArea) throws Exception {
		StationIO stationIO = new StationIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		StationAreaDTO stationAreaDTO = new StationAreaDTO();
		stationAreaDTO.setCode(stationArea.getCode());
		stationAreaDTO.setName(stationArea.getName());
		stationAreaDTO.setLatitude(stationArea.getLatitude());
		stationAreaDTO.setLongitude(stationArea.getLongitude());
		stationAreaDTO.setRadius(stationArea.getRadius());
		stationAreaDTO.setActiveFlag(stationArea.getActiveFlag());

		StationDTO stationDTO = new StationDTO();
		stationDTO.setCode(stationArea.getStation().getCode());
		stationAreaDTO.setStation(stationDTO);

		stationService.updateStationArea(authDTO, stationAreaDTO);

		stationIO.setCode(stationAreaDTO.getCode());
		stationIO.setActiveFlag(stationAreaDTO.getActiveFlag());
		return ResponseIO.success(stationIO);
	}

	@RequestMapping(value = "/area/{stationAreaCode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<StationAreaIO>> getStationAreas(@PathVariable("authtoken") String authtoken, @PathVariable("stationAreaCode") String stationAreaCode) throws Exception {
		List<StationAreaIO> stations = new ArrayList<StationAreaIO>();
		authService.getAuthDTO(authtoken);

		StationAreaDTO stationAreaDTO = new StationAreaDTO();
		stationAreaDTO.setCode(stationAreaCode);

		stationService.getStationArea(stationAreaDTO);

		StationAreaIO stationAreaIO = new StationAreaIO();
		stationAreaIO.setCode(stationAreaDTO.getCode());
		stationAreaIO.setName(stationAreaDTO.getName());
		stationAreaIO.setLatitude(StringUtil.isNull(stationAreaDTO.getLatitude(), Text.EMPTY));
		stationAreaIO.setLongitude(StringUtil.isNull(stationAreaDTO.getLongitude(), Text.EMPTY));
		stationAreaIO.setRadius(stationAreaDTO.getRadius());

		StationIO stationIO = new StationIO();
		stationIO.setCode(stationAreaDTO.getStation().getCode());
		stationIO.setName(stationAreaDTO.getStation().getName());
		stationIO.setActiveFlag(stationAreaDTO.getStation().getActiveFlag());
		stationAreaIO.setStation(stationIO);

		stationAreaIO.setActiveFlag(stationAreaDTO.getActiveFlag());

		return ResponseIO.success(stations);
	}

	@RequestMapping(value = "/{stationCode}/area", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<StationAreaIO>> getStationAreasByStation(@PathVariable("authtoken") String authtoken, @PathVariable("stationCode") String stationCode) throws Exception {
		List<StationAreaIO> stationAreas = new ArrayList<StationAreaIO>();
		authService.getAuthDTO(authtoken);

		StationDTO stationDTO = new StationDTO();
		stationDTO.setCode(stationCode);

		List<StationAreaDTO> list = stationService.getStationAreas(stationDTO);
		for (StationAreaDTO stationArea : list) {
			StationAreaIO stationAreaIO = new StationAreaIO();
			stationAreaIO.setCode(stationArea.getCode());
			stationAreaIO.setName(stationArea.getName());
			stationAreaIO.setLatitude(StringUtil.isNull(stationArea.getLatitude(), Text.EMPTY));
			stationAreaIO.setLongitude(StringUtil.isNull(stationArea.getLongitude(), Text.EMPTY));
			stationAreaIO.setRadius(stationArea.getRadius());

			StationIO stationIO = new StationIO();
			stationIO.setCode(stationArea.getStation().getCode());
			stationIO.setName(stationArea.getStation().getName());
			stationIO.setActiveFlag(stationArea.getStation().getActiveFlag());
			stationAreaIO.setStation(stationIO);

			stationAreaIO.setActiveFlag(stationArea.getActiveFlag());
			stationAreas.add(stationAreaIO);
		}
		return ResponseIO.success(stationAreas);
	}

	@RequestMapping(value = "/area/zonesync", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<StationAreaIO>> getStationAreasForZoneSync(@PathVariable("authtoken") String authtoken, String syncDate) throws Exception {
		List<StationAreaIO> stationAreas = new ArrayList<StationAreaIO>();
		authService.getAuthDTO(authtoken);

		List<StationAreaDTO> list = stationService.getStationAreasForZoneSync(syncDate);
		for (StationAreaDTO stationArea : list) {
			StationAreaIO stationAreaIO = new StationAreaIO();
			stationAreaIO.setCode(stationArea.getCode());
			stationAreaIO.setName(stationArea.getName());
			stationAreaIO.setLatitude(StringUtil.isNull(stationArea.getLatitude(), Text.EMPTY));
			stationAreaIO.setLongitude(StringUtil.isNull(stationArea.getLongitude(), Text.EMPTY));
			stationAreaIO.setRadius(stationArea.getRadius());

			StationIO stationIO = new StationIO();
			stationIO.setCode(stationArea.getStation().getCode());
			stationIO.setName(stationArea.getStation().getName());
			stationIO.setActiveFlag(stationArea.getStation().getActiveFlag());
			stationAreaIO.setStation(stationIO);

			stationAreaIO.setActiveFlag(stationArea.getActiveFlag());
			stationAreas.add(stationAreaIO);
		}
		return ResponseIO.success(stationAreas);
	}

	@RequestMapping(value = "/area/sync", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<StationAreaIO>> syncStationAreas(@PathVariable("authtoken") String authtoken, String syncDate) throws Exception {
		List<StationAreaIO> stationAreas = new ArrayList<StationAreaIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		List<StationAreaDTO> list = zoneSyncService.syncStationArea(authDTO);
		for (StationAreaDTO stationArea : list) {
			StationAreaIO stationAreaIO = new StationAreaIO();
			stationAreaIO.setCode(stationArea.getCode());
			stationAreaIO.setName(stationArea.getName());
			stationAreaIO.setLatitude(StringUtil.isNull(stationArea.getLatitude(), Text.EMPTY));
			stationAreaIO.setLongitude(StringUtil.isNull(stationArea.getLongitude(), Text.EMPTY));
			stationAreaIO.setRadius(stationArea.getRadius());

			StationIO stationIO = new StationIO();
			stationIO.setCode(stationArea.getStation().getCode());
			stationIO.setName(stationArea.getStation().getName());
			stationIO.setActiveFlag(stationArea.getStation().getActiveFlag());
			stationAreaIO.setStation(stationIO);

			stationAreaIO.setActiveFlag(stationArea.getActiveFlag());
			stationAreas.add(stationAreaIO);
		}
		return ResponseIO.success(stationAreas);
	}
}
