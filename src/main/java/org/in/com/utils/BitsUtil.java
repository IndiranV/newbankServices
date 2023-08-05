package org.in.com.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.in.com.cache.EhcacheManager;
import org.in.com.cache.StationPointCache;
import org.in.com.constants.Constants;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusVehicleDTO;
import org.in.com.dto.CommissionDTO;
import org.in.com.dto.DBQueryParamDTO;
import org.in.com.dto.FareRuleDetailsDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.MenuEventDTO;
import org.in.com.dto.NamespaceTaxDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleDiscountDTO;
import org.in.com.dto.ScheduleStageDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.ScheduleStationPointDTO;
import org.in.com.dto.ScheduleTicketTransferTermsDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StageFareDTO;
import org.in.com.dto.StageStationDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TermDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.BusCategoryTypeEM;
import org.in.com.dto.enumeration.BusSeatTypeEM;
import org.in.com.dto.enumeration.CommissionTypeEM;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.DynamicPriceProviderEM;
import org.in.com.dto.enumeration.MenuEventEM;
import org.in.com.dto.enumeration.MinutesTypeEM;
import org.in.com.dto.enumeration.EventNotificationEM;
import org.in.com.dto.enumeration.NotificationMediumEM;
import org.in.com.dto.enumeration.SeatGendarEM;
import org.in.com.dto.enumeration.UserTagEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;

import hirondelle.date4j.DateTime;
import net.sf.ehcache.Element;
import net.sf.json.JSONObject;

public class BitsUtil {

	public static final String TRIP_STAGE_CODE_REGEX = "([0-9]*)N([0-9]*)S([0-9]*)D([0-9]*)T(\\d*)";
	public static final String MASKED_MOBILE_NUMBER_PATTERN = "([0-9]{3}[x]{3}[0-9]{4})";
	private static final String STATION_POINT_SEPARATOR = Text.COLON + Text.HYPHEN + Text.COLON;

	public static boolean isValidTripStageCode(String tripStageCode) {
		if ((tripStageCode.trim()).matches(TRIP_STAGE_CODE_REGEX)) {
			return true;
		}
		return false;
	}

	public static Map<String, Integer> getTripStage(String tripStageCode) {
		Map<String, Integer> stage = new HashMap<String, Integer>();
		Pattern pattern = Pattern.compile(TRIP_STAGE_CODE_REGEX);
		Matcher matcher = pattern.matcher(tripStageCode);
		if (matcher.find()) {
			stage.put("namespace", Integer.parseInt(matcher.group(1)));
			stage.put("schedule", Integer.parseInt(matcher.group(2)));
			stage.put("tripDate", Integer.parseInt(matcher.group(3)));
			stage.put("fromStation", Integer.parseInt(matcher.group(4)));
			stage.put("toStation", Integer.parseInt(matcher.group(5)));
		}
		return stage;
	}

	public static StageStationDTO getOriginStageStation(List<StageStationDTO> stageList) {
		StageStationDTO firstStageStationDTO = null;
		for (StageStationDTO stageStation : stageList) {
			if (firstStageStationDTO == null) {
				firstStageStationDTO = stageStation;
			}
			if (stageStation.getStationSequence() < firstStageStationDTO.getStationSequence()) {
				firstStageStationDTO = stageStation;
			}
		}
		return firstStageStationDTO;
	}

	public static DateTime getOriginStationPointDateTime(List<StageStationDTO> stageList, DateTime tripDateTime) {
		StageStationDTO stageStationDTO = BitsUtil.getOriginStageStation(stageList);
		StationPointDTO stationPointDTO = BitsUtil.getOriginStationPoint(stageStationDTO.getStationPoint());
		DateTime originDateTime = DateUtil.addMinituesToDate(tripDateTime, stageStationDTO.getMinitues() + stationPointDTO.getMinitues());
		return originDateTime;
	}

	public static DateTime getDestinationStationTime(List<StageStationDTO> stageList, DateTime tripDate) {
		StageStationDTO lastStageStationDTO = BitsUtil.getDestinationStageStation(stageList);
		DateTime destinationDateTime = DateUtil.addMinituesToDate(tripDate, lastStageStationDTO.getMinitues());
		return destinationDateTime;
	}

	public static DateTime getOriginStationTime(List<StageStationDTO> stageList, DateTime tripDate) {
		StageStationDTO firstStageStationDTO = BitsUtil.getOriginStageStation(stageList);
		DateTime originDateTime = DateUtil.addMinituesToDate(tripDate, firstStageStationDTO.getMinitues());
		return originDateTime;
	}

	public static ScheduleStationDTO getOriginStation(List<ScheduleStationDTO> stationDTOList) {
		ScheduleStationDTO stationDTO = null;
		if (stationDTOList != null && !stationDTOList.isEmpty()) {
			for (ScheduleStationDTO scheduleStationDTO : stationDTOList) {
				if (stationDTO == null) {
					stationDTO = scheduleStationDTO;
				}
				if (scheduleStationDTO.getStationSequence() < stationDTO.getStationSequence()) {
					stationDTO = scheduleStationDTO;
				}
			}
		}
		return stationDTO;
	}

	public static DateTime getOriginScheduleStationTime(List<ScheduleStationDTO> stationDTOList, DateTime tripDate) {
		ScheduleStationDTO firstScheduleStationDTO = BitsUtil.getOriginStation(stationDTOList);
		DateTime originDateTime = DateUtil.addMinituesToDate(tripDate, firstScheduleStationDTO.getMinitues());
		return originDateTime;
	}

	public static DateTime getDestinationScheduleStationTime(List<ScheduleStationDTO> stationDTOList, DateTime tripDate) {
		ScheduleStationDTO lastScheduleStationDTO = BitsUtil.getDestinationStation(stationDTOList);
		DateTime destinationDateTime = DateUtil.addMinituesToDate(tripDate, lastScheduleStationDTO.getMinitues());
		return destinationDateTime;
	}

	public static ScheduleStationDTO getDestinationStation(List<ScheduleStationDTO> stationDTOList) {
		ScheduleStationDTO stationDTO = null;
		if (stationDTOList != null && !stationDTOList.isEmpty()) {
			for (ScheduleStationDTO scheduleStationDTO : stationDTOList) {
				if (stationDTO == null) {
					stationDTO = scheduleStationDTO;
				}
				if (scheduleStationDTO.getStationSequence() > stationDTO.getStationSequence()) {
					stationDTO = scheduleStationDTO;
				}
			}
		}
		return stationDTO;
	}

