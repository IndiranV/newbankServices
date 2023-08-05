package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ExportReportDTO;

public interface ExportReportService {

	public void updateExportReport(AuthDTO authDTO, ExportReportDTO reportDTO);

	public List<ExportReportDTO> getAllExportReport(AuthDTO authDTO);

	public void updateExportReportDetails(AuthDTO authDTO, ExportReportDTO reportDTO);

}
