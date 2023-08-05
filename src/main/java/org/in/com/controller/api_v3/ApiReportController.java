package org.in.com.controller.api_v3;

import hirondelle.date4j.DateTime;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.in.com.constants.Numeric;
import org.in.com.controller.web.BaseController;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.DBQueryParamDTO;
import org.in.com.dto.ReportQueryDTO;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/3.0/json/{operatorCode}/{username}/{apiToken}/report")
public class ApiReportController extends BaseController {

	@Autowired
	AuthService authService;
	@Autowired
	ReportQueryService queryService;

	@RequestMapping(value = "/dynamic/{reportCode}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<Map<String, ?>>> getDynamicReport(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken, @PathVariable("reportCode") String reportCode, @RequestBody List<DBQueryParamDTO> paramList) throws Exception {
		List<Map<String, ?>> list = null;
		DateTime now = DateUtil.NOW();
		ValidateMandatory(operatorCode, username, apiToken);
		AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
		if (authDTO != null) {
			ReportQueryDTO reportQueryDTO = new ReportQueryDTO();
			reportQueryDTO.setCode(reportCode);
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
						if ("fromDate".equals(paramDTO.getParamName())) {
							fromDate = new DateTime(paramDTO.getValue());
						}
						if ("toDate".equals(paramDTO.getParamName())) {
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
		}
		return ResponseIO.success(list);
	}

	private boolean ValidateMandatory(String operatorCode, String username, String apiToken) throws Exception {
		if (StringUtil.isNull(operatorCode)) {
			throw new ServiceException(ErrorCode.INVALID_NAMESPACE);
		}
		if (StringUtil.isNull(username)) {
			throw new ServiceException(ErrorCode.USER_INVALID_USERNAME);
		}
		if (StringUtil.isNull(apiToken)) {
			throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
		}
		return true;
	}

	private String getParameterToString(List<DBQueryParamDTO> paramList) {
		StringBuilder builder = new StringBuilder();
		for (DBQueryParamDTO param : paramList) {
			builder.append(" ").append(param.getParamName()).append(":").append(param.getValue());
		}
		return builder.toString();
	}
}
