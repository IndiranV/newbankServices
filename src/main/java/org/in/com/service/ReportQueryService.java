package org.in.com.service;

import java.util.List;
import java.util.Map;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.DBQueryParamDTO;
import org.in.com.dto.ExportReportDetailsDTO;
import org.in.com.dto.ReportQueryDTO;

public interface ReportQueryService extends BaseService<ReportQueryDTO> {
	public List<ReportQueryDTO> getAllforZoneSync(AuthDTO authDTO, String syncDate);

	public void addReportQueryAuditLog(AuthDTO authDTO, ReportQueryDTO reportQueryDTO, String params, int runTime, int resultRowCount);

	public List<List<String>> getQueryResults(AuthDTO authDTO, ReportQueryDTO reportQueryDTO, List<DBQueryParamDTO> params);

	public List<Map<String, ?>> getQueryResultsMap(AuthDTO authDTO, ReportQueryDTO reportQueryDTO, List<DBQueryParamDTO> params);

	public List<Map<String, ?>> executeQuery(AuthDTO authDTO, String query);

	public List<ExportReportDetailsDTO> getReportDetailsByStatus(AuthDTO authDTO, String status);

	public void updateReportDetailsStatus(AuthDTO authDTO, ExportReportDetailsDTO dto);

	public List<ExportReportDetailsDTO> getAllExportReportDetails(AuthDTO authDTO);

	public ExportReportDetailsDTO getReportDetailsByStatus(AuthDTO authDTO, String status, String reportCode);
}