	public static StationPointDTO getOriginStationPoint(List<StationPointDTO> stationPointList) {
		StationPointDTO stationPoint = null;
		if (stationPointList != null && !stationPointList.isEmpty()) {
			for (StationPointDTO stationPointDTO : stationPointList) {
				if (stationPoint == null) {
					stationPoint = stationPointDTO;
				}
				if (stationPointDTO.getMinitues() < stationPoint.getMinitues()) {
					stationPoint = stationPointDTO;
				}
			}
		}
		return stationPoint;
	}

	public static StationPointDTO getDestinationStationPoint(List<StationPointDTO> stationPointList) {
		StationPointDTO stationPoint = null;
		if (stationPointList != null && !stationPointList.isEmpty()) {
			for (StationPointDTO stationPointDTO : stationPointList) {
				if (stationPoint == null) {
					stationPoint = stationPointDTO;
				}
				if (stationPointDTO.getMinitues() > stationPoint.getMinitues()) {
					stationPoint = stationPointDTO;
				}
			}
		}
		return stationPoint;
	}

	public static Map<String, StationPointDTO> getBusVehicleVanPickupStationPoint(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		Map<String, StationPointDTO> stationPointMap = new HashMap<String, StationPointDTO>();
		StationPointCache stationPointCache = new StationPointCache();
		if (scheduleDTO.getStationPointList() != null) {
			for (ScheduleStationPointDTO scheduleStationPointDTO : scheduleDTO.getStationPointList()) {
				if (scheduleStationPointDTO.getBusVehicleVanPickup() != null && scheduleStationPointDTO.getBusVehicleVanPickup().getId() > 0) {
					StationPointDTO stationPointDTO = stationPointCache.getStationPointDTObyId(authDTO, scheduleStationPointDTO.getStationPoint());
					stationPointMap.put(stationPointDTO.getCode(), stationPointDTO);
				}
			}
		}
		return stationPointMap;
	}

	public static StageStationDTO getDestinationStageStation(List<StageStationDTO> stageList) {
		StageStationDTO lastStageStationDTO = null;
		for (StageStationDTO stageStation : stageList) {
			if (lastStageStationDTO == null) {
				lastStageStationDTO = stageStation;
			}
			if (stageStation.getStationSequence() > lastStageStationDTO.getStationSequence()) {
				lastStageStationDTO = stageStation;
			}
		}
		return lastStageStationDTO;
	}

	public static List<StageStationDTO> getStageStations(List<StageDTO> stageDTOList) {
		Map<String, StageStationDTO> stationMap = new HashMap<String, StageStationDTO>();
		for (StageDTO stageDTO : stageDTOList) {
			StageStationDTO fromStation = stageDTO.getFromStation();
			StageStationDTO toStation = stageDTO.getToStation();

			// boarding
			if (stationMap.get(fromStation.getStation().getCode()) != null) {
				StageStationDTO stageStationDTO = stationMap.get(fromStation.getStation().getCode());

				List<StationPointDTO> newStationPointList = new ArrayList<StationPointDTO>();
				Map<String, StationPointDTO> stationPointMap = new HashMap<String, StationPointDTO>();

				for (StationPointDTO stationPointDTO : fromStation.getStationPoint()) {
					stationPointMap.put(stationPointDTO.getCode(), stationPointDTO);
				}

				for (StationPointDTO stationPoint : stageStationDTO.getStationPoint()) {
					if (stationPointMap.get(stationPoint.getCode()) == null) {
						newStationPointList.add(stationPoint);
					}
				}
				if (!newStationPointList.isEmpty()) {
					stageStationDTO.getStationPoint().addAll(newStationPointList);
					stationMap.put(fromStation.getStation().getCode(), stageStationDTO);
				}
			}
			else {
				stationMap.put(fromStation.getStation().getCode(), fromStation);
			}

			// dropping
			if (stationMap.get(toStation.getStation().getCode()) != null) {
				StageStationDTO stageStationDTO = stationMap.get(toStation.getStation().getCode());

				List<StationPointDTO> newStationPointList = new ArrayList<StationPointDTO>();
				Map<String, StationPointDTO> stationPointMap = new HashMap<String, StationPointDTO>();

				for (StationPointDTO stationPointDTO : toStation.getStationPoint()) {
					stationPointMap.put(stationPointDTO.getCode(), stationPointDTO);
				}

				for (StationPointDTO stationPoint : stageStationDTO.getStationPoint()) {
					if (stationPointMap.get(stationPoint.getCode()) == null) {
						newStationPointList.add(stationPoint);
					}
				}
				if (!newStationPointList.isEmpty()) {
					stageStationDTO.getStationPoint().addAll(newStationPointList);
					stationMap.put(toStation.getStation().getCode(), stageStationDTO);
				}
			}
			else {
				stationMap.put(toStation.getStation().getCode(), toStation);
			}
		}
		return new ArrayList<StageStationDTO>(stationMap.values());
	}

	public static String convertStationPoint(StationPointDTO boardingPoint, StationPointDTO droppingPoint) {
		StringBuilder stationPoint = new StringBuilder();
		if (boardingPoint != null) {
			stationPoint.append(boardingPoint.getCode());
			stationPoint.append(STATION_POINT_SEPARATOR);
			stationPoint.append(boardingPoint.getName());
			stationPoint.append(STATION_POINT_SEPARATOR);
		}
		if (droppingPoint != null) {
			stationPoint.append(droppingPoint.getCode());
			stationPoint.append(STATION_POINT_SEPARATOR);
			stationPoint.append(droppingPoint.getName());
		}
		if (StringUtil.isNull(stationPoint.toString())) {
			stationPoint.append(Text.NA);
		}
		return stationPoint.toString();
	}

	public static JSONObject getStationPointJSON(String stationPoint) {
		JSONObject stationPointJSON = new JSONObject();
		if (StringUtil.isNotNull(stationPoint)) {
			String[] stationPoints = stationPoint.split(STATION_POINT_SEPARATOR);
			if (stationPoints.length == 4) {
				stationPointJSON.put("bc", stationPoints[0]);
				stationPointJSON.put("bn", stationPoints[1]);
				stationPointJSON.put("dc", stationPoints[2]);
				stationPointJSON.put("dn", stationPoints[3]);
			}
		}
		return stationPointJSON;
	}

