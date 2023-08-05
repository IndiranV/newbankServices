package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.in.com.cache.BusCache;
import org.in.com.cache.CacheCentral;
import org.in.com.cache.EhcacheManager;
import org.in.com.cache.ScheduleCache;
import org.in.com.cache.dto.ScheduleSeatVisibilityCacheDTO;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.ScheduleSeatVisibilityDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleSeatVisibilityDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.NotificationSubscriptionTypeEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.GroupService;
import org.in.com.service.NotificationPushService;
import org.in.com.service.OrganizationService;
import org.in.com.service.ScheduleDynamicStageFareService;
import org.in.com.service.ScheduleSeatVisibilityService;
import org.in.com.service.UserService;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;
import net.sf.ehcache.Element;

@Service
public class ScheduleSeatVisibilityImpl extends CacheCentral implements ScheduleSeatVisibilityService {
	@Autowired
	GroupService groupService;
	@Autowired
	UserService userService;
	@Autowired
	ScheduleDynamicStageFareService dynamicFareService;
	@Autowired
	NotificationPushService notificationPushService;
	@Autowired
	OrganizationService organizationService;

	private static String CACHEKEY = "SHSEVI";

	public List<ScheduleSeatVisibilityDTO> get(AuthDTO authDTO, ScheduleSeatVisibilityDTO visibilityDTO) {
		ScheduleSeatVisibilityDAO dao = new ScheduleSeatVisibilityDAO();
		BusCache cache = new BusCache();
		List<ScheduleSeatVisibilityDTO> list = dao.get(authDTO, visibilityDTO);
		for (ScheduleSeatVisibilityDTO seatVisibilityDTO : list) {
			if (seatVisibilityDTO.getRefferenceType().equals("GR") && seatVisibilityDTO.getGroupList() != null) {
				for (GroupDTO groupDTO : seatVisibilityDTO.getGroupList()) {
					groupService.getGroup(authDTO, groupDTO);
				}
			}
			if (seatVisibilityDTO.getRefferenceType().equals("UR") && seatVisibilityDTO.getUserList() != null) {
				for (UserDTO userDTO : seatVisibilityDTO.getUserList()) {
					getUserDTOById(authDTO, userDTO);
				}
			}
			if (seatVisibilityDTO.getRefferenceType().equals("SG") && seatVisibilityDTO.getRouteList() != null) {
				for (RouteDTO routeDTO : seatVisibilityDTO.getRouteList()) {
					routeDTO.setFromStation(getStationDTObyId(routeDTO.getFromStation()));
					routeDTO.setToStation(getStationDTObyId(routeDTO.getToStation()));
				}
				// route user
				for (UserDTO userDTO : seatVisibilityDTO.getRouteUsers()) {
					getUserDTOById(authDTO, userDTO);
				}
			}
			if (seatVisibilityDTO.getRefferenceType().equals("BR") && seatVisibilityDTO.getOrganizations() != null) {
				for (OrganizationDTO organizationDTO : seatVisibilityDTO.getOrganizations()) {
					organizationService.getOrganization(authDTO, organizationDTO);
					organizationDTO.setStation(getStationDTO(organizationDTO.getStation()));
				}
			}

			if (seatVisibilityDTO.getBus() != null && seatVisibilityDTO.getBus().getId() != 0) {
				BusDTO busDTO = new BusDTO();
				busDTO.setId(seatVisibilityDTO.getBus().getId());
				busDTO = cache.getBusDTObyId(authDTO, busDTO);
				seatVisibilityDTO.getBus().setDisplayName(busDTO.getDisplayName());
				seatVisibilityDTO.getBus().setCategoryCode(busDTO.getCategoryCode());
				seatVisibilityDTO.getBus().setName(busDTO.getName());
				seatVisibilityDTO.getBus().setCode(busDTO.getCode());
				seatVisibilityDTO.getBus().getBusSeatLayoutDTO().setList(filterByCode(busDTO.getBusSeatLayoutDTO().getList(), seatVisibilityDTO.getBus().getBusSeatLayoutDTO().getList()));
				if (seatVisibilityDTO.getOverrideList() != null && !seatVisibilityDTO.getOverrideList().isEmpty()) {
					for (ScheduleSeatVisibilityDTO overrideVisibilityDTO : seatVisibilityDTO.getOverrideList()) {
						overrideVisibilityDTO.getBus().getBusSeatLayoutDTO().setList(filterByCode(busDTO.getBusSeatLayoutDTO().getList(), overrideVisibilityDTO.getBus().getBusSeatLayoutDTO().getList()));
						if (overrideVisibilityDTO.getGroupList() != null) {
							for (GroupDTO groupDTO : overrideVisibilityDTO.getGroupList()) {
								groupService.getGroup(authDTO, groupDTO);
							}
						}
						if (overrideVisibilityDTO.getUserList() != null) {
							for (UserDTO userDTO : overrideVisibilityDTO.getUserList()) {
								getUserDTOById(authDTO, userDTO);
							}
						}
						if (overrideVisibilityDTO.getRouteList() != null) {
							for (RouteDTO routeDTO : overrideVisibilityDTO.getRouteList()) {
								routeDTO.setFromStation(getStationDTO(routeDTO.getFromStation()));
								routeDTO.setToStation(getStationDTO(routeDTO.getToStation()));
							}
							for (UserDTO userDTO : overrideVisibilityDTO.getRouteUsers()) {
								getUserDTOById(authDTO, userDTO);
							}
						}
						if (overrideVisibilityDTO.getOrganizations() != null) {
							for (OrganizationDTO organizationDTO : overrideVisibilityDTO.getOrganizations()) {
								organizationService.getOrganization(authDTO, organizationDTO);
								organizationDTO.setStation(getStationDTO(organizationDTO.getStation()));
							}
						}
					}
				}
			}
		}

		return list;
	}

