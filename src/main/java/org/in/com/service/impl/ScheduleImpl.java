package org.in.com.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.in.com.cache.BusCache;
import org.in.com.cache.ScheduleCache;
import org.in.com.constants.Numeric;
import org.in.com.dao.ScheduleDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleBusDTO;
import org.in.com.dto.ScheduleCancellationTermDTO;
import org.in.com.dto.ScheduleCategoryDTO;
import org.in.com.dto.ScheduleControlDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleStageDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.ScheduleStationPointDTO;
import org.in.com.dto.SectorDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StageFareDTO;
import org.in.com.dto.StageStationDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.UserDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.GroupService;
import org.in.com.service.NamespaceTaxService;
import org.in.com.service.ScheduleActivityService;
import org.in.com.service.ScheduleAuditLogService;
import org.in.com.service.ScheduleBusService;
import org.in.com.service.ScheduleCancellationTermService;
import org.in.com.service.ScheduleCategoryService;
import org.in.com.service.ScheduleControlService;
import org.in.com.service.ScheduleDiscountService;
import org.in.com.service.ScheduleService;
import org.in.com.service.ScheduleStageService;
import org.in.com.service.ScheduleStationPointService;
import org.in.com.service.ScheduleStationService;
import org.in.com.service.ScheduleTagService;
import org.in.com.service.ScheduleVisibilityService;
import org.in.com.service.SectorService;
import org.in.com.service.StationService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.StreamUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;

@Service
public class ScheduleImpl extends ScheduleCache implements ScheduleService {

	@Autowired
	ScheduleStationService scheduleStationService;
	@Autowired
	ScheduleStationPointService stationPointService;
	@Autowired
	ScheduleStageService stageService;
	@Autowired
	ScheduleCancellationTermService cancellationTermService;
	@Autowired
	ScheduleDiscountService discountService;
	@Autowired
	ScheduleControlService controlService;
	@Autowired
	ScheduleBusService busService;
	@Autowired
	ScheduleVisibilityService visibilityService;
	@Autowired
	ScheduleCategoryService categoryService;
	@Autowired
	NamespaceTaxService taxService;
	@Autowired
	ScheduleActivityService scheduleActivityService;
	@Autowired
	GroupService groupService;
	@Autowired
	SectorService sectorService;
	@Autowired
	ScheduleAuditLogService scheduleAuditService;
	@Autowired
	ScheduleTagService scheduleTagService;
	@Autowired
	StationService stationService;

	public ScheduleDTO getSchedule(AuthDTO authDTO, ScheduleDTO schedule) {
		ScheduleDTO scheduleDTO = null;
		if (schedule.getId() != 0) {
			scheduleDTO = getScheduleDTObyId(authDTO, schedule);
		}
		else if (StringUtil.isNotNull(schedule.getCode())) {
			scheduleDTO = getScheduleDTO(authDTO, schedule);
		}

		return scheduleDTO;
	}

	public List<ScheduleDTO> get(AuthDTO authDTO, ScheduleDTO dto) {
		ScheduleDAO dao = new ScheduleDAO();
		// get Schedule From DB Repo
		List<ScheduleDTO> list = dao.get(authDTO, dto);
		Map<Integer, ScheduleCategoryDTO> categoryMap = categoryService.getCategoryMap(authDTO);
		for (Iterator<ScheduleDTO> itrSchedule = list.iterator(); itrSchedule.hasNext();) {
			ScheduleDTO scheduleDTO = itrSchedule.next();
			if (scheduleDTO.getCategory() != null && scheduleDTO.getCategory().getId() != 0 && categoryMap.get(scheduleDTO.getCategory().getId()) != null) {
				scheduleDTO.setCategory(categoryMap.get(scheduleDTO.getCategory().getId()));
			}
			scheduleTagService.getScheduleTagsById(authDTO, scheduleDTO.getScheduleTagList());
			for (SectorDTO sectorDTO : scheduleDTO.getSectorList()) {
				SectorDTO sectorRepo = sectorService.getSectorV2(authDTO, sectorDTO);
				sectorDTO.setCode(sectorRepo.getCode());
				sectorDTO.setName(sectorRepo.getName());
			}
		}
		return list;
	}

	public ScheduleDTO Update(AuthDTO authDTO, ScheduleDTO dto) {
		// Schedule Activity Log
		scheduleActivityService.scheduleActivity(authDTO, dto);

		if (StringUtil.isNotNull(dto.getCode())) {
			scheduleAuditService.updateScheduleAudit(authDTO, dto);
		}

		if (dto.getScheduleTagList() != null) {
			scheduleTagService.getScheduleTagsByCode(authDTO, dto.getScheduleTagList());
		}
		if (dto.getSectorList() != null) {
			for (SectorDTO sectorDTO : dto.getSectorList()) {
				SectorDTO sectorRepo = sectorService.getSector(authDTO, sectorDTO);
				sectorDTO.setId(sectorRepo.getId());
			}
		}
		ScheduleDAO dao = new ScheduleDAO();
		dao.getIUD(authDTO, dto);
		// Updated Sector
		updateSectorSchedule(authDTO, dto, dto.getSectorList());

		ScheduleCache scheduleCache = new ScheduleCache();
		scheduleCache.removeScheduleDTO(authDTO, dto);

		return dto;
	}

	public List<ScheduleDTO> getClosed(AuthDTO authDTO) {
		SectorDTO sector = sectorService.getActiveUserSectorSchedule(authDTO);

		ScheduleDAO dao = new ScheduleDAO();
		ScheduleCache scheduleCache = new ScheduleCache();
		List<ScheduleDTO> scheduleDTOList = dao.getClosedSchedule(authDTO);
		Map<Integer, ScheduleCategoryDTO> categoryMap = categoryService.getCategoryMap(authDTO);
		for (Iterator<ScheduleDTO> itrSchedule = scheduleDTOList.iterator(); itrSchedule.hasNext();) {
			ScheduleDTO scheduleDTO = itrSchedule.next();

			// Apply Sector schedule filter
			if (sector.getActiveFlag() == Numeric.ONE_INT && BitsUtil.isScheduleExists(sector.getSchedule(), scheduleDTO) == null) {
				itrSchedule.remove();
				continue;
			}

			scheduleTagService.getScheduleTagsById(authDTO, scheduleDTO.getScheduleTagList());
			for (SectorDTO sectorDTO : scheduleDTO.getSectorList()) {
				SectorDTO sectorRepo = sectorService.getSectorV2(authDTO, sectorDTO);
				sectorDTO.setCode(sectorRepo.getCode());
				sectorDTO.setName(sectorRepo.getName());
			}

			// Station
			List<ScheduleStationDTO> stationList = scheduleStationService.getScheduleStation(authDTO, scheduleDTO);
			// Validate all stations
			for (Iterator<ScheduleStationDTO> iterator = stationList.iterator(); iterator.hasNext();) {
				ScheduleStationDTO stationDTO = iterator.next();
				stationDTO.setStation(getStationDTObyId(stationDTO.getStation()));
			}
			// Schedule Station Point
			List<ScheduleStationPointDTO> stationPointList = stationPointService.getScheduleStationPoint(authDTO, scheduleDTO);
			scheduleDTO.setStationList(stationList);
			List<ScheduleStageDTO> scheduleStageDTOList = scheduleCache.getScheduleStageDTO(authDTO, scheduleDTO);
			for (Iterator<ScheduleStageDTO> iterator = scheduleStageDTOList.iterator(); iterator.hasNext();) {
				ScheduleStageDTO scheduleStageDTO = iterator.next();
				if (scheduleStageDTO.getGroup() != null && scheduleStageDTO.getGroup().getId() != 0) {
					scheduleStageDTO.setGroup(groupService.getGroup(authDTO, scheduleStageDTO.getGroup()));
				}
			}
			scheduleDTO.setScheduleStageList(scheduleStageDTOList);
			scheduleDTO.setStationPointList(stationPointList);
			// Bus Type and BusMap
			ScheduleBusDTO scheduleBusDTO = busService.getByScheduleId(authDTO, scheduleDTO);
			if (scheduleBusDTO == null || scheduleBusDTO.getBus() == null) {
				getRefresh(authDTO, scheduleDTO);
				itrSchedule.remove();
				continue;
			}
			BusCache busCache = new BusCache();
			scheduleBusDTO.setBus(busCache.getBusDTObyId(authDTO, scheduleBusDTO.getBus()));
			scheduleDTO.setScheduleBus(scheduleBusDTO);

			if (scheduleDTO.getCategory() != null && scheduleDTO.getCategory().getId() != 0 && categoryMap.get(scheduleDTO.getCategory().getId()) != null) {
				scheduleDTO.setCategory(categoryMap.get(scheduleDTO.getCategory().getId()));
			}
		}

		convertScheduleStageToStage(authDTO, scheduleDTOList);
		return scheduleDTOList;

	}

