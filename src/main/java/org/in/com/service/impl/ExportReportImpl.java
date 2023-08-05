package org.in.com.service.impl;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import org.in.com.constants.Text;
import org.in.com.dao.ExportReportDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.ExportReportDTO;
import org.in.com.dto.ExportReportDetailsDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.service.ExportReportService;
import org.in.com.service.NamespaceService;
import org.in.com.service.ReportQueryService;
import org.in.com.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExportReportImpl implements ExportReportService {

	private static final String FINANCIAL_YEAR = "FINANCIAL YEAR";
	private static final String CALANDER_YEAR = "CALANDER YEAR";
	private static final String MONTHLY = "MONTHLY";

	@Autowired
	ExportReportDAO exportReportDAO;
	@Autowired
	NamespaceService namespaceService;
	@Autowired
	ReportQueryService reportQuery;

	@Override
	public void updateExportReport(AuthDTO authDTO, ExportReportDTO reportDTO) {
		NamespaceDTO namespaceDTO = null;
		List<NamespaceDTO> operatorList = new ArrayList<>();
		for (NamespaceDTO namespace : reportDTO.getNamespace()) {
			namespaceDTO = namespaceService.getNamespace(namespace.getCode());
			operatorList.add(namespaceDTO);
		}
		reportDTO.setNamespace(operatorList);
		exportReportDAO.updateExportReport(authDTO, reportDTO);
	}

	@Override
	public void updateExportReportDetails(AuthDTO authDTO, ExportReportDTO reportDTO) {
		LocalDate startDate = null;
		LocalDate endDate = null;
		LocalDate currentDate = LocalDate.now();
		ExportReportDetailsDTO detailsDTO = new ExportReportDetailsDTO();
		String name = reportDTO.getReportName();
		String reportName = name.replace("Impl", "");
		detailsDTO.setName(reportName);
		detailsDTO.setServiceName(reportDTO.getReportName());

		if (reportDTO.getFrequency().equals(FINANCIAL_YEAR)) {
			int currentYear = currentDate.getYear();
			int previousYear = currentYear - 1;
			startDate = LocalDate.of(previousYear, 4, 1);
			endDate = LocalDate.of(currentYear, 3, 31);
		}
		else if (reportDTO.getFrequency().equals(MONTHLY)) {
			LocalDate month = currentDate.minusMonths(1);
			startDate = month.with(TemporalAdjusters.firstDayOfMonth());
			endDate = month.with(TemporalAdjusters.lastDayOfMonth());
		}
		else if (reportDTO.getFrequency().equals(CALANDER_YEAR)) {
			int previousYear = currentDate.getYear() - 1;
			startDate = LocalDate.of(previousYear, 1, 1);
			endDate = LocalDate.of(previousYear, 12, 31);
		}
		String fromDate = startDate.toString();
		String toDate = endDate.toString();

		StringBuilder frequency = new StringBuilder();
		frequency.append("datePeriod");
		frequency.append(Text.COLON);
		frequency.append(reportDTO.getFrequency());
		frequency.append(Text.COMMA);
		frequency.append("fromDate");
		frequency.append(Text.COLON);
		frequency.append(fromDate);
		frequency.append(Text.COMMA);
		frequency.append("toDate");
		frequency.append(Text.COLON);
		frequency.append(toDate);
		frequency.append(Text.COMMA);
		frequency.append("reportCode");
		frequency.append(Text.COLON);
		frequency.append(reportDTO.getReportCode());
		frequency.append(Text.COMMA);
		frequency.append("filterDateType");
		frequency.append(Text.COLON);
		frequency.append(reportDTO.getFilterDateTypeFlag());
		detailsDTO.setParameter(frequency.toString());
		detailsDTO.setStatus("INITIAL");
		detailsDTO.setRequestedTime(DateUtil.NOW());
		detailsDTO.setEncryptData(Text.NA);
		detailsDTO.setActiveFlag(1);
		exportReportDAO.updateExportReportDetails(authDTO, reportDTO, detailsDTO);
	}

	@Override
	public List<ExportReportDTO> getAllExportReport(AuthDTO authDTO) {
		List<ExportReportDTO> reportList = exportReportDAO.getAllExportReport(authDTO);
		for (ExportReportDTO reportDTO : reportList) {
			for (NamespaceDTO namespace : reportDTO.getNamespace()) {
				namespaceService.getNamespace(namespace);
			}
		}
		return reportList;
	}
}