	public static String updateStationPoint(String stationPoint, String updateDetail, StationPointDTO boardingDroppinPoint) {
		StringBuilder stationPointDetails = new StringBuilder();
		String[] stationPoints = stationPoint.split(STATION_POINT_SEPARATOR);
		if (StringUtil.isNotNull(updateDetail) && "BOARDING".equals(updateDetail)) {
			stationPointDetails.append(boardingDroppinPoint.getCode());
			stationPointDetails.append(STATION_POINT_SEPARATOR);
			stationPointDetails.append(boardingDroppinPoint.getName());
			stationPointDetails.append(STATION_POINT_SEPARATOR);
			stationPointDetails.append(stationPoints.length >= 3 ? stationPoints[2] : Text.NA);
			stationPointDetails.append(STATION_POINT_SEPARATOR);
			stationPointDetails.append(stationPoints.length >= 4 ? stationPoints[3] : Text.NA);
		}
		else if (StringUtil.isNotNull(updateDetail) && "DROPPING".equals(updateDetail)) {
			stationPointDetails.append(stationPoints.length > 0 ? stationPoints[0] : Text.NA);
			stationPointDetails.append(STATION_POINT_SEPARATOR);
			stationPointDetails.append(stationPoints.length >= 2 ? stationPoints[1] : Text.NA);
			stationPointDetails.append(STATION_POINT_SEPARATOR);
			stationPointDetails.append(boardingDroppinPoint.getCode());
			stationPointDetails.append(STATION_POINT_SEPARATOR);
			stationPointDetails.append(boardingDroppinPoint.getName());
		}
		return stationPointDetails.toString();
	}

	public static UserDTO isUserExists(List<UserDTO> userList, UserDTO user) {
		UserDTO existingUser = null;
		if (userList != null && user != null) {
			for (UserDTO userDTO : userList) {
				if (userDTO.getId() != Numeric.ZERO_INT && user.getId() != Numeric.ZERO_INT && userDTO.getId() == user.getId()) {
					existingUser = userDTO;
					break;
				}
			}
		}
		return existingUser;
	}

	public static GroupDTO isGroupExists(List<GroupDTO> groupList, GroupDTO groupDTO) {
		GroupDTO existingGroup = null;
		if (groupList != null && groupDTO != null) {
			for (GroupDTO group : groupList) {
				if (group.getId() != Numeric.ZERO_INT && groupDTO.getId() != Numeric.ZERO_INT && group.getId() == groupDTO.getId()) {
					existingGroup = group;
					break;
				}
			}
		}
		return existingGroup;
	}

	public static GroupDTO isGroupExists(List<GroupDTO> groupList, List<GroupDTO> groups) {
		GroupDTO existingGroup = null;
		if (groupList != null && groups != null) {
			for (GroupDTO group : groups) {
				for (GroupDTO groupDTO : groupList) {
					if (group.getId() != Numeric.ZERO_INT && groupDTO.getId() != Numeric.ZERO_INT && groupDTO.getId() == group.getId()) {
						existingGroup = groupDTO;
						break;
					}
				}
				if (existingGroup != null) {
					break;
				}
			}
		}
		return existingGroup;
	}

	public static DeviceMediumEM isDeviceMediumExists(List<DeviceMediumEM> deviceMediumList, DeviceMediumEM medium) {
		DeviceMediumEM deviceMedium = null;
		if (deviceMediumList != null && deviceMediumList != null) {
			for (DeviceMediumEM object : deviceMediumList) {
				if (DeviceMediumEM.ALL_USER.getId() == object.getId() || object.getId() == medium.getId()) {
					deviceMedium = medium;
					break;
				}
			}
		}
		return deviceMedium;
	}

	public static EventNotificationEM isNotificationEventExists(List<EventNotificationEM> notificationEventEMs, EventNotificationEM notificationEventEM) {
		EventNotificationEM notificationEvent = null;
		if (notificationEventEMs != null && notificationEventEMs != null) {
			for (EventNotificationEM object : notificationEventEMs) {
				if (object.getId() == notificationEventEM.getId()) {
					notificationEvent = notificationEventEM;
					break;
				}
			}
		}
		return notificationEvent;
	}

	public static DeviceMediumEM getDeviceMedium(Map<String, String> deviceMap, DeviceMediumEM medium) {
		DeviceMediumEM deviceMedium = null;
		if (deviceMap != null && deviceMap.get(Text.DEVICE_MEDIUM) != null) {
			deviceMedium = DeviceMediumEM.getDeviceMediumEM(deviceMap.get(Text.DEVICE_MEDIUM));
		}
		if (deviceMedium == null || deviceMedium.getCode().equals(DeviceMediumEM.ALL_USER.getCode())) {
			deviceMedium = medium;
		}
		return deviceMedium;
	}

	public static NotificationMediumEM isNotificationMediumExists(List<NotificationMediumEM> notificationMediumList, NotificationMediumEM medium) {
		NotificationMediumEM notificationMedium = null;
		if (notificationMediumList != null && medium != null) {
			for (NotificationMediumEM object : notificationMediumList) {
				if (object.getId() == medium.getId()) {
					notificationMedium = medium;
					break;
				}
			}
		}
		return notificationMedium;
	}

	public static RouteDTO isRouteExists(List<RouteDTO> routeList, StationDTO fromStationDTO, StationDTO toStationDTO) {
		RouteDTO existRouteDTO = null;
		if (routeList != null && fromStationDTO != null && toStationDTO != null) {
			for (RouteDTO routeDTO : routeList) {
				if (routeDTO.getFromStation() != null && routeDTO.getToStation() != null && fromStationDTO.getId() != Numeric.ZERO_INT && toStationDTO.getId() != Numeric.ZERO_INT && routeDTO.getFromStation().getId() != Numeric.ZERO_INT && routeDTO.getToStation().getId() != Numeric.ZERO_INT && routeDTO.getFromStation().getId() == fromStationDTO.getId() && routeDTO.getToStation().getId() == toStationDTO.getId()) {
					existRouteDTO = routeDTO;
					break;
				}
			}
		}
		return existRouteDTO;
	}

	public static int generateOTPNumber(String mobileNumber) {
		int otpNumber = 0;
		if (StringUtil.isNotNull(mobileNumber) && StringUtil.isContains(Constants.DEFAULT_OTP_MOBILE_NUMBER, mobileNumber)) {
			otpNumber = 873201;
		}
		else if (StringUtil.isNull(mobileNumber) || !StringUtil.isContains(Constants.DEFAULT_OTP_MOBILE_NUMBER, mobileNumber)) {
			otpNumber = NumericGenerator.randInt();
		}
		return otpNumber;
	}

