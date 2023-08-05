package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.in.com.cache.EhcacheManager;
import org.in.com.dao.ScheduleFareTemplateDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleFareTemplateDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.TripDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.BusService;
import org.in.com.service.ScheduleFareTemplateService;
import org.in.com.service.ScheduleTripService;
import org.in.com.service.StationService;
import org.in.com.service.UserService;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.sf.ehcache.Element;

@Service
public class ScheduleFareTemplateServiceImpl implements ScheduleFareTemplateService {

	@Autowired
	BusService busService;
	@Autowired
	StationService stationService;
	@Autowired
	UserService userService;
	@Autowired
	ScheduleTripService scheduleTripService;

	public List<ScheduleFareTemplateDTO> get(AuthDTO authDTO, ScheduleFareTemplateDTO dto) {
		return null;
	}

	public List<ScheduleFareTemplateDTO> getAll(AuthDTO authDTO) {
		ScheduleFareTemplateDAO dao = new ScheduleFareTemplateDAO();
		List<ScheduleFareTemplateDTO> list = dao.getAllScheduleFareTemplate(authDTO);
		for (ScheduleFareTemplateDTO scheduleFareTemplate : list) {
			scheduleFareTemplate.setBus(busService.getBus(authDTO, scheduleFareTemplate.getBus()));

			for (RouteDTO stageDTO : scheduleFareTemplate.getStageFare()) {
				stageDTO.setFromStation(stationService.getStation(stageDTO.getFromStation()));
				stageDTO.setToStation(stationService.getStation(stageDTO.getToStation()));
			}
			if (scheduleFareTemplate.getAudit().getUser().getId() != 0) {
				scheduleFareTemplate.getAudit().setUser(userService.getUser(authDTO, scheduleFareTemplate.getAudit().getUser()));
			}
		}
		return list;
	}

	public ScheduleFareTemplateDTO Update(AuthDTO authDTO, ScheduleFareTemplateDTO scheduleFareTemplate) {
		ScheduleFareTemplateDAO dao = new ScheduleFareTemplateDAO();
		if (StringUtil.isNotNull(scheduleFareTemplate.getBus().getCode())) {
			scheduleFareTemplate.setBus(busService.getBus(authDTO, scheduleFareTemplate.getBus()));
		}
		for (RouteDTO stageDTO : scheduleFareTemplate.getStageFare()) {
			stageDTO.setFromStation(stationService.getStation(stageDTO.getFromStation()));
			stageDTO.setToStation(stationService.getStation(stageDTO.getToStation()));
		}
		if (scheduleFareTemplate.getActiveFlag() <= 1 && scheduleFareTemplate.getBus().getId() == 0) {
			throw new ServiceException(ErrorCode.INVALID_CODE, "Bus code");
		}
		dao.updateScheduleFareTemplate(authDTO, scheduleFareTemplate);
		// clear cache
		removeScheduleFareTemplate(authDTO);
		return scheduleFareTemplate;
	}

	public List<ScheduleFareTemplateDTO> getTripStageTemplate(AuthDTO authDTO, TripDTO trip) {
		List<ScheduleFareTemplateDTO> list = new ArrayList<ScheduleFareTemplateDTO>();
		List<ScheduleFareTemplateDTO> filterList = new ArrayList<ScheduleFareTemplateDTO>();
		Element element = EhcacheManager.getScheduleFareTemplateCache().get(authDTO.getNamespaceCode());
		if (element != null) {
			List<ScheduleFareTemplateDTO> cachelist = (List<ScheduleFareTemplateDTO>) element.getObjectValue();
			list.addAll(cachelist);
		}
		else {
			ScheduleFareTemplateDAO dao = new ScheduleFareTemplateDAO();
			List<ScheduleFareTemplateDTO> repolist = dao.getAllScheduleFareTemplate(authDTO);
			list.addAll(repolist);

			element = new Element(authDTO.getNamespaceCode(), list);
			EhcacheManager.getScheduleFareTemplateCache().put(element);
		}
		if (!list.isEmpty()) {
			TripDTO tripStage = scheduleTripService.getTripStageDetails(authDTO, trip);
			Map<String, StageDTO> stageMap = new java.util.HashMap<String, StageDTO>();
			for (StageDTO stage : tripStage.getStageList()) {
				stageMap.put(stage.getFromStation().getStation().getId() + "_" + stage.getToStation().getStation().getId(), stage);
			}

			for (Iterator<ScheduleFareTemplateDTO> iterator = list.iterator(); iterator.hasNext();) {
				ScheduleFareTemplateDTO scheduleFareTemplate = iterator.next();

				if (scheduleFareTemplate.getBus().getId() != tripStage.getBus().getId() || scheduleFareTemplate.getActiveFlag() != 1) {
					continue;
				}
				boolean foundRoute = false;
				for (RouteDTO stageDTO : scheduleFareTemplate.getStageFare()) {
					if (!foundRoute && stageMap.get(stageDTO.getFromStation().getId() + "_" + stageDTO.getToStation().getId()) != null) {
						foundRoute = true;
					}
					stageDTO.setFromStation(stationService.getStation(stageDTO.getFromStation()));
					stageDTO.setToStation(stationService.getStation(stageDTO.getToStation()));
				}
				if (!foundRoute) {
					continue;
				}
				scheduleFareTemplate.setBus(busService.getBus(authDTO, scheduleFareTemplate.getBus()));
				filterList.add(scheduleFareTemplate);
			}
		}
		return filterList;
	}

	public ScheduleFareTemplateDTO getScheduleFareTemplate(AuthDTO authDTO, ScheduleFareTemplateDTO fareTemplate) {
		ScheduleFareTemplateDAO dao = new ScheduleFareTemplateDAO();
		fareTemplate = dao.getScheduleFareTemplate(authDTO, fareTemplate);
		return fareTemplate;
	}

	private void removeScheduleFareTemplate(AuthDTO authDTO) {
		EhcacheManager.getScheduleFareTemplateCache().remove(authDTO.getNamespaceCode());
	}
}
