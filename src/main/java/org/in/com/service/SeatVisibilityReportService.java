package org.in.com.service;

import java.util.List;
import java.util.Map;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.SearchDTO;

import net.sf.json.JSONObject;

public interface SeatVisibilityReportService {
	public void sendOverallOccupancySummarySMS(AuthDTO authDTO);

	public List<Map<String, String>> getAllScheduleVisibility(AuthDTO authDTO, SearchDTO searchDTO);
	
	public JSONObject getBranchSeatAllocationReport(AuthDTO authDTO, String tripDate, List<OrganizationDTO> organizationList, List<RouteDTO> routeList, List<ScheduleDTO> scheduleList, String userCodes);
}
