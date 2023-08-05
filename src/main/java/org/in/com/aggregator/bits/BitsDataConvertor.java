package org.in.com.aggregator.bits;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.com.config.ApplicationConfig;
import org.in.com.constants.Numeric;
import org.in.com.dto.AmenitiesDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.CalendarAnnouncementDTO;
import org.in.com.dto.FareRuleDetailsDTO;
import org.in.com.dto.MenuDTO;
import org.in.com.dto.MenuEventDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.ReportQueryDTO;
import org.in.com.dto.StateDTO;
import org.in.com.dto.StationAreaDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationOtaPartnerDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.CalendarAnnouncementCategoryEM;
import org.in.com.dto.enumeration.NamespaceZoneEM;
import org.in.com.dto.enumeration.OTAPartnerEM;
import org.in.com.dto.enumeration.ProductTypeEM;
import org.in.com.dto.enumeration.SeverityPermissionTypeEM;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;

import hirondelle.date4j.DateTime;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class BitsDataConvertor {

	public List<AmenitiesDTO> getZoneSyncAmenties(JSONObject stationObject) {
		List<AmenitiesDTO> list = new ArrayList<AmenitiesDTO>();
		try {
			if (Numeric.ONE_INT == stationObject.getInt("status")) {
				JSONArray jsonArray = stationObject.getJSONArray("data");
				for (Object amenities : jsonArray) {
					AmenitiesDTO amenitiesDTO = new AmenitiesDTO();
					JSONObject jsonObject = (JSONObject) amenities;
					amenitiesDTO.setName(jsonObject.getString("name"));
					amenitiesDTO.setCode(jsonObject.getString("code"));
					amenitiesDTO.setActiveFlag(jsonObject.getInt("activeFlag"));
					list.add(amenitiesDTO);
				}
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return list;
	}

	public List<StationDTO> getZoneSyncStation(JSONObject stationObject) {
		List<StationDTO> list = new ArrayList<StationDTO>();
		try {
			if (Numeric.ONE_INT == stationObject.getInt("status")) {
				JSONArray jsonArray = stationObject.getJSONArray("data");
				for (Object stations : jsonArray) {
					StationDTO stationDTO = new StationDTO();
					JSONObject jsonObject = (JSONObject) stations;
					stationDTO.setName(jsonObject.getString("name"));
					stationDTO.setCode(jsonObject.getString("code"));
					stationDTO.setApiFlag(jsonObject.getInt("apiFlag"));
					stationDTO.setLatitude(jsonObject.getString("latitude"));
					stationDTO.setLongitude(jsonObject.getString("longitude"));
					stationDTO.setRadius(jsonObject.getInt("radius"));
					stationDTO.setActiveFlag(jsonObject.getInt("activeFlag"));

					JSONObject stateObject = jsonObject.getJSONObject("state");
					StateDTO stateDTO = new StateDTO();
					stateDTO.setName(stateObject.getString("name"));
					stateDTO.setCode(stateObject.getString("code"));
					stateDTO.setActiveFlag(stateObject.getInt("activeFlag"));
					stationDTO.setState(stateDTO);
					list.add(stationDTO);
				}
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return list;
	}

	public List<StationAreaDTO> getZoneSyncStationArea(JSONObject stationAreaObject) {
		List<StationAreaDTO> list = new ArrayList<StationAreaDTO>();
		try {
			if (Numeric.ONE_INT == stationAreaObject.getInt("status")) {
				JSONArray jsonArray = stationAreaObject.getJSONArray("data");
				for (Object stationArea : jsonArray) {
					StationAreaDTO stationAreaDTO = new StationAreaDTO();
					JSONObject jsonObject = (JSONObject) stationArea;
					stationAreaDTO.setName(jsonObject.getString("name"));
					stationAreaDTO.setCode(jsonObject.getString("code"));
					stationAreaDTO.setLatitude(jsonObject.getString("latitude"));
					stationAreaDTO.setLongitude(jsonObject.getString("longitude"));
					stationAreaDTO.setRadius(jsonObject.getInt("radius"));
					stationAreaDTO.setActiveFlag(jsonObject.getInt("activeFlag"));

					JSONObject stationObject = jsonObject.getJSONObject("station");
					StationDTO stationDTO = new StationDTO();
					stationDTO.setName(stationObject.getString("name"));
					stationDTO.setCode(stationObject.getString("code"));
					stationDTO.setActiveFlag(stationObject.getInt("activeFlag"));
					stationAreaDTO.setStation(stationDTO);
					list.add(stationAreaDTO);
				}
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return list;
	}

	public List<ReportQueryDTO> getZoneSyncReportQuery(JSONObject reportObject) {
		List<ReportQueryDTO> list = new ArrayList<ReportQueryDTO>();
		try {
			if (Numeric.ONE_INT == reportObject.getInt("status")) {
				JSONArray jsonArray = reportObject.getJSONArray("data");
				for (Object report : jsonArray) {
					ReportQueryDTO reportQueryDTO = new ReportQueryDTO();
					JSONObject jsonObject = (JSONObject) report;
					reportQueryDTO.setName(jsonObject.getString("name"));
					reportQueryDTO.setCode(jsonObject.getString("code"));
					reportQueryDTO.setDescription(jsonObject.getString("description"));
					reportQueryDTO.setQuery(jsonObject.getString("query"));
					reportQueryDTO.setDaysLimit(jsonObject.getInt("daysLimit"));
					reportQueryDTO.setActiveFlag(jsonObject.getInt("activeFlag"));
					list.add(reportQueryDTO);

				}
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return list;
	}

	public List<MenuDTO> getZoneSyncMenu(JSONObject tripObject) {
		List<MenuDTO> list = new ArrayList<MenuDTO>();
		try {
			if (Numeric.ONE_INT == tripObject.getInt("status")) {
				JSONArray jsonArray = tripObject.getJSONArray("data");
				for (Object trips : jsonArray) {
					MenuDTO menuDTO = new MenuDTO();
					JSONObject menuObject = (JSONObject) trips;
					menuDTO.setCode(menuObject.getString("code"));
					menuDTO.setName(menuObject.getString("name"));
					menuDTO.setActionCode(menuObject.has("actionCode") ? menuObject.getString("actionCode") : "");
					List<String> tagList = new ArrayList<String>();
					if (menuObject.has("tagList")) {
						JSONArray tagArray = menuObject.getJSONArray("tagList");
						for (Object tagobj : tagArray) {
							tagList.add(tagobj.toString());
						}
					}
					menuDTO.setTagList(tagList);
					menuDTO.setActiveFlag(menuObject.getInt("activeFlag"));
					menuDTO.setSeverity(menuObject.has("severity") ? SeverityPermissionTypeEM.getSeverityPermissionTypeEM(menuObject.getJSONObject("severity").getString("code")) : SeverityPermissionTypeEM.NOT_AVAILABLE);
					menuDTO.setEnabledFlag(menuObject.getInt("enabledFlag"));
					menuDTO.setDisplayFlag(menuObject.getInt("displayFlag"));
					menuDTO.setLink(menuObject.getString("link"));
					menuDTO.setProductType(ProductTypeEM.getProductTypeEM(menuObject.getJSONObject("productType").getString("code")));
					if (menuObject.has("lookup")) {
						JSONObject lookupObject = menuObject.getJSONObject("lookup");
						if (lookupObject.has("code")) {
							MenuDTO lookupDTO = new MenuDTO();
							lookupDTO.setCode(lookupObject.getString("code"));
							menuDTO.setLookup(lookupDTO);
						}
					}
					if (menuObject.has("eventList")) {
						JSONArray eventArray = menuObject.getJSONArray("eventList");
						List<MenuEventDTO> eventList = new ArrayList<MenuEventDTO>();
						for (Object event : eventArray) {
							JSONObject eventObject = (JSONObject) event;
							MenuEventDTO menuEventDTO = new MenuEventDTO();
							menuEventDTO.setCode(eventObject.getString("code"));
							menuEventDTO.setName(eventObject.getString("name"));
							menuEventDTO.setAttr1Value(eventObject.has("attr1Value") ? eventObject.getString("attr1Value") : null);
							menuEventDTO.setPermissionFlag(MenuEventDTO.getPermission(eventObject.getString("permissionType")));
							menuEventDTO.setSeverity(eventObject.has("severity") ? SeverityPermissionTypeEM.getSeverityPermissionTypeEM(eventObject.getJSONObject("severity").getString("code")) : SeverityPermissionTypeEM.NOT_AVAILABLE);
							menuEventDTO.setActiveFlag(eventObject.getInt("activeFlag"));
							menuEventDTO.setOperationCode(eventObject.has("operationCode") ? eventObject.getString("operationCode") : null);
							menuEventDTO.setEnabledFlag(eventObject.getInt("enabledFlag"));
							eventList.add(menuEventDTO);
						}
						MenuEventDTO eventDTO = new MenuEventDTO();
						eventDTO.setList(eventList);
						menuDTO.setMenuEvent(eventDTO);
					}

					list.add(menuDTO);
				}
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return list;
	}

	public JSONObject getBitsConfigure(JSONObject jsonData) {
		if (jsonData != null && Numeric.ONE_INT == jsonData.getInt("status")) {
			return jsonData.getJSONObject("data");
		}
		return null;
	}

	public JSONObject getNamespaceConfigure(AuthDTO authDTO, JSONObject jsonData) {
		JSONObject jsonObject = new JSONObject();
		try {
			if (Numeric.ONE_INT == jsonData.getInt("status") && jsonData.has("data")) {
				JSONArray jsonArray = jsonData.getJSONArray("data");
				for (Object jsonObj : jsonArray) {
					JSONObject jsondata = (JSONObject) jsonObj;
					if (authDTO.getNamespaceCode().equalsIgnoreCase(jsondata.getString("code"))) {
						jsonObject = jsondata;
						break;
					}
				}
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return jsonObject;
	}

	public List<NamespaceDTO> getAllNamespace(JSONObject jsonData) {
		List<NamespaceDTO> list = new ArrayList<NamespaceDTO>();
		try {
			if (Numeric.ONE_INT == jsonData.getInt("status") && jsonData.has("data")) {
				JSONArray jsonArray = jsonData.getJSONArray("data");
				for (Object jsonObj : jsonArray) {
					JSONObject jsonObject = (JSONObject) jsonObj;
					NamespaceDTO namespaceDTO = new NamespaceDTO();
					namespaceDTO.setCode(jsonObject.getString("code"));
					namespaceDTO.setName(jsonObject.getString("name"));
					list.add(namespaceDTO);
				}
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return list;
	}

	public List<NamespaceDTO> getBitsGatewayNamespace(JSONObject jsonData) {
		List<NamespaceDTO> list = new ArrayList<NamespaceDTO>();
		try {
			if (Numeric.ONE_INT == jsonData.getInt("status") && jsonData.has("data")) {
				JSONArray jsonArray = jsonData.getJSONArray("data");
				for (Object jsonObj : jsonArray) {
					JSONObject jsonObject = (JSONObject) jsonObj;

					if (jsonObject.getInt("usingEzeeGateway") != Numeric.ONE_INT || !ApplicationConfig.getServerZoneCode().equals(jsonObject.getString("zone")) || NamespaceZoneEM.BITS.getCode().equals(jsonObject.getString("code")) || NamespaceZoneEM.BITS_REGION_2.getCode().equals(jsonObject.getString("code")) || NamespaceZoneEM.PARVEEN_BITS.getCode().equals(jsonObject.getString("code")) || NamespaceZoneEM.SBLT_BITS.getCode().equals(jsonObject.getString("code")) || NamespaceZoneEM.SVRT_BITS.getCode().equals(jsonObject.getString("code")) || NamespaceZoneEM.TAT_BITS.getCode().equals(jsonObject.getString("code")) || NamespaceZoneEM.TRANZKING_BITS.getCode().equals(jsonObject.getString("code")) || NamespaceZoneEM.YBM_BITS.getCode().equals(jsonObject.getString("code"))) {
						continue;
					}

					NamespaceDTO namespaceDTO = new NamespaceDTO();
					namespaceDTO.setCode(jsonObject.getString("code"));
					namespaceDTO.setName(jsonObject.getString("name"));
					list.add(namespaceDTO);
				}
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return list;
	}

	public JSONObject getNotificationConfigure(JSONObject jsonData) {
		JSONObject jsonObject = null;
		try {
			if (Numeric.ONE_INT == jsonData.getInt("status") && jsonData.has("data")) {
				jsonObject = jsonData.getJSONObject("data");
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return jsonObject;
	}

	public List<UserDTO> getUsers(JSONObject jsonObject) {
		List<UserDTO> list = new ArrayList<UserDTO>();
		try {
			if (Numeric.ONE_INT == jsonObject.getInt("status")) {
				JSONArray jsonArray = jsonObject.getJSONArray("data");
				for (Object user : jsonArray) {
					UserDTO userDTO = new UserDTO();
					JSONObject userObject = (JSONObject) user;
					userDTO.setUsername(userObject.has("username") ? userObject.getString("username") : "");
					userDTO.setEmail(userObject.has("email") ? userObject.getString("email") : "");
					userDTO.setMobile(userObject.has("mobile") ? userObject.getString("mobile") : "");
					userDTO.setLastname(userObject.has("lastname") ? userObject.getString("lastname") : "");
					userDTO.setCode(userObject.has("code") ? userObject.getString("code") : "");
					userDTO.setName(userObject.has("name") ? userObject.getString("name") : "");
					userDTO.setActiveFlag(userObject.has("activeFlag") ? userObject.getInt("activeFlag") : 0);
					list.add(userDTO);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public UserDTO getAuthToken(JSONObject json) {
		UserDTO bitsAuthDTO = new UserDTO();
		try {
			if (Numeric.ONE_INT == json.getInt("status")) {
				if (json.has("data")) {
					JSONObject jsonObject = json.getJSONObject("data");
					bitsAuthDTO.setToken(jsonObject.getString("authToken"));
				}
				else if (json.has("token")) {
					bitsAuthDTO.setToken(json.getString("token"));
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return bitsAuthDTO;
	}

	public Map<String, String> getVerifyAuthToken(JSONObject json) {
		Map<String, String> auth = new HashMap<>();
		try {
			if (Numeric.ONE_INT == json.getInt("status")) {
				JSONObject jsonObject = json.getJSONObject("data");
				auth.put("USERNAME", jsonObject.getString("username"));
				auth.put("NAMESPACE_CODE", jsonObject.getString("namespaceCode"));
				auth.put("DEVICE_MEDIUM_CODE", jsonObject.getString("deviceMediumCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return auth;
	}

	public List<FareRuleDetailsDTO> syncFareRule(JSONObject jsonObject) {
		List<FareRuleDetailsDTO> fareRuleDetails = new ArrayList<>();
		try {
			if (Numeric.ONE_INT == jsonObject.getInt("status")) {
				JSONArray fareRuleDetailsArray = jsonObject.getJSONArray("data");

				for (Object object : fareRuleDetailsArray) {
					JSONObject fareRuleDetailsJson = (JSONObject) object;

					FareRuleDetailsDTO fareRuleDetailsDTO = new FareRuleDetailsDTO();

					StationDTO fromStation = new StationDTO();
					fromStation.setCode(fareRuleDetailsJson.getJSONObject("fromStation").getString("code"));
					fareRuleDetailsDTO.setFromStation(fromStation);

					StationDTO toStation = new StationDTO();
					toStation.setCode(fareRuleDetailsJson.getJSONObject("toStation").getString("code"));
					fareRuleDetailsDTO.setToStation(toStation);

					JSONObject fareJson = fareRuleDetailsJson.getJSONObject("fare");
					if (fareJson == null) {
						continue;
					}
					if (fareJson.has("nonacSeater") && fareJson.getJSONObject("nonacSeater") != null) {
						fareRuleDetailsDTO.setNonAcSeaterMinFare(BigDecimal.valueOf(fareJson.getJSONObject("nonacSeater").getDouble("min")));
						fareRuleDetailsDTO.setNonAcSeaterMaxFare(BigDecimal.valueOf(fareJson.getJSONObject("nonacSeater").getDouble("max")));
					}
					if (fareJson.has("acSeater") && fareJson.getJSONObject("acSeater") != null) {
						fareRuleDetailsDTO.setAcSeaterMinFare(BigDecimal.valueOf(fareJson.getJSONObject("acSeater").getDouble("min")));
						fareRuleDetailsDTO.setAcSeaterMaxFare(BigDecimal.valueOf(fareJson.getJSONObject("acSeater").getDouble("max")));
					}
					if (fareJson.has("nonacSleeper") && fareJson.getJSONObject("nonacSleeper") != null) {
						fareRuleDetailsDTO.setNonAcSleeperLowerMinFare(BigDecimal.valueOf(fareJson.getJSONObject("nonacSleeper").getDouble("min")));
						fareRuleDetailsDTO.setNonAcSleeperLowerMaxFare(BigDecimal.valueOf(fareJson.getJSONObject("nonacSleeper").getDouble("max")));
						fareRuleDetailsDTO.setNonAcSleeperUpperMinFare(fareRuleDetailsDTO.getNonAcSleeperLowerMinFare());
						fareRuleDetailsDTO.setNonAcSleeperUpperMaxFare(fareRuleDetailsDTO.getNonAcSleeperLowerMaxFare());
					}
					if (fareJson.has("acSleeper") && fareJson.getJSONObject("acSleeper") != null) {
						fareRuleDetailsDTO.setAcSleeperLowerMinFare(BigDecimal.valueOf(fareJson.getJSONObject("acSleeper").getDouble("min")));
						fareRuleDetailsDTO.setAcSleeperLowerMaxFare(BigDecimal.valueOf(fareJson.getJSONObject("acSleeper").getDouble("max")));
						fareRuleDetailsDTO.setAcSleeperUpperMinFare(fareRuleDetailsDTO.getAcSleeperLowerMinFare());
						fareRuleDetailsDTO.setAcSleeperUpperMaxFare(fareRuleDetailsDTO.getAcSleeperLowerMaxFare());
					}
					if (fareJson.has("brandedAcSleeper") && fareJson.getJSONObject("brandedAcSleeper") != null) {
						fareRuleDetailsDTO.setBrandedAcSleeperMinFare(BigDecimal.valueOf(fareJson.getJSONObject("brandedAcSleeper").getDouble("min")));
						fareRuleDetailsDTO.setBrandedAcSleeperMaxFare(BigDecimal.valueOf(fareJson.getJSONObject("brandedAcSleeper").getDouble("max")));
					}
					if (fareJson.has("singleaxleAcSeater") && fareJson.getJSONObject("singleaxleAcSeater") != null) {
						fareRuleDetailsDTO.setSingleAxleAcSeaterMinFare(BigDecimal.valueOf(fareJson.getJSONObject("singleaxleAcSeater").getDouble("min")));
						fareRuleDetailsDTO.setSingleAxleAcSeaterMaxFare(BigDecimal.valueOf(fareJson.getJSONObject("singleaxleAcSeater").getDouble("max")));
					}
					if (fareJson.has("multiaxleSeater") && fareJson.getJSONObject("multiaxleSeater") != null) {
						fareRuleDetailsDTO.setMultiAxleSeaterMinFare(BigDecimal.valueOf(fareJson.getJSONObject("multiaxleSeater").getDouble("min")));
						fareRuleDetailsDTO.setMultiAxleSeaterMaxFare(BigDecimal.valueOf(fareJson.getJSONObject("multiaxleSeater").getDouble("max")));
					}
					if (fareJson.has("multiaxleSleeper") && fareJson.getJSONObject("multiaxleSleeper") != null) {
						fareRuleDetailsDTO.setMultiAxleAcSleeperMinFare(BigDecimal.valueOf(fareJson.getJSONObject("multiaxleSleeper").getDouble("min")));
						fareRuleDetailsDTO.setMultiAxleAcSleeperMaxFare(BigDecimal.valueOf(fareJson.getJSONObject("multiaxleSleeper").getDouble("max")));
					}
					fareRuleDetailsDTO.setActiveFlag(1);

					fareRuleDetails.add(fareRuleDetailsDTO);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return fareRuleDetails;

	}

	public List<FareRuleDetailsDTO> getZoneSyncFareRuleDetails(JSONObject stationObject) {
		List<FareRuleDetailsDTO> list = new ArrayList<FareRuleDetailsDTO>();
		try {
			if (Numeric.ONE_INT == stationObject.getInt("status")) {
				JSONArray jsonArray = stationObject.getJSONArray("data");
				for (Object object : jsonArray) {
					JSONObject fareRuleDetailsJson = (JSONObject) object;

					FareRuleDetailsDTO fareRuleDetailsDTO = new FareRuleDetailsDTO();

					StationDTO fromStation = new StationDTO();
					fromStation.setCode(fareRuleDetailsJson.getJSONObject("fromStation").getString("code"));
					fareRuleDetailsDTO.setFromStation(fromStation);

					StationDTO toStation = new StationDTO();
					toStation.setCode(fareRuleDetailsJson.getJSONObject("toStation").getString("code"));
					fareRuleDetailsDTO.setToStation(toStation);

					fareRuleDetailsDTO.setDistance(fareRuleDetailsJson.getInt("distance"));
					fareRuleDetailsDTO.setNonAcSeaterMinFare(StringUtil.getBigDecimalValue(fareRuleDetailsJson.getString("nonAcSeaterMinFare")));
					fareRuleDetailsDTO.setNonAcSeaterMaxFare(StringUtil.getBigDecimalValue(fareRuleDetailsJson.getString("nonAcSeaterMaxFare")));
					fareRuleDetailsDTO.setAcSeaterMinFare(StringUtil.getBigDecimalValue(fareRuleDetailsJson.getString("acSeaterMinFare")));
					fareRuleDetailsDTO.setAcSeaterMaxFare(StringUtil.getBigDecimalValue(fareRuleDetailsJson.getString("acSeaterMaxFare")));
					fareRuleDetailsDTO.setNonAcSleeperLowerMinFare(StringUtil.getBigDecimalValue(fareRuleDetailsJson.getString("nonAcSleeperLowerMinFare")));
					fareRuleDetailsDTO.setNonAcSleeperLowerMaxFare(StringUtil.getBigDecimalValue(fareRuleDetailsJson.getString("nonAcSleeperLowerMaxFare")));
					fareRuleDetailsDTO.setNonAcSleeperUpperMinFare(StringUtil.getBigDecimalValue(fareRuleDetailsJson.getString("nonAcSleeperUpperMinFare")));
					fareRuleDetailsDTO.setNonAcSleeperUpperMaxFare(StringUtil.getBigDecimalValue(fareRuleDetailsJson.getString("nonAcSleeperUpperMaxFare")));
					fareRuleDetailsDTO.setAcSleeperLowerMinFare(StringUtil.getBigDecimalValue(fareRuleDetailsJson.getString("acSleeperLowerMinFare")));
					fareRuleDetailsDTO.setAcSleeperLowerMaxFare(StringUtil.getBigDecimalValue(fareRuleDetailsJson.getString("acSleeperLowerMaxFare")));
					fareRuleDetailsDTO.setAcSleeperUpperMinFare(StringUtil.getBigDecimalValue(fareRuleDetailsJson.getString("acSleeperUpperMinFare")));
					fareRuleDetailsDTO.setAcSleeperUpperMaxFare(StringUtil.getBigDecimalValue(fareRuleDetailsJson.getString("acSleeperUpperMaxFare")));
					fareRuleDetailsDTO.setBrandedAcSleeperMinFare(StringUtil.getBigDecimalValue(fareRuleDetailsJson.getString("brandedAcSleeperMinFare")));
					fareRuleDetailsDTO.setBrandedAcSleeperMaxFare(StringUtil.getBigDecimalValue(fareRuleDetailsJson.getString("brandedAcSleeperMaxFare")));
					fareRuleDetailsDTO.setSingleAxleAcSeaterMinFare(StringUtil.getBigDecimalValue(fareRuleDetailsJson.getString("singleAxleAcSeaterMinFare")));
					fareRuleDetailsDTO.setSingleAxleAcSeaterMaxFare(StringUtil.getBigDecimalValue(fareRuleDetailsJson.getString("singleAxleAcSeaterMaxFare")));
					fareRuleDetailsDTO.setMultiAxleSeaterMinFare(StringUtil.getBigDecimalValue(fareRuleDetailsJson.getString("multiAxleSeaterMinFare")));
					fareRuleDetailsDTO.setMultiAxleSeaterMaxFare(StringUtil.getBigDecimalValue(fareRuleDetailsJson.getString("multiAxleSeaterMaxFare")));
					fareRuleDetailsDTO.setMultiAxleAcSleeperMinFare(StringUtil.getBigDecimalValue(fareRuleDetailsJson.getString("multiAxleAcSleeperMinFare")));
					fareRuleDetailsDTO.setMultiAxleAcSleeperMaxFare(StringUtil.getBigDecimalValue(fareRuleDetailsJson.getString("multiAxleAcSleeperMaxFare")));
					fareRuleDetailsDTO.setActiveFlag(fareRuleDetailsJson.getInt("activeFlag"));

					list.add(fareRuleDetailsDTO);
				}
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return list;
	}

	public List<StationOtaPartnerDTO> getZoneSyncStationOtaPartner(JSONObject stationObject) {
		List<StationOtaPartnerDTO> list = new ArrayList<StationOtaPartnerDTO>();
		try {
			if (Numeric.ONE_INT == stationObject.getInt("status")) {
				JSONArray jsonArray = stationObject.getJSONArray("data");
				for (Object object : jsonArray) {
					JSONObject stationOtaPartnerJson = (JSONObject) object;
					StationOtaPartnerDTO stationOtaPartner = new StationOtaPartnerDTO();
					stationOtaPartner.setCode(stationOtaPartnerJson.getString("code"));
					stationOtaPartner.setOtaStationCode(stationOtaPartnerJson.getString("otaStationCode"));
					stationOtaPartner.setOtaStationName(stationOtaPartnerJson.getString("otaStationName"));

					StateDTO state = new StateDTO();
					state.setCode(stationOtaPartnerJson.getJSONObject("state").getString("code"));
					stationOtaPartner.setState(state);

					List<StationDTO> stations = new ArrayList<StationDTO>();
					JSONArray stationJsonArray = stationOtaPartnerJson.getJSONArray("stations");
					for (Object stationObj : stationJsonArray) {
						JSONObject stationJson = (JSONObject) stationObj;
						StationDTO stationDTO = new StationDTO();
						stationDTO.setCode(stationJson.getString("code"));
						stations.add(stationDTO);

					}
					stationOtaPartner.setStations(stations);
					stationOtaPartner.setOtaPartner(OTAPartnerEM.getOtaPartnerEM(stationOtaPartnerJson.getJSONObject("otaPartner").getString("code")));
					stationOtaPartner.setActiveFlag(stationOtaPartnerJson.getInt("activeFlag"));
					list.add(stationOtaPartner);
				}
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return list;
	}

	public List<CalendarAnnouncementDTO> getCalendarAnnouncementForZoneSync(JSONObject stationObject) {
		List<CalendarAnnouncementDTO> calendarAnnouncementList = new ArrayList<>();
		try {
			if (Numeric.ONE_INT == stationObject.getInt("status")) {
				JSONArray jsonArray = stationObject.getJSONArray("data");
				for (Object object : jsonArray) {
					JSONObject calenderAnnouncementJson = (JSONObject) object;
					CalendarAnnouncementDTO calendarAnouncementDTO = new CalendarAnnouncementDTO();
					calendarAnouncementDTO.setCode(calenderAnnouncementJson.getString("code"));
					calendarAnouncementDTO.setName(calenderAnnouncementJson.getString("name"));
					calendarAnouncementDTO.setActiveFrom(calenderAnnouncementJson.getString("activeFrom"));
					calendarAnouncementDTO.setActiveTo(calenderAnnouncementJson.getString("activeTo"));
					calendarAnouncementDTO.setDayOfWeek(calenderAnnouncementJson.getString("dayOfWeek"));

					calendarAnouncementDTO.setCategory(calenderAnnouncementJson.getJSONObject("category") != null ? CalendarAnnouncementCategoryEM.getCategoryEM(calenderAnnouncementJson.getJSONObject("category").getString("code")) : null);

					List<StateDTO> stateList = new ArrayList<StateDTO>();
					JSONArray stateJsonArray = calenderAnnouncementJson.getJSONArray("states");
					for (Object stateObject : stateJsonArray) {
						JSONObject stateJson = (JSONObject) stateObject;
						if (!stateJson.has("code") || StringUtil.isNull(stateJson.getString("code"))) {
							continue;
						}
						StateDTO stateDTO = new StateDTO();
						stateDTO.setCode(stateJson.getString("code"));
						stateList.add(stateDTO);
					}
					calendarAnouncementDTO.setStates(stateList);

					List<DateTime> dateList = new ArrayList<DateTime>();
					JSONArray datesJsonArray = calenderAnnouncementJson.getJSONArray("dates");
					for (Object dateObject : datesJsonArray) {
						String date = (String) dateObject;
						if (!DateUtil.isValidDate(date)) {
							continue;
						}
						dateList.add(DateUtil.getDateTime(date));
					}
					calendarAnouncementDTO.setDates(dateList);
					calendarAnouncementDTO.setActiveFlag(calenderAnnouncementJson.getInt("activeFlag"));
					calendarAnnouncementList.add(calendarAnouncementDTO);
				}
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return calendarAnnouncementList;
	}
}