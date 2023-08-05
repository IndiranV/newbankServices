package org.in.com.service.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanComparator;
import org.in.com.cache.CacheCentral;
import org.in.com.dao.StationPointDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.UserStationPointDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.GroupService;
import org.in.com.service.ScheduleStationPointService;
import org.in.com.service.StationPointService;
import org.in.com.service.StationService;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StationPointImpl extends CacheCentral implements StationPointService {
	@Autowired
	ScheduleStationPointService scheduleStationPointService;
	@Autowired
	GroupService groupService;
	@Autowired
	StationService stationService;
	@Autowired
	StationPointService stationPointService;

	public List<StationPointDTO> get(AuthDTO authDTO, StationPointDTO stationPointDTO) {
		StationPointDAO dao = new StationPointDAO();
		StationDTO stationDTO = stationService.getStation(stationPointDTO.getStation());
		if (stationDTO.getId() != 0) {
			stationPointDTO.setStation(stationDTO);
		}
		else {
			throw new ServiceException(301);
		}
		List<StationPointDTO> list = dao.getStationPoint(authDTO, stationPointDTO);
		@SuppressWarnings("unchecked")
		Comparator<StationPointDTO> comp = new BeanComparator("name");
		Collections.sort(list, comp);
		return list;
	}

	public StationPointDTO getStationPoint(AuthDTO auth, StationPointDTO stationPoint) {
		StationPointDTO point = null;
		if (stationPoint.getId() != 0) {
			point = getStationPointDTObyId(auth, stationPoint);
		}
		else if (StringUtil.isNotNull(stationPoint.getCode())) {
			point = getStationPointDTO(auth, stationPoint);
		}
		return point;
	}

	public List<StationPointDTO> getAll(AuthDTO authDTO) {
		StationPointDAO dao = new StationPointDAO();
		List<StationPointDTO> list = dao.getAllStationPoints(authDTO);
		@SuppressWarnings("unchecked")
		Comparator<StationPointDTO> comp = new BeanComparator("name");
		Collections.sort(list, comp);
		return list;
	}

	public StationPointDTO Update(AuthDTO authDTO, StationPointDTO pointDTO) {
		if (pointDTO.getActiveFlag() == 1) {
			StationDTO stationDTO = stationService.getStation(pointDTO.getStation());
			if (stationDTO.getId() != 0) {
				pointDTO.setStation(stationDTO);
			}
			else {
				throw new ServiceException(301);
			}
		}
		if (pointDTO.getActiveFlag() != 1 && scheduleStationPointService.CheckStationPointUsed(authDTO, pointDTO)) {
			throw new ServiceException(ErrorCode.STATION_POINT_USED_SCHEDULE);
		}
		StationPointDAO dao = new StationPointDAO();
		dao.getStationPointsUID(authDTO, pointDTO);

		// Refresh Station point cache
		removeStationPointDTO(authDTO, pointDTO);
		return pointDTO;
	}

	public void updateUserSpecificStationPoint(AuthDTO authDTO, UserStationPointDTO userStationPointDTO) {
		for (StationPointDTO stationPointDTO : userStationPointDTO.getStation().getStationPoints()) {
			stationPointService.getStationPoint(authDTO, stationPointDTO);
		}
		StationPointDAO pointDAO = new StationPointDAO();
		pointDAO.updateUserSpecificStationPoint(authDTO, userStationPointDTO);
	}

	public List<UserStationPointDTO> getUserSpecificStationPoint(AuthDTO authDTO, UserDTO userDTO, StationDTO station) {
		station = stationService.getStation(station);

		StationPointDAO pointDAO = new StationPointDAO();
		userDTO = getUserDTOById(authDTO, userDTO);
		List<UserStationPointDTO> list = pointDAO.getUserSpecificStationPoint(authDTO, userDTO, station);
		
		for (UserStationPointDTO userStatinPoint : list) {
			userStatinPoint.setStation(getStationDTObyId(userStatinPoint.getStation()));
			for (StationPointDTO stationPointDTO : userStatinPoint.getStation().getStationPoints()) {
				getStationPointDTObyId(authDTO, stationPointDTO);
				stationPointDTO.setStation(userStatinPoint.getStation());
				stationPointDTO.setBoardingCommission(userStatinPoint.getBoardingCommission());
			}
			for (GroupDTO groupDTO : userStatinPoint.getGroupList()) {
				GroupDTO group = groupService.getGroup(authDTO, groupDTO);
				groupDTO.setCode(group.getCode());
				groupDTO.setName(group.getName());
				groupDTO.setActiveFlag(group.getActiveFlag());
			}
		}
		return list;
	}
	
	public Map<String, Map<String, StationPointDTO>> getUserSpecificStationPointV2(AuthDTO authDTO, UserDTO userDTO, StationDTO station) {
		Map<String, Map<String, StationPointDTO>> stationPointMap = new HashMap<String, Map<String, StationPointDTO>>();

		List<UserStationPointDTO> list = getUserSpecificStationPoint(authDTO, userDTO, station);
		
		for (UserStationPointDTO userStatinPoint : list) {
			for (StationPointDTO stationPointDTO : userStatinPoint.getStation().getStationPoints()) {
				stationPointDTO.setUserGroupList(userStatinPoint.getGroupList());
				if (stationPointMap.get(stationPointDTO.getCode()) != null) {
					Map<String, StationPointDTO> groupWiseDataMap = stationPointMap.get(stationPointDTO.getCode());
					if (!userStatinPoint.getGroupList().isEmpty()) {
						for (GroupDTO groupDTO : userStatinPoint.getGroupList()) {
							if (groupWiseDataMap.get(groupDTO.getCode()) != null && groupWiseDataMap.get(groupDTO.getCode()).getBoardingCommission().intValue() > stationPointDTO.getBoardingCommission().intValue()) {
								groupWiseDataMap.put(groupDTO.getCode(), stationPointDTO);
							}
						}
					}
					else if (groupWiseDataMap.get("ALL") != null && groupWiseDataMap.get("ALL").getBoardingCommission().intValue() > stationPointDTO.getBoardingCommission().intValue()) {
						groupWiseDataMap.put("ALL", stationPointDTO);
					}
					stationPointMap.put(stationPointDTO.getCode(), groupWiseDataMap);
				}
				else {
					Map<String, StationPointDTO> groupWiseDataMap = new HashMap<>();
					if (!userStatinPoint.getGroupList().isEmpty()) {
						for (GroupDTO groupDTO : userStatinPoint.getGroupList()) {
							groupWiseDataMap.put(groupDTO.getCode(), stationPointDTO);
						}
					}
					else {
						groupWiseDataMap.put("ALL", stationPointDTO);
					}
					stationPointMap.put(stationPointDTO.getCode(), groupWiseDataMap);
				}
			}
		}
		return stationPointMap;
	}

}
