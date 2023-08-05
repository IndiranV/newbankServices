package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.in.com.config.ApplicationConfig;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.controller.web.io.ExportReportDetailsIO;
import org.in.com.controller.web.io.ReportQueryIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.DBQueryParamDTO;
import org.in.com.dto.ExportReportDetailsDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.ReportQueryDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.UserRoleEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.ReportQueryService;
import org.in.com.service.SeatVisibilityReportService;
import org.in.com.service.pg.PaymentRequestService;
import org.in.com.service.report.CustomReportService;
import org.in.com.service.report.ETicketTransactionReportService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import hirondelle.date4j.DateTime;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/{authtoken}/report")
public class ReportController extends BaseController {
	public static Map<String, Integer> ConcurrentRequests = new ConcurrentHashMap<String, Integer>();
	private static final Logger reportlogger = LoggerFactory.getLogger("org.in.com.controller.report");

	@Autowired
	PaymentRequestService paymentRequestService;
	@Autowired
	ReportQueryService queryService;
	@Autowired
	ETicketTransactionReportService eTicketTransactionReportService;
	@Autowired
	CustomReportService customReportService;
	@Autowired
	SeatVisibilityReportService seatVisibilityReportService;

	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<ReportQueryIO>> getAllReportQuery(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "-1") int activeFlag) throws Exception {
		List<ReportQueryIO> list = new ArrayList<ReportQueryIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<ReportQueryDTO> DTOList = queryService.getAll(authDTO);
			for (ReportQueryDTO queryDTO : DTOList) {
				if (activeFlag != -1 && activeFlag != queryDTO.getActiveFlag()) {
					continue;
				}
				ReportQueryIO queryIO = new ReportQueryIO();
				queryIO.setCode(queryDTO.getCode());
				queryIO.setName(queryDTO.getName());
				queryIO.setDescription(queryDTO.getDescription());
				queryIO.setQuery(queryDTO.getQuery());
				queryIO.setDaysLimit(queryDTO.getDaysLimit());
				queryIO.setActiveFlag(queryDTO.getActiveFlag());
				list.add(queryIO);
			}

		}
		return ResponseIO.success(list);
	}

	@RequestMapping(value = "/zonesync", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<ReportQueryIO>> getAllforZoneSync(@PathVariable("authtoken") String authtoken, String syncDate) throws Exception {
		List<ReportQueryIO> list = new ArrayList<ReportQueryIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<ReportQueryDTO> DTOList = queryService.getAllforZoneSync(authDTO, syncDate);
			for (ReportQueryDTO queryDTO : DTOList) {
				ReportQueryIO queryIO = new ReportQueryIO();
				queryIO.setCode(queryDTO.getCode());
				queryIO.setName(queryDTO.getName());
				queryIO.setDescription(queryDTO.getDescription());
				queryIO.setQuery(queryDTO.getQuery());
				queryIO.setDaysLimit(queryDTO.getDaysLimit());
				queryIO.setActiveFlag(queryDTO.getActiveFlag());
				list.add(queryIO);
			}

		}
		return ResponseIO.success(list);
	}

	/**
	 * @sample
	 *         select code from ticket where id = :{your parameter name}
	 */
	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<ReportQueryIO> updateReportQuery(@PathVariable("authtoken") String authtoken, @RequestBody ReportQueryIO reportQuery) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ReportQueryDTO reportQueryDTO = new ReportQueryDTO();
			reportQueryDTO.setActiveFlag(reportQuery.getActiveFlag());
			reportQueryDTO.setCode(reportQuery.getCode());
			reportQueryDTO.setName(reportQuery.getName());
			reportQueryDTO.setQuery(reportQuery.getQuery());
			reportQueryDTO.setDaysLimit(reportQuery.getDaysLimit());
			reportQueryDTO.setDescription(reportQuery.getDescription());
			queryService.Update(authDTO, reportQueryDTO);
			reportQuery.setCode(reportQueryDTO.getCode());
			reportQuery.setActiveFlag(reportQueryDTO.getActiveFlag());
		}
		return ResponseIO.success(reportQuery);
	}

	@RequestMapping(value = "/dynamic/{queryCode}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<List<String>>> getDynamicReport(@PathVariable("authtoken") String authtoken, @PathVariable("queryCode") String queryCode, @RequestBody List<DBQueryParamDTO> paramList) throws Exception {
		List<List<String>> list = null;
		DateTime now = DateUtil.NOW();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ReportQueryDTO reportQueryDTO = new ReportQueryDTO();
		reportQueryDTO.setCode(queryCode);
		queryService.get(authDTO, reportQueryDTO);

		try {
			checkConcurrentRequests(authDTO, queryCode, null);

			if (StringUtils.isNotEmpty(reportQueryDTO.getQuery())) {
				// Default Value
				if (reportQueryDTO.getQuery().contains(":namespaceId")) {
					DBQueryParamDTO namespaceParamDTO = new DBQueryParamDTO();
					namespaceParamDTO.setParamName("namespaceId");
					namespaceParamDTO.setValue(String.valueOf(authDTO.getNamespace().getId()));
					paramList.add(namespaceParamDTO);
				}
				if (reportQueryDTO.getQuery().contains(":superNamespaceFlag")) {
					DBQueryParamDTO namespaceParamDTO = new DBQueryParamDTO();
					namespaceParamDTO.setParamName("superNamespaceFlag");
					namespaceParamDTO.setValue(ApplicationConfig.getServerZoneCode().equals(authDTO.getNamespaceCode()) ? Numeric.ONE : Numeric.ZERO);
					paramList.add(namespaceParamDTO);
				}
				if (reportQueryDTO.getQuery().contains(":loginUserId")) {
					DBQueryParamDTO userParamDTO = new DBQueryParamDTO();
					userParamDTO.setParamName("loginUserId");
					userParamDTO.setValue(String.valueOf(authDTO.getUser().getId()));
					paramList.add(userParamDTO);
				}
				if (reportQueryDTO.getQuery().contains(":userCustomerId")) {
					DBQueryParamDTO userParamDTO = new DBQueryParamDTO();
					userParamDTO.setParamName("userCustomerId");
					userParamDTO.setValue(authDTO.getUserCustomer() != null ? String.valueOf(authDTO.getUserCustomer().getId()) : Numeric.ZERO);
					paramList.add(userParamDTO);
				}
				// Remove Extra unwanted parameters
				for (Iterator<DBQueryParamDTO> iterator = paramList.iterator(); iterator.hasNext();) {
					DBQueryParamDTO paramDTO = iterator.next();
					if (!reportQueryDTO.getQuery().contains(paramDTO.getParamName())) {
						iterator.remove();
					}
				}
				// Date range validation as per days limit
				BitsUtil.validateDateRange(paramList, reportQueryDTO.getDaysLimit());

				list = queryService.getQueryResults(authDTO, reportQueryDTO, paramList);
				reportQueryDTO.setDescription(ErrorCode.SUCCESS.getMessage());
			}
			else {
				reportQueryDTO.setDescription(ErrorCode.INVALID_CODE.getMessage());
			}
		}
		catch (ServiceException e) {
			reportQueryDTO.setDescription(e.getErrorCode() != null ? e.getErrorCode().getMessage() : Text.NA);
			throw e;
		}
		catch (Exception e) {
			reportQueryDTO.setDescription(e.getMessage());
			throw e;

		}
		finally {
			queryService.addReportQueryAuditLog(authDTO, reportQueryDTO, BitsUtil.convertParameterToString(paramList), DateUtil.getSecondsDifferent(now, DateUtil.NOW()), list != null ? list.size() : 0);
			releaseConcurrentRequests(authDTO.getNamespaceCode(), authDTO.getUserCode(), queryCode, null);
		}
		return ResponseIO.success(list);
	}

	@RequestMapping(value = "/dynamic/map/{queryCode}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<Map<String, ?>>> getDynamicReportMap(@PathVariable("authtoken") String authtoken, @PathVariable("queryCode") String queryCode, @RequestBody List<DBQueryParamDTO> paramList) throws Exception {
		List<Map<String, ?>> list = null;
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		DateTime now = DateUtil.NOW();
		ReportQueryDTO reportQueryDTO = new ReportQueryDTO();
		try {
			reportQueryDTO.setCode(queryCode);
			queryService.get(authDTO, reportQueryDTO);
			if (paramList == null) {
				paramList = new ArrayList<DBQueryParamDTO>();
			}
			checkConcurrentRequests(authDTO, queryCode, null);

			// validate before date limit
			int reportingDays = authDTO.getNamespace().getProfile().getReportingDays();
			if (reportQueryDTO.getQuery().contains(":fromDate") && reportingDays != 0) {
				validateReportingDate(paramList, reportingDays);
			}
			if (StringUtils.isNotEmpty(reportQueryDTO.getQuery())) {
				// Default Value
				if (reportQueryDTO.getQuery().contains(":namespaceId")) {
					DBQueryParamDTO namespaceParamDTO = new DBQueryParamDTO();
					namespaceParamDTO.setParamName("namespaceId");
					namespaceParamDTO.setValue(String.valueOf(authDTO.getNamespace().getId()));
					paramList.add(namespaceParamDTO);
				}
				if (reportQueryDTO.getQuery().contains(":superNamespaceFlag")) {
					DBQueryParamDTO namespaceParamDTO = new DBQueryParamDTO();
					namespaceParamDTO.setParamName("superNamespaceFlag");
					namespaceParamDTO.setValue(ApplicationConfig.getServerZoneCode().equals(authDTO.getNamespaceCode()) ? Numeric.ONE : Numeric.ZERO);
					paramList.add(namespaceParamDTO);
				}
				if (reportQueryDTO.getQuery().contains(":loginUserId")) {
					DBQueryParamDTO userParamDTO = new DBQueryParamDTO();
					userParamDTO.setParamName("loginUserId");
					userParamDTO.setValue(String.valueOf(authDTO.getUser().getId()));
					paramList.add(userParamDTO);
				}
				if (reportQueryDTO.getQuery().contains(":userCustomerId")) {
					DBQueryParamDTO userParamDTO = new DBQueryParamDTO();
					userParamDTO.setParamName("userCustomerId");
					userParamDTO.setValue(authDTO.getUserCustomer() != null ? String.valueOf(authDTO.getUserCustomer().getId()) : Numeric.ZERO);
					paramList.add(userParamDTO);
				}
				// Date range validation as per days limit
				BitsUtil.validateDateRange(paramList, reportQueryDTO.getDaysLimit());
				list = queryService.getQueryResultsMap(authDTO, reportQueryDTO, paramList);
				reportQueryDTO.setDescription(ErrorCode.SUCCESS.getMessage());
			}
			else {
				reportQueryDTO.setDescription(ErrorCode.INVALID_CODE.getMessage());
			}
		}
		catch (ServiceException e) {
			reportQueryDTO.setDescription(e.getErrorCode() != null ? e.getErrorCode().getMessage() : Text.NA);
			throw e;
		}
		catch (Exception e) {
			reportQueryDTO.setDescription(e.getMessage());
			throw e;
		}
		finally {
			queryService.addReportQueryAuditLog(authDTO, reportQueryDTO, BitsUtil.convertParameterToString(paramList), DateUtil.getSecondsDifferent(now, DateUtil.NOW()), list != null ? list.size() : 0);
			releaseConcurrentRequests(authDTO.getNamespaceCode(), authDTO.getUserCode(), queryCode, null);
		}
		return ResponseIO.success(list);
	}

	@RequestMapping(value = "/dynamic/execute/query", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<Map<String, ?>>> executeDynamicQuery(@PathVariable("authtoken") String authtoken, String query) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<Map<String, ?>> list = new ArrayList<>();
		try {
			checkConcurrentRequests(authDTO, "sqlexecute", null);
			if (StringUtil.isNull(query)) {
				throw new ServiceException(ErrorCode.UNDEFINE_EXCEPTION, "Query cannot be null!");
			}
			StringTokenizer stringTokenizer = new StringTokenizer(query);
			if (stringTokenizer.hasMoreTokens() && !"select".equalsIgnoreCase(stringTokenizer.nextToken())) {
				throw new ServiceException(ErrorCode.UNDEFINE_EXCEPTION, "Invalid query!");
			}
			list = queryService.executeQuery(authDTO, query);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			releaseConcurrentRequests(authDTO.getNamespaceCode(), authDTO.getUserCode(), "sqlexecute", null);
		}
		return ResponseIO.success(list);
	}

	@RequestMapping(value = "/etransaction/report", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<Map<String, String>>> getETicketTransactions(@PathVariable("authtoken") String authtoken, @RequestParam(required = true) String fromDate, @RequestParam(required = true) String toDate, int travelDateFlag) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<Map<String, String>> results = null;
		DateTime now = DateUtil.NOW();

		ReportQueryDTO reportQueryDTO = new ReportQueryDTO();
		reportQueryDTO.setCode("RQKA3GN53");
		try {
			queryService.get(authDTO, reportQueryDTO);
			checkConcurrentRequests(authDTO, reportQueryDTO.getCode(), null);

			results = eTicketTransactionReportService.getETicketTransactions(authDTO, reportQueryDTO, fromDate, toDate, travelDateFlag);
		}
		catch (ServiceException e) {
			reportQueryDTO.setDescription(e.getErrorCode() != null ? e.getErrorCode().getMessage() : Text.NA);
			throw e;
		}
		catch (Exception e) {
			reportQueryDTO.setDescription(e.getMessage());
			throw e;
		}
		finally {
			queryService.addReportQueryAuditLog(authDTO, reportQueryDTO, fromDate + Text.HYPHEN + toDate + Text.HYPHEN + travelDateFlag, DateUtil.getSecondsDifferent(now, DateUtil.NOW()), results != null ? results.size() : 0);
			releaseConcurrentRequests(authDTO.getNamespaceCode(), authDTO.getUserCode(), reportQueryDTO.getCode(), null);
		}
		return ResponseIO.success(results);
	}

	@RequestMapping(value = "/branch/seat/allocation/report", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<JSONObject> getBranchBasedSeatAllocation(@PathVariable("authtoken") String authtoken, @RequestParam(required = true) String tripDate, @RequestParam(required = true) String organizationCodes, String userCodes, String routeCodes, String scheduleCodes) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		List<OrganizationDTO> organizationList = new ArrayList<>();
		if (StringUtil.isNotNull(organizationCodes)) {
			for (String organizationCode : organizationCodes.split(Text.COMMA)) {
				OrganizationDTO organizationDTO = new OrganizationDTO();
				organizationDTO.setCode(organizationCode);
				organizationList.add(organizationDTO);
			}
		}

		List<RouteDTO> routeList = new ArrayList<>();
		if (StringUtil.isNotNull(routeCodes)) {
			for (String routes : routeCodes.split(Text.COMMA)) {
				if (routes.split("\\-").length != 2 || StringUtil.isNull(routes.split("\\-")[0]) || StringUtil.isNull(routes.split("\\-")[1])) {
					continue;
				}
				StationDTO fromStation = new StationDTO();
				fromStation.setCode(routes.split("\\-")[0]);
				StationDTO toStation = new StationDTO();
				toStation.setCode(routes.split("\\-")[1]);

				RouteDTO routeDTO = new RouteDTO();
				routeDTO.setFromStation(fromStation);
				routeDTO.setToStation(toStation);
				routeList.add(routeDTO);
			}
		}
		List<ScheduleDTO> scheduleList = new ArrayList<>();
		if (StringUtil.isNotNull(scheduleCodes)) {
			for (String scheduelCode : scheduleCodes.split(Text.COMMA)) {
				if (StringUtil.isNull(scheduelCode)) {
					continue;
				}
				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setCode(scheduelCode);
				scheduleList.add(scheduleDTO);
			}
		}
		JSONObject results = seatVisibilityReportService.getBranchSeatAllocationReport(authDTO, tripDate, organizationList, routeList, scheduleList, userCodes);
		return ResponseIO.success(results);
	}

	private void validateReportingDate(List<DBQueryParamDTO> paramList, int reportingDays) {
		DateTime fromDate = null;
		for (DBQueryParamDTO queryParamDTO : paramList) {
			String paramName = queryParamDTO.getParamName();
			String value = queryParamDTO.getValue();
			if (paramName.equals("fromDate") && DateUtil.isValidDate(value)) {
				fromDate = DateUtil.getDateTime(value);
				break;
			}
		}
		if (fromDate != null && !DateUtil.isValidBeforeDate(fromDate, reportingDays)) {
			throw new ServiceException(ErrorCode.INVALID_DATE_RANGE, "From date limit should be " + reportingDays + " days before from current date");
		}
	}

	@RequestMapping(value = "/{queryCode}/organization/collection/{filterType}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<Map<String, String>>> generateBranchCollectionSummary(@PathVariable("authtoken") String authtoken, @PathVariable("queryCode") String queryCode, @PathVariable("filterType") String filterType, @RequestBody List<DBQueryParamDTO> paramList) throws Exception {
		List<Map<String, String>> list = null;
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		ReportQueryDTO reportQuery = new ReportQueryDTO();
		reportQuery.setCode(queryCode);
		queryService.get(authDTO, reportQuery);

		try {
			Map<String, String> requestMap = new HashMap<String, String>();
			for (DBQueryParamDTO queryParamDTO : paramList) {
				requestMap.put(queryParamDTO.getParamName(), queryParamDTO.getValue());
			}
			if ("DETAILS".equals(filterType)) {
				list = customReportService.generateBranchCollectionDetails(authDTO, reportQuery, requestMap);
			}
			else if ("SUMMARY".equals(filterType)) {
				list = customReportService.generateBranchCollectionSummary(authDTO, reportQuery, requestMap);
			}
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			throw e;
		}
		return ResponseIO.success(list);
	}

	@RequestMapping(value = "/user/{userCode}/specific/stationpoint/report", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<JSONArray> getUserSpecificBoardingCommissionReport(@PathVariable("authtoken") String authtoken, @PathVariable("userCode") String userCode, @RequestParam(required = true) String fromDate, @RequestParam(required = true) String toDate, @RequestParam(required = false, defaultValue = "NA") String stationCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		JSONArray response = new JSONArray();
		try {
			StationDTO stationDTO = new StationDTO();
			stationDTO.setCode(stationCode);

			UserDTO userDTO = new UserDTO();
			userDTO.setCode(userCode);

			response = customReportService.getUserSpecificBoardingCommissionReport(authDTO, userDTO, stationDTO, DateUtil.getDateTime(fromDate), DateUtil.getDateTime(toDate));
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			throw e;
		}
		return ResponseIO.success(response);
	}

	public static synchronized boolean checkConcurrentRequests(AuthDTO auth, String reportCode, String data) {
		if (ConcurrentRequests.get(auth.getNamespaceCode() + auth.getUserCode() + reportCode) != null && auth.getUser().getUserRole().getId() == UserRoleEM.USER_ROLE.getId()) {
			reportlogger.error("Error reached Max Concurrent Request RCRE01:" + auth.getNamespaceCode() + " - " + auth.getUserCode() + "-->" + reportCode);
			System.out.println(DateUtil.NOW() + " RCRE01 - " + auth.getNamespaceCode() + " - " + auth.getUserCode() + " - " + reportCode + " - reached Max Concurrent Request - " + data);
			throw new ServiceException(ErrorCode.REACHED_MAX_CONCURRENT_REQUESTS);
		}
		ConcurrentRequests.put(auth.getNamespaceCode() + auth.getUserCode() + reportCode, 1);
		return true;
	}

	public static synchronized boolean releaseConcurrentRequests(String namespaceCode, String userCode, String reportCode, String data) {
		if (ConcurrentRequests.get(namespaceCode + userCode + reportCode) != null) {
			ConcurrentRequests.remove(namespaceCode + userCode + reportCode);
		}
		return true;
	}

	@RequestMapping(value = "/export/report/details", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<ExportReportDetailsIO>> getAllExportReportDetails(@PathVariable("authtoken") String authtoken) throws Exception {
		List<ExportReportDetailsIO> list = new ArrayList<>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<ExportReportDetailsDTO> queueList = queryService.getAllExportReportDetails(authDTO);
			for (ExportReportDetailsDTO reportDetailsDTO : queueList) {
				ExportReportDetailsIO detailsIO = new ExportReportDetailsIO();
				detailsIO.setCode(reportDetailsDTO.getCode());
				detailsIO.setName(reportDetailsDTO.getName());
				detailsIO.setServiceName(reportDetailsDTO.getServiceName());
				detailsIO.setStatus(reportDetailsDTO.getStatus());
				detailsIO.setRequestedTime(DateUtil.convertDateTime(reportDetailsDTO.getRequestedTime()));
				detailsIO.setEncryptData(reportDetailsDTO.getEncryptData());
				detailsIO.setActiveFlag(reportDetailsDTO.getActiveFlag());
				list.add(detailsIO);
			}
		}
		return ResponseIO.success(list);
	}
}