	public static void validateDateRange(List<DBQueryParamDTO> paramList, int DaysLimit) {
		DateTime fromDate = null;
		DateTime toDate = null;
		for (DBQueryParamDTO paramDTO : paramList) {
			if (StringUtil.isNull(paramDTO.getValue()) && !Text.NA.equals(paramDTO.getValue())) {
				throw new ServiceException(ErrorCode.REQURIED_FIELD_SHOULD_NOT_NULL);
			}

			if (!paramDTO.getParamName().equals("fromDate") && !paramDTO.getParamName().equals("toDate")) {
				continue;
			}

			if (paramDTO.getParamName().equals("fromDate") && DateUtil.isValidDate(paramDTO.getValue())) {
				fromDate = new DateTime(paramDTO.getValue());
			}
			else if (paramDTO.getParamName().equals("toDate") && DateUtil.isValidDate(paramDTO.getValue())) {
				toDate = new DateTime(paramDTO.getValue());
			}
		}

		if (fromDate != null && toDate != null && DateUtil.getDayDifferent(fromDate, toDate) > DaysLimit) {
			// throw new ServiceException(ErrorCode.INVALID_DATE_RANGE);
		}
	}

	public static String convertParameterToString(List<DBQueryParamDTO> paramList) {
		StringBuilder builder = new StringBuilder();
		for (DBQueryParamDTO param : paramList) {
			builder.append(" ").append(param.getParamName()).append(":").append(param.getValue());
		}
		return builder.toString();
	}

	public static boolean validateBlockReleaseTime(int releaseMinutes, DateTime tripDateTime, DateTime ticketAt) {
		boolean checkPhoneBlockLiveTime = Text.FALSE;
		if (releaseMinutes < Numeric.ZERO_INT && DateUtil.getMinutiesDifferent(ticketAt, DateUtil.NOW()) > Math.abs(releaseMinutes)) {
			checkPhoneBlockLiveTime = Text.TRUE;
		}
		if (releaseMinutes > Numeric.ZERO_INT && DateUtil.getMinutiesDifferent(DateUtil.NOW(), tripDateTime) < releaseMinutes) {
			checkPhoneBlockLiveTime = Text.TRUE;
		}
		return checkPhoneBlockLiveTime;
	}

	public static DateTime getBlockReleaseDateTime(int releaseMinutes, DateTime tripDateTime, DateTime ticketAt) {
		DateTime releaseDateTime = null;
		if (releaseMinutes < Numeric.ZERO_INT) {
			releaseDateTime = DateUtil.addMinituesToDate(ticketAt, Math.abs(releaseMinutes));
		}
		if (releaseMinutes > Numeric.ZERO_INT) {
			releaseDateTime = DateUtil.minusMinituesToDate(tripDateTime, releaseMinutes);
		}
		return releaseDateTime;
	}

	public static BusSeatTypeEM existBusSeatType(List<BusSeatTypeEM> busSeatTypeList, BusSeatTypeEM busSeatTypeEM) {
		BusSeatTypeEM existingBusSeatType = null;
		// Bus Seat Type
		for (BusSeatTypeEM BusSeatType : busSeatTypeList) {
			if (BusSeatType.getId() == BusSeatTypeEM.ALL_BUS_SEAT_TYPE.getId() || BusSeatType.getId() == busSeatTypeEM.getId()) {
				existingBusSeatType = BusSeatType;
				break;
			}
		}

		if (busSeatTypeList.isEmpty()) {
			existingBusSeatType = busSeatTypeEM;
		}
		return existingBusSeatType;
	}

	public static BusSeatTypeEM existBusSeatTypeList(List<BusSeatTypeEM> repoSeatTypeList, List<BusSeatTypeEM> busSeatTypeList) {
		BusSeatTypeEM existingBusSeatType = null;
		// Bus Seat Type
		if (repoSeatTypeList != null && busSeatTypeList != null) {
			for (BusSeatTypeEM busSeatType : busSeatTypeList) {
				for (BusSeatTypeEM repoBusSeatType : repoSeatTypeList) {
					if (busSeatType.getId() == repoBusSeatType.getId()) {
						existingBusSeatType = busSeatType;
						break;
					}
				}
				if (existingBusSeatType != null) {
					break;
				}
			}
		}
		return existingBusSeatType;
	}

	public static void checkTransactionValidityCache(String key) {
		Element element = EhcacheManager.getTransactionValidityEhCache().get(key);
		if (element == null) {
			element = new Element(key, Numeric.ONE_INT);
			EhcacheManager.getTransactionValidityEhCache().put(element);
		}
		else if (element != null) {
			int otpCount = (int) element.getObjectValue();
			if (otpCount < Numeric.THREE_INT) {
				otpCount = otpCount + 1;

				element = new Element(key, otpCount);
				EhcacheManager.getTransactionValidityEhCache().put(element);
			}
			else if (otpCount >= Numeric.THREE_INT) {
				throw new ServiceException(ErrorCode.PARALLEL_SAME_TRANSACTION_OCCUR);
			}
		}
	}

	public static boolean validateAge(String ageRanges, int age) {
		boolean isExist = Text.FALSE;
		List<String> ageRangeList = Arrays.asList(ageRanges.split(Text.COMMA));
		for (String ageRange : ageRangeList) {

			int startAge = StringUtil.getIntegerValue(ageRange.split(Text.HYPHEN)[Numeric.ZERO_INT]);
			int endAge = StringUtil.getIntegerValue(ageRange.split(Text.HYPHEN)[Numeric.ONE_INT]);

			if (age >= startAge && age <= endAge) {
				isExist = Text.TRUE;
				break;
			}
		}
		return isExist;
	}

	public static boolean validateAgeV2(String ageRanges, List<Integer> ages) {
		boolean isExist = Text.FALSE;
		if (StringUtil.isNotNull(ageRanges)) {
			List<String> ageRangeList = Arrays.asList(ageRanges.split(Text.COMMA));
			for (String ageRange : ageRangeList) {
				int startAge = StringUtil.getIntegerValue(ageRange.split(Text.HYPHEN)[Numeric.ZERO_INT]);
				int endAge = StringUtil.getIntegerValue(ageRange.split(Text.HYPHEN)[Numeric.ONE_INT]);
				for (Integer age : ages) {
					if (age >= startAge && age <= endAge) {
						isExist = Text.TRUE;
						break;
					}
				}
			}
		}
		return isExist;
	}