	@Override
	public List<ScheduleDTO> getExpire(AuthDTO authDTO) {
		SectorDTO sector = sectorService.getActiveUserSectorSchedule(authDTO);

		ScheduleDAO dao = new ScheduleDAO();
		ScheduleCache scheduleCache = new ScheduleCache();
		List<ScheduleDTO> scheduleDTOList = dao.getExpireSchedule(authDTO);
		Map<Integer, ScheduleCategoryDTO> categoryMap = categoryService.getCategoryMap(authDTO);
		for (Iterator<ScheduleDTO> itrSchedule = scheduleDTOList.iterator(); itrSchedule.hasNext();) {
			ScheduleDTO scheduleDTO = itrSchedule.next();

			// Apply Sector schedule filter
			if (sector.getActiveFlag() == Numeric.ONE_INT && BitsUtil.isScheduleExists(sector.getSchedule(), scheduleDTO) == null) {
				itrSchedule.remove();
				continue;
			}
			scheduleTagService.getScheduleTagsById(authDTO, scheduleDTO.getScheduleTagList());
			for (SectorDTO sectorDTO : scheduleDTO.getSectorList()) {
				SectorDTO sectorRepo = sectorService.getSectorV2(authDTO, sectorDTO);
				sectorDTO.setCode(sectorRepo.getCode());
				sectorDTO.setName(sectorRepo.getName());
			}

			// Station
			List<ScheduleStationDTO> stationList = scheduleStationService.getScheduleStation(authDTO, scheduleDTO);
			// Validate all stations
			for (Iterator<ScheduleStationDTO> iterator = stationList.iterator(); iterator.hasNext();) {
				ScheduleStationDTO stationDTO = iterator.next();
				stationDTO.setStation(getStationDTObyId(stationDTO.getStation()));
			}
			// Schedule Station Point
			List<ScheduleStationPointDTO> stationPointList = stationPointService.getScheduleStationPoint(authDTO, scheduleDTO);
			scheduleDTO.setStationList(stationList);
			List<ScheduleStageDTO> scheduleStageDTOList = scheduleCache.getScheduleStageDTO(authDTO, scheduleDTO);
			for (Iterator<ScheduleStageDTO> iterator = scheduleStageDTOList.iterator(); iterator.hasNext();) {
				ScheduleStageDTO scheduleStageDTO = iterator.next();
				if (scheduleStageDTO.getGroup() != null && scheduleStageDTO.getGroup().getId() != 0) {
					scheduleStageDTO.setGroup(groupService.getGroup(authDTO, scheduleStageDTO.getGroup()));
				}
			}
			scheduleDTO.setScheduleStageList(scheduleStageDTOList);
			scheduleDTO.setStationPointList(stationPointList);
			// Bus Type and BusMap
			ScheduleBusDTO scheduleBusDTO = busService.getByScheduleId(authDTO, scheduleDTO);
			if (scheduleBusDTO == null || scheduleBusDTO.getBus() == null) {
				getRefresh(authDTO, scheduleDTO);
				itrSchedule.remove();
				continue;
			}
			BusCache busCache = new BusCache();
			scheduleBusDTO.setBus(busCache.getBusDTObyId(authDTO, scheduleBusDTO.getBus()));
			scheduleDTO.setScheduleBus(scheduleBusDTO);

			if (scheduleDTO.getCategory() != null && scheduleDTO.getCategory().getId() != 0 && categoryMap.get(scheduleDTO.getCategory().getId()) != null) {
				scheduleDTO.setCategory(categoryMap.get(scheduleDTO.getCategory().getId()));
			}
		}

		convertScheduleStageToStage(authDTO, scheduleDTOList);
		return scheduleDTOList;

	}

	@Override
	public List<ScheduleDTO> getPartial(AuthDTO authDTO) {
		SectorDTO sector = sectorService.getActiveUserSectorSchedule(authDTO);

		ScheduleDAO dao = new ScheduleDAO();
		ScheduleCache scheduleCache = new ScheduleCache();
		Map<Integer, ScheduleCategoryDTO> categoryMap = categoryService.getCategoryMap(authDTO);
		List<ScheduleDTO> scheduleDTOList = dao.getPartialSchedule(authDTO);
		for (Iterator<ScheduleDTO> itrSchedule = scheduleDTOList.iterator(); itrSchedule.hasNext();) {
			ScheduleDTO scheduleDTO = itrSchedule.next();

			// Apply Sector schedule filter
			if (sector.getActiveFlag() == Numeric.ONE_INT && BitsUtil.isScheduleExists(sector.getSchedule(), scheduleDTO) == null) {
				itrSchedule.remove();
				continue;
			}
			scheduleTagService.getScheduleTagsById(authDTO, scheduleDTO.getScheduleTagList());
			for (SectorDTO sectorDTO : scheduleDTO.getSectorList()) {
				SectorDTO sectorRepo = sectorService.getSectorV2(authDTO, sectorDTO);
				sectorDTO.setCode(sectorRepo.getCode());
				sectorDTO.setName(sectorRepo.getName());
			}
			// Station
			List<ScheduleStationDTO> stationList = scheduleStationService.getScheduleStation(authDTO, scheduleDTO);
			// Validate all stations
			for (Iterator<ScheduleStationDTO> iterator = stationList.iterator(); iterator.hasNext();) {
				ScheduleStationDTO stationDTO = iterator.next();
				stationDTO.setStation(getStationDTObyId(stationDTO.getStation()));
			}
			// Schedule Station Point
			List<ScheduleStationPointDTO> stationPointList = stationPointService.getScheduleStationPoint(authDTO, scheduleDTO);
			scheduleDTO.setStationList(stationList);
			List<ScheduleStageDTO> scheduleStageDTOList = scheduleCache.getScheduleStageDTO(authDTO, scheduleDTO);
			for (Iterator<ScheduleStageDTO> iterator = scheduleStageDTOList.iterator(); iterator.hasNext();) {
				ScheduleStageDTO scheduleStageDTO = iterator.next();
				if (scheduleStageDTO.getGroup() != null && scheduleStageDTO.getGroup().getId() != 0) {
					scheduleStageDTO.setGroup(groupService.getGroup(authDTO, scheduleStageDTO.getGroup()));
				}
			}
			scheduleDTO.setScheduleStageList(scheduleStageDTOList);
			scheduleDTO.setStationPointList(stationPointList);
			// Bus Type and BusMap
			ScheduleBusDTO scheduleBusDTO = busService.getByScheduleId(authDTO, scheduleDTO);
			if (scheduleBusDTO != null && scheduleBusDTO.getBus() != null) {
				BusCache busCache = new BusCache();
				scheduleBusDTO.setBus(busCache.getBusDTObyId(authDTO, scheduleBusDTO.getBus()));
				scheduleDTO.setScheduleBus(scheduleBusDTO);
			}

			if (scheduleDTO.getCategory() != null && scheduleDTO.getCategory().getId() != 0 && categoryMap.get(scheduleDTO.getCategory().getId()) != null) {
				scheduleDTO.setCategory(categoryMap.get(scheduleDTO.getCategory().getId()));
			}
		}

		convertScheduleStageToStage(authDTO, scheduleDTOList);
		return scheduleDTOList;
	}

