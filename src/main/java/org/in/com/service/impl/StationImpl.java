package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.in.com.cache.EhcacheManager;
import org.in.com.config.ApplicationConfig;
import org.in.com.constants.Constants;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.ScheduleStationDAO;
import org.in.com.dao.StationDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.StateDTO;
import org.in.com.dto.StationAreaDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationOtaPartnerDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.StateService;
import org.in.com.service.StationService;
import org.in.com.utils.StringUtil;
import org.in.com.utils.TokenGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;

import net.sf.ehcache.Element;

@Service
public class StationImpl extends BaseImpl implements StationService {
	@Autowired
	StateService stateService;

	private static final String ROUTE = "route";
	private static final String TOP_ROUTE = "topRoute";
	private static final String STATION_SIZE = "stationSize";
	private static final String ACTUAL_STATION_SIZE = "actualStationSize";

	public StationDTO getStation(StationDTO station) {
		StationDTO stationDTO = null;
		if (station.getId() != 0) {
			stationDTO = getStationDTObyId(station);
		}
		else if (StringUtil.isNotNull(station.getCode())) {
			stationDTO = getStationDTO(station);
		}
		return stationDTO;
	}

	public StationDTO Update(AuthDTO authDTO, StationDTO dto) {
		if (!ArrayUtils.contains(Constants.SUPER_REGIONS_ZONE, ApplicationConfig.getServerZoneCode())) {
			throw new ServiceException(ErrorCode.INVALID_APPLICATION_ZONE);
		}
		StationDAO dao = new StationDAO();
		if (dto.getActiveFlag() == 1) {
			dto.setState(stateService.getState(dto.getState()));
		}
		else if (dto.getActiveFlag() == 2) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED);

		}
		dao.StationUID(authDTO, dto);
		return dto;
	}

	public List<StationDTO> getAll(AuthDTO authDTO) {
		StationDAO dao = new StationDAO();
		List<StationDTO> stations = dao.getNamespaceStations(authDTO);
		for (StationDTO stationDTO : stations) {
			for (StationDTO relatedStation : stationDTO.getList()) {
				relatedStation = getStation(relatedStation);
			}
		}
		return stations;
	}

	public StationDTO updateNamespace(AuthDTO authDTO, StationDTO stationDTO) {
		StationDTO cacheStationDTO = new StationDTO();
		cacheStationDTO.setCode(stationDTO.getCode());
		StationDTO cacheDTO = getStation(cacheStationDTO);
		if (cacheDTO == null || cacheDTO.getId() == 0) {
			throw new ServiceException(201);
		}

		stationDTO.setId(cacheDTO.getId());
		StationDAO dao = new StationDAO();
		ScheduleStationDAO scheduleStationDAO = new ScheduleStationDAO();

		if (stationDTO.getActiveFlag() != 1 && scheduleStationDAO.CheckStationUsed(authDTO, stationDTO)) {
			throw new ServiceException(ErrorCode.STATION_USED_SCHEDULE);
		}

		/** Convert related station id's */
		for (StationDTO relatedStation : stationDTO.getList()) {
			relatedStation = getStation(relatedStation);
		}

		dao.updateNamespaceStation(authDTO, stationDTO);
		String key = authDTO.getNamespace().getCode() + Constants.NAMESPACE_STATION;
		EhcacheManager.getCommerceStaticEhCache().remove(key);

		String relatedStationCacheKey = authDTO.getNamespace().getCode() + Constants.NAMESPACE_STATION + "_" + stationDTO.getCode();
		EhcacheManager.getCommerceStaticEhCache().remove(relatedStationCacheKey);

		String cacheKey = authDTO.getNamespace().getCode() + Constants.TOP_ROUTE;
		EhcacheManager.getCommerceStaticEhCache().remove(cacheKey);
		return stationDTO;
	}

	public RouteDTO updateRoute(AuthDTO authDTO, RouteDTO routeDTO) {
		if (routeDTO.getActiveFlag() == 1 && StringUtil.isNotNull(routeDTO.getFromStation().getCode()) && StringUtil.isNotNull(routeDTO.getToStation().getCode())) {

			StationDTO cacheToDTO = getStation(routeDTO.getToStation());
			StationDTO cacheFromDTO = getStation(routeDTO.getFromStation());
			if (cacheFromDTO.getId() != 0 && cacheToDTO.getId() != 0) {
				routeDTO.setFromStation(cacheFromDTO);
				routeDTO.setToStation(cacheToDTO);
				StationDAO dao = new StationDAO();
				dao.NamespaceRouteUID(authDTO, routeDTO);
			}
		}
		else if (routeDTO.getActiveFlag() != 1 && StringUtil.isNotNull(routeDTO.getCode())) {
			StationDAO dao = new StationDAO();
			dao.NamespaceRouteUID(authDTO, routeDTO);
		}
		else {
			// throw Exception
			throw new ServiceException(201);
		}
		String key = authDTO.getNamespace().getCode() + Constants.NAMESPACE_ROUTE;
		EhcacheManager.getCommerceStaticEhCache().remove(key);

		String cachekey = authDTO.getNamespace().getCode() + Constants.TOP_ROUTE;
		EhcacheManager.getCommerceStaticEhCache().remove(cachekey);
		return routeDTO;
	}

	public List<RouteDTO> getRoute(AuthDTO authDTO) {
		StationDAO dao = new StationDAO();
		List<RouteDTO> list = dao.getNamespaceRoutes(authDTO);
		for (RouteDTO routeDTO : list) {
			routeDTO.setFromStation(getStation(routeDTO.getFromStation()));
			routeDTO.setToStation(getStation(routeDTO.getToStation()));
		}
		return list;
	}

	public List<StationDTO> get(AuthDTO authDTO, StationDTO stationDTO) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<StationDTO> getAll(AuthDTO authDTO, StateDTO stateDTO) {
		StationDAO dao = new StationDAO();
		List<StationDTO> list = (List<StationDTO>) dao.getAllStations(stateDTO);
		return list;
	}

	public List<StationDTO> getAllforZoneSync(AuthDTO authDTO, String syncDate) {
		StationDAO dao = new StationDAO();
		return dao.getAllforZoneSync(syncDate);
	}

	@SuppressWarnings("unchecked")
	public List<StationDTO> getCommerceStation(AuthDTO authDTO) {
		List<StationDTO> list = new ArrayList<StationDTO>();
		String key = authDTO.getNamespaceCode() + Constants.NAMESPACE_STATION;
		Element stationElement = EhcacheManager.getCommerceStaticEhCache().get(key);
		if (stationElement != null) {
			list = (List<StationDTO>) stationElement.getObjectValue();
		}
		else {
			StationDAO dao = new StationDAO();
			list = dao.getNamespaceStations(authDTO);

			/** Convert related station id's */
			Map<String, StationDTO> namespaceStationMap = new HashMap<>();
			for (StationDTO stationDTO : list) {
				if (namespaceStationMap.get(stationDTO.getCode()) == null) {
					namespaceStationMap.put(stationDTO.getCode(), stationDTO);
				}
				for (StationDTO relatedStation : stationDTO.getList()) {
					relatedStation = getStation(relatedStation);
					if (namespaceStationMap.get(relatedStation.getCode()) == null) {
						namespaceStationMap.put(relatedStation.getCode(), relatedStation);
					}
				}

				/** Put related station cache */
				putRelatedStation(authDTO, stationDTO);
			}

			list = new ArrayList<>(namespaceStationMap.values());
			// Sorting
			Comparator<StationDTO> comp = new BeanComparator("name");
			Collections.sort(list, comp);

			Element element = new Element(key, list);
			EhcacheManager.getCommerceStaticEhCache().put(element);
		}
		return list;
	}

	public List<StationDTO> getRelatedStation(AuthDTO authDTO, StationDTO stationDTO) {
		List<StationDTO> relatedStations = new ArrayList<StationDTO>();
		String key = authDTO.getNamespace().getCode() + Constants.NAMESPACE_STATION;
		Element stationElement = EhcacheManager.getCommerceStaticEhCache().get(key + "_" + stationDTO.getCode());
		if (stationElement == null) {
			getCommerceStation(authDTO);
		}

		stationElement = EhcacheManager.getCommerceStaticEhCache().get(key + "_" + stationDTO.getCode());
		if (stationElement != null) {
			relatedStations = (List<StationDTO>) stationElement.getObjectValue();
		}

		return relatedStations;
	}

	private void putRelatedStation(AuthDTO authDTO, StationDTO stationDTO) {
		if (stationDTO.getList() != null && !stationDTO.getList().isEmpty()) {
			String key = authDTO.getNamespaceCode() + Constants.NAMESPACE_STATION + "_" + stationDTO.getCode();

			Element element = new Element(key, stationDTO.getList());
			EhcacheManager.getCommerceStaticEhCache().put(element);
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, List<String>> getCommerceRoutes(AuthDTO authDTO) {
		Map<String, List<String>> Maplist = null;
		String key = authDTO.getNamespaceCode() + Constants.NAMESPACE_ROUTE;
		Element routeElement = EhcacheManager.getCommerceStaticEhCache().get(key);
		if (routeElement != null) {
			Maplist = (Map<String, List<String>>) routeElement.getObjectValue();
		}
		else {
			StationDAO dao = new StationDAO();
			Map<String, List<StationDTO>> daoMaplist = new HashMap<String, List<StationDTO>>();
			List<RouteDTO> Routelist = dao.getNamespaceRoutes(authDTO);

			// Validate Namespace Route has active Station
			List<StationDTO> stationlist = getCommerceStation(authDTO);
			StringBuilder stationCodeBuilder = new StringBuilder();
			for (StationDTO station : stationlist) {
				stationCodeBuilder.append(station.getCode());
				stationCodeBuilder.append(Text.COMMA);
			}
			String stationCodes = stationCodeBuilder.toString();

			// Remove non active routes
			for (Iterator<RouteDTO> iterator = Routelist.iterator(); iterator.hasNext();) {
				RouteDTO routeDTO = iterator.next();
				if (routeDTO.getActiveFlag() != 1) {
					iterator.remove();
					continue;
				}
				routeDTO.setFromStation(getStation(routeDTO.getFromStation()));
				routeDTO.setToStation(getStation(routeDTO.getToStation()));

				if (!stationCodes.contains(routeDTO.getFromStation().getCode()) || !stationCodes.contains(routeDTO.getToStation().getCode())) {
					iterator.remove();
					continue;
				}
			}
			if (Routelist != null && !Routelist.isEmpty()) {
				Maplist = new HashMap<String, List<String>>();
				for (RouteDTO routeDTO : Routelist) {
					if (daoMaplist.get(routeDTO.getFromStation().getCode()) == null) {
						List<StationDTO> valueList = new ArrayList<>();
						valueList.add(routeDTO.getToStation());
						daoMaplist.put(routeDTO.getFromStation().getCode(), valueList);
					}
					else {
						List<StationDTO> valueList = daoMaplist.get(routeDTO.getFromStation().getCode());
						valueList.add(routeDTO.getToStation());
						daoMaplist.put(routeDTO.getFromStation().getCode(), valueList);
					}
				}
				// Sorting routes
				Comparator<StationDTO> comp = new BeanComparator("name");
				for (Map.Entry<String, List<StationDTO>> entry : daoMaplist.entrySet()) {
					Collections.sort(entry.getValue(), comp);
					List<String> list = new ArrayList<>();
					for (StationDTO stationDTO : entry.getValue()) {
						list.add(stationDTO.getCode());
					}
					Maplist.put(entry.getKey(), list);
				}

				Element element = new Element(key, Maplist);
				EhcacheManager.getCommerceStaticEhCache().put(element);
			}
		}
		return Maplist;
	}

	@Override
	public List<StationPointDTO> getCommerceALLStationAndPoint(AuthDTO authDTO) {
		StationDAO dao = new StationDAO();
		List<StationPointDTO> list = dao.getAllStationsAndPoints(authDTO);
		for (StationPointDTO pointDTO : list) {
			pointDTO.setStation(getStation(pointDTO.getStation()));
		}

		return list;
	}

	@Override
	public RouteDTO getRouteDTO(AuthDTO authDTO, StationDTO fromStation, StationDTO toStation) {
		StationDAO dao = new StationDAO();
		return dao.getRouteDTO(authDTO, fromStation, toStation);
	}

	public List<StationDTO> getAllStations() {
		StationDAO dao = new StationDAO();
		List<StationDTO> list = dao.getAllStations();
		return list;
	}

	@Override
	public List<StationDTO> getStationAndStationPoints(AuthDTO authDTO) {
		StationDAO dao = new StationDAO();
		return dao.getStationAndStationPoint(authDTO);
	}

	@Override
	public void updateStationOtaPartner(AuthDTO authDTO, StationOtaPartnerDTO stationOtaPartnerDTO) {
		StationDAO stationOtaPartnerDAO = new StationDAO();
		for (StationDTO stationDTO : stationOtaPartnerDTO.getStations()) {
			getStation(stationDTO);
		}
		stationOtaPartnerDAO.updateStationOtaPartner(authDTO, stationOtaPartnerDTO);
	}

	@Override
	public void updateStationOtaPartnerV2(AuthDTO authDTO, StationOtaPartnerDTO stationOtaPartnerDTO) {
		StationDAO stationOtaPartnerDAO = new StationDAO();

		StationDTO stationDTO = new StationDTO();
		stationDTO.setCode(stationOtaPartnerDTO.getCode());
		stationDTO = getStation(stationDTO);

		for (Iterator<StationOtaPartnerDTO> otaIterator = stationOtaPartnerDTO.getOtaStations().iterator(); otaIterator.hasNext();) {
			StationOtaPartnerDTO otaStation = otaIterator.next();
			StationOtaPartnerDTO existPartner = stationOtaPartnerDAO.getOtaStationByCode(authDTO, otaStation);
			if (existPartner == null) {
				otaIterator.remove();
				continue;
			}

			Map<String, StationDTO> stationMap = convertStationMap(existPartner.getStations());
			if (otaStation.getActiveFlag() == Numeric.ONE_INT) {
				stationMap.put(stationDTO.getCode(), stationDTO);
			}
			else {
				stationMap.remove(stationDTO.getCode());
			}

			List<StationDTO> stationList = new ArrayList<StationDTO>(stationMap.values());
			otaStation.setStations(stationList);
		}
		stationOtaPartnerDAO.updateStationOtaPartnerV2(authDTO, stationOtaPartnerDTO.getOtaStations());
	}

	public void addStationOtaPartner(AuthDTO authDTO, StationOtaPartnerDTO stationOtaPartner) {
		if (!ArrayUtils.contains(Constants.SUPER_REGIONS_ZONE, ApplicationConfig.getServerZoneCode())) {
			throw new ServiceException(ErrorCode.INVALID_APPLICATION_ZONE);
		}

		StationDAO stationOtaPartnerDAO = new StationDAO();

		StationOtaPartnerDTO existPartner = stationOtaPartnerDAO.getOtaPartnerStationByCode(authDTO, stationOtaPartner);
		if (existPartner != null && StringUtil.isNotNull(existPartner.getCode())) {
			throw new ServiceException(ErrorCode.INVALID_STATION, "already existing this station: " + existPartner.getCode() + "-" + existPartner.getOtaStationCode() + "-" + existPartner.getOtaStationName() + "-" + stateService.getState(existPartner.getState()).getName());
		}

		stationOtaPartner.setState(stateService.getState(stationOtaPartner.getState()));
		stationOtaPartner.setCode(TokenGenerator.generateCode("OTA", 12));
		stationOtaPartnerDAO.addStationOtaPartner(authDTO, stationOtaPartner);

	}

	private Map<String, StationDTO> convertStationMap(List<StationDTO> list) {
		Map<String, StationDTO> stationMap = new HashMap<String, StationDTO>();
		for (StationDTO stationDTO : list) {
			if (StringUtil.isNull(stationDTO.getCode())) {
				continue;
			}
			stationMap.put(stationDTO.getCode(), stationDTO);
		}
		return stationMap;

	}

	@Override
	public List<StationOtaPartnerDTO> getStationOtaPartners(AuthDTO authDTO, StationOtaPartnerDTO stationOtaPartnerDTO) {
		StationDAO stationOtaPartnerDAO = new StationDAO();
		if (StringUtil.isNotNull(stationOtaPartnerDTO.getState().getCode())) {
			stationOtaPartnerDTO.setState(stateService.getState(stationOtaPartnerDTO.getState()));
		}
		List<StationOtaPartnerDTO> stationOtaPartners = stationOtaPartnerDAO.getStationOtaPartners(authDTO, stationOtaPartnerDTO);
		for (StationOtaPartnerDTO stationOtaPartner : stationOtaPartners) {
			for (StationDTO stationDTO : stationOtaPartner.getStations()) {
				getStation(stationDTO);
			}
		}
		return stationOtaPartners;
	}

	@Override
	public List<StationOtaPartnerDTO> getStationOtaPartnersV2(AuthDTO authDTO, StationOtaPartnerDTO stationOtaPartnerDTO) {
		Map<String, StationOtaPartnerDTO> otaStationMap = new HashMap<String, StationOtaPartnerDTO>();
		StationDAO stationOtaPartnerDAO = new StationDAO();
		if (StringUtil.isNotNull(stationOtaPartnerDTO.getState().getCode())) {
			stationOtaPartnerDTO.setState(stateService.getState(stationOtaPartnerDTO.getState()));
		}

		List<StationOtaPartnerDTO> stationOtaPartners = stationOtaPartnerDAO.getStationOtaPartners(authDTO, stationOtaPartnerDTO);
		for (StationOtaPartnerDTO stationOtaPartner : stationOtaPartners) {

			List<StationDTO> stationList = new ArrayList<>();
			StationDTO station = new StationDTO();
			station.setCode(stationOtaPartner.getCode() + Text.COMMA + stationOtaPartner.getOtaStationCode());
			station.setName(stationOtaPartner.getOtaStationName());
			station.setActiveFlag(stationOtaPartner.getActiveFlag());
			stationList.add(station);

			for (StationDTO stationDTO : stationOtaPartner.getStations()) {
				getStation(stationDTO);
				if (stationDTO.getId() == 0) {
					continue;
				}
				if (otaStationMap.get(stationDTO.getCode()) == null) {
					StationOtaPartnerDTO partner = new StationOtaPartnerDTO();
					partner.setCode(stationOtaPartner.getCode());
					partner.setOtaStationCode(stationDTO.getCode());
					partner.setOtaStationName(stationDTO.getName());
					partner.setActiveFlag(stationDTO.getActiveFlag());
					partner.setStations(stationList);

					otaStationMap.put(stationDTO.getCode(), partner);
				}
				else if (otaStationMap.get(stationDTO.getCode()) != null) {
					StationOtaPartnerDTO existPartner = otaStationMap.get(stationDTO.getCode());
					existPartner.getStations().addAll(stationList);

					otaStationMap.put(stationDTO.getCode(), existPartner);
				}
			}
		}
		List<StationDTO> stationlist = getAll(authDTO, stationOtaPartnerDTO.getState());
		for (StationDTO station : stationlist) {
			if (otaStationMap.get(station.getCode()) == null) {
				StationOtaPartnerDTO partner = new StationOtaPartnerDTO();
				partner.setOtaStationCode(station.getCode());
				partner.setOtaStationName(station.getName());
				partner.setActiveFlag(station.getActiveFlag());
				partner.setStations(new ArrayList<StationDTO>());
				otaStationMap.put(station.getCode(), partner);
			}
		}

		return new ArrayList<StationOtaPartnerDTO>(otaStationMap.values());
	}

	@Override
	public List<StationOtaPartnerDTO> getOtaStation(AuthDTO authDTO, StationOtaPartnerDTO stationOtaPartnerDTO) {
		StationDAO stationOtaPartnerDAO = new StationDAO();
		List<StationOtaPartnerDTO> stationOtaPartners = stationOtaPartnerDAO.getOtaStation(authDTO, stationOtaPartnerDTO);
		for (StationOtaPartnerDTO stationOtaPartner : stationOtaPartners) {
			stationOtaPartner.setState(stateService.getState(stationOtaPartner.getState()));
			for (StationDTO stationDTO : stationOtaPartner.getStations()) {
				getStation(stationDTO);
			}
		}
		return stationOtaPartners;
	}

	@Override
	public List<StationOtaPartnerDTO> getAllStationOtaforZoneSync(AuthDTO authDTO, String syncDate) {
		StationDAO dao = new StationDAO();
		List<StationOtaPartnerDTO> stationOtaPartners = dao.getStationOtaforZoneSync(syncDate);
		for (StationOtaPartnerDTO stationOtaPartner : stationOtaPartners) {
			stateService.getState(stationOtaPartner.getState());
			for (StationDTO stationDTO : stationOtaPartner.getStations()) {
				getStation(stationDTO);
			}
		}
		return stationOtaPartners;
	}

	@Override
	public void updateOtaPartner(AuthDTO authDTO, List<StationOtaPartnerDTO> stationOtaPartners) {
		StationDAO dao = new StationDAO();
		dao.updateStationOtaZoneSync(authDTO, stationOtaPartners);

	}

	@Override
	public void updateRouteStatus(AuthDTO authDTO, List<RouteDTO> routes, int enableFlag) {
		StationDAO dao = new StationDAO();
		dao.updateRouteStatus(authDTO, routes, enableFlag);
	}

	@Override
	public StationAreaDTO updateStationArea(AuthDTO authDTO, StationAreaDTO stationAreaDTO) {
		StationDAO dao = new StationDAO();
		dao.updateStationArea(authDTO, stationAreaDTO);
		return stationAreaDTO;
	}

	@Override
	public List<StationAreaDTO> getStationAreas(StationDTO stationDTO) {
		StationDAO dao = new StationDAO();
		return dao.getStationAreas(stationDTO);
	}

	@Override
	public void getStationArea(StationAreaDTO stationAreaDTO) {
		StationDAO dao = new StationDAO();
		dao.getStationArea(stationAreaDTO);
	}

	@Override
	public List<StationAreaDTO> getStationAreasForZoneSync(String syncDate) {
		StationDAO dao = new StationDAO();
		List<StationAreaDTO> areaList = dao.getStationAreaZoneSync(syncDate);
		return areaList;
	}

	@Override
	public Map<String, Object> getTopRoutes(AuthDTO authDTO) {
		Map<String, Object> dataMap = Maps.newHashMap();
		String key = authDTO.getNamespaceCode() + Constants.TOP_ROUTE;
		Element routeElement = EhcacheManager.getCommerceStaticEhCache().get(key);
		if (routeElement != null) {
			dataMap = (Map<String, Object>) routeElement.getObjectValue();
		}
		else {
			StationDAO dao = new StationDAO();
			List<RouteDTO> Routelist = dao.getNamespaceRoutes(authDTO);

			// Validate Namespace Route has active Station
			List<StationDTO> stationlist = getCommerceStation(authDTO);
			StringBuilder stationCodeBuilder = new StringBuilder();
			for (StationDTO station : stationlist) {
				stationCodeBuilder.append(station.getCode());
				stationCodeBuilder.append(Text.COMMA);
			}
			String stationCodes = stationCodeBuilder.toString();

			// Remove non active routes
			for (Iterator<RouteDTO> iterator = Routelist.iterator(); iterator.hasNext();) {
				RouteDTO routeDTO = iterator.next();
				if (routeDTO.getActiveFlag() != 1) {
					iterator.remove();
					continue;
				}
				routeDTO.setFromStation(getStation(routeDTO.getFromStation()));
				routeDTO.setToStation(getStation(routeDTO.getToStation()));

				if (!stationCodes.contains(routeDTO.getFromStation().getCode()) || !stationCodes.contains(routeDTO.getToStation().getCode())) {
					iterator.remove();
					continue;
				}
			}

			Map<String, Map<String, List<String>>> finalRouteMap = Maps.newHashMap();
			List<String> topRouteToStationList = new ArrayList<>();
			dataMap.put(ROUTE, finalRouteMap);
			dataMap.put(TOP_ROUTE, topRouteToStationList);

			if (Routelist != null && !Routelist.isEmpty()) {
				Map<String, List<StationDTO>> routeMap = Maps.newHashMap();
				Map<String, List<StationDTO>> topRouteMap = Maps.newHashMap();
				Map<String, Integer> stationBookingCountMap = Maps.newHashMap();
				for (RouteDTO routeDTO : Routelist) {
					String fromStationCode = routeDTO.getFromStation().getCode();
					boolean isTopRoute = BooleanUtils.toBoolean(routeDTO.getTopRouteFlag());

					if (isTopRoute) {
						if (topRouteMap.get(fromStationCode) != null) {
							List<StationDTO> toStationList = topRouteMap.get(fromStationCode);
							toStationList.add(routeDTO.getToStation());
							topRouteMap.put(fromStationCode, toStationList);
						}
						else {
							List<StationDTO> toStationList = new ArrayList<>();
							toStationList.add(routeDTO.getToStation());
							topRouteMap.put(fromStationCode, toStationList);
						}
						if (stationBookingCountMap.get(fromStationCode) != null) {
							int bookingCount = stationBookingCountMap.get(fromStationCode);
							stationBookingCountMap.put(fromStationCode, routeDTO.getBookingCount() + bookingCount);
						}
						else {
							stationBookingCountMap.put(fromStationCode, routeDTO.getBookingCount());
						}
					}
					if (routeMap.get(fromStationCode) != null) {
						List<StationDTO> toStationList = routeMap.get(fromStationCode);
						toStationList.add(routeDTO.getToStation());
						routeMap.put(fromStationCode, toStationList);
					}
					else {
						List<StationDTO> toStationList = new ArrayList<StationDTO>();
						toStationList.add(routeDTO.getToStation());
						routeMap.put(fromStationCode, toStationList);
					}
				}

				Map<String, Map<String, Integer>> topFromStationMap = Maps.newHashMap();
				for (Map.Entry<String, List<StationDTO>> route : routeMap.entrySet()) {
					String fromStationCode = route.getKey();
					List<StationDTO> toStationList = route.getValue();

					boolean isEligible = toStationList.size() >= 5 ? true : false;
					int size = 0;
					int actualSize = stationBookingCountMap.get(fromStationCode) != null ? stationBookingCountMap.get(fromStationCode) : 0;
					if (isEligible) {
						int toStationSize = toStationList.size() / 3;
						size = toStationSize > Numeric.SIX_INT ? Numeric.SIX_INT : toStationSize;
					}

					List<StationDTO> topToStations = topRouteMap.get(fromStationCode) != null ? topRouteMap.get(fromStationCode) : new ArrayList<>();
					List<StationDTO> topToStationsList = new ArrayList<>();
					for (Iterator<StationDTO> toStationItr = toStationList.iterator(); toStationItr.hasNext();) {
						StationDTO toStation = toStationItr.next();

						topToStationsList = size > Numeric.ZERO_INT && topToStations.size() > size ? topToStations.subList(0, size - 1) : topToStations;
						if (topToStationsList != null) {
							for (StationDTO topToStation : topToStationsList) {
								if (toStation.getCode().equals(topToStation.getCode())) {
									toStationItr.remove();
									break;
								}
							}
						}
					}

					if (actualSize == 0 && size != 0) {
						actualSize = size;
					}

					Comparator<StationDTO> comp = new BeanComparator("name");
					Collections.sort(toStationList, comp);

					List<String> routeToStations = toStationList.stream().map(station -> station.getCode()).collect(Collectors.toList());
					List<String> topRouteToStations = topToStationsList.stream().sorted((s1, s2) -> s1.getName().compareTo(s2.getName())).map(station -> station.getCode()).collect(Collectors.toList());

					Map<String, List<String>> topToStationMap = Maps.newHashMap();
					topToStationMap.put(ROUTE, routeToStations);
					topToStationMap.put(TOP_ROUTE, topRouteToStations);
					finalRouteMap.put(fromStationCode, topToStationMap);

					dataMap.put(ROUTE, finalRouteMap);

					if (topRouteToStations.size() > 0) {
						Map<String, Integer> toStationSizeMap = new HashMap<>();
						toStationSizeMap.put(STATION_SIZE, topRouteToStations.size());
						toStationSizeMap.put(ACTUAL_STATION_SIZE, actualSize);
						topFromStationMap.put(fromStationCode, toStationSizeMap);
					}
				}

				if (!finalRouteMap.isEmpty()) {
					if (!topFromStationMap.isEmpty()) {
						topRouteToStationList = topFromStationMap.entrySet().stream().sorted((e1, e2) -> e2.getValue().get(ACTUAL_STATION_SIZE).compareTo(e1.getValue().get(ACTUAL_STATION_SIZE))).map(station -> station.getKey()).collect(Collectors.toList());
						topRouteToStationList = topRouteToStationList.size() > 6 ? topRouteToStationList.subList(0, 5) : topRouteToStationList;
					}
					Map<String, String> topRouteSortMap = Maps.newHashMap();
					for (String stationCode : topRouteToStationList) {
						StationDTO stationDTO = new StationDTO();
						stationDTO.setCode(stationCode);
						getStation(stationDTO);

						topRouteSortMap.put(stationCode, stationDTO.getName());
					}
					if (!topRouteSortMap.isEmpty()) {
						topRouteToStationList = topRouteSortMap.entrySet().stream().sorted((e1, e2) -> e1.getValue().compareTo(e2.getValue())).map(station -> station.getKey()).collect(Collectors.toList());
					}
					dataMap.put(TOP_ROUTE, topRouteToStationList);

					Element element = new Element(key, dataMap);
					EhcacheManager.getCommerceStaticEhCache().put(element);
				}
			}
		}
		return dataMap;
	}
}
