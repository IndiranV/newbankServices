package org.in.com.service.report;

import org.in.com.dto.AuthDTO;

import net.sf.json.JSONObject;

public interface ExportReportService {

	public void exportReport(AuthDTO authDTO, JSONObject json);

	public void exportReportV2(AuthDTO authDTO);
}
