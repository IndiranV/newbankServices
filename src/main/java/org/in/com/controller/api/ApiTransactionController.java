package org.in.com.controller.api;

import hirondelle.date4j.DateTime;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.controller.api.io.ResponseIO;
import org.in.com.controller.web.BaseController;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.DBQueryParamDTO;
import org.in.com.dto.ReportQueryDTO;
import org.in.com.dto.enumeration.TransactionTypeEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.AuthService;
import org.in.com.service.ReportQueryService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/json/{operatorCode}/{username}/{apiToken}/report")
public class ApiTransactionController extends BaseController {

	private static final String FROM_DATE = "fromDate";
	private static final String TO_DATE = "toDate";
	private static final String USER_CODE = "userCode";

	private static final String USER_BALANCE_REPORT_CODE = "RQF22D17";
	private static final String TRIP_REVENUE_REPORT_CODE = "RQF9H1363";
	private static final String RECHARGE_REPORT_CODE = "RQFATL54";
	private static final String VOUCHER_REPORT_CODE = "RQFATL54";

	@Autowired
	AuthService authService;
	@Autowired
	ReportQueryService queryService;

	@RequestMapping(value = "/user/balance/{transactionDate}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<Map<String, ?>>> getUserBalance(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken, @PathVariable("transactionDate") String transactionDate) throws Exception {
		AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
		if (!DateUtil.isValidDate(transactionDate)) {
			throw new ServiceException(ErrorCode.INVALID_DATE);
		}

		// Validate Date Range
		validatePreviousDate(DateUtil.getDateTime(transactionDate), 31);

		ReportQueryDTO reportQueryDTO = new ReportQueryDTO();
		reportQueryDTO.setCode(USER_BALANCE_REPORT_CODE);

		List<DBQueryParamDTO> paramList = new ArrayList<DBQueryParamDTO>();
		DBQueryParamDTO paramDTO = new DBQueryParamDTO();
		paramDTO.setParamName("transactionDate");
		paramDTO.setValue(transactionDate);
		paramList.add(paramDTO);

		List<Map<String, ?>> list = getDynamicReport(authDTO, reportQueryDTO, paramList);
		return ResponseIO.success(list);
	}

	@RequestMapping(value = "/trip/revenue/{fromDate}/{toDate}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<Map<String, ?>>> getTripRevenueDetails(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken, @PathVariable(FROM_DATE) String fromDate, @PathVariable(TO_DATE) String toDate) throws Exception {
		AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
		if (!DateUtil.isValidDate(fromDate) || !DateUtil.isValidDate(toDate)) {
			throw new ServiceException(ErrorCode.INVALID_DATE);
		}
		// Validate Date Range
		validatePreviousDate(DateUtil.getDateTime(fromDate), 93);

		ReportQueryDTO reportQueryDTO = new ReportQueryDTO();
		reportQueryDTO.setCode(TRIP_REVENUE_REPORT_CODE);

		List<DBQueryParamDTO> paramList = new ArrayList<DBQueryParamDTO>();
		DBQueryParamDTO paramDTO = new DBQueryParamDTO();
		paramDTO.setParamName(FROM_DATE);
		paramDTO.setValue(fromDate);
		paramList.add(paramDTO);

		paramDTO = new DBQueryParamDTO();
		paramDTO.setParamName(TO_DATE);
		paramDTO.setValue(toDate);
		paramList.add(paramDTO);

		paramDTO = new DBQueryParamDTO();
		paramDTO.setParamName(USER_CODE);
		paramDTO.setValue(Text.NA);
		paramList.add(paramDTO);

		paramDTO = new DBQueryParamDTO();
		paramDTO.setParamName("travelDateFlag");
		paramDTO.setValue(Numeric.ONE);
		paramList.add(paramDTO);

		paramDTO = new DBQueryParamDTO();
		paramDTO.setParamName("scheduleCode");
		paramDTO.setValue(Text.NA);
		paramList.add(paramDTO);

		paramDTO = new DBQueryParamDTO();
		paramDTO.setParamName("tagCode");
		paramDTO.setValue(Text.NA);
		paramList.add(paramDTO);

		List<Map<String, ?>> list = getDynamicReport(authDTO, reportQueryDTO, paramList);
		return ResponseIO.success(list);
	}

