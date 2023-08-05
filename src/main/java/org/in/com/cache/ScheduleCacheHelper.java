package org.in.com.cache;

import java.util.ArrayList;
import java.util.List;

import org.in.com.cache.dto.ScheduleBusCacheDTO;
import org.in.com.cache.dto.ScheduleCacheDTO;
import org.in.com.cache.dto.ScheduleCancellationTermCacheDTO;
import org.in.com.cache.dto.ScheduleControlCacheDTO;
import org.in.com.cache.dto.ScheduleStageCacheDTO;
import org.in.com.cache.dto.ScheduleStationCacheDTO;
import org.in.com.cache.dto.ScheduleStationPointCacheDTO;
import org.in.com.cache.dto.ScheduleStationPointExceptionCacheDTO;
import org.in.com.cache.dto.ScheduleVirtualSeatBlockCacheDTO;
import org.in.com.constants.Numeric;
import org.in.com.dto.AmenitiesDTO;
import org.in.com.dto.BusBreakevenSettingsDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusVehicleVanPickupDTO;
import org.in.com.dto.CancellationTermDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.NamespaceTaxDTO;
import org.in.com.dto.ScheduleBusDTO;
import org.in.com.dto.ScheduleCancellationTermDTO;
import org.in.com.dto.ScheduleCategoryDTO;
import org.in.com.dto.ScheduleControlDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleStageDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.ScheduleStationPointDTO;
import org.in.com.dto.ScheduleTagDTO;
import org.in.com.dto.ScheduleVirtualSeatBlockDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.enumeration.BusSeatTypeEM;
import org.in.com.utils.StringUtil;

public class ScheduleCacheHelper extends CacheCentral {

	protected void bindScheduleToCacheObject(ScheduleCacheDTO cacheDTO, ScheduleDTO scheduleDTO) {
		cacheDTO.setActiveFlag(scheduleDTO.getActiveFlag());
		cacheDTO.setId(scheduleDTO.getId());
		cacheDTO.setCode(scheduleDTO.getCode());
		cacheDTO.setActiveFrom(scheduleDTO.getActiveFrom());
		cacheDTO.setActiveTo(scheduleDTO.getActiveTo());
		cacheDTO.setServiceNumber(scheduleDTO.getServiceNumber());
		cacheDTO.setName(scheduleDTO.getName());
		cacheDTO.setDayOfWeek(scheduleDTO.getDayOfWeek());
		cacheDTO.setDisplayName(scheduleDTO.getDisplayName());
		cacheDTO.setApiDisplayName(scheduleDTO.getApiDisplayName());
		cacheDTO.setPnrStartCode(scheduleDTO.getPnrStartCode());
		cacheDTO.setPreRequrities(scheduleDTO.getPreRequrities());
		cacheDTO.setCategoryId(scheduleDTO.getCategory() != null ? scheduleDTO.getCategory().getId() : 0);
		cacheDTO.setTagId(scheduleDTO.getScheduleTagIds());
		List<ScheduleCacheDTO> overrideScheduleCacheList = null;
		if (scheduleDTO.getOverrideList() != null && !scheduleDTO.getOverrideList().isEmpty()) {
			overrideScheduleCacheList = new ArrayList<>();
			for (ScheduleDTO dto : scheduleDTO.getOverrideList()) {
				ScheduleCacheDTO scheduleCacheDTO = new ScheduleCacheDTO();
				scheduleCacheDTO.setActiveFlag(dto.getActiveFlag());
				scheduleCacheDTO.setActiveFrom(dto.getActiveFrom());
				scheduleCacheDTO.setActiveTo(dto.getActiveTo());
				scheduleCacheDTO.setServiceNumber(dto.getServiceNumber());
				scheduleCacheDTO.setName(dto.getName());
				scheduleCacheDTO.setDayOfWeek(dto.getDayOfWeek());
				scheduleCacheDTO.setDisplayName(dto.getDisplayName());
				scheduleCacheDTO.setPnrStartCode(dto.getPnrStartCode());
				scheduleCacheDTO.setPreRequrities(dto.getPreRequrities());
				overrideScheduleCacheList.add(scheduleCacheDTO);
			}
			cacheDTO.setOverrideListCacheDTO(overrideScheduleCacheList);
		}
	}

	protected void bindScheduleFromCacheObject(ScheduleCacheDTO cacheDTO, ScheduleDTO scheduleDTO) {

		scheduleDTO.setId(cacheDTO.getId());
		scheduleDTO.setActiveFlag(cacheDTO.getActiveFlag());
		scheduleDTO.setCode(cacheDTO.getCode());
		scheduleDTO.setName(cacheDTO.getName());
		scheduleDTO.setActiveFrom(cacheDTO.getActiveFrom());
		scheduleDTO.setActiveTo(cacheDTO.getActiveTo());
		scheduleDTO.setServiceNumber(cacheDTO.getServiceNumber());
		scheduleDTO.setDisplayName(cacheDTO.getDisplayName());
		scheduleDTO.setApiDisplayName(cacheDTO.getApiDisplayName());
		scheduleDTO.setDayOfWeek(cacheDTO.getDayOfWeek());
		scheduleDTO.setDisplayName(cacheDTO.getDisplayName());
		scheduleDTO.setPnrStartCode(cacheDTO.getPnrStartCode());
		scheduleDTO.setPreRequrities(cacheDTO.getPreRequrities());

		ScheduleCategoryDTO categoryDTO = new ScheduleCategoryDTO();
		categoryDTO.setId(cacheDTO.getCategoryId());
		scheduleDTO.setCategory(categoryDTO);

		List<ScheduleTagDTO> scheduleTagList = getScheduleTagList(cacheDTO.getTagId());
		scheduleDTO.setScheduleTagList(scheduleTagList);

		scheduleDTO.setLookupCode(cacheDTO.getLookupCode());
		List<ScheduleDTO> overrideScheduleList = null;
		if (cacheDTO.getOverrideListCacheDTO() != null && !cacheDTO.getOverrideListCacheDTO().isEmpty()) {
			overrideScheduleList = new ArrayList<>();
			for (ScheduleCacheDTO dto : cacheDTO.getOverrideListCacheDTO()) {
				ScheduleDTO schedule = new ScheduleDTO();
				schedule.setActiveFlag(dto.getActiveFlag());
				schedule.setActiveFrom(dto.getActiveFrom());
				schedule.setActiveTo(dto.getActiveTo());
				schedule.setServiceNumber(dto.getServiceNumber());
				schedule.setDisplayName(dto.getDisplayName());
				schedule.setDayOfWeek(dto.getDayOfWeek());
				schedule.setDisplayName(dto.getDisplayName());
				schedule.setPnrStartCode(dto.getPnrStartCode());
				schedule.setPreRequrities(dto.getPreRequrities());
				schedule.setLookupCode(dto.getLookupCode());
				overrideScheduleList.add(schedule);
			}
			scheduleDTO.setOverrideList(overrideScheduleList);
		}
	}

