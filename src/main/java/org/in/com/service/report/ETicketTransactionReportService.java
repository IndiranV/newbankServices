package org.in.com.service.report;

import java.util.List;
import java.util.Map;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ReportQueryDTO;

public interface ETicketTransactionReportService {
	public List<Map<String, String>> getETicketTransactions(AuthDTO authDTO, ReportQueryDTO reportQuery, String fromDate, String toDate, int travelDateFlag);
}
