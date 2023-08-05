package org.in.com.service.report;

import java.util.List;
import java.util.Map;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ReportQueryDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.UserDTO;

import hirondelle.date4j.DateTime;
import net.sf.json.JSONArray;

public interface CustomReportService {

	public List<Map<String, String>> generateBranchCollectionDetails(AuthDTO authDTO, ReportQueryDTO reportQuery, Map<String, String> requestMap);

	public List<Map<String, String>> generateBranchCollectionSummary(AuthDTO authDTO, ReportQueryDTO reportQuery, Map<String, String> requestMap);
	
	public JSONArray getUserSpecificBoardingCommissionReport(AuthDTO authDTO, UserDTO userDTO, StationDTO stationDTO, DateTime fromDate, DateTime toDate);
}