	public static SeatGendarEM isSeatGenderExists(List<SeatGendarEM> seatGenderList, SeatGendarEM seatGendarEM) {
		SeatGendarEM existingGender = null;
		for (SeatGendarEM seatGender : seatGenderList) {
			if (SeatGendarEM.ALL.getId() == seatGender.getId() || seatGender.getId() == seatGendarEM.getId()) {
				existingGender = seatGender;
				break;
			}
		}
		return existingGender;
	}

	public static SeatGendarEM isExistSeatGender(List<SeatGendarEM> repoSeatGenderList, List<SeatGendarEM> seatGenderList) {
		SeatGendarEM existingSeatGender = null;
		if (repoSeatGenderList != null && seatGenderList != null) {
			for (SeatGendarEM busSeatGender : seatGenderList) {
				for (SeatGendarEM repoSeatGender : repoSeatGenderList) {
					if (busSeatGender.getId() == repoSeatGender.getId()) {
						existingSeatGender = repoSeatGender;
						break;
					}
				}
				if (existingSeatGender != null) {
					break;
				}
			}
		}
		return existingSeatGender;
	}

	public static ScheduleDTO isScheduleExists(List<ScheduleDTO> schedules, ScheduleDTO schedule) {
		ScheduleDTO existScheduleDTO = null;
		if (schedules != null && schedule != null) {
			for (ScheduleDTO scheduleDTO : schedules) {
				if (scheduleDTO.getId() != Numeric.ZERO_INT && scheduleDTO.getId() == schedule.getId()) {
					existScheduleDTO = scheduleDTO;
					break;
				}
			}
		}
		return existScheduleDTO;
	}

	public static ScheduleDTO isScheduleExists(List<ScheduleDTO> schedules, String scheduleCode) {
		ScheduleDTO existScheduleDTO = null;
		if (schedules != null && StringUtil.isNotNull(scheduleCode)) {
			for (ScheduleDTO scheduleDTO : schedules) {
				if (StringUtil.isNotNull(scheduleDTO.getCode()) && scheduleDTO.getCode().equals(scheduleCode)) {
					existScheduleDTO = scheduleDTO;
					break;
				}
			}
		}
		return existScheduleDTO;
	}

	public static StationDTO isStationExists(List<StationDTO> repoStations, List<ScheduleStationDTO> scheduleStations) {
		StationDTO existStationDTO = null;
		if (repoStations != null && scheduleStations != null) {
			for (ScheduleStationDTO scheduleStation : scheduleStations) {
				for (StationDTO repoStation : repoStations) {
					if (scheduleStation.getStation().getId() == repoStation.getId()) {
						existStationDTO = scheduleStation.getStation();
						break;
					}
				}
				if (existStationDTO != null) {
					break;
				}
			}
		}
		return existStationDTO;
	}

	public static StationDTO isStationExistsV2(List<StationDTO> repoStations, List<StationDTO> stations) {
		StationDTO existStationDTO = null;
		if (repoStations != null && stations != null) {
			for (StationDTO station : stations) {
				for (StationDTO repoStation : repoStations) {
					if (station.getId() == repoStation.getId()) {
						existStationDTO = station;
						break;
					}
				}
				if (existStationDTO != null) {
					break;
				}
			}
		}
		return existStationDTO;
	}

	public static BusVehicleDTO isVehicleExist(List<BusVehicleDTO> vehicleList, BusVehicleDTO vehicleDTO) {
		BusVehicleDTO existVehicle = null;
		if (vehicleList != null && vehicleDTO != null) {
			for (BusVehicleDTO repoVehicle : vehicleList) {
				if (repoVehicle.getId() == vehicleDTO.getId()) {
					existVehicle = vehicleDTO;
					break;
				}
			}
		}
		return existVehicle;
	}

	public static OrganizationDTO isOrganizationExists(List<OrganizationDTO> organizations, OrganizationDTO organization) {
		OrganizationDTO existOrganization = null;
		if (organizations != null && organization != null) {
			for (OrganizationDTO organizationDTO : organizations) {
				if (organizationDTO.getId() != Numeric.ZERO_INT && organizationDTO.getId() == organization.getId()) {
					existOrganization = organizationDTO;
					break;
				}
			}
		}
		return existOrganization;
	}