	protected List<ScheduleControlCacheDTO> bindScheduleControlToCacheObject(List<ScheduleControlDTO> scheduleControlDTOList) {
		List<ScheduleControlCacheDTO> scheduleControlCacheList = new ArrayList<>();
		// copy to cache
		if (scheduleControlDTOList != null && !scheduleControlDTOList.isEmpty()) {
			for (ScheduleControlDTO controlDTO : scheduleControlDTOList) {
				ScheduleControlCacheDTO controlCacheDTO = new ScheduleControlCacheDTO();
				controlCacheDTO.setCode(controlDTO.getCode());
				controlCacheDTO.setActiveFrom(controlDTO.getActiveFrom());
				controlCacheDTO.setActiveTo(controlDTO.getActiveTo());
				controlCacheDTO.setDayOfWeek(controlDTO.getDayOfWeek());
				controlCacheDTO.setAllowBookingFlag(controlDTO.getAllowBookingFlag());
				controlCacheDTO.setCloseMinitues(controlDTO.getCloseMinitues());
				controlCacheDTO.setOpenMinitues(controlDTO.getOpenMinitues());
				controlCacheDTO.setGroupId(controlDTO.getGroup() != null ? controlDTO.getGroup().getId() : 0);
				controlCacheDTO.setFromStationId(controlDTO.getFromStation() != null ? controlDTO.getFromStation().getId() : 0);
				controlCacheDTO.setToStationId(controlDTO.getToStation() != null ? controlDTO.getToStation().getId() : 0);

				List<ScheduleControlCacheDTO> overrideControlList = new ArrayList<>();
				if (controlDTO.getOverrideList() != null && !controlDTO.getOverrideList().isEmpty()) {
					for (ScheduleControlDTO scheduleControlDTO : controlDTO.getOverrideList()) {
						ScheduleControlCacheDTO cacheDTO = new ScheduleControlCacheDTO();
						cacheDTO.setCode(scheduleControlDTO.getCode());
						cacheDTO.setActiveFrom(scheduleControlDTO.getActiveFrom());
						cacheDTO.setActiveTo(scheduleControlDTO.getActiveTo());
						cacheDTO.setDayOfWeek(scheduleControlDTO.getDayOfWeek());
						cacheDTO.setAllowBookingFlag(scheduleControlDTO.getAllowBookingFlag());
						cacheDTO.setCloseMinitues(scheduleControlDTO.getCloseMinitues());
						cacheDTO.setOpenMinitues(scheduleControlDTO.getOpenMinitues());
						cacheDTO.setGroupId(scheduleControlDTO.getGroup() != null ? controlDTO.getGroup().getId() : 0);
						cacheDTO.setFromStationId(scheduleControlDTO.getFromStation() != null ? scheduleControlDTO.getFromStation().getId() : 0);
						cacheDTO.setToStationId(scheduleControlDTO.getToStation() != null ? scheduleControlDTO.getToStation().getId() : 0);
						overrideControlList.add(cacheDTO);
					}
					controlCacheDTO.setOverrideListControlCacheDTO(overrideControlList);
				}
				scheduleControlCacheList.add(controlCacheDTO);
			}
		}
		return scheduleControlCacheList;
	}

	protected List<ScheduleControlDTO> bindScheduleControlFromCacheObject(List<ScheduleControlCacheDTO> controlCacheDTOList) {
		List<ScheduleControlDTO> scheduleControlDTOList = new ArrayList<>();
		if (controlCacheDTOList != null && !controlCacheDTOList.isEmpty()) {
			// copy from cache
			for (ScheduleControlCacheDTO controlCache : controlCacheDTOList) {
				ScheduleControlDTO controlDTO = new ScheduleControlDTO();
				controlDTO.setCode(controlCache.getCode());
				controlDTO.setActiveFrom(controlCache.getActiveFrom());
				controlDTO.setActiveTo(controlCache.getActiveTo());
				controlDTO.setDayOfWeek(controlCache.getDayOfWeek());
				controlDTO.setAllowBookingFlag(controlCache.getAllowBookingFlag());
				controlDTO.setCloseMinitues(controlCache.getCloseMinitues());
				controlDTO.setOpenMinitues(controlCache.getOpenMinitues());
				if (controlCache.getGroupId() != 0) {
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setId(controlCache.getGroupId());
					controlDTO.setGroup(groupDTO);
				}
				if (controlCache.getFromStationId() != 0 && controlCache.getToStationId() != 0) {
					StationDTO fromStationDTO = new StationDTO();
					StationDTO toStationDTO = new StationDTO();
					fromStationDTO.setId(controlCache.getFromStationId());
					toStationDTO.setId(controlCache.getToStationId());
					controlDTO.setFromStation(fromStationDTO);
					controlDTO.setToStation(toStationDTO);
				}
				controlDTO.setLookupCode(controlDTO.getLookupCode());
				List<ScheduleControlDTO> overrideControlList = new ArrayList<>();
				if (controlCache.getOverrideListControlCacheDTO() != null && !controlCache.getOverrideListControlCacheDTO().isEmpty()) {
					for (ScheduleControlCacheDTO overRideCacheDTO : controlCache.getOverrideListControlCacheDTO()) {
						ScheduleControlDTO overrideControlDTO = new ScheduleControlDTO();
						overrideControlDTO.setCode(overRideCacheDTO.getCode());
						overrideControlDTO.setActiveFrom(overRideCacheDTO.getActiveFrom());
						overrideControlDTO.setActiveTo(overRideCacheDTO.getActiveTo());
						overrideControlDTO.setDayOfWeek(overRideCacheDTO.getDayOfWeek());
						overrideControlDTO.setAllowBookingFlag(overRideCacheDTO.getAllowBookingFlag());
						overrideControlDTO.setCloseMinitues(overRideCacheDTO.getCloseMinitues());
						overrideControlDTO.setOpenMinitues(overRideCacheDTO.getOpenMinitues());
						if (overRideCacheDTO.getGroupId() != 0) {
							GroupDTO groupDTO = new GroupDTO();
							groupDTO.setId(overRideCacheDTO.getGroupId());
							overrideControlDTO.setGroup(groupDTO);
						}
						if (overRideCacheDTO.getFromStationId() != 0 && overRideCacheDTO.getToStationId() != 0) {
							StationDTO fromStationDTO = new StationDTO();
							StationDTO toStationDTO = new StationDTO();
							fromStationDTO.setId(overRideCacheDTO.getFromStationId());
							toStationDTO.setId(overRideCacheDTO.getToStationId());
							overrideControlDTO.setFromStation(fromStationDTO);
							overrideControlDTO.setToStation(toStationDTO);
						}
						overrideControlList.add(overrideControlDTO);
					}
					controlDTO.setOverrideList(overrideControlList);
				}
				scheduleControlDTOList.add(controlDTO);
			}
		}
		return scheduleControlDTOList;
	}