	@Override
	public List<ScheduleDTO> getActive(AuthDTO authDTO, DateTime activeDate) {
		SectorDTO sector = sectorService.getActiveUserSectorSchedule(authDTO);

		ScheduleCache scheduleCache = new ScheduleCache();
		ScheduleDAO dao = new ScheduleDAO();
		List<ScheduleDTO> scheduleDTOList = dao.getActiveSchedule(authDTO, activeDate);
		Map<Integer, ScheduleCategoryDTO> categoryMap = categoryService.getCategoryMap(authDTO);

		for (Iterator<ScheduleDTO> itrSchedule = scheduleDTOList.iterator(); itrSchedule.hasNext();) {
			ScheduleDTO scheduleDTO = itrSchedule.next();

			// Apply Sector schedule filter
			if (sector.getActiveFlag() == Numeric.ONE_INT && BitsUtil.isScheduleExists(sector.getSchedule(), scheduleDTO) == null) {
				itrSchedule.remove();
				continue;
			}
			scheduleTagService.getScheduleTagsById(authDTO, scheduleDTO.getScheduleTagList());
			for (SectorDTO sectorDTO : scheduleDTO.getSectorList()) {
				SectorDTO sectorRepo = sectorService.getSectorV2(authDTO, sectorDTO);
				sectorDTO.setCode(sectorRepo.getCode());
				sectorDTO.setName(sectorRepo.getName());
			}

			// Station
			List<ScheduleStationDTO> stationList = scheduleStationService.getScheduleStation(authDTO, scheduleDTO);
			Map<Integer, ScheduleStationDTO> stationMap = new HashMap<>();
			// Validate all stations
			for (Iterator<ScheduleStationDTO> iterator = stationList.iterator(); iterator.hasNext();) {
				ScheduleStationDTO stationDTO = iterator.next();
				stationDTO.setStation(getStationDTObyId(stationDTO.getStation()));
				stationMap.put(stationDTO.getStation().getId(), stationDTO);
			}
			// Schedule Station Point
			List<ScheduleStationPointDTO> stationPointList = stationPointService.getScheduleStationPoint(authDTO, scheduleDTO);
			scheduleDTO.setStationList(stationList);
			List<ScheduleStageDTO> scheduleStageDTOList = scheduleCache.getScheduleStageDTO(authDTO, scheduleDTO);
			for (Iterator<ScheduleStageDTO> iterator = scheduleStageDTOList.iterator(); iterator.hasNext();) {
				ScheduleStageDTO scheduleStageDTO = iterator.next();
				if (scheduleStageDTO.getGroup() != null && scheduleStageDTO.getGroup().getId() != 0) {
					scheduleStageDTO.setGroup(groupService.getGroup(authDTO, scheduleStageDTO.getGroup()));
				}
				if (stationMap.get(scheduleStageDTO.getFromStation().getId()) != null) {
					ScheduleStationDTO stationDTO = stationMap.get(scheduleStageDTO.getFromStation().getId());
					scheduleStageDTO.setFromStationSequence(stationDTO.getStationSequence());
				}
				if (stationMap.get(scheduleStageDTO.getToStation().getId()) != null) {
					ScheduleStationDTO stationDTO = stationMap.get(scheduleStageDTO.getToStation().getId());
					scheduleStageDTO.setToStationSequence(stationDTO.getStationSequence());
				}
			}
			scheduleDTO.setScheduleStageList(scheduleStageDTOList);
			scheduleDTO.setStationPointList(stationPointList);

			if (scheduleStageDTOList.isEmpty() || stationList.isEmpty() || stationPointList.isEmpty()) {
				getRefresh(authDTO, scheduleDTO);
				itrSchedule.remove();
				continue;
			}
			// Bus Type and BusMap
			ScheduleBusDTO scheduleBusDTO = busService.getByScheduleId(authDTO, scheduleDTO);
			if (scheduleBusDTO == null || scheduleBusDTO.getBus() == null) {
				getRefresh(authDTO, scheduleDTO);
				itrSchedule.remove();
				continue;
			}
			BusCache busCache = new BusCache();
			scheduleBusDTO.setBus(busCache.getBusDTObyId(authDTO, scheduleBusDTO.getBus()));
			if (scheduleBusDTO.getBus().getActiveFlag() != 1) {
				getRefresh(authDTO, scheduleDTO);
				itrSchedule.remove();
				continue;
			}
			scheduleDTO.setScheduleBus(scheduleBusDTO);
			if (scheduleDTO.getCategory() != null && scheduleDTO.getCategory().getId() != 0 && categoryMap.get(scheduleDTO.getCategory().getId()) != null) {
				scheduleDTO.setCategory(categoryMap.get(scheduleDTO.getCategory().getId()));
			}
		}

		convertScheduleStageToStage(authDTO, scheduleDTOList);
		return scheduleDTOList;
	}

	@Override
	public boolean getRefresh(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		ScheduleDAO dao = new ScheduleDAO();
		dao.getScheduleRefreshIUD(authDTO, scheduleDTO);
		ScheduleCache scheduleCache = new ScheduleCache();
		scheduleCache.removeScheduleDTO(authDTO, scheduleDTO);
		stageService.removeScheduleSearchStageCache(authDTO, scheduleDTO);
		return true;
	}

	public boolean clearScheduleCache(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		ScheduleCache scheduleCache = new ScheduleCache();
		scheduleCache.removeScheduleDTO(authDTO, scheduleDTO);
		return true;
	}

