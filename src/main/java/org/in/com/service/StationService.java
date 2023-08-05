package org.in.com.service;

import java.util.List;
import java.util.Map;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.StateDTO;
import org.in.com.dto.StationAreaDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationOtaPartnerDTO;
import org.in.com.dto.StationPointDTO;

public interface StationService extends BaseService<StationDTO> {

	public StationDTO getStation(StationDTO station);

	public List<StationDTO> getAll(AuthDTO authDTO, StateDTO stateDTO);

	public List<StationDTO> getAllforZoneSync(AuthDTO authDTO, String syncDate);

	public List<RouteDTO> getRoute(AuthDTO authDTO);

	public RouteDTO updateRoute(AuthDTO authDTO, RouteDTO routeDTO);

	public StationDTO updateNamespace(AuthDTO authDTO, StationDTO stationDTO);

	public List<StationDTO> getCommerceStation(AuthDTO authDTO);

	public List<StationPointDTO> getCommerceALLStationAndPoint(AuthDTO authDTO);

	public Map<String, List<String>> getCommerceRoutes(AuthDTO authDTO);

	public RouteDTO getRouteDTO(AuthDTO authDTO, StationDTO fromStation, StationDTO toStation);

	public List<StationDTO> getAllStations();

	public List<StationDTO> getStationAndStationPoints(AuthDTO authDTO);

	public void updateStationOtaPartner(AuthDTO authDTO, StationOtaPartnerDTO stationOtaPartnerDTO);

	public void updateStationOtaPartnerV2(AuthDTO authDTO, StationOtaPartnerDTO stationOtaPartnerDTO);

	public List<StationOtaPartnerDTO> getStationOtaPartners(AuthDTO authDTO, StationOtaPartnerDTO stationOtaPartnerDTO);

	public List<StationOtaPartnerDTO> getStationOtaPartnersV2(AuthDTO authDTO, StationOtaPartnerDTO stationOtaPartnerDTO);

	public List<StationOtaPartnerDTO> getOtaStation(AuthDTO authDTO, StationOtaPartnerDTO stationOtaPartnerDTO);

	public List<StationOtaPartnerDTO> getAllStationOtaforZoneSync(AuthDTO authDTO, String syncDate);

	public void updateOtaPartner(AuthDTO authDTO, List<StationOtaPartnerDTO> stationOtaPartners);

	public void updateRouteStatus(AuthDTO authDTO, List<RouteDTO> routes, int enableFlag);

	public StationAreaDTO updateStationArea(AuthDTO authDTO, StationAreaDTO stationAreaDTO);

	public List<StationAreaDTO> getStationAreas(StationDTO stationDTO);

	public void getStationArea(StationAreaDTO stationAreaDTO);

	public List<StationAreaDTO> getStationAreasForZoneSync(String syncDate);

	public Map<String, Object> getTopRoutes(AuthDTO authDTO);

	public List<StationDTO> getRelatedStation(AuthDTO authDTO, StationDTO stationDTO);

	public void addStationOtaPartner(AuthDTO authDTO, StationOtaPartnerDTO stationOtaPartner);

}