	@RequestMapping(value = "/recharge/{fromDate}/{toDate}/{acknowledgementStatus}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<Map<String, ?>>> getRechargeDetails(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken, @PathVariable(FROM_DATE) String fromDate, @PathVariable(TO_DATE) String toDate, @PathVariable("acknowledgementStatus") String acknowledgementStatus) throws Exception {
		AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
		if (!DateUtil.isValidDate(fromDate) || !DateUtil.isValidDate(toDate)) {
			throw new ServiceException(ErrorCode.INVALID_DATE);
		}
		// Validate Date Range
		validatePreviousDate(DateUtil.getDateTime(fromDate), 93);

		ReportQueryDTO reportQueryDTO = new ReportQueryDTO();
		reportQueryDTO.setCode(RECHARGE_REPORT_CODE);

		List<DBQueryParamDTO> paramList = new ArrayList<>();
		DBQueryParamDTO paramDTO = new DBQueryParamDTO();
		paramDTO.setParamName(FROM_DATE);
		paramDTO.setValue(fromDate);
		paramList.add(paramDTO);

		paramDTO = new DBQueryParamDTO();
		paramDTO.setParamName(TO_DATE);
		paramDTO.setValue(toDate);
		paramList.add(paramDTO);

		paramDTO = new DBQueryParamDTO();
		paramDTO.setParamName(USER_CODE);
		paramDTO.setValue(Text.NA);
		paramList.add(paramDTO);

		paramDTO = new DBQueryParamDTO();
		paramDTO.setParamName("transactionType");
		paramDTO.setValue(TransactionTypeEM.RECHARGE.getCode());
		paramList.add(paramDTO);

		paramDTO = new DBQueryParamDTO();
		paramDTO.setParamName("transactionMode");
		paramDTO.setValue(Text.NA);
		paramList.add(paramDTO);

		paramDTO = new DBQueryParamDTO();
		paramDTO.setParamName("acknowledmentStatus");
		paramDTO.setValue(StringUtil.isNull(acknowledgementStatus) || acknowledgementStatus.equals("ALL") ? Text.NA : acknowledgementStatus);
		paramList.add(paramDTO);

		List<Map<String, ?>> list = getDynamicReport(authDTO, reportQueryDTO, paramList);
		return ResponseIO.success(list);
	}

	@RequestMapping(value = "/voucher/{fromDate}/{toDate}/{acknowledgementStatus}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<Map<String, ?>>> getVoucherDetails(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken, @PathVariable(FROM_DATE) String fromDate, @PathVariable(TO_DATE) String toDate, @PathVariable("acknowledgementStatus") String acknowledgementStatus) throws Exception {
		AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
		if (!DateUtil.isValidDate(fromDate) || !DateUtil.isValidDate(toDate)) {
			throw new ServiceException(ErrorCode.INVALID_DATE);
		}
		// Validate Date Range
		validatePreviousDate(DateUtil.getDateTime(fromDate), 93);

		ReportQueryDTO reportQueryDTO = new ReportQueryDTO();
		reportQueryDTO.setCode(VOUCHER_REPORT_CODE);

		List<DBQueryParamDTO> paramList = new ArrayList<>();
		DBQueryParamDTO paramDTO = new DBQueryParamDTO();
		paramDTO.setParamName(FROM_DATE);
		paramDTO.setValue(fromDate);
		paramList.add(paramDTO);

		paramDTO = new DBQueryParamDTO();
		paramDTO.setParamName(TO_DATE);
		paramDTO.setValue(toDate);
		paramList.add(paramDTO);

		paramDTO = new DBQueryParamDTO();
		paramDTO.setParamName(USER_CODE);
		paramDTO.setValue(Text.NA);
		paramList.add(paramDTO);

		paramDTO = new DBQueryParamDTO();
		paramDTO.setParamName("transactionType");
		paramDTO.setValue(TransactionTypeEM.PAYMENT_VOUCHER.getCode());
		paramList.add(paramDTO);

		paramDTO = new DBQueryParamDTO();
		paramDTO.setParamName("transactionMode");
		paramDTO.setValue(Text.NA);
		paramList.add(paramDTO);

		paramDTO = new DBQueryParamDTO();
		paramDTO.setParamName("acknowledmentStatus");
		paramDTO.setValue(StringUtil.isNull(acknowledgementStatus) || acknowledgementStatus.equals("ALL") ? Text.NA : acknowledgementStatus);
		paramList.add(paramDTO);

		List<Map<String, ?>> list = getDynamicReport(authDTO, reportQueryDTO, paramList);
		return ResponseIO.success(list);
	}