	protected List<ScheduleStationCacheDTO> bindScheduleStationToCacheObject(List<ScheduleStationDTO> stationDTOList) {
		List<ScheduleStationCacheDTO> scheduleControlCacheList = new ArrayList<>();
		// copy to cache
		if (stationDTOList != null && !stationDTOList.isEmpty()) {
			for (ScheduleStationDTO stationDTO : stationDTOList) {
				ScheduleStationCacheDTO stationCacheDTO = new ScheduleStationCacheDTO();
				stationCacheDTO.setCode(stationDTO.getCode());
				stationCacheDTO.setActiveFrom(stationDTO.getActiveFrom());
				stationCacheDTO.setActiveTo(stationDTO.getActiveTo());
				stationCacheDTO.setDayOfWeek(stationDTO.getDayOfWeek());
				stationCacheDTO.setMinitues(stationDTO.getMinitues());
				stationCacheDTO.setMobileNumber(stationDTO.getMobileNumber());
				stationCacheDTO.setStationSequence(stationDTO.getStationSequence());
				if (stationDTO.getStation() != null) {
					stationCacheDTO.setStationId(stationDTO.getStation().getId());
				}
				stationCacheDTO.setLookupCode(stationDTO.getLookupCode());
				List<ScheduleStationCacheDTO> overrideControlList = new ArrayList<>();
				if (stationDTO.getOverrideList() != null && !stationDTO.getOverrideList().isEmpty()) {
					for (ScheduleStationDTO scheduleStationDTO : stationDTO.getOverrideList()) {
						ScheduleStationCacheDTO cacheDTO = new ScheduleStationCacheDTO();
						cacheDTO.setCode(scheduleStationDTO.getCode());
						cacheDTO.setActiveFrom(scheduleStationDTO.getActiveFrom());
						cacheDTO.setActiveTo(scheduleStationDTO.getActiveTo());
						cacheDTO.setDayOfWeek(scheduleStationDTO.getDayOfWeek());
						cacheDTO.setLookupCode(scheduleStationDTO.getLookupCode());
						cacheDTO.setMinitues(scheduleStationDTO.getMinitues());
						cacheDTO.setMobileNumber(scheduleStationDTO.getMobileNumber());
						cacheDTO.setStationSequence(scheduleStationDTO.getStationSequence());
						if (scheduleStationDTO.getStation() != null) {
							cacheDTO.setStationId(scheduleStationDTO.getStation().getId());
						}
						overrideControlList.add(cacheDTO);
					}
					stationCacheDTO.setOverrideList(overrideControlList);
				}
				scheduleControlCacheList.add(stationCacheDTO);
			}
		}
		return scheduleControlCacheList;
	}

	protected List<ScheduleStationDTO> bindScheduleStationFromCacheObject(List<ScheduleStationCacheDTO> scheduleStationCacheList) {

		List<ScheduleStationDTO> ScheduleStationDTOList = new ArrayList<>();
		if (scheduleStationCacheList != null && !scheduleStationCacheList.isEmpty()) {
			// copy from cache
			for (ScheduleStationCacheDTO stationCache : scheduleStationCacheList) {
				ScheduleStationDTO controlDTO = new ScheduleStationDTO();
				controlDTO.setCode(stationCache.getCode());
				controlDTO.setActiveFrom(stationCache.getActiveFrom());
				controlDTO.setActiveTo(stationCache.getActiveTo());
				controlDTO.setDayOfWeek(stationCache.getDayOfWeek());
				controlDTO.setLookupCode(stationCache.getLookupCode());
				controlDTO.setMinitues(stationCache.getMinitues());
				controlDTO.setMobileNumber(stationCache.getMobileNumber());
				controlDTO.setActiveFlag(Numeric.ONE_INT);
				controlDTO.setStationSequence(stationCache.getStationSequence());

				StationDTO stationDTO = new StationDTO();
				stationDTO.setId(stationCache.getStationId());
				controlDTO.setStation(stationDTO);
				List<ScheduleStationDTO> overrideControlList = new ArrayList<>();
				if (stationCache.getOverrideList() != null && !stationCache.getOverrideList().isEmpty()) {
					for (ScheduleStationCacheDTO overRideCacheDTO : stationCache.getOverrideList()) {
						ScheduleStationDTO overrideControlDTO = new ScheduleStationDTO();
						overrideControlDTO.setCode(overRideCacheDTO.getCode());
						overrideControlDTO.setActiveFrom(overRideCacheDTO.getActiveFrom());
						overrideControlDTO.setDayOfWeek(overRideCacheDTO.getDayOfWeek());
						overrideControlDTO.setActiveTo(overRideCacheDTO.getActiveTo());
						overrideControlDTO.setMinitues(overRideCacheDTO.getMinitues());
						overrideControlDTO.setMobileNumber(overRideCacheDTO.getMobileNumber());
						overrideControlDTO.setStationSequence(overRideCacheDTO.getStationSequence());
						StationDTO statDTO = new StationDTO();
						statDTO.setId(overRideCacheDTO.getStationId());
						overrideControlDTO.setStation(statDTO);
						overrideControlDTO.setLookupCode(overRideCacheDTO.getLookupCode());
						overrideControlList.add(overrideControlDTO);
					}
					controlDTO.setOverrideList(overrideControlList);
				}
				ScheduleStationDTOList.add(controlDTO);
			}
		}
		return ScheduleStationDTOList;
	}

