package org.in.com.cache.redis;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.in.com.cache.dto.ExtraCommissionCacheDTO;
import org.in.com.cache.dto.TripStageSeatDetailCacheDTO;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.ExtraCommissionDTO;
import org.in.com.dto.ExtraCommissionSlabDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TicketExtraDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.DateTypeEM;
import org.in.com.dto.enumeration.FareTypeEM;
import org.in.com.dto.enumeration.SeatGendarEM;
import org.in.com.dto.enumeration.SlabCalenderModeEM;
import org.in.com.dto.enumeration.SlabCalenderTypeEM;
import org.in.com.dto.enumeration.SlabModeEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.dto.enumeration.TravelStatusEM;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class RedisTripCacheServiceImpl implements RedisTripCacheService {
	@Autowired
	private CacheManager cacheManager;

	public List<TicketDetailsDTO> getBookedBlockedSeatsCache(AuthDTO authDTO, TripDTO tripDTO) {
		List<TicketDetailsDTO> list = null;
		List<TripStageSeatDetailCacheDTO> seatDetailCacheDTOList = null;
		ValueWrapper cache = cacheManager.getCache(RedisCacheTypeEM.TRIP_DETAILS.getCode()).get(tripDTO.getCode());
		if (cache != null && cache.get() != null) {
			seatDetailCacheDTOList = (List<TripStageSeatDetailCacheDTO>) cache.get();
			list = bindSeatDetailsFromCacheObject(seatDetailCacheDTOList);
		}
		return list;
	}

	public void putBookedBlockedSeatsCache(AuthDTO authDTO, TripDTO tripDTO, List<TicketDetailsDTO> list) {
		if (list != null) {
			List<TripStageSeatDetailCacheDTO> cacheDTOList = bindSeatDetailsToCacheObject(authDTO, list);
			cacheManager.getCache(RedisCacheTypeEM.TRIP_DETAILS.getCode()).put(tripDTO.getCode(), cacheDTOList);
		}
	}

	private List<TripStageSeatDetailCacheDTO> bindSeatDetailsToCacheObject(AuthDTO authDTO, List<TicketDetailsDTO> list) {
		List<TripStageSeatDetailCacheDTO> cacheList = new ArrayList<TripStageSeatDetailCacheDTO>();
		if (list != null && !list.isEmpty()) {
			for (TicketDetailsDTO ticketDetailsDTO : list) {
				if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId() && DateUtil.getMinutiesDifferent(ticketDetailsDTO.getUpdatedAt(), DateUtil.NOW()) > authDTO.getNamespace().getProfile().getSeatBlockTime()) {
					continue;
				}
				TripStageSeatDetailCacheDTO detailCacheDTO = new TripStageSeatDetailCacheDTO();
				detailCacheDTO.setTicketCode(ticketDetailsDTO.getTicketCode());
				detailCacheDTO.setBoardingPointName(ticketDetailsDTO.getBoardingPointName());
				detailCacheDTO.setStationPoint(ticketDetailsDTO.getStationPoint());
				detailCacheDTO.setTripStageCode(ticketDetailsDTO.getTripStageCode());
				detailCacheDTO.setFromStationId(ticketDetailsDTO.getFromStation().getId());
				detailCacheDTO.setToStationId(ticketDetailsDTO.getToStation().getId());
				detailCacheDTO.setSeatCode(ticketDetailsDTO.getSeatCode());
				detailCacheDTO.setSeatName(ticketDetailsDTO.getSeatName());
				detailCacheDTO.setSeatFare(ticketDetailsDTO.getSeatFare().toString());
				detailCacheDTO.setSeatGendarCode(ticketDetailsDTO.getSeatGendar().getCode());
				detailCacheDTO.setPassengerName(ticketDetailsDTO.getPassengerName());
				detailCacheDTO.setPassengerAge(ticketDetailsDTO.getPassengerAge());
				detailCacheDTO.setContactNumber(ticketDetailsDTO.getContactNumber());
				detailCacheDTO.setUserId(ticketDetailsDTO.getUser().getId());
				detailCacheDTO.setTicketStatusCode(ticketDetailsDTO.getTicketStatus().getCode());
				detailCacheDTO.setUpdatedAt(DateUtil.convertDateTime(ticketDetailsDTO.getUpdatedAt()));
				detailCacheDTO.setTicketAt(DateUtil.convertDateTime(ticketDetailsDTO.getTicketAt()));
				detailCacheDTO.setBlockReleaseMinutes(ticketDetailsDTO.getBlockReleaseMinutes());
				detailCacheDTO.setNetAmount(ticketDetailsDTO.getNetAmount().toString());
				detailCacheDTO.setAcBusTax(ticketDetailsDTO.getTicketExtra() != null && ticketDetailsDTO.getTicketExtra().getAcBusTax() != null ? ticketDetailsDTO.getTicketExtra().getAcBusTax().toString() : BigDecimal.ZERO.toString());
				detailCacheDTO.setTicketEditDetails(ticketDetailsDTO.getTicketExtra() != null && StringUtil.isNotNull(ticketDetailsDTO.getTicketExtra().getTicketEditDetails()) ? ticketDetailsDTO.getTicketExtra().getTicketEditDetails() : "000000");
				detailCacheDTO.setTicketTransferMinutes(ticketDetailsDTO.getTicketExtra() != null ? ticketDetailsDTO.getTicketExtra().getTicketTransferMinutes() : Numeric.ZERO_INT);
				detailCacheDTO.setTravelStatus(ticketDetailsDTO.getTicketExtra() != null ? ticketDetailsDTO.getTicketExtra().getTravelStatus().getId() : Numeric.ONE_INT);
				cacheList.add(detailCacheDTO);
			}
		}
		return cacheList;
	}

	private List<TicketDetailsDTO> bindSeatDetailsFromCacheObject(List<TripStageSeatDetailCacheDTO> seatDetailCacheDTOList) {
		List<TicketDetailsDTO> list = new ArrayList<TicketDetailsDTO>();
		if (seatDetailCacheDTOList != null && !seatDetailCacheDTOList.isEmpty()) {
			for (TripStageSeatDetailCacheDTO seatDetailCacheDTO : seatDetailCacheDTOList) {
				TicketDetailsDTO detailsDTO = new TicketDetailsDTO();
				detailsDTO.setTicketCode(seatDetailCacheDTO.getTicketCode());
				detailsDTO.setBoardingPointName(seatDetailCacheDTO.getBoardingPointName());
				detailsDTO.setStationPoint(seatDetailCacheDTO.getStationPoint());
				detailsDTO.setTripStageCode(seatDetailCacheDTO.getTripStageCode());
				StationDTO fromStationDTO = new StationDTO();
				StationDTO toStationDTO = new StationDTO();
				fromStationDTO.setId(seatDetailCacheDTO.getFromStationId());
				detailsDTO.setFromStation(fromStationDTO);
				toStationDTO.setId(seatDetailCacheDTO.getToStationId());
				detailsDTO.setToStation(toStationDTO);
				detailsDTO.setSeatCode(seatDetailCacheDTO.getSeatCode());
				detailsDTO.setSeatName(seatDetailCacheDTO.getSeatName());
				detailsDTO.setSeatFare(new BigDecimal(seatDetailCacheDTO.getSeatFare()));
				detailsDTO.setSeatGendar(SeatGendarEM.getSeatGendarEM(seatDetailCacheDTO.getSeatGendarCode()));
				detailsDTO.setPassengerName(seatDetailCacheDTO.getPassengerName());
				detailsDTO.setPassengerAge(seatDetailCacheDTO.getPassengerAge());
				detailsDTO.setContactNumber(seatDetailCacheDTO.getContactNumber());
				UserDTO userDTO = new UserDTO();
				userDTO.setId(seatDetailCacheDTO.getUserId());
				detailsDTO.setUser(userDTO);
				detailsDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(seatDetailCacheDTO.getTicketStatusCode()));
				detailsDTO.setUpdatedAt(DateUtil.getDateTime(seatDetailCacheDTO.getUpdatedAt()));
				detailsDTO.setTicketAt(DateUtil.getDateTime(seatDetailCacheDTO.getTicketAt()));
				detailsDTO.setNetAmount(StringUtil.getBigDecimalValue(seatDetailCacheDTO.getNetAmount()));
				detailsDTO.setAcBusTax(StringUtil.getBigDecimalValue(seatDetailCacheDTO.getAcBusTax()));

				TicketExtraDTO ticketExtra = new TicketExtraDTO();
				ticketExtra.setBlockReleaseMinutes(seatDetailCacheDTO.getBlockReleaseMinutes());
				ticketExtra.setAcBusTax(StringUtil.getBigDecimalValue(seatDetailCacheDTO.getAcBusTax()));
				if (StringUtil.isNotNull(seatDetailCacheDTO.getTicketEditDetails()) && seatDetailCacheDTO.getTicketEditDetails().length() == Numeric.SIX_INT) {
					convertTicketExtra(seatDetailCacheDTO.getTicketEditDetails(), ticketExtra);
				}
				ticketExtra.setTicketTransferMinutes(seatDetailCacheDTO.getTicketTransferMinutes());
				ticketExtra.setTravelStatus(TravelStatusEM.getTravelStatusEM(seatDetailCacheDTO.getTravelStatus()));
				detailsDTO.setTicketExtra(ticketExtra);

				list.add(detailsDTO);
			}
		}
		return list;
	}

	private TicketExtraDTO convertTicketExtra(String ticketEditDetails, TicketExtraDTO ticketExtraDTO) {
		ticketExtraDTO.setEditBoardingPoint(StringUtil.getIntegerValue(String.valueOf(ticketEditDetails.charAt(Numeric.ZERO_INT))));
		ticketExtraDTO.setEditDroppingPoint(StringUtil.getIntegerValue(String.valueOf(ticketEditDetails.charAt(Numeric.ONE_INT))));
		ticketExtraDTO.setEditPassengerDetails(StringUtil.getIntegerValue(String.valueOf(ticketEditDetails.charAt(Numeric.TWO_INT))));
		ticketExtraDTO.setEditChangeSeat(StringUtil.getIntegerValue(String.valueOf(ticketEditDetails.charAt(Numeric.THREE_INT))));
		ticketExtraDTO.setEditMobileNumber(StringUtil.getIntegerValue(String.valueOf(ticketEditDetails.charAt(Numeric.FOUR_INT))));
		ticketExtraDTO.setTicketTransfer(StringUtil.getIntegerValue(String.valueOf(ticketEditDetails.charAt(Numeric.FIVE_INT))));
		return ticketExtraDTO;
	}

	public void clearBookedBlockedSeatsCache(AuthDTO authDTO, TripDTO tripDTO) {
		if (StringUtil.isNull(tripDTO.getCode())) {
			System.out.println("REDISER01 not able Clear Trip stage seat details: " + tripDTO.getId());
		}
		cacheManager.getCache(RedisCacheTypeEM.TRIP_DETAILS.getCode()).evict(tripDTO.getCode());
	}

	public List<ExtraCommissionDTO> getAllExtraCommissionCache(AuthDTO authDTO) {
		List<ExtraCommissionDTO> extraCommissionDTOList = null;
		List<ExtraCommissionCacheDTO> extraCommissionCacheDTOList = null;
		ValueWrapper cache = cacheManager.getCache(RedisCacheTypeEM.ONE_DAY_CACHE.getCode()).get(getExtraCommissionCacheKey(authDTO));
		if (cache != null && cache.get() != null) {
			extraCommissionCacheDTOList = (List<ExtraCommissionCacheDTO>) cache.get();
			extraCommissionDTOList = bindExtraCommissionFromCacheObject(extraCommissionCacheDTOList);
		}
		return extraCommissionDTOList;
	}

	public void putgetAllExtraCommissionCache(AuthDTO authDTO, List<ExtraCommissionDTO> extraCommissionDTOList) {
		if (extraCommissionDTOList != null) {
			List<ExtraCommissionCacheDTO> cacheDTOList = bindExtraCommissionToCacheObject(authDTO, extraCommissionDTOList);
			cacheManager.getCache(RedisCacheTypeEM.ONE_DAY_CACHE.getCode()).put(getExtraCommissionCacheKey(authDTO), cacheDTOList);
		}
	}

	private List<ExtraCommissionCacheDTO> bindExtraCommissionToCacheObject(AuthDTO authDTO, List<ExtraCommissionDTO> extraCommissionDTOList) {
		List<ExtraCommissionCacheDTO> list = new ArrayList<ExtraCommissionCacheDTO>();
		if (extraCommissionDTOList != null && !extraCommissionDTOList.isEmpty()) {
			for (ExtraCommissionDTO extraCommissionDTO : extraCommissionDTOList) {
				ExtraCommissionCacheDTO cacheDTO = new ExtraCommissionCacheDTO();
				cacheDTO.setActiveFrom(extraCommissionDTO.getActiveFrom());
				cacheDTO.setActiveTo(extraCommissionDTO.getActiveTo());
				cacheDTO.setCode(extraCommissionDTO.getCode());
				cacheDTO.setCommissionValue(extraCommissionDTO.getCommissionValue());
				cacheDTO.setCommissionValueTypeCode(extraCommissionDTO.getCommissionValueType().getCode());
				cacheDTO.setDateTypeCode(extraCommissionDTO.getDateType().getCode());
				cacheDTO.setDayOfWeek(extraCommissionDTO.getDayOfWeek());
				cacheDTO.setOverrideCommissionFlag(extraCommissionDTO.getOverrideCommissionFlag());
				if (extraCommissionDTO.getGroup() != null) {
					List<Integer> groupIds = new ArrayList<>();
					for (GroupDTO groupDTO : extraCommissionDTO.getGroup()) {
						groupIds.add(groupDTO.getId());
					}
					cacheDTO.setGroupId(groupIds);
				}
				cacheDTO.setMaxCommissionLimit(extraCommissionDTO.getMaxCommissionLimit());
				cacheDTO.setMaxExtraCommissionAmount(extraCommissionDTO.getMaxExtraCommissionAmount());
				cacheDTO.setMinSeatCount(extraCommissionDTO.getMinSeatCount());
				cacheDTO.setMinTicketFare(extraCommissionDTO.getMinTicketFare());
				cacheDTO.setRefferenceType(extraCommissionDTO.getRefferenceType());

				StringBuilder scheduleCodes = new StringBuilder();
				if (extraCommissionDTO.getScheduleList() != null && !extraCommissionDTO.getScheduleList().isEmpty()) {
					for (ScheduleDTO scheduleDTO : extraCommissionDTO.getScheduleList()) {
						if (scheduleCodes.length() > 0) {
							scheduleCodes.append(",");
						}
						scheduleCodes.append(scheduleDTO.getCode());
					}
				}
				cacheDTO.setScheduleCodeList(scheduleCodes.toString());

				StringBuilder routeCodes = new StringBuilder();
				if (extraCommissionDTO.getRouteList() != null && !extraCommissionDTO.getRouteList().isEmpty()) {
					for (RouteDTO routeDTO : extraCommissionDTO.getRouteList()) {
						if (routeCodes.length() > 0) {
							routeCodes.append(",");
						}
						routeCodes.append(routeDTO.getCode());
					}
				}

				cacheDTO.setRouteCodeList(routeCodes.toString());
				if (extraCommissionDTO.getUser() != null) {
					List<Integer> userIds = new ArrayList<>();
					for (UserDTO user : extraCommissionDTO.getUser()) {
						userIds.add(user.getId());
					}
					cacheDTO.setUserId(userIds);
				}
				if (extraCommissionDTO.getCommissionSlab() != null) {
					cacheDTO.setExtraCommissionSlabcode(extraCommissionDTO.getCommissionSlab().getCode());
					cacheDTO.setSlabCalenderTypeCode(extraCommissionDTO.getCommissionSlab().getSlabCalenderType().getCode());
					cacheDTO.setSlabCalenderModeCode(extraCommissionDTO.getCommissionSlab().getSlabCalenderMode().getCode());
					cacheDTO.setSlabFromValue(extraCommissionDTO.getCommissionSlab().getSlabFromValue());
					cacheDTO.setSlabToValue(extraCommissionDTO.getCommissionSlab().getSlabToValue());
					cacheDTO.setSlabModeCode(extraCommissionDTO.getCommissionSlab().getSlabMode().getCode());
				}

				if (extraCommissionDTO.getOverrideList() != null && !extraCommissionDTO.getOverrideList().isEmpty()) {
					List<ExtraCommissionCacheDTO> overrideList = new ArrayList<ExtraCommissionCacheDTO>();
					for (ExtraCommissionDTO overrideCommissionDTO : extraCommissionDTO.getOverrideList()) {
						ExtraCommissionCacheDTO overrideCacheDTO = new ExtraCommissionCacheDTO();
						overrideCacheDTO.setActiveFrom(overrideCommissionDTO.getActiveFrom());
						overrideCacheDTO.setActiveTo(overrideCommissionDTO.getActiveTo());
						overrideCacheDTO.setDayOfWeek(overrideCommissionDTO.getDayOfWeek());
						overrideList.add(overrideCacheDTO);
					}
					cacheDTO.setOverrideList(overrideList);
				}

				list.add(cacheDTO);
			}
		}
		return list;
	}

	private List<ExtraCommissionDTO> bindExtraCommissionFromCacheObject(List<ExtraCommissionCacheDTO> extraCommissionCacheDTOList) {
		List<ExtraCommissionDTO> list = new ArrayList<ExtraCommissionDTO>();
		if (extraCommissionCacheDTOList != null && !extraCommissionCacheDTOList.isEmpty()) {
			for (ExtraCommissionCacheDTO cacheDTO : extraCommissionCacheDTOList) {
				ExtraCommissionDTO commissionDTO = new ExtraCommissionDTO();
				commissionDTO.setActiveFlag(1);
				commissionDTO.setActiveFrom(cacheDTO.getActiveFrom());
				commissionDTO.setActiveTo(cacheDTO.getActiveTo());
				commissionDTO.setCode(cacheDTO.getCode());
				commissionDTO.setCommissionValue(cacheDTO.getCommissionValue());
				commissionDTO.setCommissionValueType(FareTypeEM.getFareTypeEM(cacheDTO.getCommissionValueTypeCode()));
				commissionDTO.setDateType(DateTypeEM.getDateTypeEM(cacheDTO.getDateTypeCode()));
				commissionDTO.setDayOfWeek(cacheDTO.getDayOfWeek());
				commissionDTO.setOverrideCommissionFlag(cacheDTO.getOverrideCommissionFlag());
				if (cacheDTO.getGroupId() != null) {
					List<GroupDTO> groupList = new ArrayList<>();
					for (int groupId : cacheDTO.getGroupId()) {
						GroupDTO groupDTO = new GroupDTO();
						groupDTO.setId(groupId);
						groupList.add(groupDTO);
					}
					commissionDTO.setGroup(groupList);
				}
				commissionDTO.setMaxCommissionLimit(cacheDTO.getMaxCommissionLimit());
				commissionDTO.setMaxExtraCommissionAmount(cacheDTO.getMaxExtraCommissionAmount());
				commissionDTO.setMinSeatCount(cacheDTO.getMinSeatCount());
				commissionDTO.setMinTicketFare(cacheDTO.getMinTicketFare());
				commissionDTO.setRefferenceType(cacheDTO.getRefferenceType());
				List<ScheduleDTO> scheduleList = new ArrayList<ScheduleDTO>();
				if (StringUtil.isNotNull(cacheDTO.getScheduleCodeList())) {
					for (String code : cacheDTO.getScheduleCodeList().split(",")) {
						ScheduleDTO scheduleDTO = new ScheduleDTO();
						scheduleDTO.setCode(code);
						scheduleList.add(scheduleDTO);
					}
				}
				commissionDTO.setScheduleList(scheduleList);
				List<RouteDTO> routeList = new ArrayList<RouteDTO>();
				if (StringUtil.isNotNull(cacheDTO.getRouteCodeList())) {
					for (String code : cacheDTO.getRouteCodeList().split(",")) {
						RouteDTO routeDTO = new RouteDTO();
						routeDTO.setCode(code);
						routeList.add(routeDTO);
					}
				}
				commissionDTO.setScheduleList(scheduleList);

				commissionDTO.setRouteList(routeList);
				if (cacheDTO.getUserId() != null) {
					List<UserDTO> userList = new ArrayList<>();
					for (int userId : cacheDTO.getUserId()) {
						UserDTO userDTO = new UserDTO();
						userDTO.setId(userId);
						userList.add(userDTO);
					}
					commissionDTO.setUser(userList);
				}
				if (StringUtil.isNotNull(cacheDTO.getExtraCommissionSlabcode())) {
					ExtraCommissionSlabDTO commissionSlabDTO = new ExtraCommissionSlabDTO();
					commissionSlabDTO.setCode(cacheDTO.getExtraCommissionSlabcode());
					commissionSlabDTO.setSlabCalenderType(SlabCalenderTypeEM.getSlabCalenderTypeEM(cacheDTO.getSlabCalenderTypeCode()));
					commissionSlabDTO.setSlabCalenderMode(SlabCalenderModeEM.getSlabCalenderModeEM(cacheDTO.getSlabCalenderModeCode()));
					commissionSlabDTO.setSlabFromValue(cacheDTO.getSlabFromValue());
					commissionSlabDTO.setSlabToValue(cacheDTO.getSlabToValue());
					commissionSlabDTO.setSlabMode(SlabModeEM.getSlabModeEM(cacheDTO.getSlabModeCode()));
					commissionDTO.setCommissionSlab(commissionSlabDTO);
				}
				if (cacheDTO.getOverrideList() != null && !cacheDTO.getOverrideList().isEmpty()) {
					List<ExtraCommissionDTO> overrideList = new ArrayList<ExtraCommissionDTO>();
					for (ExtraCommissionCacheDTO overrideCacheDTO : cacheDTO.getOverrideList()) {
						ExtraCommissionDTO overrideDTO = new ExtraCommissionDTO();
						overrideDTO.setActiveFrom(overrideCacheDTO.getActiveFrom());
						overrideDTO.setActiveTo(overrideCacheDTO.getActiveTo());
						overrideDTO.setDayOfWeek(overrideCacheDTO.getDayOfWeek());
						overrideList.add(overrideDTO);
					}
					commissionDTO.setOverrideList(overrideList);
				}

				list.add(commissionDTO);
			}
		}
		return list;
	}

	public void clearAllExtraCommissionCache(AuthDTO authDTO) {
		cacheManager.getCache(RedisCacheTypeEM.ONE_DAY_CACHE.getCode()).evict(getExtraCommissionCacheKey(authDTO));
	}

	private Object getExtraCommissionCacheKey(AuthDTO authDTO) {
		return authDTO.getNamespaceCode() + "_EXTRA_COMMISSION";
	}

	public void putNotifyFareChangeRequest(AuthDTO authDTO, ScheduleDTO schedule) {
		String request = authDTO.getNamespaceCode() + Text.UNDER_SCORE + schedule.getCode() + Text.UNDER_SCORE + DateUtil.convertDate(schedule.getTripDate());
		cacheManager.getCache(RedisCacheTypeEM.ONE_DAY_CACHE.getCode()).put("DP_REQUEST_" + request, request);
	}

	public JSONArray getNotifyFareChangeRequest() {
		JSONArray requests = new JSONArray();
		if (cacheManager != null && cacheManager.getCache(RedisCacheTypeEM.ONE_DAY_CACHE.getCode()) != null) {
			Cache cache = cacheManager.getCache(RedisCacheTypeEM.ONE_DAY_CACHE.getCode());
			RedisTemplate<String, Object> template = (RedisTemplate<String, Object>) cache.getNativeCache();
			for (String cacheKey : template.keys("DP_REQUEST_*")) {
				try {
					ValueWrapper valueWrapper = cacheManager.getCache(RedisCacheTypeEM.ONE_DAY_CACHE.getCode()).get(cacheKey);
					if (valueWrapper != null && valueWrapper.get() != null) {
						String request = (String) valueWrapper.get();
						String[] req = request.split("\\_");
						JSONObject jsonObject = new JSONObject();
						jsonObject.put("namespaceCode", req[0]);
						jsonObject.put("scheduleCode", req[1]);
						jsonObject.put("tripDate", req[2]);
						requests.add(jsonObject);
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return requests;
	}

	public void removeNotifyFareChangeRequest(AuthDTO authDTO, ScheduleDTO schedule) {
		String key = authDTO.getNamespaceCode() + Text.UNDER_SCORE + schedule.getCode() + Text.UNDER_SCORE + DateUtil.convertDate(schedule.getTripDate());
		cacheManager.getCache(RedisCacheTypeEM.ONE_DAY_CACHE.getCode()).evict("DP_REQUEST_" + key);
	}
		
	public Map<String, Map<String, String>> getTripDataCountCache(AuthDTO authDTO) {
		Map<String, Map<String, String>> dataMap = null;
		ValueWrapper cache = cacheManager.getCache(RedisCacheTypeEM.TRIP_DATA_CACHE.getCode()).get(authDTO.getNamespaceCode());
		if (cache != null && cache.get() != null) {
			dataMap = (Map<String, Map<String, String>>) cache.get();
		}
		return dataMap;
	}

	public void putTripDataCountCache(AuthDTO authDTO, Map<String, Map<String, String>> dataMap) {
		if (dataMap != null) {
			cacheManager.getCache(RedisCacheTypeEM.TRIP_DATA_CACHE.getCode()).put(authDTO.getNamespaceCode(), dataMap);
		}
	}
}