	private List<Map<String, ?>> getDynamicReport(AuthDTO authDTO, ReportQueryDTO reportQueryDTO, List<DBQueryParamDTO> paramList) throws Exception {
		List<Map<String, ?>> list = new ArrayList<Map<String, ?>>();
		DateTime now = DateUtil.NOW();
		queryService.get(authDTO, reportQueryDTO);
		try {
			if (StringUtils.isNotEmpty(reportQueryDTO.getQuery())) {
				// Default Value
				if (reportQueryDTO.getQuery().contains(":namespaceId")) {
					DBQueryParamDTO namespaceParamDTO = new DBQueryParamDTO();
					namespaceParamDTO.setParamName("namespaceId");
					namespaceParamDTO.setValue(String.valueOf(authDTO.getNamespace().getId()));
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
				DateTime fromDate = null;
				DateTime toDate = null;
				for (Iterator<DBQueryParamDTO> iterator = paramList.iterator(); iterator.hasNext();) {
					DBQueryParamDTO paramDTO = iterator.next();
					if (FROM_DATE.equals(paramDTO.getParamName())) {
						fromDate = new DateTime(paramDTO.getValue());
					}
					if (TO_DATE.equals(paramDTO.getParamName())) {
						toDate = new DateTime(paramDTO.getValue());
					}
					if (!reportQueryDTO.getQuery().contains(paramDTO.getParamName())) {
						iterator.remove();
					}
				}
				if (fromDate != null && toDate != null && DateUtil.getDayDifferent(fromDate, toDate) > 30) {
					throw new ServiceException(ErrorCode.INVALID_DATE_RANGE);
				}

				list = queryService.getQueryResultsMap(authDTO, reportQueryDTO, paramList);
				reportQueryDTO.setDescription(ErrorCode.SUCCESS.getMessage());
			}
			else {
				reportQueryDTO.setDescription(ErrorCode.INVALID_CODE.getMessage());
			}
		}
		catch (Exception e) {
			reportQueryDTO.setDescription(e.getMessage());
			throw e;
		}
		finally {
			queryService.addReportQueryAuditLog(authDTO, reportQueryDTO, getParameterToString(paramList), DateUtil.getSecondsDifferent(now, DateUtil.NOW()), list.size());
		}
		return list;
	}

	private String getParameterToString(List<DBQueryParamDTO> paramList) {
		StringBuilder builder = new StringBuilder();
		for (DBQueryParamDTO param : paramList) {
			builder.append(Text.SINGLE_SPACE).append(param.getParamName()).append(Text.COLON).append(param.getValue());
		}
		return builder.toString();
	}

	private void validatePreviousDate(DateTime datetime, int days) {
		if (DateUtil.getDayDifferent(datetime, DateUtil.NOW()) > days) {
			throw new ServiceException(ErrorCode.INVALID_DATE_RANGE, "Last " + days + " days data only available!");
		}
	}
}