	public static StageFareDTO applyFareRule(AuthDTO authDTO, StageFareDTO stageFareDTO, BusDTO bus, FareRuleDetailsDTO fareRuleDetailsDTO) {
		BusSeatTypeEM busSeatType = stageFareDTO.getBusSeatType();

		if (bus.getCategoryCode().contains(BusCategoryTypeEM.CLIMATE_CONTROL_AC.getCode())) {
			if (busSeatType.getId() == BusSeatTypeEM.SEMI_SLEEPER.getId() || busSeatType.getId() == BusSeatTypeEM.SEATER.getId() || busSeatType.getId() == BusSeatTypeEM.PUSH_BACK.getId() || busSeatType.getId() == BusSeatTypeEM.SINGLE_SEMI_SLEEPER.getId() || busSeatType.getId() == BusSeatTypeEM.SINGLE_SEATER.getId()) {
				if (bus.getCategoryCode().contains(BusCategoryTypeEM.CHASIS_MULTIAXLE.getCode()) || bus.getCategoryCode().contains(BusCategoryTypeEM.CHASIS_SCANIA_MULTI_AXLE.getCode()) || bus.getCategoryCode().contains(BusCategoryTypeEM.CHASIS_B11R_MULTI_AXLE.getCode()) || bus.getCategoryCode().contains(BusCategoryTypeEM.CHASIS_B9R_MULTI_AXLE.getCode()) || bus.getCategoryCode().contains(BusCategoryTypeEM.CHASIS_B11R_MULTI_AXLE_AUTO_TRANSMISSION.getCode()) || bus.getCategoryCode().contains(BusCategoryTypeEM.CHASIS_B11R_MULTI_AXLE_ISHIFT.getCode())) {
					stageFareDTO.setMinFare(fareRuleDetailsDTO.getMultiAxleSeaterMinFare());
					stageFareDTO.setMaxFare(fareRuleDetailsDTO.getMultiAxleSeaterMaxFare());
				}
				else {
					stageFareDTO.setMinFare(fareRuleDetailsDTO.getAcSeaterMinFare());
					stageFareDTO.setMaxFare(fareRuleDetailsDTO.getAcSeaterMaxFare());
				}
			}
			else if ((busSeatType.getId() == BusSeatTypeEM.SLEEPER.getId() || busSeatType.getId() == BusSeatTypeEM.UPPER_SLEEPER.getId() || busSeatType.getId() == BusSeatTypeEM.SINGLE_UPPER_SLEEPER.getId() || busSeatType.getId() == BusSeatTypeEM.LOWER_SLEEPER.getId() || busSeatType.getId() == BusSeatTypeEM.SINGLE_LOWER_SLEEPER.getId()) && (bus.getCategoryCode().contains(BusCategoryTypeEM.CHASIS_MULTIAXLE.getCode()) || bus.getCategoryCode().contains(BusCategoryTypeEM.CHASIS_SCANIA_MULTI_AXLE.getCode()) || bus.getCategoryCode().contains(BusCategoryTypeEM.CHASIS_B11R_MULTI_AXLE.getCode()) || bus.getCategoryCode().contains(BusCategoryTypeEM.CHASIS_B9R_MULTI_AXLE.getCode()) || bus.getCategoryCode().contains(BusCategoryTypeEM.CHASIS_B11R_MULTI_AXLE_AUTO_TRANSMISSION.getCode()) || bus.getCategoryCode().contains(BusCategoryTypeEM.CHASIS_B11R_MULTI_AXLE_ISHIFT.getCode()))) {
				stageFareDTO.setMinFare(fareRuleDetailsDTO.getMultiAxleAcSleeperMinFare());
				stageFareDTO.setMaxFare(fareRuleDetailsDTO.getMultiAxleAcSleeperMaxFare());
			}
			else if (busSeatType.getId() == BusSeatTypeEM.UPPER_SLEEPER.getId() || busSeatType.getId() == BusSeatTypeEM.SINGLE_UPPER_SLEEPER.getId()) {
				stageFareDTO.setMinFare(fareRuleDetailsDTO.getAcSleeperUpperMinFare());
				stageFareDTO.setMaxFare(fareRuleDetailsDTO.getAcSleeperUpperMaxFare());
			}
			else if (busSeatType.getId() == BusSeatTypeEM.SLEEPER.getId() || busSeatType.getId() == BusSeatTypeEM.LOWER_SLEEPER.getId() || busSeatType.getId() == BusSeatTypeEM.SINGLE_LOWER_SLEEPER.getId()) {
				stageFareDTO.setMinFare(fareRuleDetailsDTO.getAcSleeperLowerMinFare());
				stageFareDTO.setMaxFare(fareRuleDetailsDTO.getAcSleeperLowerMaxFare());
			}
		}
		else if (bus.getCategoryCode().contains(BusCategoryTypeEM.CLIMATE_CONTROL_NON_AC.getCode())) {
			if (busSeatType.getId() == BusSeatTypeEM.SEMI_SLEEPER.getId() || busSeatType.getId() == BusSeatTypeEM.SEATER.getId() || busSeatType.getId() == BusSeatTypeEM.PUSH_BACK.getId() || busSeatType.getId() == BusSeatTypeEM.SINGLE_SEMI_SLEEPER.getId() || busSeatType.getId() == BusSeatTypeEM.SINGLE_SEATER.getId()) {
				stageFareDTO.setMinFare(fareRuleDetailsDTO.getNonAcSeaterMinFare());
				stageFareDTO.setMaxFare(fareRuleDetailsDTO.getNonAcSeaterMaxFare());
			}
			else if (busSeatType.getId() == BusSeatTypeEM.UPPER_SLEEPER.getId() || busSeatType.getId() == BusSeatTypeEM.SINGLE_UPPER_SLEEPER.getId()) {
				stageFareDTO.setMinFare(fareRuleDetailsDTO.getNonAcSleeperUpperMinFare());
				stageFareDTO.setMaxFare(fareRuleDetailsDTO.getNonAcSleeperUpperMaxFare());
			}
			else if (busSeatType.getId() == BusSeatTypeEM.LOWER_SLEEPER.getId() || busSeatType.getId() == BusSeatTypeEM.SLEEPER.getId() || busSeatType.getId() == BusSeatTypeEM.SINGLE_LOWER_SLEEPER.getId()) {
				stageFareDTO.setMinFare(fareRuleDetailsDTO.getNonAcSleeperLowerMinFare());
				stageFareDTO.setMaxFare(fareRuleDetailsDTO.getNonAcSleeperLowerMaxFare());
			}
		}
		return stageFareDTO;
	}

	public static String getBusCategoryUsingEM(String categoryCode) {
		StringBuilder busType = new StringBuilder();
		if (StringUtil.isNotNull(categoryCode)) {
			for (String code : categoryCode.split("\\|")) {
				BusCategoryTypeEM category = BusCategoryTypeEM.getBusCategoryType(code);
				if (category != null && StringUtil.isNotNull(category.getName())) {
					busType.append(Text.SINGLE_SPACE + category.getName());
				}
			}
		}
		return busType.toString().trim();
	}

	public static CommissionDTO getCommission(List<CommissionDTO> commissionList, CommissionTypeEM commissionType) {
		CommissionDTO commissionDTO = null;
		for (CommissionDTO commission : commissionList) {
			if (commissionType.getId() == commission.getCommissionType().getId()) {
				commissionDTO = commission;
				break;
			}
		}
		return commissionDTO;
	}

	public static NamespaceTaxDTO getNamespaceTax(List<NamespaceTaxDTO> taxList, NamespaceTaxDTO tax) {
		NamespaceTaxDTO namespaceTax = null;
		for (NamespaceTaxDTO taxItem : taxList) {
			if (namespaceTax == null || tax.getId() == taxItem.getId()) {
				namespaceTax = taxItem;
			}
		}
		if (namespaceTax == null) {
			namespaceTax = tax;
		}
		return namespaceTax;
	}

	public static boolean isTagExists(List<UserTagEM> tagList, UserTagEM tag) {
		for (UserTagEM tags : tagList) {
			if (tags.getId() == tag.getId()) {
				return true;
			}
		}
		return false;
	}