	// schedule station point
	protected List<ScheduleStationPointCacheDTO> bindScheduleStationPointToCacheObject(List<ScheduleStationPointDTO> stationDTOList) {
		List<ScheduleStationPointCacheDTO> scheduleControlCacheList = new ArrayList<>();
		// copy to cache
		if (stationDTOList != null && !stationDTOList.isEmpty()) {
			for (ScheduleStationPointDTO stationDTO : stationDTOList) {
				ScheduleStationPointCacheDTO stationCacheDTO = new ScheduleStationPointCacheDTO();
				stationCacheDTO.setCode(stationDTO.getCode());
				stationCacheDTO.setActiveFrom(stationDTO.getActiveFrom());
				stationCacheDTO.setActiveTo(stationDTO.getActiveTo());
				stationCacheDTO.setDayOfWeek(stationDTO.getDayOfWeek());
				stationCacheDTO.setMinitues(stationDTO.getMinitues());
				stationCacheDTO.setFare(stationDTO.getFare());
				stationCacheDTO.setMobileNumber(stationDTO.getMobileNumber());
				stationCacheDTO.setAddress(stationDTO.getAddress());
				stationCacheDTO.setAmenitiesCodes(stationDTO.getAmenities());

				if (stationDTO.getBusVehicleVanPickup() != null) {
					stationCacheDTO.setVanRouteId(stationDTO.getBusVehicleVanPickup().getId());
				}

				stationCacheDTO.setCreditDebitFlag(stationDTO.getCreditDebitFlag());
				stationCacheDTO.setBoardingDroppingFlag(stationDTO.getBoardingDroppingFlag());
				if (stationDTO.getStation() != null) {
					stationCacheDTO.setStationId(stationDTO.getStation().getId());
				}
				if (stationDTO.getStationPoint() != null) {
					stationCacheDTO.setStationPointId(stationDTO.getStationPoint().getId());
				}
				List<ScheduleStationPointCacheDTO> overrideControlList = new ArrayList<>();
				if (stationDTO.getOverrideList() != null && !stationDTO.getOverrideList().isEmpty()) {
					for (ScheduleStationPointDTO stationPointDTO : stationDTO.getOverrideList()) {
						ScheduleStationPointCacheDTO cacheDTO = new ScheduleStationPointCacheDTO();
						cacheDTO.setCode(stationPointDTO.getCode());
						cacheDTO.setActiveFrom(stationPointDTO.getActiveFrom());
						cacheDTO.setActiveTo(stationPointDTO.getActiveTo());
						cacheDTO.setDayOfWeek(stationPointDTO.getDayOfWeek());
						cacheDTO.setMinitues(stationPointDTO.getMinitues());
						cacheDTO.setCreditDebitFlag(stationPointDTO.getCreditDebitFlag());
						cacheDTO.setFare(stationPointDTO.getFare());
						cacheDTO.setMobileNumber(stationPointDTO.getMobileNumber());
						cacheDTO.setAddress(stationPointDTO.getAddress());
						cacheDTO.setAmenitiesCodes(stationPointDTO.getAmenities());

						if (stationPointDTO.getStation() != null) {
							cacheDTO.setStationId(stationPointDTO.getStation().getId());
						}
						if (stationPointDTO.getStationPoint() != null) {
							cacheDTO.setStationPointId(stationPointDTO.getStationPoint().getId());
						}
						overrideControlList.add(cacheDTO);
					}
					stationCacheDTO.setOverrideList(overrideControlList);
				}
				scheduleControlCacheList.add(stationCacheDTO);
			}
		}
		return scheduleControlCacheList;
	}

	// Schedule Cancellation Terms