	private void convertScheduleStageToStage(AuthDTO authDTO, List<ScheduleDTO> scheduleDTOList) {
		try {
			for (Iterator<ScheduleDTO> itrSchedule = scheduleDTOList.iterator(); itrSchedule.hasNext();) {
				ScheduleDTO scheduleDTO = itrSchedule.next();
				Map<Integer, StageStationDTO> stationMap = new HashMap<>();
				for (Iterator<ScheduleStationDTO> iterator = scheduleDTO.getStationList().iterator(); iterator.hasNext();) {
					ScheduleStationDTO stationDTO = iterator.next();
					StageStationDTO stageStationDTO = new StageStationDTO();
					stageStationDTO.setMinitues(stationDTO.getMinitues());
					stageStationDTO.setStationSequence(stationDTO.getStationSequence());
					stageStationDTO.setStation(stationDTO.getStation());
					stationMap.put(stationDTO.getStation().getId(), stageStationDTO);
				}
				for (Iterator<ScheduleStationPointDTO> iterator = scheduleDTO.getStationPointList().iterator(); iterator.hasNext();) {
					ScheduleStationPointDTO pointDTO = iterator.next();
					if (stationMap.get(pointDTO.getStation().getId()) != null) {
						StageStationDTO stageStationDTO = stationMap.get(pointDTO.getStation().getId());
						StationPointDTO stationPointDTO = new StationPointDTO();
						stationPointDTO.setId(pointDTO.getStationPoint().getId());
						getStationPointDTObyId(authDTO, stationPointDTO);
						stationPointDTO.setCreditDebitFlag(pointDTO.getCreditDebitFlag());
						stationPointDTO.setMinitues(pointDTO.getMinitues());
						stationPointDTO.setBusVehicleVanPickup(pointDTO.getBusVehicleVanPickup());
						stageStationDTO.getStationPoint().add(stationPointDTO);
						stationMap.put(stageStationDTO.getStation().getId(), stageStationDTO);
					}
					else {
						iterator.remove();
						continue;
					}
				}
				if (scheduleDTO.getScheduleStageList() != null) {
					Map<String, StageDTO> fareMap = new HashMap<>();
					for (Iterator<ScheduleStageDTO> iterator = scheduleDTO.getScheduleStageList().iterator(); iterator.hasNext();) {
						ScheduleStageDTO scheduleStageDTO = iterator.next();
						StageDTO stageDTO = new StageDTO();
						if (stationMap.get(scheduleStageDTO.getFromStation().getId()) != null && stationMap.get(scheduleStageDTO.getToStation().getId()) != null) {
							stageDTO.setFromStation(stationMap.get(scheduleStageDTO.getFromStation().getId()));
							stageDTO.setToStation(stationMap.get(scheduleStageDTO.getToStation().getId()));
							stageDTO.getFromStation().setStation(getStationDTObyId(scheduleStageDTO.getFromStation()));
							stageDTO.getToStation().setStation(getStationDTObyId(scheduleStageDTO.getToStation()));
							stageDTO.getFromStation().setMinitues(stageDTO.getFromStation().getMinitues());
							stageDTO.getToStation().setMinitues(stageDTO.getToStation().getMinitues());
							StageFareDTO stageFareDTO = new StageFareDTO();
							stageFareDTO.setFare(new BigDecimal(scheduleStageDTO.getFare()));
							stageFareDTO.setBusSeatType(scheduleStageDTO.getBusSeatType());
							if (scheduleStageDTO.getGroup() != null) {
								stageFareDTO.setGroup(scheduleStageDTO.getGroup());
							}
							if (fareMap.get(stageDTO.getFromStation().getStation().getId() + "_" + stageDTO.getToStation().getStation().getId()) == null) {
								List<StageFareDTO> fareList = new ArrayList<>();
								stageDTO.setStageFare(fareList);
								fareMap.put(stageDTO.getFromStation().getStation().getId() + "_" + stageDTO.getToStation().getStation().getId(), stageDTO);
							}
							List<StageFareDTO> fareList = (fareMap.get(stageDTO.getFromStation().getStation().getId() + "_" + stageDTO.getToStation().getStation().getId())).getStageFare();
							fareList.add(stageFareDTO);
							stageDTO.setStageFare(fareList);
							fareMap.put(stageDTO.getFromStation().getStation().getId() + "_" + stageDTO.getToStation().getStation().getId(), stageDTO);

						}
						else {
							iterator.remove();
							continue;
						}
					}

					List<StageDTO> stageDTOList = new ArrayList<>();
					Set<String> mapList = fareMap.keySet();
					for (String mapKey : mapList) {
						stageDTOList.add(fareMap.get(mapKey));
					}
					if (!stageDTOList.isEmpty()) {
						scheduleDTO.setStageList(stageDTOList);
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public ScheduleDTO clone(AuthDTO authDTO, ScheduleDTO scheduleDTO, List<String> entityList) {

		ScheduleCache scheduleCache = new ScheduleCache();
		ScheduleDAO dao = new ScheduleDAO();
		scheduleDTO = dao.getActiveSchedule(authDTO, scheduleDTO);
		if (scheduleDTO.getId() != 0) {
			// Station
			List<ScheduleStationDTO> stationList = scheduleStationService.getScheduleStation(authDTO, scheduleDTO);
			Map<Integer, ScheduleStationDTO> stationMap = new HashMap<>();
			// Validate all stations
			for (Iterator<ScheduleStationDTO> iterator = stationList.iterator(); iterator.hasNext();) {
				ScheduleStationDTO stationDTO = iterator.next();
				stationDTO.setStation(getStationDTObyId(stationDTO.getStation()));
				stationMap.put(stationDTO.getStation().getId(), stationDTO);
			}
			// Schedule Station Point
			List<ScheduleStationPointDTO> stationPointList = stationPointService.getScheduleStationPoint(authDTO, scheduleDTO);
			scheduleDTO.setStationList(stationList);
			List<ScheduleStageDTO> scheduleStageDTOList = scheduleCache.getScheduleStageDTO(authDTO, scheduleDTO);
			for (Iterator<ScheduleStageDTO> iterator = scheduleStageDTOList.iterator(); iterator.hasNext();) {
				ScheduleStageDTO scheduleStageDTO = iterator.next();
				if (scheduleStageDTO.getGroup() != null && scheduleStageDTO.getGroup().getId() != 0) {
					scheduleStageDTO.setGroup(groupService.getGroup(authDTO, scheduleStageDTO.getGroup()));
				}
				if (stationMap.get(scheduleStageDTO.getFromStation().getId()) != null) {
					ScheduleStationDTO stationDTO = stationMap.get(scheduleStageDTO.getFromStation().getId());
					scheduleStageDTO.setFromStationSequence(stationDTO.getStationSequence());
				}
				if (stationMap.get(scheduleStageDTO.getToStation().getId()) != null) {
					ScheduleStationDTO stationDTO = stationMap.get(scheduleStageDTO.getToStation().getId());
					scheduleStageDTO.setToStationSequence(stationDTO.getStationSequence());
				}
			}
			scheduleDTO.setScheduleStageList(scheduleStageDTOList);
			scheduleDTO.setStationPointList(stationPointList);

			if (scheduleStageDTOList.isEmpty() || stationList.isEmpty() || stationPointList.isEmpty()) {
				getRefresh(authDTO, scheduleDTO);
				throw new ServiceException(ErrorCode.REQURIED_SCHEDULE_DATA, "Station or Station points are empty");
			}
			// Bus Type and BusMap
			ScheduleBusDTO scheduleBusDTO = busService.getByScheduleId(authDTO, scheduleDTO);
			if (scheduleBusDTO == null || scheduleBusDTO.getBus() == null) {
				getRefresh(authDTO, scheduleDTO);
				throw new ServiceException(ErrorCode.REQURIED_SCHEDULE_DATA, "Active Bus Map not found");
			}
			BusCache busCache = new BusCache();
			scheduleBusDTO.setBus(busCache.getBusDTObyId(authDTO, scheduleBusDTO.getBus()));
			if (scheduleBusDTO.getBus().getActiveFlag() != 1) {
				getRefresh(authDTO, scheduleDTO);
				throw new ServiceException(ErrorCode.REQURIED_SCHEDULE_DATA, "Active Bus Map not found");
			}
			scheduleDTO.setScheduleBus(scheduleBusDTO);
		}
		else {
			throw new ServiceException(ErrorCode.REQURIED_SCHEDULE_DATA, "This is not active schedule");
		}

		Map<Integer, ScheduleStationDTO> stationMap = new HashMap<>();
		for (ScheduleStationDTO stationDTO : scheduleDTO.getStationList()) {
			stationMap.put(stationDTO.getStation().getId(), stationDTO);
		}
		for (Iterator<ScheduleStationPointDTO> iterator = scheduleDTO.getStationPointList().iterator(); iterator.hasNext();) {
			ScheduleStationPointDTO pointDTO = iterator.next();
			if (stationMap.get(pointDTO.getStation().getId()) == null) {
				iterator.remove();
				continue;
			}
		}
		for (Iterator<ScheduleStageDTO> iterator = scheduleDTO.getScheduleStageList().iterator(); iterator.hasNext();) {
			ScheduleStageDTO scheduleStageDTO = iterator.next();
			if (stationMap.get(scheduleStageDTO.getFromStation().getId()) == null || stationMap.get(scheduleStageDTO.getToStation().getId()) == null) {
				iterator.remove();
				continue;
			}
		}
		ScheduleDTO clonescScheduleDTO = new ScheduleDTO();
		clonescScheduleDTO.setName("copy of " + scheduleDTO.getName());
		clonescScheduleDTO.setActiveFrom(scheduleDTO.getActiveFrom());
		clonescScheduleDTO.setActiveTo(scheduleDTO.getActiveTo());
		clonescScheduleDTO.setActiveFlag(scheduleDTO.getActiveFlag());
		clonescScheduleDTO.setApiDisplayName(scheduleDTO.getApiDisplayName());
		clonescScheduleDTO.setDayOfWeek(scheduleDTO.getDayOfWeek());
		clonescScheduleDTO.setDisplayName(scheduleDTO.getDisplayName());
		clonescScheduleDTO.setServiceNumber(scheduleDTO.getServiceNumber());
		clonescScheduleDTO.setPnrStartCode(scheduleDTO.getPnrStartCode());

		clonescScheduleDTO = Update(authDTO, clonescScheduleDTO);

		// Schedule Station
		if (entityList.contains("STATION")) {
			ScheduleStationDTO scheduleStationDTO = new ScheduleStationDTO();
			scheduleStationDTO.setSchedule(scheduleDTO);
			List<ScheduleStationDTO> stationList = scheduleStationService.get(authDTO, scheduleStationDTO);
			List<String> stationCodes = null;
			if (entityList.contains("EXCLUDESTATIONEXCEPTION")) {
				stationCodes = stationList.stream().flatMap(o -> o.getOverrideList().stream()).filter(StreamUtil.distinctByKey(p -> p.getLookupCode())).filter(p -> p.getMinitues() == -1 && StringUtil.isNotNull(p.getLookupCode())).map(p -> p.getLookupCode()).collect(Collectors.toList());
			}

			List<ScheduleStationDTO> stations = new ArrayList<>();
			for (ScheduleStationDTO stationDTO : stationList) {
				if (stationCodes != null && (stationCodes.contains(String.valueOf(stationDTO.getId())) || stationCodes.contains(stationDTO.getLookupCode()))) {
					continue;
				}
				stationDTO.setId(0);
				stationDTO.setCode(null);
				stationDTO.setSchedule(clonescScheduleDTO);
				if (entityList.contains("NIGHTTODAYSERVICE")) {
					stationDTO.setMinitues(stationDTO.getMinitues() - 720);
				}
				stations.add(stationDTO);
			}
			ScheduleStationDTO cloneStationDTO = new ScheduleStationDTO();
			Comparator<ScheduleStationDTO> comp = new BeanComparator("stationSequence");
			Collections.sort(stations, comp);
			cloneStationDTO.setList(stations);
			scheduleStationService.Update(authDTO, cloneStationDTO);
		}
		// Schedule Station Point
		if (entityList.contains("STATIONPOINT")) {
			ScheduleStationPointDTO pointDTO = new ScheduleStationPointDTO();
			pointDTO.setSchedule(scheduleDTO);
			List<ScheduleStationPointDTO> stationPointList = stationPointService.get(authDTO, pointDTO);
			List<String> stationPointCodes = null;
			if (entityList.contains("EXCLUDESTATIONPOINTEXCEPTION")) {
				stationPointCodes = stationPointList.stream().flatMap(o -> o.getOverrideList().stream()).filter(StreamUtil.distinctByKey(p -> p.getLookupCode())).filter(p -> p.getMinitues() == -1 && StringUtil.isNotNull(p.getLookupCode())).map(p -> p.getLookupCode()).collect(Collectors.toList());
			}
			
			List<String> vanPickupPointsCodes = null;
			if (entityList.contains("EXCLUDEVANPICKUPSTATIONPOINTS")) {
				vanPickupPointsCodes = stationPointList.stream().filter(stp -> stp.getBusVehicleVanPickup() != null && stp.getBusVehicleVanPickup().getId() != 0).map(stp -> stp.getCode()).collect(Collectors.toList());
			}

			List<ScheduleStationPointDTO> stationPoints = new ArrayList<>();
			for (ScheduleStationPointDTO stationPointDTO : stationPointList) {
				if (stationPointCodes != null && (stationPointCodes.contains(String.valueOf(stationPointDTO.getId())) || stationPointCodes.contains(stationPointDTO.getLookupCode()))) {
					continue;
				}
				if (vanPickupPointsCodes != null && vanPickupPointsCodes.contains(stationPointDTO.getCode())) {
					continue;
				}
				stationPointDTO.setId(0);
				stationPointDTO.setCode(null);
				stationPointDTO.setSchedule(clonescScheduleDTO);
				stationPoints.add(stationPointDTO);
			}
			ScheduleStationPointDTO stationPointDTO = new ScheduleStationPointDTO();
			stationPointDTO.setList(stationPoints);
			stationPointService.Update(authDTO, stationPointDTO);
		}
		// Schedule Stage
		if (entityList.contains("STAGEFARE")) {
			List<ScheduleStageDTO> stageList = stageService.get(authDTO, scheduleDTO);
			for (ScheduleStageDTO scheduleStageDTO : stageList) {
				scheduleStageDTO.setId(0);
				scheduleStageDTO.setCode(null);
				scheduleStageDTO.setSchedule(clonescScheduleDTO);
			}
			ScheduleStageDTO scheduleStageDTO = new ScheduleStageDTO();
			scheduleStageDTO.setList(stageList);
			stageService.Update(authDTO, scheduleStageDTO);
		}
		// Schedule cancellation Terms
		if (entityList.contains("TERMS")) {
			ScheduleCancellationTermDTO scheduleCancellationTermDTO = new ScheduleCancellationTermDTO();
			scheduleCancellationTermDTO.setSchedule(scheduleDTO);
			List<ScheduleCancellationTermDTO> cancellationList = cancellationTermService.get(authDTO, scheduleCancellationTermDTO);
			for (ScheduleCancellationTermDTO cancellationTermDTO : cancellationList) {
				cancellationTermDTO.setId(0);
				cancellationTermDTO.setCode(null);
				cancellationTermDTO.setSchedule(clonescScheduleDTO);
			}
			ScheduleCancellationTermDTO cancellationTermDTO = new ScheduleCancellationTermDTO();
			cancellationTermDTO.setList(cancellationList);
			cancellationTermService.Update(authDTO, cancellationTermDTO);
		}
		// Schedule Bus
		if (entityList.contains("BUS")) {
			ScheduleBusDTO busDTO = scheduleDTO.getScheduleBus();
			busDTO.setId(0);
			busDTO.setCode(null);
			busDTO.setSchedule(clonescScheduleDTO);
			busService.Update(authDTO, busDTO);
		}
		// Schedule Control Terms
		if (entityList.contains("CONTROL")) {
			ScheduleControlDTO scheduleControlDTO = new ScheduleControlDTO();
			scheduleControlDTO.setSchedule(scheduleDTO);
			List<ScheduleControlDTO> controlList = controlService.get(authDTO, scheduleControlDTO);
			for (ScheduleControlDTO controlDTO : controlList) {
				controlDTO.setId(0);
				controlDTO.setCode(null);
				controlDTO.setSchedule(clonescScheduleDTO);
			}
			scheduleControlDTO.setList(controlList);
			controlService.Update(authDTO, scheduleControlDTO);
		}
		// Schedule Seat Visibility
		if (entityList.contains("VISIBILITY")) {
			List<UserDTO> userList = visibilityService.get(authDTO, scheduleDTO.getCode());
			if (!userList.isEmpty()) {
				visibilityService.Update(authDTO, clonescScheduleDTO.getCode(), userList);
			}
		}
		return clonescScheduleDTO;
	}

	public ScheduleDTO reverseClone(AuthDTO authDTO, ScheduleDTO scheduleDTO, List<String> entityList) {

		ScheduleCache scheduleCache = new ScheduleCache();
		ScheduleDAO dao = new ScheduleDAO();
		scheduleDTO = dao.getActiveSchedule(authDTO, scheduleDTO);
		if (scheduleDTO.getId() != 0) {
			// Station
			List<ScheduleStationDTO> stationList = scheduleStationService.getScheduleStation(authDTO, scheduleDTO);

			Map<Integer, ScheduleStationDTO> sequenceStationMap = new HashMap<>();
			List<ScheduleStationDTO> sequenceStationList = scheduleStationService.getScheduleStation(authDTO, scheduleDTO);

			for (ScheduleStationDTO scheduleStationDTO : sequenceStationList) {
				sequenceStationMap.put(scheduleStationDTO.getStationSequence(), scheduleStationDTO);
			}

			// Sorting
			Collections.sort(stationList, new Comparator<ScheduleStationDTO>() {
				@Override
				public int compare(ScheduleStationDTO a, ScheduleStationDTO b) {
					return b.getStationSequence() > a.getStationSequence() ? Numeric.ONE_INT : -1;
				}
			});

			// Sorting
			Collections.sort(sequenceStationList, new Comparator<ScheduleStationDTO>() {
				@Override
				public int compare(ScheduleStationDTO a, ScheduleStationDTO b) {
					return b.getStationSequence() > a.getStationSequence() ? Numeric.ONE_INT : -1;
				}
			});
			ScheduleStationDTO lastStation = sequenceStationList.get(Numeric.ZERO_INT);
			int firstStationMinutes = Numeric.ZERO_INT;
			int stationSequence = Numeric.ONE_INT;

			Map<Integer, ScheduleStationDTO> stationMap = new HashMap<>();
			// Validate all stations
			for (Iterator<ScheduleStationDTO> iterator = stationList.iterator(); iterator.hasNext();) {
				ScheduleStationDTO stationDTO = iterator.next();
				ScheduleStationDTO scheduleStationDTO = sequenceStationMap.get(stationSequence);
				stationDTO.setStation(getStationDTObyId(stationDTO.getStation()));
				stationDTO.setStationSequence(stationSequence);

				int minutes = Numeric.ZERO_INT;
				if (lastStation.getStation().getId() != stationDTO.getStation().getId()) {
					minutes = lastStation.getMinitues() - stationDTO.getMinitues();
					minutes = firstStationMinutes + minutes;
				}
				else {
					firstStationMinutes = scheduleStationDTO.getMinitues();
					minutes = firstStationMinutes;
				}
				stationDTO.setMinitues(minutes);
				stationMap.put(stationDTO.getStation().getId(), stationDTO);

				stationSequence = stationSequence + Numeric.ONE_INT;
			}

			// Schedule Station Point
			List<ScheduleStationPointDTO> stationPointList = stationPointService.getScheduleStationPoint(authDTO, scheduleDTO);
			scheduleDTO.setStationList(stationList);
			List<ScheduleStageDTO> scheduleStageDTOList = scheduleCache.getScheduleStageDTO(authDTO, scheduleDTO);
			for (Iterator<ScheduleStageDTO> iterator = scheduleStageDTOList.iterator(); iterator.hasNext();) {
				ScheduleStageDTO scheduleStageDTO = iterator.next();

				StationDTO fromStationDTO = scheduleStageDTO.getToStation();
				StationDTO toStationDTO = scheduleStageDTO.getFromStation();

				if (scheduleStageDTO.getGroup() != null && scheduleStageDTO.getGroup().getId() != 0) {
					scheduleStageDTO.setGroup(groupService.getGroup(authDTO, scheduleStageDTO.getGroup()));
				}
				if (stationMap.get(fromStationDTO.getId()) != null) {
					ScheduleStationDTO stationDTO = stationMap.get(fromStationDTO.getId());
					scheduleStageDTO.setFromStationSequence(stationDTO.getStationSequence());
				}
				if (stationMap.get(toStationDTO.getId()) != null) {
					ScheduleStationDTO stationDTO = stationMap.get(toStationDTO.getId());
					scheduleStageDTO.setToStationSequence(stationDTO.getStationSequence());
				}
				scheduleStageDTO.setFromStation(fromStationDTO);
				scheduleStageDTO.setToStation(toStationDTO);
			}
			scheduleDTO.setScheduleStageList(scheduleStageDTOList);
			scheduleDTO.setStationPointList(stationPointList);

			if (scheduleStageDTOList.isEmpty() || stationList.isEmpty() || stationPointList.isEmpty()) {
				getRefresh(authDTO, scheduleDTO);
				throw new ServiceException(ErrorCode.REQURIED_SCHEDULE_DATA, "Station or Station points are empty");
			}
			// Bus Type and BusMap
			ScheduleBusDTO scheduleBusDTO = busService.getByScheduleId(authDTO, scheduleDTO);
			if (scheduleBusDTO == null || scheduleBusDTO.getBus() == null) {
				getRefresh(authDTO, scheduleDTO);
				throw new ServiceException(ErrorCode.REQURIED_SCHEDULE_DATA, "Active Bus Map not found");
			}
			BusCache busCache = new BusCache();
			scheduleBusDTO.setBus(busCache.getBusDTObyId(authDTO, scheduleBusDTO.getBus()));
			if (scheduleBusDTO.getBus().getActiveFlag() != 1) {
				getRefresh(authDTO, scheduleDTO);
				throw new ServiceException(ErrorCode.REQURIED_SCHEDULE_DATA, "Active Bus Map not found");
			}
			scheduleDTO.setScheduleBus(scheduleBusDTO);
		}
		else {
			throw new ServiceException(ErrorCode.REQURIED_SCHEDULE_DATA, "This is not active schedule");
		}

		Map<Integer, ScheduleStationDTO> stationMap = new HashMap<>();
		for (ScheduleStationDTO stationDTO : scheduleDTO.getStationList()) {
			stationMap.put(stationDTO.getStation().getId(), stationDTO);
		}
		for (Iterator<ScheduleStationPointDTO> iterator = scheduleDTO.getStationPointList().iterator(); iterator.hasNext();) {
			ScheduleStationPointDTO pointDTO = iterator.next();
			if (stationMap.get(pointDTO.getStation().getId()) == null) {
				iterator.remove();
				continue;
			}
		}
		Map<Integer, ScheduleStageDTO> scheduleStageMap = new HashMap<Integer, ScheduleStageDTO>();
		for (Iterator<ScheduleStageDTO> iterator = scheduleDTO.getScheduleStageList().iterator(); iterator.hasNext();) {
			ScheduleStageDTO scheduleStageDTO = iterator.next();
			if (stationMap.get(scheduleStageDTO.getFromStation().getId()) == null || stationMap.get(scheduleStageDTO.getToStation().getId()) == null) {
				iterator.remove();
				continue;
			}
			scheduleStageMap.put(scheduleStageDTO.getId(), scheduleStageDTO);
		}
		ScheduleDTO clonescScheduleDTO = new ScheduleDTO();
		clonescScheduleDTO.setName("Reverse of " + scheduleDTO.getName());
		clonescScheduleDTO.setActiveFrom(scheduleDTO.getActiveFrom());
		clonescScheduleDTO.setActiveTo(scheduleDTO.getActiveTo());
		clonescScheduleDTO.setActiveFlag(scheduleDTO.getActiveFlag());
		clonescScheduleDTO.setApiDisplayName(scheduleDTO.getApiDisplayName());
		clonescScheduleDTO.setDayOfWeek(scheduleDTO.getDayOfWeek());
		clonescScheduleDTO.setDisplayName(scheduleDTO.getDisplayName());
		clonescScheduleDTO.setServiceNumber(scheduleDTO.getServiceNumber());
		clonescScheduleDTO.setPnrStartCode(scheduleDTO.getPnrStartCode());

		clonescScheduleDTO = Update(authDTO, clonescScheduleDTO);

		// Schedule Station
		if (entityList.contains("STATION")) {
			ScheduleStationDTO scheduleStationDTO = new ScheduleStationDTO();
			scheduleStationDTO.setSchedule(scheduleDTO);
			List<ScheduleStationDTO> stationList = scheduleStationService.get(authDTO, scheduleStationDTO);
			List<String> stationCodes = null;
			if (entityList.contains("EXCLUDESTATIONEXCEPTION")) {
				stationCodes = stationList.stream().flatMap(o -> o.getOverrideList().stream()).filter(StreamUtil.distinctByKey(p -> p.getLookupCode())).filter(p -> p.getMinitues() == -1 && StringUtil.isNotNull(p.getLookupCode())).map(p -> p.getLookupCode()).collect(Collectors.toList());
			}

			List<ScheduleStationDTO> stations = new ArrayList<>();
			for (ScheduleStationDTO stationDTO : stationList) {
				if (stationCodes != null && (stationCodes.contains(String.valueOf(stationDTO.getId())) || stationCodes.contains(stationDTO.getLookupCode()))) {
					continue;
				}
				ScheduleStationDTO scheduleStation = stationMap.get(stationDTO.getStation().getId());
				stationDTO.setId(0);
				stationDTO.setCode(null);
				stationDTO.setMinitues(scheduleStation.getMinitues());
				stationDTO.setStationSequence(scheduleStation.getStationSequence());
				stationDTO.setSchedule(clonescScheduleDTO);
				if (entityList.contains("NIGHTTODAYSERVICE")) {
					stationDTO.setMinitues(stationDTO.getMinitues() - 720);
				}
				stations.add(stationDTO);
			}
			ScheduleStationDTO cloneStationDTO = new ScheduleStationDTO();
			Comparator<ScheduleStationDTO> comp = new BeanComparator("stationSequence");
			Collections.sort(stations, comp);
			cloneStationDTO.setList(stations);
			scheduleStationService.Update(authDTO, cloneStationDTO);
		}
		// Schedule Station Point
		if (entityList.contains("STATIONPOINT")) {
			ScheduleStationPointDTO pointDTO = new ScheduleStationPointDTO();
			pointDTO.setSchedule(scheduleDTO);
			List<ScheduleStationPointDTO> stationPointList = stationPointService.get(authDTO, pointDTO);
			List<String> stationPointCodes = null;
			if (entityList.contains("EXCLUDESTATIONPOINTEXCEPTION")) {
				stationPointCodes = stationPointList.stream().flatMap(o -> o.getOverrideList().stream()).filter(StreamUtil.distinctByKey(p -> p.getLookupCode())).filter(p -> p.getMinitues() == -1 && StringUtil.isNotNull(p.getLookupCode())).map(p -> p.getLookupCode()).collect(Collectors.toList());
			}
			
			List<String> vanPickupPointsCodes = null;
			if (entityList.contains("EXCLUDEVANPICKUPSTATIONPOINTS")) {
				vanPickupPointsCodes = stationPointList.stream().filter(stp -> stp.getBusVehicleVanPickup() != null && stp.getBusVehicleVanPickup().getId() != 0).map(stp -> stp.getCode()).collect(Collectors.toList());
			}

			List<ScheduleStationPointDTO> stationPoints = new ArrayList<>();
			for (ScheduleStationPointDTO stationPointDTO : stationPointList) {
				if (stationPointCodes != null && (stationPointCodes.contains(String.valueOf(stationPointDTO.getId())) || stationPointCodes.contains(stationPointDTO.getLookupCode()))) {
					continue;
				}
				if (vanPickupPointsCodes != null && vanPickupPointsCodes.contains(stationPointDTO.getCode())) {
					continue;
				}
				stationPointDTO.setId(0);
				stationPointDTO.setCode(null);
				stationPointDTO.setMinitues(-stationPointDTO.getMinitues());
				stationPointDTO.setSchedule(clonescScheduleDTO);
				stationPoints.add(stationPointDTO);
			}
			ScheduleStationPointDTO stationPointDTO = new ScheduleStationPointDTO();
			stationPointDTO.setList(stationPoints);
			stationPointService.Update(authDTO, stationPointDTO);
		}
		// Schedule Stage
		if (entityList.contains("STAGEFARE")) {
			List<ScheduleStageDTO> stageList = stageService.get(authDTO, scheduleDTO);
			for (ScheduleStageDTO scheduleStageDTO : stageList) {
				ScheduleStageDTO scheduleStage = scheduleStageMap.get(scheduleStageDTO.getId());
				if (scheduleStage == null) {
					continue;
				}
				scheduleStageDTO.setId(0);
				scheduleStageDTO.setCode(null);
				scheduleStageDTO.setFromStationSequence(scheduleStage.getFromStationSequence());
				scheduleStageDTO.setToStationSequence(scheduleStage.getToStationSequence());
				scheduleStageDTO.setFromStation(scheduleStage.getFromStation());
				scheduleStageDTO.setToStation(scheduleStage.getToStation());
				scheduleStageDTO.setSchedule(clonescScheduleDTO);
			}
			ScheduleStageDTO scheduleStageDTO = new ScheduleStageDTO();
			scheduleStageDTO.setList(stageList);
			stageService.Update(authDTO, scheduleStageDTO);
		}
		// Schedule cancellation Terms
		if (entityList.contains("TERMS")) {
			ScheduleCancellationTermDTO scheduleCancellationTermDTO = new ScheduleCancellationTermDTO();
			scheduleCancellationTermDTO.setSchedule(scheduleDTO);
			List<ScheduleCancellationTermDTO> cancellationList = cancellationTermService.get(authDTO, scheduleCancellationTermDTO);
			for (ScheduleCancellationTermDTO cancellationTermDTO : cancellationList) {
				cancellationTermDTO.setId(0);
				cancellationTermDTO.setCode(null);
				cancellationTermDTO.setSchedule(clonescScheduleDTO);
			}
			ScheduleCancellationTermDTO cancellationTermDTO = new ScheduleCancellationTermDTO();
			cancellationTermDTO.setList(cancellationList);
			cancellationTermService.Update(authDTO, cancellationTermDTO);
		}
		// Schedule Bus
		if (entityList.contains("BUS")) {
			ScheduleBusDTO busDTO = scheduleDTO.getScheduleBus();
			busDTO.setId(0);
			busDTO.setCode(null);
			busDTO.setSchedule(clonescScheduleDTO);
			busService.Update(authDTO, busDTO);
		}
		// Schedule Control Terms
		if (entityList.contains("CONTROL")) {
			ScheduleControlDTO scheduleControlDTO = new ScheduleControlDTO();
			scheduleControlDTO.setSchedule(scheduleDTO);
			List<ScheduleControlDTO> controlList = controlService.get(authDTO, scheduleControlDTO);
			for (ScheduleControlDTO controlDTO : controlList) {
				controlDTO.setId(0);
				controlDTO.setCode(null);
				controlDTO.setSchedule(clonescScheduleDTO);
			}
			scheduleControlDTO.setList(controlList);
			controlService.Update(authDTO, scheduleControlDTO);
		}
		return clonescScheduleDTO;
	}

	public ScheduleDTO getScheduleDetails(AuthDTO authDTO, ScheduleDTO schedule) {
		ScheduleDAO dao = new ScheduleDAO();
		schedule = dao.getActiveSchedule(authDTO, schedule);
		ScheduleBusDTO scheduleBus = busService.getByScheduleId(authDTO, schedule);
		List<ScheduleStationDTO> stationList = scheduleStationService.getScheduleStation(authDTO, schedule);
		List<ScheduleStageDTO> stageList = stageService.getScheduleStageV2(authDTO, schedule);
		///
		StageStationDTO firstStation = null;
		StageStationDTO lastStation = null;
		Map<Integer, StageStationDTO> stationMap = new HashMap<>();
		for (ScheduleStationDTO stationDTO : stationList) {
			StageStationDTO stageStationDTO = new StageStationDTO();
			stageStationDTO.setMinitues(stationDTO.getMinitues());
			stageStationDTO.setStationSequence(stationDTO.getStationSequence());
			stageStationDTO.setStation(stationDTO.getStation());
			stationMap.put(stationDTO.getStation().getId(), stageStationDTO);
			if (firstStation == null || stationDTO.getStationSequence() < firstStation.getStationSequence()) {
				firstStation = stageStationDTO;
			}
		}
		Map<String, StageDTO> stageMap = new HashMap<>();
		for (ScheduleStageDTO scheduleStageDTO : stageList) {
			StageDTO stageDTO = new StageDTO();
			if (stationMap.get(scheduleStageDTO.getFromStation().getId()) != null && stationMap.get(scheduleStageDTO.getToStation().getId()) != null) {
				stageDTO.setFromStation(stationMap.get(scheduleStageDTO.getFromStation().getId()));
				stageDTO.setToStation(stationMap.get(scheduleStageDTO.getToStation().getId()));
				stageDTO.getFromStation().setStation(stationService.getStation(scheduleStageDTO.getFromStation()));
				stageDTO.getToStation().setStation(stationService.getStation(scheduleStageDTO.getToStation()));
				stageDTO.getFromStation().setMinitues(stageDTO.getFromStation().getMinitues());
				stageDTO.getToStation().setMinitues(stageDTO.getToStation().getMinitues());
				stageMap.put(stageDTO.getFromStation().getStation().getId() + "_" + stageDTO.getToStation().getStation().getId(), stageDTO);
				if (stageMap.get(firstStation.getStation().getId() + "_" + stageDTO.getToStation().getStation().getId()) != null && (lastStation == null || stageDTO.getToStation().getStationSequence() > lastStation.getStationSequence())) {
					lastStation = stageDTO.getToStation();
				}
			}
		}
		List<StageDTO> finalStageList = new ArrayList<StageDTO>(stageMap.values());
		for (StageDTO stage : finalStageList) {
			stage.setStageSequence(Integer.parseInt(stage.getFromStation().getStationSequence() + "" + (lastStation.getStationSequence() - stage.getToStation().getStationSequence())));
		}
		// Sorting Trips
		Collections.sort(finalStageList, new Comparator<StageDTO>() {
			@Override
			public int compare(StageDTO t1, StageDTO t2) {
				return new CompareToBuilder().append(t1.getStageSequence(), t2.getStageSequence()).toComparison();
			}
		});
		schedule.setStageList(finalStageList);
		///
		schedule.getAdditionalAttributes().put(firstStation.getStation().getId() + "_" + lastStation.getStation().getId(), firstStation.getStation().getId() + "_" + lastStation.getStation().getId());

		schedule.setScheduleBus(scheduleBus);
		return schedule;

	}

	public ScheduleDTO getActiveSchedule(AuthDTO authDTO, ScheduleDTO schedule) {
		DateTime tripDate = schedule.getTripDate();
		// common Schedule validations
		DateTime scheduleFromDate = new DateTime(schedule.getActiveFrom());
		DateTime scheduleEndDate = new DateTime(schedule.getActiveTo());

		try {
			if (!tripDate.gteq(scheduleFromDate)) {
				schedule.setActiveFlag(Numeric.ZERO_INT);
				throw new ServiceException(ErrorCode.SCHEDULE_NOT_ACTIVE, "not active on from date");
			}
			if (!tripDate.lteq(scheduleEndDate)) {
				schedule.setActiveFlag(Numeric.ZERO_INT);
				throw new ServiceException(ErrorCode.SCHEDULE_NOT_ACTIVE, "not active on to date");
			}
			if (schedule.getDayOfWeek() == null || schedule.getDayOfWeek().length() != 7) {
				schedule.setActiveFlag(Numeric.ZERO_INT);
				throw new ServiceException(ErrorCode.SCHEDULE_NOT_ACTIVE, "not active on DayOfWeek");
			}
			if (schedule.getDayOfWeek().substring(tripDate.getWeekDay() - 1, tripDate.getWeekDay()).equals("0")) {
				schedule.setActiveFlag(Numeric.ZERO_INT);
				throw new ServiceException(ErrorCode.SCHEDULE_NOT_ACTIVE, "not active on DayOfWeek");
			}

			// check for any exception has been added
			List<ScheduleDTO> scheduleOverrideList = schedule.getOverrideList();
			for (Iterator<ScheduleDTO> itrlookupSchedule = scheduleOverrideList.iterator(); itrlookupSchedule.hasNext();) {
				ScheduleDTO lookupscheduleDTO = itrlookupSchedule.next();
				// common validations
				if (!tripDate.gteq(new DateTime(lookupscheduleDTO.getActiveFrom()))) {
					itrlookupSchedule.remove();
					continue;
				}
				if (!tripDate.lteq(new DateTime(lookupscheduleDTO.getActiveTo()))) {
					itrlookupSchedule.remove();
					continue;
				}
				if (lookupscheduleDTO.getDayOfWeek() == null || lookupscheduleDTO.getDayOfWeek().length() != 7) {
					itrlookupSchedule.remove();
					continue;
				}
				if (lookupscheduleDTO.getDayOfWeek().substring(tripDate.getWeekDay() - 1, tripDate.getWeekDay()).equals("0")) {
					itrlookupSchedule.remove();
					continue;
				}
			}
			if (!scheduleOverrideList.isEmpty()) {
				schedule.setActiveFlag(Numeric.ZERO_INT);
				throw new ServiceException(ErrorCode.SCHEDULE_NOT_ACTIVE, "exception added on the schedule");
			}
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return schedule;
	}

	private void updateSectorSchedule(AuthDTO authDTO, ScheduleDTO scheduleDTO, List<SectorDTO> sectorList) {
		if (sectorList != null && StringUtil.isNotNull(scheduleDTO.getCode()) && scheduleDTO.getActiveFlag() == 1 && StringUtil.isNull(scheduleDTO.getLookupCode())) {
			for (SectorDTO sectorDTO : sectorList) {
				sectorDTO.setActiveFlag(Numeric.ONE_INT);
				sectorService.updateSectorSchedule(authDTO, sectorDTO, scheduleDTO);
			}
		}
	}
}