	public static boolean isTagExists(List<UserTagEM> tagMasterList, UserTagEM[] checktags) {
		for (UserTagEM mtags : tagMasterList) {
			for (UserTagEM ctags : checktags) {
				if (mtags.getId() == ctags.getId()) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isRouteExists(List<RouteDTO> sourceRouteList, List<RouteDTO> findRouteList) {
		for (RouteDTO route1 : sourceRouteList) {
			for (RouteDTO route2 : findRouteList) {
				if (route1.getFromStation().getId() == route2.getFromStation().getId() && route1.getToStation().getId() == route2.getToStation().getId()) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isRouteExists(RouteDTO routeDTO, List<RouteDTO> findRouteList) {
		for (RouteDTO route2 : findRouteList) {
			if (routeDTO.getFromStation().getId() == route2.getFromStation().getId() && routeDTO.getToStation().getId() == route2.getToStation().getId()) {
				return true;
			}
		}
		return false;
	}

	public static List<RouteDTO> filterRouteExists(List<RouteDTO> sourceRouteList, List<RouteDTO> filterRouteList) {
		List<RouteDTO> finalRouteList = new ArrayList<>();
		for (RouteDTO route1 : sourceRouteList) {
			if (!isRouteExists(route1, filterRouteList)) {
				finalRouteList.add(route1);
			}
		}
		return finalRouteList;
	}

	public static String getTripStatusBasedOnStageTime(TripDTO tripDTO) {
		String tripStatus = tripDTO.getTripStatus().getName();
		DateTime originDateTime = getOriginScheduleStationTime(tripDTO.getStationList(), tripDTO.getTripDate());
		DateTime destinationDateTime = getDestinationScheduleStationTime(tripDTO.getStationList(), tripDTO.getTripDate());
		DateTime now = DateUtil.NOW();
		if (originDateTime.gteq(now)) {
			tripStatus = "Booking Open";
		}
		else if (now.gt(originDateTime) && destinationDateTime.gt(now)) {
			tripStatus = "Departed";
		}
		else if (now.gt(destinationDateTime)) {
			tripStatus = "Booking Closed";
		}
		return tripStatus;
	}

	public static String getGeneratedTripStageCode(AuthDTO authDTO, ScheduleDTO scheduleDTO, TripDTO tripDTO, ScheduleStageDTO scheduleStageDTO) {
		return authDTO.getNamespace().getId() + "N" + scheduleDTO.getId() + "S" + DateUtil.getCompressDate(tripDTO.getTripDate()) + "D" + scheduleStageDTO.getFromStation().getId() + "T" + scheduleStageDTO.getToStation().getId();
	}

	public static String getGeneratedTripStageCode(AuthDTO authDTO, ScheduleDTO scheduleDTO, TripDTO tripDTO, StationDTO fromStationDTO, StationDTO toStationDTO) {
		return authDTO.getNamespace().getId() + "N" + scheduleDTO.getId() + "S" + DateUtil.getCompressDate(tripDTO.getTripDate()) + "D" + fromStationDTO.getId() + "T" + toStationDTO.getId();
	}

	public static String getGeneratedTripCode(AuthDTO authDTO, ScheduleDTO scheduleDTO, TripDTO tripDTO) {
		return authDTO.getNamespace().getId() + "N" + scheduleDTO.getId() + "S" + DateUtil.getCompressDate(tripDTO.getTripDate()) + "D";
	}

	public static String getGeneratedTripCodeV2(AuthDTO authDTO, ScheduleDTO scheduleDTO, DateTime tripDate) {
		return authDTO.getNamespace().getId() + "N" + scheduleDTO.getId() + "S" + DateUtil.getCompressDate(tripDate) + "D";
	}

	public static String getGeneratedTripStageCode(AuthDTO authDTO, ScheduleDTO scheduleDTO, SearchDTO searchDTO, StageDTO stageDTO) {
		return authDTO.getNamespace().getId() + "N" + scheduleDTO.getId() + "S" + DateUtil.getCompressDate(searchDTO.getTravelDate()) + "D" + stageDTO.getFromStation().getStation().getId() + "T" + stageDTO.getToStation().getStation().getId();
	}

	public static String getGeneratedTripCode(AuthDTO authDTO, ScheduleDTO scheduleDTO, SearchDTO searchDTO, StageDTO stageDTO) {
		return authDTO.getNamespace().getId() + "N" + scheduleDTO.getId() + "S" + DateUtil.getCompressDate(searchDTO.getTravelDate()) + "D";
	}

	public static boolean isSimilarityMobileNumber(String maskedMobileNumber, String mobileNumber) {
		boolean isSimilarNumber = false;
		try {
			if (StringUtil.isValidMobileNumber(mobileNumber) && StringUtil.similarity(maskedMobileNumber, mobileNumber) >= .7) {
				isSimilarNumber = true;
			}
		}
		catch (Exception e) {
			isSimilarNumber = false;
			e.printStackTrace();
		}
		return isSimilarNumber;
	}

	public static boolean isPermissionEnabled(List<MenuEventDTO> menuEvents, MenuEventEM menuEventEM) {
		boolean isPermissionEnabled = Text.FALSE;
		for (MenuEventDTO eventDTO : menuEvents) {
			if (menuEventEM.getOperationCode().equals(eventDTO.getOperationCode())) {
				isPermissionEnabled = Text.TRUE;
				break;
			}
		}
		return isPermissionEnabled;
	}

	// instant cancellation applied selected groups only
	public static DateTime getInstantCancellationTill(AuthDTO authDTO, DateTime dateTime) {
		DateTime instantCancellationTill = null;
		GroupDTO instantCancellationGroup = BitsUtil.isGroupExists(authDTO.getNamespace().getProfile().getInstantCancellationGroup(), authDTO.getUser().getGroup());
		if (instantCancellationGroup != null && authDTO.getNamespace().getProfile().getInstantCancellationMinitues() != 0) {
			instantCancellationTill = DateUtil.addMinituesToDate(dateTime, authDTO.getNamespace().getProfile().getInstantCancellationMinitues());
			if (DateUtil.NOW().compareTo(instantCancellationTill) > 0) {
				instantCancellationTill = null;
			}
		}
		return instantCancellationTill;
	}

	// instant cancellation applied selected groups only
	public static String getInstantCancellationMinutes(AuthDTO authDTO) {
		String instantCancellationMinutes = null;
		GroupDTO instantCancellationGroup = BitsUtil.isGroupExists(authDTO.getNamespace().getProfile().getInstantCancellationGroup(), authDTO.getUser().getGroup());
		if (instantCancellationGroup != null && authDTO.getNamespace().getProfile().getInstantCancellationMinitues() != 0) {
			instantCancellationMinutes = String.valueOf(authDTO.getNamespace().getProfile().getInstantCancellationMinitues());
		}
		return instantCancellationMinutes;
	}

	public static boolean isCalculateCancellationChargeTax(AuthDTO authDTO, UserDTO user) {
		boolean allow = true;
		UserDTO cancellationChargeTaxExceptionUser = BitsUtil.isUserExists(authDTO.getNamespace().getProfile().getCancellationChargeTaxException(), user);
		if (!authDTO.getNamespace().getProfile().getCancellationChargeTaxException().isEmpty() && cancellationChargeTaxExceptionUser != null) {
			allow = false;
		}
		return allow;
	}

	// instant cancellation applied selected groups only
	public static boolean isAllowInstantCancellation(AuthDTO authDTO) {
		boolean allow = false;
		GroupDTO instantCancellationGroup = BitsUtil.isGroupExists(authDTO.getNamespace().getProfile().getInstantCancellationGroup(), authDTO.getUser().getGroup());
		if (instantCancellationGroup != null) {
			allow = true;
		}
		return allow;
	}

	public static int getTicketTransferMinutes(AuthDTO authDTO, ScheduleTicketTransferTermsDTO scheduleTicketTransferTerms, DateTime bookedStageDateTime, DateTime stageDateTime) {
		DateTime tripDateTime = getTicketTransferDateTime(authDTO, scheduleTicketTransferTerms, bookedStageDateTime, stageDateTime);

		int minutes = 0;
		if (scheduleTicketTransferTerms.getMinutesType().getId() == MinutesTypeEM.MINUTES.getId()) {
			minutes = scheduleTicketTransferTerms.getMinutes();
		}
		else if (scheduleTicketTransferTerms.getMinutesType().getId() == MinutesTypeEM.AM.getId()) {
			DateTime checkTime = DateUtil.addMinituesToDate(tripDateTime.getStartOfDay(), scheduleTicketTransferTerms.getMinutes());
			minutes = DateUtil.getMinutesFromDateTime(checkTime);
		}
		else if (scheduleTicketTransferTerms.getMinutesType().getId() == MinutesTypeEM.PM.getId()) {
			DateTime checkTime = DateUtil.addMinituesToDate(tripDateTime.getStartOfDay(), 720 + scheduleTicketTransferTerms.getMinutes());
			minutes = DateUtil.getMinutesFromDateTime(checkTime);
		}
		return minutes;
	}

	public static DateTime getTicketTransferTermsDateTime(AuthDTO authDTO, ScheduleTicketTransferTermsDTO scheduleTicketTransferTerms, DateTime bookedStageDateTime, DateTime stageDateTime) {
		DateTime tripDateTime = getTicketTransferDateTime(authDTO, scheduleTicketTransferTerms, bookedStageDateTime, stageDateTime);

		DateTime dateTime = null;
		if (scheduleTicketTransferTerms.getMinutesType().getId() == MinutesTypeEM.MINUTES.getId()) {
			dateTime = DateUtil.minusMinituesToDate(tripDateTime, scheduleTicketTransferTerms.getMinutes());
		}
		else if (scheduleTicketTransferTerms.getMinutesType().getId() == MinutesTypeEM.AM.getId()) {
			dateTime = DateUtil.addMinituesToDate(tripDateTime.getStartOfDay(), scheduleTicketTransferTerms.getMinutes());
		}
		else if (scheduleTicketTransferTerms.getMinutesType().getId() == MinutesTypeEM.PM.getId()) {
			dateTime = DateUtil.addMinituesToDate(tripDateTime.getStartOfDay(), 720 + scheduleTicketTransferTerms.getMinutes());
		}
		return dateTime;
	}

	public static DateTime getTicketTransferDateTime(AuthDTO authDTO, ScheduleTicketTransferTermsDTO scheduleTicketTransferTerms, DateTime bookedStageDateTime, DateTime stageDateTime) {
		DateTime tripDateTime = null;
		if (authDTO.getNamespace().getProfile().getCancellationTimeType().equals(Constants.STAGE)) {
			tripDateTime = bookedStageDateTime;
		}
		else {
			tripDateTime = stageDateTime;
		}
		return tripDateTime;
	}

	public static DynamicPriceProviderEM getDynamicPriceProvider(List<DynamicPriceProviderEM> dynamicPriceProviders, DynamicPriceProviderEM dynamicPriceProviderEM) {
		DynamicPriceProviderEM dynamicPriceProvider = null;
		for (DynamicPriceProviderEM priceProviderEM : dynamicPriceProviders) {
			if (priceProviderEM.getId() == dynamicPriceProviderEM.getId()) {
				dynamicPriceProvider = priceProviderEM;
				break;
			}
		}
		return dynamicPriceProvider;
	}

	public static BigDecimal getDiscountFare(ScheduleDiscountDTO scheduleDiscount, BigDecimal fare) {
		BigDecimal discountFare = BigDecimal.ZERO;
		if (scheduleDiscount.getPercentageFlag() == 0) {
			discountFare = scheduleDiscount.getDiscountValue();
		}
		else if (scheduleDiscount.getPercentageFlag() == 1) {
			discountFare = fare.divide(Numeric.ONE_HUNDRED, 2, RoundingMode.CEILING).multiply(scheduleDiscount.getDiscountValue());
		}
		return discountFare.setScale(0, RoundingMode.HALF_UP);
	}

	public static boolean getTagList(TermDTO termDTO, TermDTO term) {
		boolean isExist = false;
		if (termDTO.getTagList() != null && term.getTagList() != null) {
			for (String tag : termDTO.getTagList()) {
				for (String tag1 : term.getTagList()) {
					if (tag.equals(tag1)) {
						isExist = true;
						break;
					}
				}
			}
		}
		return isExist;
	}

	public static String getTagValue(String tagValue, TermDTO termDTO) {
		String tags = null;
		for (String tag : termDTO.getTagList()) {
			if (tagValue.equals(tag)) {
				tags = tagValue;
				break;
			}
		}
		return tags;
	}

	public static ScheduleDTO isScheduleExistsV2(List<ScheduleDTO> schedule, ScheduleDTO scheduleDTO) {
		ScheduleDTO existScheduleDTO = null;
		if (schedule != null && scheduleDTO != null) {
			for (ScheduleDTO scheduleDTO1 : schedule) {
				if (scheduleDTO1.getCode().equals(scheduleDTO.getCode())) {
					existScheduleDTO = scheduleDTO1;
					break;
				}
			}
		}
		return existScheduleDTO;
	}
}