	protected List<ScheduleStationPointDTO> bindScheduleStationPointFromCacheObject(List<ScheduleStationPointCacheDTO> scheduleStationCacheList) {

		List<ScheduleStationPointDTO> ScheduleStationPointDTOList = new ArrayList<>();
		if (scheduleStationCacheList != null && !scheduleStationCacheList.isEmpty()) {
			// copy from cache
			for (ScheduleStationPointCacheDTO scheduleStationPointCacheDTO : scheduleStationCacheList) {
				ScheduleStationPointDTO scheduleStationPointDTO = new ScheduleStationPointDTO();
				scheduleStationPointDTO.setCode(scheduleStationPointCacheDTO.getCode());
				scheduleStationPointDTO.setActiveFrom(scheduleStationPointCacheDTO.getActiveFrom());
				scheduleStationPointDTO.setActiveTo(scheduleStationPointCacheDTO.getActiveTo());
				scheduleStationPointDTO.setDayOfWeek(scheduleStationPointCacheDTO.getDayOfWeek());
				scheduleStationPointDTO.setMinitues(scheduleStationPointCacheDTO.getMinitues());
				scheduleStationPointDTO.setActiveFlag(Numeric.ONE_INT);
				scheduleStationPointDTO.setFare(scheduleStationPointCacheDTO.getFare());
				scheduleStationPointDTO.setMobileNumber(scheduleStationPointCacheDTO.getMobileNumber());
				scheduleStationPointDTO.setAddress(scheduleStationPointCacheDTO.getAddress());
				scheduleStationPointDTO.setAmenities(scheduleStationPointCacheDTO.getAmenitiesCodes());

				BusVehicleVanPickupDTO vanRouteDTO = new BusVehicleVanPickupDTO();
				vanRouteDTO.setId(scheduleStationPointCacheDTO.getVanRouteId());
				scheduleStationPointDTO.setBusVehicleVanPickup(vanRouteDTO);

				scheduleStationPointDTO.setCreditDebitFlag(scheduleStationPointCacheDTO.getCreditDebitFlag());
				scheduleStationPointDTO.setBoardingDroppingFlag(scheduleStationPointCacheDTO.getBoardingDroppingFlag());
				{
					StationDTO stationDTO = new StationDTO();
					stationDTO.setId(scheduleStationPointCacheDTO.getStationId());
					scheduleStationPointDTO.setStation(stationDTO);
					StationPointDTO pointDTO = new StationPointDTO();
					pointDTO.setId(scheduleStationPointCacheDTO.getStationPointId());
					scheduleStationPointDTO.setStationPoint(pointDTO);
				}
				List<ScheduleStationPointDTO> overrideControlList = new ArrayList<>();
				if (scheduleStationPointCacheDTO.getOverrideList() != null && !scheduleStationPointCacheDTO.getOverrideList().isEmpty()) {
					for (ScheduleStationPointCacheDTO overRideCacheDTO : scheduleStationPointCacheDTO.getOverrideList()) {
						ScheduleStationPointDTO overrideControlDTO = new ScheduleStationPointDTO();
						overrideControlDTO.setCode(overRideCacheDTO.getCode());
						overrideControlDTO.setActiveFrom(overRideCacheDTO.getActiveFrom());
						overrideControlDTO.setDayOfWeek(overRideCacheDTO.getDayOfWeek());
						overrideControlDTO.setActiveTo(overRideCacheDTO.getActiveTo());
						overrideControlDTO.setMinitues(overRideCacheDTO.getMinitues());
						overrideControlDTO.setCreditDebitFlag(overRideCacheDTO.getCreditDebitFlag());
						overrideControlDTO.setActiveFlag(Numeric.ONE_INT);
						overrideControlDTO.setFare(overRideCacheDTO.getFare());
						overrideControlDTO.setMobileNumber(overRideCacheDTO.getMobileNumber());
						overrideControlDTO.setAddress(overRideCacheDTO.getAddress());
						overrideControlDTO.setAmenities(overRideCacheDTO.getAmenitiesCodes());

						{
							StationDTO stationDTO = new StationDTO();
							stationDTO.setId(overRideCacheDTO.getStationId());
							overrideControlDTO.setStation(stationDTO);
							StationPointDTO pointDTO = new StationPointDTO();
							pointDTO.setId(overRideCacheDTO.getStationPointId());
							overrideControlDTO.setStationPoint(pointDTO);
						}
						overrideControlList.add(overrideControlDTO);
					}
					scheduleStationPointDTO.setOverrideList(overrideControlList);
				}
				ScheduleStationPointDTOList.add(scheduleStationPointDTO);
			}
		}
		return ScheduleStationPointDTOList;
	}

	protected List<ScheduleCancellationTermDTO> bindScheduleTermsFromCacheObject(List<ScheduleCancellationTermCacheDTO> scheduleTermsCacheList) {

		List<ScheduleCancellationTermDTO> scheduleTermsDTOList = new ArrayList<>();
		if (scheduleTermsCacheList != null && !scheduleTermsCacheList.isEmpty()) {
			// copy from cache
			for (ScheduleCancellationTermCacheDTO termCacheDTO : scheduleTermsCacheList) {
				ScheduleCancellationTermDTO cancellationTermDTO = new ScheduleCancellationTermDTO();
				cancellationTermDTO.setCode(termCacheDTO.getCode());
				cancellationTermDTO.setActiveFrom(termCacheDTO.getActiveFrom());
				cancellationTermDTO.setActiveTo(termCacheDTO.getActiveTo());
				cancellationTermDTO.setDayOfWeek(termCacheDTO.getDayOfWeek());
				cancellationTermDTO.setActiveFlag(Numeric.ONE_INT);
				{
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setId(termCacheDTO.getGroupId());
					cancellationTermDTO.setGroup(groupDTO);
					CancellationTermDTO termDTO = new CancellationTermDTO();
					termDTO.setId(termCacheDTO.getCancellationTermId());
					cancellationTermDTO.setCancellationTerm(termDTO);
				}
				List<ScheduleCancellationTermDTO> overrideList = new ArrayList<>();
				if (termCacheDTO.getOverrideList() != null && !termCacheDTO.getOverrideList().isEmpty()) {
					for (ScheduleCancellationTermCacheDTO overRideCacheDTO : termCacheDTO.getOverrideList()) {
						ScheduleCancellationTermDTO overrideDTO = new ScheduleCancellationTermDTO();
						overrideDTO.setCode(overRideCacheDTO.getCode());
						overrideDTO.setActiveFrom(overRideCacheDTO.getActiveFrom());
						overrideDTO.setDayOfWeek(overRideCacheDTO.getDayOfWeek());
						overrideDTO.setActiveTo(overRideCacheDTO.getActiveTo());
						overrideDTO.setActiveFlag(Numeric.ONE_INT);
						{
							GroupDTO groupDTO = new GroupDTO();
							groupDTO.setId(overRideCacheDTO.getGroupId());
							overrideDTO.setGroup(groupDTO);
							CancellationTermDTO termDTO = new CancellationTermDTO();
							termDTO.setId(overRideCacheDTO.getCancellationTermId());
							overrideDTO.setCancellationTerm(termDTO);
						}
						overrideList.add(overrideDTO);
					}
					cancellationTermDTO.setOverrideList(overrideList);
				}
				scheduleTermsDTOList.add(cancellationTermDTO);
			}
		}
		return scheduleTermsDTOList;
	}

