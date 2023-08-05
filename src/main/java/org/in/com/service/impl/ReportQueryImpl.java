package org.in.com.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.in.com.config.ApplicationConfig;
import org.in.com.constants.Constants;
import org.in.com.dao.ReportQueryDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.DBQueryParamDTO;
import org.in.com.dto.ExportReportDetailsDTO;
import org.in.com.dto.ReportQueryDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.ReportQueryService;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;

@Service
public class ReportQueryImpl implements ReportQueryService {
	private static final Logger reportlogger = LoggerFactory.getLogger("org.in.com.controller.report");

	public List<ReportQueryDTO> get(AuthDTO authDTO, ReportQueryDTO dto) {
		ReportQueryDAO queryDAO = new ReportQueryDAO();
		queryDAO.get(authDTO, dto);
		if (dto.getId() == 0) {
			throw new ServiceException(ErrorCode.INVALID_CODE, "Report code not found");
		}
		return null;
	}

	public List<ReportQueryDTO> getAllforZoneSync(AuthDTO authDTO, String syncDate) {
		ReportQueryDAO queryDAO = new ReportQueryDAO();
		return queryDAO.getAllforZoneSync(authDTO, syncDate);
	}

	public List<ReportQueryDTO> getAll(AuthDTO authDTO) {
		ReportQueryDAO queryDAO = new ReportQueryDAO();
		return queryDAO.getAll(authDTO);
	}

	public ReportQueryDTO Update(AuthDTO authDTO, ReportQueryDTO reportQueryDTO) {
		if (!ArrayUtils.contains(Constants.SUPER_REGIONS_ZONE, ApplicationConfig.getServerZoneCode())) {
			throw new ServiceException(ErrorCode.INVALID_APPLICATION_ZONE);
		}
		ReportQueryDAO queryDAO = new ReportQueryDAO();
		queryDAO.getUpdate(authDTO, reportQueryDTO);
		return null;
	}

	public List<List<String>> getQueryResults(AuthDTO authDTO, ReportQueryDTO reportQueryDTO, List<DBQueryParamDTO> params) {
		ReportQueryDAO reportDAO = new ReportQueryDAO();
		return reportDAO.getQueryResults(reportQueryDTO.getQuery(), params);
	}

	public List<Map<String, ?>> getQueryResultsMap(AuthDTO authDTO, ReportQueryDTO reportQueryDTO, List<DBQueryParamDTO> params) {
		reportlogger.info("RPTMAP {} {} {} {} {} Start\n{}", authDTO.getNamespaceCode(), authDTO.getUser().getUsername(), authDTO.getUser().getName(), reportQueryDTO.getCode(), reportQueryDTO.getName(), Lists.transform(params, Functions.toStringFunction()));
		ReportQueryDAO reportDAO = new ReportQueryDAO();
		List<Map<String, ?>> listMapData = reportDAO.getQueryResultsMap(reportQueryDTO.getQuery(), params);
		reportlogger.info("RPTMAP {} {} {} {} {} - {} End", authDTO.getNamespaceCode(), authDTO.getUser().getUsername(), authDTO.getUser().getName(), reportQueryDTO.getCode(), reportQueryDTO.getName(), listMapData.size());
		return listMapData;
	}

	@Async
	public void addReportQueryAuditLog(AuthDTO authDTO, ReportQueryDTO reportQueryDTO, String params, int runTime, int resultRowCount) {
		ReportQueryDAO reportDAO = new ReportQueryDAO();
		reportDAO.addReportQueryAuditLog(authDTO, reportQueryDTO, params, runTime, resultRowCount);
	}

	@Override
	public List<Map<String, ?>> executeQuery(AuthDTO authDTO, String query) {
		reportlogger.info("RPT01 {} {} {}", authDTO.getUser().getUsername(), authDTO.getUser().getName(), query);
		if (StringUtil.isNull(query) || query.toLowerCase().contains("update ") || query.toLowerCase().contains("delete ") || query.toLowerCase().contains("insert ") || query.toLowerCase().contains("drop ") || query.toLowerCase().contains("truncate ")) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED);
		}
		ReportQueryDAO queryDAO = new ReportQueryDAO();
		return queryDAO.executeDynamicQuery(authDTO, query);
	}

	@Override
	public List<ExportReportDetailsDTO> getReportDetailsByStatus(AuthDTO authDTO, String status) {
		ReportQueryDAO queryDAO = new ReportQueryDAO();
		return queryDAO.getReportDetailsByStatus(authDTO, status);
	}

	@Override
	public void updateReportDetailsStatus(AuthDTO authDTO, ExportReportDetailsDTO dto) {
		ReportQueryDAO queryDAO = new ReportQueryDAO();
		queryDAO.updateReportDetailsStatus(authDTO, dto);
	}

	@Override
	public List<ExportReportDetailsDTO> getAllExportReportDetails(AuthDTO authDTO) {
		ReportQueryDAO queryDAO = new ReportQueryDAO();
		return queryDAO.getAllExportReportDetails(authDTO);
	}

	@Override
	public ExportReportDetailsDTO getReportDetailsByStatus(AuthDTO authDTO, String status, String reportCode) {
		ReportQueryDAO queryDAO = new ReportQueryDAO();
		return queryDAO.getReportDetailsByStatus(authDTO, status, reportCode);
	}

}