	public ScheduleSeatVisibilityDTO Update(AuthDTO authDTO, ScheduleSeatVisibilityDTO visibilityDTO) {
		ScheduleSeatVisibilityDAO dao = new ScheduleSeatVisibilityDAO();
		for (ScheduleSeatVisibilityDTO seatVisibilityDTO : visibilityDTO.getList()) {
			// Validate User Group Level Permission
			if (authDTO.getNativeNamespaceCode().equals(authDTO.getNamespaceCode()) && (seatVisibilityDTO.getRefferenceType().equals("GR") || seatVisibilityDTO.getRefferenceType().equals("UR"))) {
				validateUserGroupPermission(authDTO, seatVisibilityDTO);
			}

			for (GroupDTO groupDTO : seatVisibilityDTO.getGroupList()) {
				groupService.getGroup(authDTO, groupDTO);
			}
			for (UserDTO userDTO : seatVisibilityDTO.getUserList()) {
				getUserDTO(authDTO, userDTO);
			}
			for (RouteDTO routeDTO : seatVisibilityDTO.getRouteList()) {
				if (StringUtil.isNotNull(routeDTO.getFromStation().getCode()) && StringUtil.isNotNull(routeDTO.getToStation().getCode())) {
					routeDTO.setFromStation(getStationDTO(routeDTO.getFromStation()));
					routeDTO.setToStation(getStationDTO(routeDTO.getToStation()));
				}
			}
			if (seatVisibilityDTO.getRouteUsers() != null) {
				for (UserDTO userDTO : seatVisibilityDTO.getRouteUsers()) {
					getUserDTO(authDTO, userDTO);
				}
			}
			if (seatVisibilityDTO.getOrganizations() != null) {
				for (OrganizationDTO organizationDTO : seatVisibilityDTO.getOrganizations()) {
					organizationService.getOrganization(authDTO, organizationDTO);
				}
			}
		}
		ScheduleSeatVisibilityDTO visibilityDTO2 = dao.getIUD(authDTO, visibilityDTO);
		ScheduleCache scheduleCache = new ScheduleCache();
		scheduleCache.removeScheduleDTO(authDTO, visibilityDTO.getSchedule());

		// Update Dynamic Seat Visibility Status
		// removed this block/release information as discussed on 2022-06-15
		// with redbus DP team - aishwarya Ezee Info Seat Sale @ Wed 15 Jun 2022
		// 4:15pm - 5:15pm
		/*
		 * if
		 * (authDTO.getNamespace().getProfile().getDynamicPriceProvider().getId(
		 * ) == DynamicPriceProviderEM.REDBUS.getId()) {
		 * dynamicFareService.updateSeatStatus(authDTO, visibilityDTO);
		 * }
		 */
		if (authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.SEAT_VISIBILITY)) {
			notificationPushService.pushSeatVisibiltyNotification(authDTO, visibilityDTO);
		}
		return visibilityDTO2;
	}

	private void validateUserGroupPermission(AuthDTO authDTO, ScheduleSeatVisibilityDTO scheduleSeatVisibilityDTO) {
		ScheduleSeatVisibilityDTO seatVisibilityDTO = null;
		ScheduleSeatVisibilityDAO seatVisibilityDAO = new ScheduleSeatVisibilityDAO();

		// Override & Exception Handling
		if (StringUtil.isNotNull(scheduleSeatVisibilityDTO.getCode()) || StringUtil.isNotNull(scheduleSeatVisibilityDTO.getLookupCode())) {
			seatVisibilityDTO = new ScheduleSeatVisibilityDTO();
			seatVisibilityDTO.setCode(scheduleSeatVisibilityDTO.getCode());
			if (StringUtil.isNotNull(scheduleSeatVisibilityDTO.getLookupCode()) && !scheduleSeatVisibilityDTO.getLookupCode().equals(Text.FALSE_STRING)) {
				seatVisibilityDTO.setCode(scheduleSeatVisibilityDTO.getLookupCode());
			}
			seatVisibilityDAO.getScheduleSeatVisibility(authDTO, seatVisibilityDTO);
		}

		if (seatVisibilityDTO != null && seatVisibilityDTO.getId() > Numeric.ONE_INT && seatVisibilityDTO.getActiveFlag() == Numeric.ONE_INT) {
			// Validate User Permission
			if (authDTO.getAdditionalAttribute() != null && Numeric.ONE_INT == StringUtil.getIntegerValue(authDTO.getAdditionalAttribute().get(Text.SEAT_VISIBILITY_EDIT_RIGHTS)) && StringUtil.getIntegerValue(seatVisibilityDTO.getUpdatedBy()) != authDTO.getUser().getId()) {
				throw new ServiceException(ErrorCode.UNAUTHORIZED);
			}

			// Validate Group level Permission
			List<GroupDTO> groupList = new ArrayList<GroupDTO>();
			if ("HIDE".equals(seatVisibilityDTO.getVisibilityType())) {
				UserDTO visibilityUser = new UserDTO();
				visibilityUser.setId(Integer.valueOf(seatVisibilityDTO.getUpdatedBy()));
				visibilityUser = getUserDTOById(authDTO, visibilityUser);
				if (visibilityUser != null && visibilityUser.getId() != Numeric.ZERO_INT && visibilityUser.getGroup() != null) {
					GroupDTO groupDTO = groupService.getGroup(authDTO, visibilityUser.getGroup());
					groupList.add(groupDTO);
				}
			}
			else if (seatVisibilityDTO.getRefferenceType().equals("GR")) {
				for (GroupDTO visibilityGroup : seatVisibilityDTO.getGroupList()) {
					if (visibilityGroup.getId() == Numeric.ZERO_INT) {
						continue;
					}
					visibilityGroup = groupService.getGroup(authDTO, visibilityGroup);
					groupList.add(visibilityGroup);
				}
			}
			else if (seatVisibilityDTO.getRefferenceType().equals("UR")) {
				for (UserDTO visibilityUser : seatVisibilityDTO.getUserList()) {
					if (visibilityUser.getId() == Numeric.ZERO_INT) {
						continue;
					}
					GroupDTO visibilityGroup = groupService.getGroup(authDTO, getUserDTOById(authDTO, visibilityUser).getGroup());
					groupList.add(visibilityGroup);
				}
			}

			// Validate Group Level Permission
			GroupDTO authUserGroup = groupService.getGroup(authDTO, authDTO.getGroup());
			for (GroupDTO groupDTO : groupList) {
				if (groupDTO.getLevel() > authUserGroup.getLevel()) {
					throw new ServiceException(ErrorCode.UNAUTHORIZED);
				}
			}
		}

	}

	private List<BusSeatLayoutDTO> filterByCode(List<BusSeatLayoutDTO> Orglist, List<BusSeatLayoutDTO> fillerlist) {
		List<BusSeatLayoutDTO> list = new ArrayList<>();
		for (BusSeatLayoutDTO fitterdto : fillerlist) {
			for (BusSeatLayoutDTO dto : Orglist) {
				if (dto.getCode().equals(fitterdto.getCode())) {
					list.add(dto);
				}
			}
		}
		return list;
	}

	public List<ScheduleSeatVisibilityDTO> getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO) {

		List<ScheduleSeatVisibilityDTO> seatVisibilityDTOList = getBySchedule(authDTO, scheduleDTO);
		// Validate all Seat visibility
		for (Iterator<ScheduleSeatVisibilityDTO> iterator = seatVisibilityDTOList.iterator(); iterator.hasNext();) {
			ScheduleSeatVisibilityDTO visibilityDTO = iterator.next();
			// common validations
			if (visibilityDTO.getActiveFrom() != null && !scheduleDTO.getTripDate().gteq(new DateTime(visibilityDTO.getActiveFrom()))) {
				iterator.remove();
				continue;
			}
			if (visibilityDTO.getActiveTo() != null && !scheduleDTO.getTripDate().lteq(new DateTime(visibilityDTO.getActiveTo()))) {
				iterator.remove();
				continue;
			}
			if (visibilityDTO.getDayOfWeek() != null && visibilityDTO.getDayOfWeek().length() != 7) {
				iterator.remove();
				continue;
			}
			if (visibilityDTO.getDayOfWeek() != null && visibilityDTO.getDayOfWeek().substring(scheduleDTO.getTripDate().getWeekDay() - 1, scheduleDTO.getTripDate().getWeekDay()).equals("0")) {
				iterator.remove();
				continue;
			}
			// Exceptions and Override
			BusDTO orverrideBusDTO = null;
			for (Iterator<ScheduleSeatVisibilityDTO> overrideIterator = visibilityDTO.getOverrideList().iterator(); overrideIterator.hasNext();) {
				ScheduleSeatVisibilityDTO overrideSeatVisibilityDTO = overrideIterator.next();
				if (!scheduleDTO.getTripDate().gteq(new DateTime(overrideSeatVisibilityDTO.getActiveFrom()))) {
					overrideIterator.remove();
					continue;
				}
				if (!scheduleDTO.getTripDate().lteq(new DateTime(overrideSeatVisibilityDTO.getActiveTo()))) {
					overrideIterator.remove();
					continue;
				}
				if (overrideSeatVisibilityDTO.getDayOfWeek() != null && overrideSeatVisibilityDTO.getDayOfWeek().length() != 7) {
					overrideIterator.remove();
					continue;
				}
				if (overrideSeatVisibilityDTO.getDayOfWeek() != null && overrideSeatVisibilityDTO.getDayOfWeek().substring(scheduleDTO.getTripDate().getWeekDay() - 1, scheduleDTO.getTripDate().getWeekDay()).equals("0")) {
					overrideIterator.remove();
					continue;
				}
				// Apply Exceptions
				if (overrideSeatVisibilityDTO.getBus() == null || overrideSeatVisibilityDTO.getBus().getBusSeatLayoutDTO() == null || overrideSeatVisibilityDTO.getBus().getBusSeatLayoutDTO().getList() == null || overrideSeatVisibilityDTO.getBus().getBusSeatLayoutDTO().getList().isEmpty()) {
					iterator.remove();
					visibilityDTO.setActiveFlag(0);
					break;
				}
				else {
					// Apply Override
					if (orverrideBusDTO == null) {
						orverrideBusDTO = overrideSeatVisibilityDTO.getBus();
					}
					else {
						orverrideBusDTO.getBusSeatLayoutDTO().getList().addAll(overrideSeatVisibilityDTO.getBus().getBusSeatLayoutDTO().getList());
					}
				}
			}
			if (visibilityDTO.getActiveFlag() == 1 && orverrideBusDTO != null) {
				for (Iterator<BusSeatLayoutDTO> visibilityIterator = visibilityDTO.getBus().getBusSeatLayoutDTO().getList().iterator(); visibilityIterator.hasNext();) {
					BusSeatLayoutDTO layoutDTO = visibilityIterator.next();
					for (BusSeatLayoutDTO orderrideLayoutDTO : orverrideBusDTO.getBusSeatLayoutDTO().getList()) {
						if (orderrideLayoutDTO.getCode().equals(layoutDTO.getCode())) {
							visibilityIterator.remove();
							break;
						}
					}
				}
			}
			if (visibilityDTO.getActiveFlag() == 1 && visibilityDTO.getBus().getBusSeatLayoutDTO().getList().isEmpty()) {
				iterator.remove();
				continue;
			}
		}
		return seatVisibilityDTOList;
	}

	private List<ScheduleSeatVisibilityDTO> getBySchedule(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		String key = CACHEKEY + scheduleDTO.getCode();
		List<ScheduleSeatVisibilityDTO> seatVisibilityDTOList = null;
		Element element = EhcacheManager.getScheduleEhCache().get(key);
		if (element != null) {
			List<ScheduleSeatVisibilityCacheDTO> scheduleSeatVisibilityCacheList = (List<ScheduleSeatVisibilityCacheDTO>) element.getObjectValue();
			seatVisibilityDTOList = bindSeatVisibilityFromCacheObject(scheduleSeatVisibilityCacheList);
		}
		else if (scheduleDTO.getId() != 0) {
			ScheduleSeatVisibilityDAO visibilityDAO = new ScheduleSeatVisibilityDAO();
			seatVisibilityDTOList = visibilityDAO.getByScheduleId(authDTO, scheduleDTO);
			// Save to schedule station Point Cache
			List<ScheduleSeatVisibilityCacheDTO> scheduleSeatVisibilityCacheList = bindSeatVisibilityToCacheObject(authDTO, seatVisibilityDTOList);
			element = new Element(key, scheduleSeatVisibilityCacheList);
			EhcacheManager.getScheduleEhCache().put(element);
		}
		return seatVisibilityDTOList;
	}

	public List<ScheduleSeatVisibilityDTO> getSeatVisibilities(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		List<ScheduleSeatVisibilityDTO> seatVisibilityDTOList = getBySchedule(authDTO, scheduleDTO);

		List<ScheduleSeatVisibilityDTO> releaseList = new ArrayList<ScheduleSeatVisibilityDTO>();

		DateTime tripDate = scheduleDTO.getTripDate().getStartOfDay();

		// Validate all Seat visibility
		for (Iterator<ScheduleSeatVisibilityDTO> iterator = seatVisibilityDTOList.iterator(); iterator.hasNext();) {
			ScheduleSeatVisibilityDTO visibilityDTO = iterator.next();
			// common validations
			if (visibilityDTO.getActiveFrom() != null && !tripDate.gteq(new DateTime(visibilityDTO.getActiveFrom()).getStartOfDay())) {
				iterator.remove();
				continue;
			}
			if (visibilityDTO.getActiveTo() != null && !tripDate.lteq(new DateTime(visibilityDTO.getActiveTo()).getEndOfDay())) {
				iterator.remove();
				continue;
			}
			if (visibilityDTO.getDayOfWeek() != null && visibilityDTO.getDayOfWeek().length() != 7) {
				iterator.remove();
				continue;
			}
			if (visibilityDTO.getDayOfWeek() != null && visibilityDTO.getDayOfWeek().substring(tripDate.getWeekDay() - 1, tripDate.getWeekDay()).equals("0")) {
				iterator.remove();
				continue;
			}
			// Exceptions and Override
			BusDTO orverrideBusDTO = null;
			for (Iterator<ScheduleSeatVisibilityDTO> overrideIterator = visibilityDTO.getOverrideList().iterator(); overrideIterator.hasNext();) {
				ScheduleSeatVisibilityDTO overrideSeatVisibilityDTO = overrideIterator.next();
				if (!tripDate.gteq(new DateTime(overrideSeatVisibilityDTO.getActiveFrom()).getStartOfDay())) {
					overrideIterator.remove();
					continue;
				}
				if (!tripDate.lteq(new DateTime(overrideSeatVisibilityDTO.getActiveTo()).getEndOfDay())) {
					overrideIterator.remove();
					continue;
				}
				if (overrideSeatVisibilityDTO.getDayOfWeek() != null && overrideSeatVisibilityDTO.getDayOfWeek().length() != 7) {
					overrideIterator.remove();
					continue;
				}
				if (overrideSeatVisibilityDTO.getDayOfWeek() != null && overrideSeatVisibilityDTO.getDayOfWeek().substring(tripDate.getWeekDay() - 1, tripDate.getWeekDay()).equals("0")) {
					overrideIterator.remove();
					continue;
				}
				// Apply Exceptions
				if (overrideSeatVisibilityDTO.getBus() == null || overrideSeatVisibilityDTO.getBus().getBusSeatLayoutDTO() == null || overrideSeatVisibilityDTO.getBus().getBusSeatLayoutDTO().getList() == null || overrideSeatVisibilityDTO.getBus().getBusSeatLayoutDTO().getList().isEmpty()) {
					overrideSeatVisibilityDTO.setVisibilityType("EXPN");
					overrideSeatVisibilityDTO.getOverrideList().add(visibilityDTO);
					releaseList.add(overrideSeatVisibilityDTO);

					iterator.remove();
					visibilityDTO.setActiveFlag(0);
					break;
				}
				else {
					// Apply Override
					if (orverrideBusDTO == null) {
						orverrideBusDTO = overrideSeatVisibilityDTO.getBus();
					}
					else {
						orverrideBusDTO.getBusSeatLayoutDTO().getList().addAll(overrideSeatVisibilityDTO.getBus().getBusSeatLayoutDTO().getList());
					}
					overrideSeatVisibilityDTO.setVisibilityType("REL");
					releaseList.add(overrideSeatVisibilityDTO);
				}
			}
			if (visibilityDTO.getActiveFlag() == 1 && orverrideBusDTO != null) {
				for (Iterator<BusSeatLayoutDTO> visibilityIterator = visibilityDTO.getBus().getBusSeatLayoutDTO().getList().iterator(); visibilityIterator.hasNext();) {
					BusSeatLayoutDTO layoutDTO = visibilityIterator.next();
					for (BusSeatLayoutDTO orderrideLayoutDTO : orverrideBusDTO.getBusSeatLayoutDTO().getList()) {
						if (orderrideLayoutDTO.getCode().equals(layoutDTO.getCode())) {
							visibilityIterator.remove();
							break;
						}
					}
				}
			}
			if (visibilityDTO.getActiveFlag() == 1 && visibilityDTO.getBus().getBusSeatLayoutDTO().getList().isEmpty()) {
				iterator.remove();
				continue;
			}
		}

		seatVisibilityDTOList.addAll(releaseList);
		return seatVisibilityDTOList;
	}

	private List<ScheduleSeatVisibilityCacheDTO> bindSeatVisibilityToCacheObject(AuthDTO authDTO, List<ScheduleSeatVisibilityDTO> ScheduleSeatVisibilityDTOList) {
		List<ScheduleSeatVisibilityCacheDTO> seatVisibilityCacheList = new ArrayList<>();
		// copy to cache
		if (ScheduleSeatVisibilityDTOList != null && !ScheduleSeatVisibilityDTOList.isEmpty()) {
			for (ScheduleSeatVisibilityDTO seatVisibilityDTO : ScheduleSeatVisibilityDTOList) {
				ScheduleSeatVisibilityCacheDTO seatVisibilityCacheDTO = new ScheduleSeatVisibilityCacheDTO();
				seatVisibilityCacheDTO.setActiveFlag(seatVisibilityDTO.getActiveFlag());
				seatVisibilityCacheDTO.setId(seatVisibilityDTO.getId());
				seatVisibilityCacheDTO.setCode(seatVisibilityDTO.getCode());
				seatVisibilityCacheDTO.setActiveFrom(seatVisibilityDTO.getActiveFrom());
				seatVisibilityCacheDTO.setActiveTo(seatVisibilityDTO.getActiveTo());
				seatVisibilityCacheDTO.setDayOfWeek(seatVisibilityDTO.getDayOfWeek());
				seatVisibilityCacheDTO.setVisibilityType(seatVisibilityDTO.getVisibilityType());
				seatVisibilityCacheDTO.setRefferenceType(seatVisibilityDTO.getRefferenceType());
				seatVisibilityCacheDTO.setReleaseMinutes(seatVisibilityDTO.getReleaseMinutes());
				seatVisibilityCacheDTO.setRemarks(seatVisibilityDTO.getRemarks());

				UserDTO updatedUser = new UserDTO();
				updatedUser.setId(Integer.valueOf(seatVisibilityDTO.getUpdatedBy()));
				updatedUser = getUserDTO(authDTO, updatedUser);
				if (StringUtil.isNull(updatedUser.getCode())) {
					userService.getUserV2(authDTO, updatedUser);
				}
				seatVisibilityDTO.setUpdatedBy(updatedUser.getName() + (StringUtil.isNotNull(updatedUser.getLastname()) ? " " + updatedUser.getLastname() : Text.EMPTY));

				seatVisibilityCacheDTO.setUpdatedBy(seatVisibilityDTO.getUpdatedBy());
				seatVisibilityCacheDTO.setUpdatedAt(seatVisibilityDTO.getUpdatedAt());

				List<String> refferenceList = new ArrayList<String>();
				if (seatVisibilityDTO.getRefferenceType().equals("GR")) {
					for (GroupDTO groupDTO : seatVisibilityDTO.getGroupList()) {
						refferenceList.add(String.valueOf(groupDTO.getId()));
					}
				}
				else if (seatVisibilityDTO.getRefferenceType().equals("UR")) {
					for (UserDTO userDTO : seatVisibilityDTO.getUserList()) {
						refferenceList.add(String.valueOf(userDTO.getId()));
					}
				}
				else if (seatVisibilityDTO.getRefferenceType().equals("SG") && seatVisibilityDTO.getRouteList() != null) {
					for (RouteDTO routeDTO : seatVisibilityDTO.getRouteList()) {
						String route = routeDTO.getFromStation().getId() + Text.UNDER_SCORE + routeDTO.getToStation().getId();
						refferenceList.add(route);
					}
					List<String> routeUserCodes = new ArrayList<String>();
					if (seatVisibilityDTO.getRouteUsers() != null) {
						for (UserDTO userDTO : seatVisibilityDTO.getRouteUsers()) {
							routeUserCodes.add(userDTO.getCode());
						}
					}
					seatVisibilityCacheDTO.setRouteUsers(routeUserCodes);
				}
				else if (seatVisibilityDTO.getRefferenceType().equals("BR") && seatVisibilityDTO.getOrganizations() != null) {
					for (OrganizationDTO organizationDTO : seatVisibilityDTO.getOrganizations()) {
						refferenceList.add(String.valueOf(organizationDTO.getId()));
					}
				}
				seatVisibilityCacheDTO.setRefferenceList(refferenceList);

				StringBuilder seatCodes = new StringBuilder();
				if (seatVisibilityDTO.getBus() != null && seatVisibilityDTO.getBus().getBusSeatLayoutDTO() != null && seatVisibilityDTO.getBus().getBusSeatLayoutDTO().getList() != null) {
					for (BusSeatLayoutDTO layoutDTO : seatVisibilityDTO.getBus().getBusSeatLayoutDTO().getList()) {
						if (seatCodes.length() > 0) {
							seatCodes.append(Text.COMMA);
						}
						seatCodes.append(layoutDTO.getCode());
					}
				}

				seatVisibilityCacheDTO.setSeatCodeList(seatCodes.toString());
				seatVisibilityCacheDTO.setBusId(seatVisibilityDTO.getBus().getId());

				List<ScheduleSeatVisibilityCacheDTO> overrideControlList = new ArrayList<>();
				if (seatVisibilityDTO.getOverrideList() != null && !seatVisibilityDTO.getOverrideList().isEmpty()) {
					for (ScheduleSeatVisibilityDTO overrideSeatVisibilityDTO : seatVisibilityDTO.getOverrideList()) {
						ScheduleSeatVisibilityCacheDTO cacheDTO = new ScheduleSeatVisibilityCacheDTO();
						cacheDTO.setActiveFlag(overrideSeatVisibilityDTO.getActiveFlag());
						cacheDTO.setId(overrideSeatVisibilityDTO.getId());
						cacheDTO.setCode(overrideSeatVisibilityDTO.getCode());
						cacheDTO.setActiveFrom(overrideSeatVisibilityDTO.getActiveFrom());
						cacheDTO.setActiveTo(overrideSeatVisibilityDTO.getActiveTo());
						cacheDTO.setDayOfWeek(overrideSeatVisibilityDTO.getDayOfWeek());
						cacheDTO.setVisibilityType(overrideSeatVisibilityDTO.getVisibilityType());
						cacheDTO.setRefferenceType(overrideSeatVisibilityDTO.getRefferenceType());
						cacheDTO.setRemarks(overrideSeatVisibilityDTO.getRemarks());

						UserDTO updateUser = new UserDTO();
						updateUser.setId(Integer.valueOf(overrideSeatVisibilityDTO.getUpdatedBy()));
						updateUser = getUserDTO(authDTO, updateUser);
						overrideSeatVisibilityDTO.setUpdatedBy(updateUser.getName() + (StringUtil.isNotNull(updateUser.getLastname()) ? " " + updateUser.getLastname() : Text.EMPTY));
						cacheDTO.setUpdatedBy(overrideSeatVisibilityDTO.getUpdatedBy());
						cacheDTO.setUpdatedAt(overrideSeatVisibilityDTO.getUpdatedAt());

						List<String> overrideRefferenceList = new ArrayList<String>();
						if (seatVisibilityDTO.getRefferenceType().equals("GR") && overrideSeatVisibilityDTO.getGroupList() != null) {
							for (GroupDTO groupDTO : overrideSeatVisibilityDTO.getGroupList()) {
								overrideRefferenceList.add(String.valueOf(groupDTO.getId()));
							}
						}
						else if (seatVisibilityDTO.getRefferenceType().equals("UR") && overrideSeatVisibilityDTO.getUserList() != null) {
							for (UserDTO userDTO : overrideSeatVisibilityDTO.getUserList()) {
								overrideRefferenceList.add(String.valueOf(userDTO.getId()));
							}
						}
						else if (seatVisibilityDTO.getRefferenceType().equals("SG") && overrideSeatVisibilityDTO.getRouteList() != null) {
							for (RouteDTO routeDTO : overrideSeatVisibilityDTO.getRouteList()) {
								String route = routeDTO.getFromStation().getId() + Text.UNDER_SCORE + routeDTO.getToStation().getId();
								overrideRefferenceList.add(route);
							}

							List<String> overrideRouteUserCodes = new ArrayList<String>();
							if (overrideSeatVisibilityDTO.getRouteUsers() != null) {
								for (UserDTO userDTO : overrideSeatVisibilityDTO.getRouteUsers()) {
									overrideRouteUserCodes.add(userDTO.getCode());
								}
							}
							cacheDTO.setRouteUsers(overrideRouteUserCodes);
						}
						else if (seatVisibilityDTO.getRefferenceType().equals("BR") && overrideSeatVisibilityDTO.getOrganizations() != null) {
							for (OrganizationDTO organizationDTO : overrideSeatVisibilityDTO.getOrganizations()) {
								overrideRefferenceList.add(String.valueOf(organizationDTO.getId()));
							}
						}
						cacheDTO.setRefferenceList(overrideRefferenceList);

						StringBuilder orverrideSeatCodes = new StringBuilder();
						if (overrideSeatVisibilityDTO.getBus() != null && overrideSeatVisibilityDTO.getBus().getBusSeatLayoutDTO() != null && overrideSeatVisibilityDTO.getBus().getBusSeatLayoutDTO().getList() != null) {
							for (BusSeatLayoutDTO layoutDTO : overrideSeatVisibilityDTO.getBus().getBusSeatLayoutDTO().getList()) {
								if (orverrideSeatCodes.length() > 0) {
									orverrideSeatCodes.append(Text.COMMA);
								}
								orverrideSeatCodes.append(layoutDTO.getCode());
							}
						}
						cacheDTO.setSeatCodeList(orverrideSeatCodes.toString());
						cacheDTO.setBusId(seatVisibilityDTO.getBus().getId());

						overrideControlList.add(cacheDTO);
					}
					seatVisibilityCacheDTO.setOverrideList(overrideControlList);
				}
				seatVisibilityCacheList.add(seatVisibilityCacheDTO);
			}
		}
		return seatVisibilityCacheList;
	}

	private List<ScheduleSeatVisibilityDTO> bindSeatVisibilityFromCacheObject(List<ScheduleSeatVisibilityCacheDTO> seatVisibilityCacheDTOList) {
		List<ScheduleSeatVisibilityDTO> ScheduleSeatVisibilityDTOList = new ArrayList<>();
		if (seatVisibilityCacheDTOList != null && !seatVisibilityCacheDTOList.isEmpty()) {
			// copy from cache
			for (ScheduleSeatVisibilityCacheDTO seatVisibilityCache : seatVisibilityCacheDTOList) {
				ScheduleSeatVisibilityDTO seatVisibilityDTO = new ScheduleSeatVisibilityDTO();
				seatVisibilityDTO.setActiveFlag(seatVisibilityCache.getActiveFlag());
				seatVisibilityDTO.setCode(seatVisibilityCache.getCode());
				seatVisibilityDTO.setActiveFrom(seatVisibilityCache.getActiveFrom());
				seatVisibilityDTO.setActiveTo(seatVisibilityCache.getActiveTo());
				seatVisibilityDTO.setDayOfWeek(seatVisibilityCache.getDayOfWeek());
				seatVisibilityDTO.setVisibilityType(seatVisibilityCache.getVisibilityType());
				seatVisibilityDTO.setRefferenceType(seatVisibilityCache.getRefferenceType());
				seatVisibilityDTO.setReleaseMinutes(seatVisibilityCache.getReleaseMinutes());
				seatVisibilityDTO.setRemarks(seatVisibilityCache.getRemarks());
				seatVisibilityDTO.setUpdatedBy(seatVisibilityCache.getUpdatedBy());
				seatVisibilityDTO.setUpdatedAt(seatVisibilityCache.getUpdatedAt());

				if (seatVisibilityCache.getBusId() != 0) {
					BusDTO busDTO = new BusDTO();
					busDTO.setId(seatVisibilityCache.getBusId());

					List<BusSeatLayoutDTO> seatlist = new ArrayList<>();
					if (StringUtil.isNotNull(seatVisibilityCache.getSeatCodeList())) {
						String[] seatCodes = seatVisibilityCache.getSeatCodeList().split(",");
						for (String seatCode : seatCodes) {
							if (StringUtil.isNotNull(seatCode)) {
								BusSeatLayoutDTO busSeatTypeDTO = new BusSeatLayoutDTO();
								busSeatTypeDTO.setCode(seatCode);
								seatlist.add(busSeatTypeDTO);
							}
						}
					}
					BusSeatLayoutDTO busSeatTypeDTO = new BusSeatLayoutDTO();
					busSeatTypeDTO.setList(seatlist);
					busDTO.setBusSeatLayoutDTO(busSeatTypeDTO);
					seatVisibilityDTO.setBus(busDTO);
				}
				if (seatVisibilityCache.getRefferenceType().equals("GR")) {
					List<GroupDTO> groups = new ArrayList<GroupDTO>();
					for (String groupId : seatVisibilityCache.getRefferenceList()) {
						GroupDTO groupDTO = new GroupDTO();
						groupDTO.setId(Integer.parseInt(groupId));
						groups.add(groupDTO);
					}
					seatVisibilityDTO.setGroupList(groups);
				}
				else if (seatVisibilityCache.getRefferenceType().equals("UR")) {
					List<UserDTO> users = new ArrayList<UserDTO>();
					for (String userId : seatVisibilityCache.getRefferenceList()) {
						UserDTO userDTO = new UserDTO();
						userDTO.setId(Integer.parseInt(userId));
						users.add(userDTO);
					}
					seatVisibilityDTO.setUserList(users);
				}
				else if (seatVisibilityCache.getRefferenceType().equals("SG") && seatVisibilityCache.getRefferenceList() != null) {
					List<RouteDTO> routeList = new ArrayList<RouteDTO>();
					for (String route : seatVisibilityCache.getRefferenceList()) {
						if (StringUtil.isNull(route)) {
							continue;
						}
						RouteDTO routeDTO = new RouteDTO();

						StationDTO fromStation = new StationDTO();
						fromStation.setId(Integer.valueOf(route.split(Text.UNDER_SCORE)[Numeric.ZERO_INT]));
						routeDTO.setFromStation(fromStation);

						StationDTO toStation = new StationDTO();
						toStation.setId(Integer.valueOf(route.split(Text.UNDER_SCORE)[Numeric.ONE_INT]));
						routeDTO.setToStation(toStation);
						routeList.add(routeDTO);
					}
					seatVisibilityDTO.setRouteList(routeList);

					List<UserDTO> routeUsers = new ArrayList<UserDTO>();
					for (String userCode : seatVisibilityCache.getRouteUsers()) {
						UserDTO userDTO = new UserDTO();
						userDTO.setCode(userCode);
						routeUsers.add(userDTO);
					}
					seatVisibilityDTO.setRouteUsers(routeUsers);
				}
				else if (seatVisibilityCache.getRefferenceType().equals("BR")) {
					List<OrganizationDTO> organizations = new ArrayList<OrganizationDTO>();
					for (String organizationId : seatVisibilityCache.getRefferenceList()) {
						OrganizationDTO organizationDTO = new OrganizationDTO();
						organizationDTO.setId(Integer.parseInt(organizationId));
						organizations.add(organizationDTO);
					}
					seatVisibilityDTO.setOrganizations(organizations);
				}

				seatVisibilityDTO.setLookupCode(seatVisibilityDTO.getLookupCode());
				List<ScheduleSeatVisibilityDTO> overrideControlList = new ArrayList<>();
				if (seatVisibilityCache.getOverrideList() != null && !seatVisibilityCache.getOverrideList().isEmpty()) {
					for (ScheduleSeatVisibilityCacheDTO overRideCacheDTO : seatVisibilityCache.getOverrideList()) {
						ScheduleSeatVisibilityDTO overrideControlDTO = new ScheduleSeatVisibilityDTO();
						overrideControlDTO.setActiveFlag(overRideCacheDTO.getActiveFlag());
						overrideControlDTO.setCode(overRideCacheDTO.getCode());
						overrideControlDTO.setActiveFrom(overRideCacheDTO.getActiveFrom());
						overrideControlDTO.setActiveTo(overRideCacheDTO.getActiveTo());
						overrideControlDTO.setVisibilityType(overRideCacheDTO.getVisibilityType());
						overrideControlDTO.setRefferenceType(overRideCacheDTO.getRefferenceType());
						overrideControlDTO.setReleaseMinutes(overRideCacheDTO.getReleaseMinutes());
						overrideControlDTO.setRemarks(overRideCacheDTO.getRemarks());
						overrideControlDTO.setUpdatedAt(overRideCacheDTO.getUpdatedAt());
						overrideControlDTO.setUpdatedBy(overRideCacheDTO.getUpdatedBy());

						if (overRideCacheDTO.getRefferenceType().equals("GR")) {
							List<GroupDTO> groups = new ArrayList<>();
							for (String groupId : overRideCacheDTO.getRefferenceList()) {
								GroupDTO groupDTO = new GroupDTO();
								groupDTO.setId(Integer.parseInt(groupId));
								groups.add(groupDTO);
							}
							overrideControlDTO.setGroupList(groups);
						}
						else if (overRideCacheDTO.getRefferenceType().equals("UR")) {
							List<UserDTO> users = new ArrayList<>();
							for (String userId : overRideCacheDTO.getRefferenceList()) {
								UserDTO userDTO = new UserDTO();
								userDTO.setId(Integer.parseInt(userId));
								users.add(userDTO);
							}
							overrideControlDTO.setUserList(users);
						}
						else if (overRideCacheDTO.getRefferenceType().equals("SG")) {
							List<RouteDTO> routes = new ArrayList<RouteDTO>();
							for (String route : overRideCacheDTO.getRefferenceList()) {
								if (StringUtil.isNull(route)) {
									continue;
								}
								RouteDTO routeDTO = new RouteDTO();

								StationDTO fromStation = new StationDTO();
								fromStation.setId(Integer.valueOf(route.split(Text.UNDER_SCORE)[Numeric.ZERO_INT]));
								routeDTO.setFromStation(fromStation);

								StationDTO toStation = new StationDTO();
								toStation.setId(Integer.valueOf(route.split(Text.UNDER_SCORE)[Numeric.ONE_INT]));
								routeDTO.setToStation(toStation);
								routes.add(routeDTO);
							}
							overrideControlDTO.setRouteList(routes);

							List<UserDTO> overrideRouteUsers = new ArrayList<>();
							if (overRideCacheDTO.getRouteUsers() != null) {
								for (String userCode : overRideCacheDTO.getRouteUsers()) {
									UserDTO userDTO = new UserDTO();
									userDTO.setCode(userCode);
									overrideRouteUsers.add(userDTO);
								}
							}
							overrideControlDTO.setRouteUsers(overrideRouteUsers);
						}
						else if (overRideCacheDTO.getRefferenceType().equals("BR")) {
							List<OrganizationDTO> overrideOrganizations = new ArrayList<OrganizationDTO>();
							for (String organizationId : overRideCacheDTO.getRefferenceList()) {
								OrganizationDTO organizationDTO = new OrganizationDTO();
								organizationDTO.setId(Integer.parseInt(organizationId));
								overrideOrganizations.add(organizationDTO);
							}
							overrideControlDTO.setOrganizations(overrideOrganizations);
						}

						if (overRideCacheDTO.getBusId() != 0) {
							BusDTO busDTO = new BusDTO();
							busDTO.setId(overRideCacheDTO.getBusId());

							List<BusSeatLayoutDTO> seatlist = new ArrayList<>();
							if (StringUtil.isNotNull(overRideCacheDTO.getSeatCodeList())) {
								String[] seatCodes = overRideCacheDTO.getSeatCodeList().split(",");
								for (String seatCode : seatCodes) {
									if (StringUtil.isNotNull(seatCode)) {
										BusSeatLayoutDTO busSeatTypeDTO = new BusSeatLayoutDTO();
										busSeatTypeDTO.setCode(seatCode);
										seatlist.add(busSeatTypeDTO);
									}
								}
							}
							BusSeatLayoutDTO busSeatTypeDTO = new BusSeatLayoutDTO();
							busSeatTypeDTO.setList(seatlist);
							busDTO.setBusSeatLayoutDTO(busSeatTypeDTO);
							overrideControlDTO.setBus(busDTO);
						}

						overrideControlList.add(overrideControlDTO);
					}
					seatVisibilityDTO.setOverrideList(overrideControlList);
				}
				ScheduleSeatVisibilityDTOList.add(seatVisibilityDTO);
			}
		}
		return ScheduleSeatVisibilityDTOList;
	}
}