	protected List<ScheduleCancellationTermCacheDTO> bindScheduleTermsToCacheObject(List<ScheduleCancellationTermDTO> stationDTOList) {
		List<ScheduleCancellationTermCacheDTO> scheduleControlCacheList = new ArrayList<>();
		// copy to cache
		if (stationDTOList != null && !stationDTOList.isEmpty()) {
			for (ScheduleCancellationTermDTO stationDTO : stationDTOList) {
				ScheduleCancellationTermCacheDTO stationCacheDTO = new ScheduleCancellationTermCacheDTO();
				stationCacheDTO.setCode(stationDTO.getCode());
				stationCacheDTO.setActiveFrom(stationDTO.getActiveFrom());
				stationCacheDTO.setActiveTo(stationDTO.getActiveTo());
				stationCacheDTO.setDayOfWeek(stationDTO.getDayOfWeek());

				if (stationDTO.getGroup() != null) {
					stationCacheDTO.setGroupId(stationDTO.getGroup().getId());
				}
				if (stationDTO.getCancellationTerm() != null) {
					stationCacheDTO.setCancellationTermId(stationDTO.getCancellationTerm().getId());
				}
				List<ScheduleCancellationTermCacheDTO> overrideControlList = new ArrayList<>();
				if (stationDTO.getOverrideList() != null && !stationDTO.getOverrideList().isEmpty()) {
					for (ScheduleCancellationTermDTO stationPointDTO : stationDTO.getOverrideList()) {
						ScheduleCancellationTermCacheDTO cacheDTO = new ScheduleCancellationTermCacheDTO();
						cacheDTO.setCode(stationPointDTO.getCode());
						cacheDTO.setActiveFrom(stationPointDTO.getActiveFrom());
						cacheDTO.setActiveTo(stationPointDTO.getActiveTo());
						cacheDTO.setDayOfWeek(stationPointDTO.getDayOfWeek());
						if (stationPointDTO.getGroup() != null) {
							cacheDTO.setGroupId(stationDTO.getGroup().getId());
						}
						if (stationPointDTO.getCancellationTerm() != null) {
							cacheDTO.setCancellationTermId(stationPointDTO.getCancellationTerm().getId());
						}
						overrideControlList.add(cacheDTO);
					}
					stationCacheDTO.setOverrideList(overrideControlList);
				}
				scheduleControlCacheList.add(stationCacheDTO);
			}
		}
		return scheduleControlCacheList;
	}// schedule Stage

	protected List<ScheduleStageCacheDTO> bindScheduleStageToCacheObject(List<ScheduleStageDTO> stationDTOList) {
		List<ScheduleStageCacheDTO> scheduleControlCacheList = new ArrayList<>();
		// copy to cache
		if (stationDTOList != null && !stationDTOList.isEmpty()) {
			for (ScheduleStageDTO stageDTO : stationDTOList) {
				ScheduleStageCacheDTO stationCacheDTO = new ScheduleStageCacheDTO();
				stationCacheDTO.setActiveFlag(stageDTO.getActiveFlag());
				stationCacheDTO.setId(stageDTO.getId());
				// stationCacheDTO.setCode(stageDTO.getCode());
				stationCacheDTO.setActiveFrom(stageDTO.getActiveFrom());
				stationCacheDTO.setActiveTo(stageDTO.getActiveTo());
				stationCacheDTO.setDayOfWeek(stageDTO.getDayOfWeek());
				stationCacheDTO.setFare(stageDTO.getFare());
				if (stageDTO.getBusSeatType() != null) {
					stationCacheDTO.setBusSeatTypeCode(stageDTO.getBusSeatType().getCode());
				}
				if (stageDTO.getFromStation() != null) {
					stationCacheDTO.setFromStationId(stageDTO.getFromStation().getId());
				}
				if (stageDTO.getToStation() != null) {
					stationCacheDTO.setToStationId(stageDTO.getToStation().getId());
				}
				if (stageDTO.getGroup() != null) {
					stationCacheDTO.setGroupId(stageDTO.getGroup().getId());
				}
				List<ScheduleStageCacheDTO> overrideControlList = new ArrayList<>();
				if (stageDTO.getOverrideList() != null && !stageDTO.getOverrideList().isEmpty()) {
					for (ScheduleStageDTO overrideStageDTO : stageDTO.getOverrideList()) {
						ScheduleStageCacheDTO cacheDTO = new ScheduleStageCacheDTO();
						cacheDTO.setActiveFlag(overrideStageDTO.getActiveFlag());
						cacheDTO.setId(overrideStageDTO.getId());
						// cacheDTO.setCode(overrideStageDTO.getCode());
						cacheDTO.setActiveFrom(overrideStageDTO.getActiveFrom());
						cacheDTO.setActiveTo(overrideStageDTO.getActiveTo());
						cacheDTO.setDayOfWeek(overrideStageDTO.getDayOfWeek());
						cacheDTO.setFare(overrideStageDTO.getFare());
						if (overrideStageDTO.getBusSeatType() != null) {
							cacheDTO.setBusSeatTypeCode(overrideStageDTO.getBusSeatType().getCode());
						}
						if (overrideStageDTO.getFromStation() != null) {
							cacheDTO.setFromStationId(overrideStageDTO.getFromStation().getId());
						}
						if (overrideStageDTO.getToStation() != null) {
							cacheDTO.setToStationId(overrideStageDTO.getToStation().getId());
						}
						if (overrideStageDTO.getGroup() != null) {
							cacheDTO.setGroupId(overrideStageDTO.getGroup().getId());
						}
						overrideControlList.add(cacheDTO);
					}
					stationCacheDTO.setOverrideList(overrideControlList);
				}
				scheduleControlCacheList.add(stationCacheDTO);
			}
		}
		return scheduleControlCacheList;
	}

	// Schedule Stage

