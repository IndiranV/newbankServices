package org.in.com.service.report;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.TicketDAO;
import org.in.com.dao.TripDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.DBQueryParamDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.ReportQueryDTO;
import org.in.com.dto.ScheduleTagDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TicketExtraDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.dto.enumeration.TravelStatusEM;
import org.in.com.exception.ServiceException;
import org.in.com.service.GroupService;
import org.in.com.service.OrganizationService;
import org.in.com.service.ReportQueryService;
import org.in.com.service.ScheduleTagService;
import org.in.com.service.StationPointService;
import org.in.com.service.StationService;
import org.in.com.service.UserService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class CustomReportServiceImpl implements CustomReportService {

	@Autowired
	ReportQueryService reportQueryService;
	@Autowired
	OrganizationService organizationService;
	@Autowired
	ScheduleTagService scheduleTagService;
	@Autowired
	StationPointService stationPointService;
	@Autowired
	StationService stationService;
	@Autowired
	UserService userService;
	@Autowired
	GroupService groupService;

	@Override
	public List<Map<String, String>> generateBranchCollectionDetails(AuthDTO authDTO, ReportQueryDTO reportQuery, Map<String, String> requestMap) {
		List<Map<String, String>> branchCollectionReport = new ArrayList<>();
		try {
			String fromDate = DateUtil.convertDateTime(DateUtil.getDateTime(requestMap.get("fromDate")).getStartOfDay());
			String toDate = DateUtil.convertDateTime(DateUtil.getDateTime(requestMap.get("fromDate")).getEndOfDay());

			if (StringUtil.isNotNull(requestMap.get("organizationCode"))) {
				OrganizationDTO organizationDTO = new OrganizationDTO();
				organizationDTO.setCode(requestMap.get("organizationCode"));
				organizationDTO = organizationService.getOrganization(authDTO, organizationDTO);

				DateTime previousDay = DateUtil.getDateTime(requestMap.get("fromDate")).minusDays(1).getStartOfDay();
				fromDate = DateUtil.convertDateTime(DateUtil.addMinituesToDate(previousDay, organizationDTO.getWorkingMinutes()));

				DateTime toDateTime = DateUtil.getDateTime(requestMap.get("fromDate")).getStartOfDay();
				toDate = DateUtil.convertDateTime(toDateTime.getEndOfDay());

				if (StringUtil.isNotNull(requestMap.get("branchWorkingTime")) && "ALLOW".equals(requestMap.get("branchWorkingTime"))) {
					toDate = DateUtil.convertDateTime(DateUtil.addMinituesToDate(toDateTime, organizationDTO.getWorkingMinutes()));
				}
			}

			requestMap.put("fromDate", fromDate);
			requestMap.put("toDate", toDate);

			List<DBQueryParamDTO> params = getBranchCollectionReportParams(authDTO, reportQuery, requestMap);
			List<Map<String, ?>> branchCollectionDetails = reportQueryService.getQueryResultsMap(authDTO, reportQuery, params);
			ScheduleTagDTO scheduleTagDTO = new ScheduleTagDTO();
			for (Map<String, ?> responseMap : branchCollectionDetails) {
				Map<String, String> branchCollectionMap = (Map<String, String>) responseMap;
				if (responseMap.get("tag") != null) {
					List<String> tags = Arrays.asList(String.valueOf(responseMap.get("tag")).split(","));

					StringBuilder tagNames = new StringBuilder();
					for (String tag : tags) {
						if (StringUtil.isNotNull(tag) && !tag.equals(Numeric.ZERO)) {
							scheduleTagDTO.setId(Integer.valueOf(tag));
							scheduleTagService.get(authDTO, scheduleTagDTO);

							tagNames.append(scheduleTagDTO.getName()).append(",");
						}
					}
					branchCollectionMap.put("tag", tagNames.toString());
					branchCollectionReport.add(branchCollectionMap);
				}
			}
		}
		catch (ServiceException e) {
			throw new ServiceException(e.getErrorCode(), e.getData());
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return branchCollectionReport;
	}

	@Override
	public List<Map<String, String>> generateBranchCollectionSummary(AuthDTO authDTO, ReportQueryDTO reportQuery, Map<String, String> requestMap) {
		List<Map<String, String>> organizationCollectionSummary = new ArrayList<>();
		try {
			Map<String, Map<String, String>> uniqueOrganization = new HashMap<>();
			DateTime dateTime = DateUtil.getDateTime(requestMap.get("fromDate")).getStartOfDay();
			String fromDate = DateUtil.convertDateTime(DateUtil.getDateTime(requestMap.get("fromDate")).minusDays(1).getStartOfDay());
			String toDate = DateUtil.convertDateTime(DateUtil.getDateTime(requestMap.get("fromDate")).getEndOfDay());
			requestMap.put("fromDate", fromDate);
			requestMap.put("toDate", toDate);

			List<DBQueryParamDTO> params = getBranchCollectionReportParams(authDTO, reportQuery, requestMap);
			List<Map<String, ?>> branchCollectionDetails = reportQueryService.getQueryResultsMap(authDTO, reportQuery, params);
			List<OrganizationDTO> organizations = organizationService.getAll(authDTO);

			DateTime previousDay = DateUtil.getDateTime(fromDate);

			for (OrganizationDTO organization : organizations) {
				if (StringUtil.isNotNull(requestMap.get("organizationCode")) && !organization.getCode().equals(requestMap.get("organizationCode"))) {
					continue;
				}
				Map<String, String> organizationMap = new HashMap<>();
				organizationMap.put("OFFTIME_AMOUNT", Numeric.ZERO);
				organizationMap.put("OFFTIME_NEXT", Numeric.ZERO);
				organizationMap.put("12_12_AMOUNT", Numeric.ZERO);
				organizationMap.put("WORK_START_DATE_TIME", DateUtil.convertDateTime(DateUtil.addMinituesToDate(previousDay, organization.getWorkingMinutes())));
				organizationMap.put("WORK_END_DATE_TIME", DateUtil.convertDateTime(DateUtil.addMinituesToDate(dateTime, organization.getWorkingMinutes())));
				organizationMap.put("FROM_DATE_TIME", DateUtil.convertDateTime(dateTime));
				organizationMap.put("WORKING_HOURS", DateUtil.getMinutesToTime(organization.getWorkingMinutes()));
				organizationMap.put("ORGANIZATION_CODE", organization.getCode());
				organizationMap.put("ORGANIZATION_NAME", organization.getName());
				uniqueOrganization.put(organization.getCode(), organizationMap);
			}

			Map<String, Map<String, BigDecimal>> organizationTagTransaction = new HashMap<>();
			Map<String, ScheduleTagDTO> overallBookedTags = new HashMap<>();
			for (Map<String, ?> resultMap : branchCollectionDetails) {
				Map<String, String> organizationMap = uniqueOrganization.get(resultMap.get("organization_code"));
				if (organizationMap == null) {
					continue;
				}

				DateTime previousDayWorkStartDateTime = DateUtil.getDateTime(organizationMap.get("WORK_START_DATE_TIME"));
				DateTime currentDayWorkClosingDateTime = DateUtil.getDateTime(organizationMap.get("WORK_END_DATE_TIME"));
				DateTime currentDayStartOfDateTime = DateUtil.getDateTime(organizationMap.get("FROM_DATE_TIME"));
				DateTime transactionDate = DateUtil.getDateTime(String.valueOf(resultMap.get("transaction_date")));

				if (transactionDate.lt(previousDayWorkStartDateTime)) {
					continue;
				}

				BigDecimal ticketAmount = StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("ticket_amount")));
				BigDecimal acBusTax = StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("ac_bus_tax")));
				BigDecimal bookingCommission = StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("commission_amount")));
				BigDecimal bookingCommissionTds = StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("tds_tax")));
				BigDecimal addonsAmount = StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("addons_amount")));
				BigDecimal bookingPayable = ticketAmount.add(acBusTax).subtract(bookingCommission).subtract(addonsAmount).add(bookingCommissionTds);

				BigDecimal cancellationChargeAmount = StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("cancellation_charges")));
				BigDecimal cancelCommission = StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("cancel_commission")));
				BigDecimal cancelCommissionTds = StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("cancel_tds_tax")));
				BigDecimal revokeCommission = StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("revoke_commission_amount")));
				BigDecimal cancelBookPayable = ticketAmount.add(acBusTax).subtract(addonsAmount).subtract(revokeCommission).add(bookingCommissionTds);
				BigDecimal cancelChargePayable = cancellationChargeAmount.subtract(cancelCommission).add(cancelCommissionTds);
				BigDecimal cancelPayable = cancelBookPayable.subtract(cancelChargePayable);

				if (organizationTagTransaction.get(resultMap.get("organization_code")) == null && transactionDate.lteq(currentDayWorkClosingDateTime)) {
					Map<String, BigDecimal> tagTransaction = new HashMap<>();
					if (resultMap.get("tag") != null) {
						List<String> tags = Arrays.asList(String.valueOf(resultMap.get("tag")).split(","));
						for (String tag : tags) {
							ScheduleTagDTO scheduleTagDTO = new ScheduleTagDTO();
							if (StringUtil.isNotNull(tag) && !tag.equals(Numeric.ZERO)) {
								scheduleTagDTO.setId(Integer.valueOf(tag));
								scheduleTagService.get(authDTO, scheduleTagDTO);
							}

							if ("BO".equals(String.valueOf(resultMap.get("ticket_status_code"))) || "PBL".equals(String.valueOf(resultMap.get("ticket_status_code")))) {
								if (scheduleTagDTO.getId() == 0 && tagTransaction.get("NA_others") != null) {
									BigDecimal amount = tagTransaction.get("NA_others");
									tagTransaction.put("NA_others", bookingPayable.add(amount));
								}
								else if (scheduleTagDTO.getId() == 0 && tagTransaction.get("NA_others") == null) {
									tagTransaction.put("NA_others", bookingPayable);
								}
								else if (tagTransaction.get(scheduleTagDTO.getCode() + "_" + scheduleTagDTO.getName()) != null) {
									BigDecimal amount = tagTransaction.get(scheduleTagDTO.getCode() + "_" + scheduleTagDTO.getName());
									tagTransaction.put(scheduleTagDTO.getCode() + "_" + scheduleTagDTO.getName(), bookingPayable.add(amount));
								}
								else if (tagTransaction.get(scheduleTagDTO.getCode() + "_" + scheduleTagDTO.getName()) == null) {
									tagTransaction.put(scheduleTagDTO.getCode() + "_" + scheduleTagDTO.getName(), bookingPayable);
								}
							}
							else {
								if (scheduleTagDTO.getId() == 0 && tagTransaction.get("NA_others") != null) {
									BigDecimal amount = tagTransaction.get("NA_others");
									tagTransaction.put("NA_others", amount.subtract(cancelPayable));
								}
								else if (scheduleTagDTO.getId() == 0 && tagTransaction.get("NA_others") == null) {
									tagTransaction.put("NA_others", cancelPayable.multiply(BigDecimal.valueOf(-1)));
								}
								else if (tagTransaction.get(scheduleTagDTO.getCode() + "_" + scheduleTagDTO.getName()) != null) {
									BigDecimal amount = tagTransaction.get(scheduleTagDTO.getCode() + "_" + scheduleTagDTO.getName());
									tagTransaction.put(scheduleTagDTO.getCode() + "_" + scheduleTagDTO.getName(), amount.subtract(cancelPayable));
								}
								else if (tagTransaction.get(scheduleTagDTO.getCode() + "_" + scheduleTagDTO.getName()) == null) {
									tagTransaction.put(scheduleTagDTO.getCode() + "_" + scheduleTagDTO.getName(), cancelPayable.multiply(BigDecimal.valueOf(-1)));
								}
							}
							overallBookedTags.put(scheduleTagDTO.getCode() + "_" + scheduleTagDTO.getName(), scheduleTagDTO);
						}
					}
					else {
						if ("BO".equals(String.valueOf(resultMap.get("ticket_status_code"))) || "PBL".equals(String.valueOf(resultMap.get("ticket_status_code")))) {
							if (tagTransaction.get("NA_others") != null) {
								BigDecimal amount = tagTransaction.get("NA_others");
								tagTransaction.put("NA_others", bookingPayable.add(amount));
							}
							else if (tagTransaction.get("NA_others") == null) {
								tagTransaction.put("NA_others", bookingPayable);
							}
						}
						else {
							if (tagTransaction.get("NA_others") != null) {
								BigDecimal amount = tagTransaction.get("NA_others");
								tagTransaction.put("NA_others", amount.subtract(cancelPayable));
							}
							else if (tagTransaction.get("NA_others") == null) {
								tagTransaction.put("NA_others", cancelPayable.multiply(BigDecimal.valueOf(-1)));
							}
						}
					}

					organizationTagTransaction.put(String.valueOf(resultMap.get("organization_code")), tagTransaction);
				}
				else if (transactionDate.lteq(currentDayWorkClosingDateTime)) {
					Map<String, BigDecimal> tagTransaction = organizationTagTransaction.get(resultMap.get("organization_code"));
					if (resultMap.get("tag") != null) {
						List<String> tags = Arrays.asList(String.valueOf(resultMap.get("tag")).split(","));
						for (String tag : tags) {
							ScheduleTagDTO scheduleTagDTO = new ScheduleTagDTO();
							if (StringUtil.isNotNull(tag) && !tag.equals(Numeric.ZERO)) {
								scheduleTagDTO.setId(Integer.valueOf(tag));
								scheduleTagService.get(authDTO, scheduleTagDTO);
							}
							if ("BO".equals(String.valueOf(resultMap.get("ticket_status_code"))) || "PBL".equals(String.valueOf(resultMap.get("ticket_status_code")))) {
								if (scheduleTagDTO.getId() == 0 && tagTransaction.get("NA_others") != null) {
									BigDecimal amount = tagTransaction.get("NA_others");
									tagTransaction.put("NA_others", bookingPayable.add(amount));
								}
								else if (scheduleTagDTO.getId() == 0 && tagTransaction.get("NA_others") == null) {
									tagTransaction.put("NA_others", bookingPayable);
								}
								else if (tagTransaction.get(scheduleTagDTO.getCode() + "_" + scheduleTagDTO.getName()) != null) {
									BigDecimal amount = tagTransaction.get(scheduleTagDTO.getCode() + "_" + scheduleTagDTO.getName());
									tagTransaction.put(scheduleTagDTO.getCode() + "_" + scheduleTagDTO.getName(), bookingPayable.add(amount));
								}
								else if (tagTransaction.get(scheduleTagDTO.getCode() + "_" + scheduleTagDTO.getName()) == null) {
									tagTransaction.put(scheduleTagDTO.getCode() + "_" + scheduleTagDTO.getName(), bookingPayable);
								}
							}
							else {
								if (scheduleTagDTO.getId() == 0 && tagTransaction.get("NA_others") != null) {
									BigDecimal amount = tagTransaction.get("NA_others");
									tagTransaction.put("NA_others", amount.subtract(cancelPayable));
								}
								else if (scheduleTagDTO.getId() == 0 && tagTransaction.get("NA_others") == null) {
									tagTransaction.put("NA_others", cancelPayable.multiply(BigDecimal.valueOf(-1)));
								}
								else if (tagTransaction.get(scheduleTagDTO.getCode() + "_" + scheduleTagDTO.getName()) != null) {
									BigDecimal amount = tagTransaction.get(scheduleTagDTO.getCode() + "_" + scheduleTagDTO.getName());
									tagTransaction.put(scheduleTagDTO.getCode() + "_" + scheduleTagDTO.getName(), amount.subtract(cancelPayable));
								}
								else if (tagTransaction.get(scheduleTagDTO.getCode() + "_" + scheduleTagDTO.getName()) == null) {
									tagTransaction.put(scheduleTagDTO.getCode() + "_" + scheduleTagDTO.getName(), cancelPayable.multiply(BigDecimal.valueOf(-1)));
								}
							}
						}
					}
					else {
						if ("BO".equals(String.valueOf(resultMap.get("ticket_status_code"))) || "PBL".equals(String.valueOf(resultMap.get("ticket_status_code")))) {
							if (tagTransaction.get("NA_others") != null) {
								BigDecimal amount = tagTransaction.get("NA_others");
								tagTransaction.put("NA_others", bookingPayable.add(amount));
							}
							else if (tagTransaction.get("NA_others") == null) {
								tagTransaction.put("NA_others", bookingPayable);
							}
						}
						else {
							if (tagTransaction.get("NA_others") != null) {
								BigDecimal amount = tagTransaction.get("NA_others");
								tagTransaction.put("NA_others", amount.subtract(cancelPayable));
							}
							else if (tagTransaction.get("NA_others") == null) {
								tagTransaction.put("NA_others", cancelPayable.multiply(BigDecimal.valueOf(-1)));
							}
						}
					}

					organizationTagTransaction.put(String.valueOf(resultMap.get("organization_code")), tagTransaction);
				}

				if ("BO".equals(String.valueOf(resultMap.get("ticket_status_code"))) || "PBL".equals(String.valueOf(resultMap.get("ticket_status_code")))) {
					if (transactionDate.gt(previousDayWorkStartDateTime) && transactionDate.lt(currentDayStartOfDateTime)) {
						organizationMap.put("OFFTIME_AMOUNT", String.valueOf(StringUtil.getBigDecimalValue(organizationMap.get("OFFTIME_AMOUNT")).add(bookingPayable)));
					}
					else if (transactionDate.gteq(currentDayWorkClosingDateTime)) {
						organizationMap.put("OFFTIME_NEXT", String.valueOf(StringUtil.getBigDecimalValue(organizationMap.get("OFFTIME_NEXT")).add(bookingPayable)));
					}
					if (transactionDate.gteq(currentDayStartOfDateTime)) {
						organizationMap.put("12_12_AMOUNT", String.valueOf(StringUtil.getBigDecimalValue(organizationMap.get("12_12_AMOUNT")).add(bookingPayable)));
					}
				}
				else {
					if (transactionDate.gt(previousDayWorkStartDateTime) && transactionDate.lt(currentDayStartOfDateTime)) {
						organizationMap.put("OFFTIME_AMOUNT", String.valueOf(StringUtil.getBigDecimalValue(organizationMap.get("OFFTIME_AMOUNT")).subtract(cancelPayable)));
					}
					else if (transactionDate.gteq(currentDayWorkClosingDateTime)) {
						organizationMap.put("OFFTIME_NEXT", String.valueOf(StringUtil.getBigDecimalValue(organizationMap.get("OFFTIME_NEXT")).subtract(cancelPayable)));
					}
					if (transactionDate.gteq(currentDayStartOfDateTime)) {
						organizationMap.put("12_12_AMOUNT", String.valueOf(StringUtil.getBigDecimalValue(organizationMap.get("12_12_AMOUNT")).subtract(cancelPayable)));
					}
				}

				uniqueOrganization.put(String.valueOf(resultMap.get("organization_code")), organizationMap);
			}

			List<ScheduleTagDTO> scheduleTags = scheduleTagService.getAll(authDTO);

			for (Entry<String, Map<String, String>> entry : uniqueOrganization.entrySet()) {
				Map<String, String> summary = entry.getValue();

				Map<String, BigDecimal> tagTransaction = organizationTagTransaction.get(entry.getKey());
				Map<String, String> addedTags = new HashMap<>();
				if (tagTransaction != null) {
					for (Entry<String, BigDecimal> entry1 : tagTransaction.entrySet()) {
						Map<String, String> summaryMap = new HashMap<>();
						summaryMap.putAll(summary);
						summaryMap.put("TAG_CODE", entry1.getKey().split("\\_")[0]);
						summaryMap.put("TAG_NAME", entry1.getKey().split("\\_")[1]);
						summaryMap.put("TAG_AMOUNT", String.valueOf(entry1.getValue()));
						organizationCollectionSummary.add(summaryMap);
						addedTags.put(entry1.getKey(), entry1.getKey());
					}
				}

				for (ScheduleTagDTO scheduleTag : scheduleTags) {
					if (addedTags.get(scheduleTag.getCode() + "_" + scheduleTag.getName()) == null && overallBookedTags.get(scheduleTag.getCode() + "_" + scheduleTag.getName()) != null) {
						Map<String, String> summaryMap = new HashMap<>();
						summaryMap.putAll(summary);
						summaryMap.put("TAG_CODE", scheduleTag.getCode());
						summaryMap.put("TAG_NAME", scheduleTag.getName());
						summaryMap.put("TAG_AMOUNT", Numeric.ZERO);
						organizationCollectionSummary.add(summaryMap);
					}
				}
			}
		}
		catch (ServiceException e) {
			throw new ServiceException(e.getErrorCode(), e.getData());
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return organizationCollectionSummary;
	}

	private List<DBQueryParamDTO> getBranchCollectionReportParams(AuthDTO authDTO, ReportQueryDTO reportQueryDTO, Map<String, String> requestMap) {
		List<DBQueryParamDTO> params = new ArrayList<>();
		if (reportQueryDTO.getQuery().contains(":namespaceId")) {
			DBQueryParamDTO paramDTO = new DBQueryParamDTO();
			paramDTO.setParamName("namespaceId");
			paramDTO.setValue(String.valueOf(authDTO.getNamespace().getId()));
			params.add(paramDTO);
		}
		if (reportQueryDTO.getQuery().contains(":fromDate")) {
			DBQueryParamDTO paramDTO = new DBQueryParamDTO();
			paramDTO.setParamName("fromDate");
			paramDTO.setValue(StringUtil.isNull(requestMap.get("fromDate"), Text.NA));
			params.add(paramDTO);
		}
		if (reportQueryDTO.getQuery().contains(":toDate")) {
			DBQueryParamDTO paramDTO = new DBQueryParamDTO();
			paramDTO.setParamName("toDate");
			paramDTO.setValue(StringUtil.isNull(requestMap.get("toDate"), Text.NA));
			params.add(paramDTO);
		}
		if (reportQueryDTO.getQuery().contains(":organizationCode")) {
			DBQueryParamDTO paramDTO = new DBQueryParamDTO();
			paramDTO.setParamName("organizationCode");
			paramDTO.setValue(StringUtil.isNull(requestMap.get("organizationCode"), Text.NA));
			params.add(paramDTO);
		}
		if (reportQueryDTO.getQuery().contains(":phoneTicketFlag")) {
			DBQueryParamDTO paramDTO = new DBQueryParamDTO();
			paramDTO.setParamName("phoneTicketFlag");
			paramDTO.setValue(requestMap.get("phoneTicketFlag"));
			params.add(paramDTO);
		}
		if (reportQueryDTO.getQuery().contains(":groupCode")) {
			DBQueryParamDTO paramDTO = new DBQueryParamDTO();
			paramDTO.setParamName("groupCode");
			paramDTO.setValue(StringUtil.isNull(requestMap.get("groupCode"), Text.NA));
			params.add(paramDTO);
		}
		if (reportQueryDTO.getQuery().contains(":tagCode")) {
			DBQueryParamDTO paramDTO = new DBQueryParamDTO();
			paramDTO.setParamName("tagCode");
			paramDTO.setValue(StringUtil.isNull(requestMap.get("tagCode"), Text.NA));
			params.add(paramDTO);
		}
		return params;
	}

	@Override
	public JSONArray getUserSpecificBoardingCommissionReport(AuthDTO authDTO, UserDTO userDTO, StationDTO stationDTO, DateTime fromDate, DateTime toDate) {
		JSONArray response = new JSONArray();
		try {
			Map<String, Map<String, StationPointDTO>> usertationPointMap = stationPointService.getUserSpecificStationPointV2(authDTO, userDTO, stationDTO);
			List<DateTime> dateList = DateUtil.getDateList(fromDate, toDate);
			
			Map<String, JSONArray> reportResponse = new HashMap<>();
			for (DateTime tripDate : dateList) {
				List<TicketDetailsDTO> ticketDetails = new ArrayList<>();
				if (DateUtil.getDayDifferent(tripDate.getStartOfDay(), DateUtil.NOW()) < 3) {
					TripDAO tripDAO = new TripDAO();
					ticketDetails = tripDAO.getBookedBlockedSeatsV2(authDTO, DateUtil.convertDate(tripDate));
				}
				else {
					TicketDAO ticketDAO = new TicketDAO();
					List<TicketDTO> tickets = ticketDAO.getTicketsByTripDate(authDTO, DateUtil.convertDate(tripDate));
					ticketDetails = getTicketDetails(authDTO, tickets);
				}
	
				String ticketStationBoardingPointKey = "";
				String stationBoardingPointKey = "";
				for (TicketDetailsDTO ticketDetailsDTO : ticketDetails) {
					if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId() && DateUtil.getMinutiesDifferent(ticketDetailsDTO.getUpdatedAt(), DateUtil.NOW()) > authDTO.getNamespace().getProfile().getSeatBlockTime()) {
						continue;
					}
					if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BOOKING_CANCELLED.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TENTATIVE_BLOCK_CANCELLED.getId()) {
						continue;
					}
					if (ticketDetailsDTO.getTicketExtra() != null && ticketDetailsDTO.getTicketExtra().getTravelStatus() != null && ticketDetailsDTO.getTicketExtra().getTravelStatus().getId() == TravelStatusEM.NOT_TRAVELED.getId()) {
						continue;
					}
	
					StationPointDTO boardingPoint = null;
					if (StringUtil.isNotNull(ticketDetailsDTO.getStationPoint())) {
						String[] stationPoints = ticketDetailsDTO.getStationPoint().split(":-:");
	
						boardingPoint = new StationPointDTO();
						boardingPoint.setCode(stationPoints[Numeric.ZERO_INT]);
						boardingPoint.setName(stationPoints[Numeric.ONE_INT]);
					}
					Map<String, StationPointDTO> dataMap = (Map<String, StationPointDTO>) usertationPointMap.get(boardingPoint.getCode());
					if (dataMap == null) {
						continue;
					}
					
					ticketDetailsDTO.setUser(userService.getUser(authDTO, ticketDetailsDTO.getUser()));
					ticketDetailsDTO.getUser().setGroup(groupService.getGroup(authDTO, ticketDetailsDTO.getUser().getGroup()));

					StationPointDTO stationPointDTO = null;
					if (dataMap.get(ticketDetailsDTO.getUser().getGroup().getCode()) != null) {
						stationPointDTO = dataMap.get(ticketDetailsDTO.getUser().getGroup().getCode());
					}
					else if (dataMap.get("ALL") != null) {
						stationPointDTO = dataMap.get("ALL");
					}
					if (stationPointDTO == null) {
						continue;
					}
	
					ticketStationBoardingPointKey = ticketDetailsDTO.getFromStation().getId() + Text.HYPHEN + boardingPoint.getCode();
					stationBoardingPointKey = stationPointDTO.getStation().getId() + Text.HYPHEN + stationPointDTO.getCode();
					if (!stationBoardingPointKey.equals(ticketStationBoardingPointKey)) {
						continue;
					}
	
//					if (!stationPointDTO.getUserGroupList().isEmpty()) {
//						ticketDetailsDTO.setUser(userService.getUser(authDTO, ticketDetailsDTO.getUser()));
//						ticketDetailsDTO.getUser().setGroup(groupService.getGroup(authDTO, ticketDetailsDTO.getUser().getGroup()));
//	
//						GroupDTO group = null;
//						for (GroupDTO userGroup : stationPointDTO.getUserGroupList()) {
//							if (ticketDetailsDTO.getUser().getGroup().getCode().equals(userGroup.getCode())) {
//								group = ticketDetailsDTO.getUser().getGroup();
//								break;
//							}
//						}
//						if (group == null || group.getId() == 0) {
//							continue;
//						}
//					}
	
					ticketDetailsDTO.setFromStation(stationService.getStation(ticketDetailsDTO.getFromStation()));
					ticketDetailsDTO.setToStation(stationService.getStation(ticketDetailsDTO.getToStation()));
	
					String routeKey = ticketDetailsDTO.getFromStation().getCode() + Text.COMMA + ticketDetailsDTO.getToStation().getCode();
					String boardingKey = ticketDetailsDTO.getFromStation().getCode() + Text.HYPHEN + ticketDetailsDTO.getToStation().getCode() + Text.HYPHEN + stationPointDTO.getCode();
	
					if (reportResponse.get(routeKey) == null) {
						JSONArray boardingJsonArray = new JSONArray();
						JSONObject boardingJson = new JSONObject();
						boardingJson.put("fromStationCode", ticketDetailsDTO.getFromStation().getCode());
						boardingJson.put("fromStationName", ticketDetailsDTO.getFromStation().getName());
	
						boardingJson.put("toStationCode", ticketDetailsDTO.getToStation().getCode());
						boardingJson.put("toStationName", ticketDetailsDTO.getToStation().getName());
	
						boardingJson.put("organizationCode", stationPointDTO.getCode());
						boardingJson.put("organizationName", stationPointDTO.getName());
						boardingJson.put("boardingCommission", stationPointDTO.getBoardingCommission());
						boardingJson.put("seatNames", ticketDetailsDTO.getSeatName());
	
						List<String> boardingPointKeys = new ArrayList<>();
						String boardingKey1 = boardingJson.getString("fromStationCode") + Text.HYPHEN + boardingJson.getString("toStationCode") + Text.HYPHEN + boardingJson.getString("organizationCode");
						boardingPointKeys.add(boardingKey1);
						boardingJson.put("boardingPointKeys", boardingPointKeys);
	
						boardingJsonArray.add(boardingJson);
						reportResponse.put(routeKey, boardingJsonArray);
					}
					else {
						JSONArray boardingJsonArray = reportResponse.get(routeKey);
						JSONObject boardingPointJson = boardingJsonArray.getJSONObject(0);
						List<String> boardingPointKeys = boardingPointJson.getJSONArray("boardingPointKeys");
	
						if (boardingPointKeys.contains(boardingKey)) {
							for (Object json : boardingJsonArray) {
								JSONObject boardingJson = (JSONObject) json;
								String boardingKey1 = boardingJson.getString("fromStationCode") + Text.HYPHEN + boardingJson.getString("toStationCode") + Text.HYPHEN + boardingJson.getString("organizationCode");
								if (boardingKey.equals(boardingKey1)) {
									boardingJson.put("boardingCommission", StringUtil.getBigDecimalValue(boardingJson.getString("boardingCommission")).add(stationPointDTO.getBoardingCommission()));
									boardingJson.put("seatNames", boardingJson.getString("seatNames") + Text.COMMA + ticketDetailsDTO.getSeatName());
									break;
								}
							}
						}
						else {
							JSONObject boardingJson = new JSONObject();
							boardingJson.put("fromStationCode", ticketDetailsDTO.getFromStation().getCode());
							boardingJson.put("fromStationName", ticketDetailsDTO.getFromStation().getName());
	
							boardingJson.put("toStationCode", ticketDetailsDTO.getToStation().getCode());
							boardingJson.put("toStationName", ticketDetailsDTO.getToStation().getName());
	
							boardingJson.put("organizationCode", stationPointDTO.getCode());
							boardingJson.put("organizationName", stationPointDTO.getName());
							boardingJson.put("boardingCommission", stationPointDTO.getBoardingCommission());
							boardingJson.put("seatNames", ticketDetailsDTO.getSeatName());
	
							String boardingKey1 = boardingJson.getString("fromStationCode") + Text.HYPHEN + boardingJson.getString("toStationCode") + Text.HYPHEN + boardingJson.getString("organizationCode");
							boardingPointKeys.add(boardingKey1);
							boardingJson.put("boardingPointKeys", boardingPointKeys);
	
							boardingJsonArray.add(boardingJson);
							reportResponse.put(routeKey, boardingJsonArray);
						}
					}
				}
			}

			for (Entry<String, JSONArray> entry : reportResponse.entrySet()) {
				JSONObject routeJson = new JSONObject();

				JSONArray boardingJsonArray = new JSONArray();
				for (Object json : entry.getValue()) {
					JSONObject boardingJson = (JSONObject) json;

					routeJson.put("fromStationCode", boardingJson.getString("fromStationCode"));
					routeJson.put("fromStationName", boardingJson.getString("fromStationName"));
					routeJson.put("toStationCode", boardingJson.getString("toStationCode"));
					routeJson.put("toStationName", boardingJson.getString("toStationName"));

					JSONObject jsonObject = new JSONObject();
					jsonObject.put("organizationCode", boardingJson.getString("organizationCode"));
					jsonObject.put("organizationName", boardingJson.getString("organizationName"));
					jsonObject.put("boardingCommission", boardingJson.get("boardingCommission"));
					jsonObject.put("seatNames", boardingJson.get("seatNames"));
					boardingJsonArray.add(jsonObject);
				}
				routeJson.put("boardingPoints", boardingJsonArray);
				response.add(routeJson);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	private List<TicketDetailsDTO> getTicketDetails(AuthDTO authDTO, List<TicketDTO> tickets) {
		List<TicketDetailsDTO> ticketDetails = new ArrayList<TicketDetailsDTO>();
		for (TicketDTO ticketDTO : tickets) {
			TicketDetailsDTO ticketDetailsDTO = ticketDTO.getTicketDetails().get(0);
			ticketDTO.setBoardingPoint(stationPointService.getStationPoint(authDTO, ticketDTO.getBoardingPoint()));
			ticketDTO.setDroppingPoint(stationPointService.getStationPoint(authDTO, ticketDTO.getDroppingPoint()));
			ticketDetailsDTO.setBoardingPointName(ticketDTO.getBoardingPoint().getName());
			ticketDetailsDTO.setStationPoint(BitsUtil.convertStationPoint(ticketDTO.getBoardingPoint(), ticketDTO.getDroppingPoint()));
			TicketExtraDTO ticketExtraDTO = new TicketExtraDTO();
			ticketExtraDTO.setTravelStatus(ticketDetailsDTO.getTravelStatus());
			ticketDetails.add(ticketDetailsDTO);
		}
		return ticketDetails;
	}
}