	protected List<ScheduleStageDTO> bindScheduleStageFromCacheObject(List<ScheduleStageCacheDTO> scheduleStationCacheList) {

		List<ScheduleStageDTO> scheduleStageDTOList = new ArrayList<>();
		if (scheduleStationCacheList != null && !scheduleStationCacheList.isEmpty()) {
			// copy from cache
			for (ScheduleStageCacheDTO stageCache : scheduleStationCacheList) {
				ScheduleStageDTO stageDTO = new ScheduleStageDTO();
				stageDTO.setId(stageCache.getId());
				// stageDTO.setCode(stageCache.getCode());
				stageDTO.setActiveFlag(stageCache.getActiveFlag());
				stageDTO.setActiveFrom(stageCache.getActiveFrom());
				stageDTO.setActiveTo(stageCache.getActiveTo());
				stageDTO.setDayOfWeek(stageCache.getDayOfWeek());
				stageDTO.setFare(stageCache.getFare());
				{
					StationDTO fromStationDTO = new StationDTO();
					fromStationDTO.setId(stageCache.getFromStationId());
					stageDTO.setFromStation(fromStationDTO);
					StationDTO toStationDTO = new StationDTO();
					toStationDTO.setId(stageCache.getToStationId());
					stageDTO.setToStation(toStationDTO);
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setId(stageCache.getGroupId());
					stageDTO.setGroup(groupDTO);
					stageDTO.setBusSeatType(BusSeatTypeEM.getBusSeatTypeEM(stageCache.getBusSeatTypeCode()));
				}
				List<ScheduleStageDTO> overrideControlList = new ArrayList<>();
				if (stageCache.getOverrideList() != null && !stageCache.getOverrideList().isEmpty()) {
					for (ScheduleStageCacheDTO overRideCacheDTO : stageCache.getOverrideList()) {
						ScheduleStageDTO overrideStageDTO = new ScheduleStageDTO();
						overrideStageDTO.setId(overRideCacheDTO.getId());
						// overrideStageDTO.setCode(overRideCacheDTO.getCode());
						overrideStageDTO.setActiveFrom(overRideCacheDTO.getActiveFrom());
						overrideStageDTO.setActiveTo(overRideCacheDTO.getActiveTo());
						overrideStageDTO.setDayOfWeek(overRideCacheDTO.getDayOfWeek());
						overrideStageDTO.setFare(overRideCacheDTO.getFare());
						{
							StationDTO fromStationDTO = new StationDTO();
							fromStationDTO.setId(overRideCacheDTO.getFromStationId());
							overrideStageDTO.setFromStation(fromStationDTO);
							StationDTO toStationDTO = new StationDTO();
							toStationDTO.setId(overRideCacheDTO.getToStationId());
							overrideStageDTO.setToStation(toStationDTO);
							GroupDTO groupDTO = new GroupDTO();
							groupDTO.setId(overRideCacheDTO.getId());
							overrideStageDTO.setGroup(groupDTO);
							stageDTO.setBusSeatType(BusSeatTypeEM.getBusSeatTypeEM(stageCache.getBusSeatTypeCode()));
						}
						overrideControlList.add(overrideStageDTO);
					}
					stageDTO.setOverrideList(overrideControlList);
				}
				scheduleStageDTOList.add(stageDTO);
			}
		}
		return scheduleStageDTOList;
	}

	protected ScheduleBusCacheDTO bindScheduleBusToCacheObject(ScheduleBusDTO busDTO) {
		ScheduleBusCacheDTO busCacheDTO = new ScheduleBusCacheDTO();
		busCacheDTO.setActiveFlag(busDTO.getActiveFlag());
		busCacheDTO.setBusId(busDTO.getBus().getId());
		busCacheDTO.setTaxId(busDTO.getTax() != null ? busDTO.getTax().getId() : 0);
		busCacheDTO.setBreakevenId(busDTO.getBreakevenSettings() != null ? busDTO.getBreakevenSettings().getId() : 0);
		busCacheDTO.setDistance(busDTO.getDistance());
		List<String> amentiesList = new ArrayList<>();
		if (busDTO.getAmentiesList() != null) {
			for (AmenitiesDTO amenitiesDTO : busDTO.getAmentiesList()) {
				amentiesList.add(amenitiesDTO.getCode());
			}
		}
		busCacheDTO.setAmentiesList(amentiesList);
		return busCacheDTO;
	}

	protected ScheduleBusDTO bindScheduleBusFromCacheObject(ScheduleBusCacheDTO scheduleBusCacheDTO) {
		ScheduleBusDTO scheduleBusDTO = new ScheduleBusDTO();
		scheduleBusDTO.setActiveFlag(scheduleBusCacheDTO.getActiveFlag());
		BusDTO busDTO = new BusDTO();
		busDTO.setId(scheduleBusCacheDTO.getBusId());
		scheduleBusDTO.setBus(busDTO);

		NamespaceTaxDTO tax = new NamespaceTaxDTO();
		tax.setId(scheduleBusCacheDTO.getTaxId());
		scheduleBusDTO.setTax(tax);

		BusBreakevenSettingsDTO breakeveDTO = new BusBreakevenSettingsDTO();
		breakeveDTO.setId(scheduleBusCacheDTO.getBreakevenId());
		scheduleBusDTO.setBreakevenSettings(breakeveDTO);
		scheduleBusDTO.setDistance(scheduleBusCacheDTO.getDistance());

		List<AmenitiesDTO> amentiesList = new ArrayList<AmenitiesDTO>();
		for (String amenties : scheduleBusCacheDTO.getAmentiesList()) {
			AmenitiesDTO amenitiesDTO = new AmenitiesDTO();
			amenitiesDTO.setCode(amenties);
			amentiesList.add(amenitiesDTO);
		}
		scheduleBusDTO.setAmentiesList(amentiesList);
		return scheduleBusDTO;
	}

	// Station Point exception
	protected List<ScheduleStationPointDTO> bindScheduleStationPointExceptionFromCacheObject(List<ScheduleStationPointExceptionCacheDTO> scheduleStationCacheList) {
		List<ScheduleStationPointDTO> ScheduleStationPointDTOList = new ArrayList<>();
		if (scheduleStationCacheList != null && !scheduleStationCacheList.isEmpty()) {
			// copy from cache
			for (ScheduleStationPointExceptionCacheDTO controlCache : scheduleStationCacheList) {
				ScheduleStationPointDTO schedulePoint = new ScheduleStationPointDTO();
				schedulePoint.setCode(controlCache.getCode());
				schedulePoint.setActiveFrom(controlCache.getActiveFrom());
				schedulePoint.setActiveTo(controlCache.getActiveTo());
				schedulePoint.setDayOfWeek(controlCache.getDayOfWeek());
				schedulePoint.setTripDates(controlCache.getTripDates());
				schedulePoint.setReleaseMinutes(controlCache.getReleaseMinutes());
				schedulePoint.setBoardingDroppingFlag(controlCache.getBoardingDroppingFlag());
				schedulePoint.setStationPointType(controlCache.getStationPointType());
				{
					StationDTO stationDTO = new StationDTO();
					stationDTO.setId(controlCache.getStationId());
					schedulePoint.setStation(stationDTO);

					StationPointDTO pointDTO = new StationPointDTO();
					pointDTO.setCode(controlCache.getStationPointCode());
					schedulePoint.setStationPoint(pointDTO);
				}

				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setCode(controlCache.getScheduleCode());
				schedulePoint.setSchedule(scheduleDTO);

				ScheduleStationPointDTOList.add(schedulePoint);
			}
		}
		return ScheduleStationPointDTOList;
	}

	// schedule station point exception
	protected List<ScheduleStationPointExceptionCacheDTO> bindScheduleStationPointExceptionToCacheObject(List<ScheduleStationPointDTO> stationDTOList) {
		List<ScheduleStationPointExceptionCacheDTO> scheduleControlCacheList = new ArrayList<>();
		// copy to cache
		if (stationDTOList != null && !stationDTOList.isEmpty()) {
			for (ScheduleStationPointDTO stationDTO : stationDTOList) {
				ScheduleStationPointExceptionCacheDTO pointCacheDTO = new ScheduleStationPointExceptionCacheDTO();
				pointCacheDTO.setCode(stationDTO.getCode());
				pointCacheDTO.setActiveFrom(stationDTO.getActiveFrom());
				pointCacheDTO.setActiveTo(stationDTO.getActiveTo());
				pointCacheDTO.setDayOfWeek(stationDTO.getDayOfWeek());
				pointCacheDTO.setTripDates(stationDTO.getTripDates());
				pointCacheDTO.setReleaseMinutes(stationDTO.getReleaseMinutes());
				pointCacheDTO.setBoardingDroppingFlag(stationDTO.getBoardingDroppingFlag());
				if (stationDTO.getStation() != null) {
					pointCacheDTO.setStationId(stationDTO.getStation().getId());
				}
				pointCacheDTO.setStationPointType(stationDTO.getStationPointType());
				pointCacheDTO.setStationPointCode(stationDTO.getStationPoint().getCode());
				pointCacheDTO.setScheduleCode(stationDTO.getSchedule().getCode());
				scheduleControlCacheList.add(pointCacheDTO);
			}
		}
		return scheduleControlCacheList;
	}

	// Schedule Virtual Seat Block
	protected List<ScheduleVirtualSeatBlockDTO> bindScheduleVirtualSeatBlockFromCacheObject(List<ScheduleVirtualSeatBlockCacheDTO> scheduleStationCacheList) {
		List<ScheduleVirtualSeatBlockDTO> scheduleVirtualSeatBlockList = new ArrayList<>();
		if (scheduleStationCacheList != null && !scheduleStationCacheList.isEmpty()) {
			// copy from cache
			for (ScheduleVirtualSeatBlockCacheDTO seatBlockCache : scheduleStationCacheList) {
				ScheduleVirtualSeatBlockDTO scheduleVirtualSeatBlockDTO = new ScheduleVirtualSeatBlockDTO();
				scheduleVirtualSeatBlockDTO.setCode(seatBlockCache.getCode());
				scheduleVirtualSeatBlockDTO.setActiveFrom(seatBlockCache.getActiveFrom());
				scheduleVirtualSeatBlockDTO.setActiveTo(seatBlockCache.getActiveTo());
				scheduleVirtualSeatBlockDTO.setDayOfWeek(seatBlockCache.getDayOfWeek());
				scheduleVirtualSeatBlockDTO.setRefreshMinutes(seatBlockCache.getRefreshMinutes());
				scheduleVirtualSeatBlockDTO.setLookupCode(seatBlockCache.getLookupCode());

				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setCode(seatBlockCache.getScheduleCode());
				scheduleVirtualSeatBlockDTO.setSchedule(scheduleDTO);

				GroupDTO group = new GroupDTO();
				group.setCode(seatBlockCache.getGroupCode());
				scheduleVirtualSeatBlockDTO.setGroup(group);

				scheduleVirtualSeatBlockDTO.setActiveFlag(seatBlockCache.getActiveFlag());
				scheduleVirtualSeatBlockList.add(scheduleVirtualSeatBlockDTO);
			}
		}
		return scheduleVirtualSeatBlockList;
	}

	protected List<ScheduleVirtualSeatBlockCacheDTO> bindScheduleVirtualSeatBlockToCacheObject(List<ScheduleVirtualSeatBlockDTO> scheduleVirtualSeatBlockList) {
		List<ScheduleVirtualSeatBlockCacheDTO> scheduleStationCacheList = new ArrayList<>();
		if (scheduleVirtualSeatBlockList != null && !scheduleVirtualSeatBlockList.isEmpty()) {
			// copy to cache
			for (ScheduleVirtualSeatBlockDTO seatBlock : scheduleVirtualSeatBlockList) {
				ScheduleVirtualSeatBlockCacheDTO seatBlockCache = new ScheduleVirtualSeatBlockCacheDTO();
				seatBlockCache.setCode(seatBlock.getCode());
				seatBlockCache.setActiveFrom(seatBlock.getActiveFrom());
				seatBlockCache.setActiveTo(seatBlock.getActiveTo());
				seatBlockCache.setDayOfWeek(seatBlock.getDayOfWeek());
				seatBlockCache.setRefreshMinutes(seatBlock.getRefreshMinutes());
				seatBlockCache.setLookupCode(seatBlock.getLookupCode());
				seatBlockCache.setScheduleCode(seatBlock.getSchedule().getCode());
				seatBlockCache.setGroupCode(seatBlock.getGroup().getCode());
				seatBlockCache.setActiveFlag(seatBlock.getActiveFlag());
				scheduleStationCacheList.add(seatBlockCache);
			}
		}
		return scheduleStationCacheList;
	}

	protected List<ScheduleTagDTO> getScheduleTagList(String tagIds) {
		List<ScheduleTagDTO> tagList = new ArrayList<ScheduleTagDTO>();
		if (StringUtil.isNotNull(tagIds)) {
			String[] scheduleTag = tagIds.split(",");
			for (String tagId : scheduleTag) {
				if (tagId.equals(Numeric.ZERO)) {
					continue;
				}
				ScheduleTagDTO scheduleTagDTO = new ScheduleTagDTO();
				scheduleTagDTO.setId(Integer.valueOf(tagId));
				tagList.add(scheduleTagDTO);
			}
		}
		return tagList;

	}
}
