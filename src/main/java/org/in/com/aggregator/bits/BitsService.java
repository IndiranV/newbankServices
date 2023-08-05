package org.in.com.aggregator.bits;

import java.util.List;
import java.util.Map;

import org.in.com.dto.AmenitiesDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.CalendarAnnouncementDTO;
import org.in.com.dto.FareRuleDetailsDTO;
import org.in.com.dto.IntegrationDTO;
import org.in.com.dto.MenuDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.ReportQueryDTO;
import org.in.com.dto.StationAreaDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationOtaPartnerDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.NamespaceZoneEM;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public interface BitsService {
	public List<AmenitiesDTO> getZoneSyncAmenties(AuthDTO authDTO, String bitsAuthtoken, String syncDate2);

	public List<StationDTO> getZoneSyncStation(AuthDTO authDTO, String bitsAuthtoken, String syncDate);
	
	public List<StationAreaDTO> getZoneSyncStationArea(AuthDTO authDTO, String bitsAuthtoken, String syncDate);

	public List<ReportQueryDTO> getZoneSyncReportQuery(AuthDTO authDTO, String bitsAuthtoken, String syncDate);

	public List<MenuDTO> getZoneSyncMenu(AuthDTO authDTO, String bitsAuthtoken, String syncDate);

	public JSONObject getBitsConfigure(AuthDTO authDTO);

	public JSONObject getNamespaceConfigure(AuthDTO authDTO);

	public List<NamespaceDTO> getAllNamespace();

	public JSONObject getNotificationConfigure();

	public void serverPaymentResponseHandler(NamespaceZoneEM namespaceZone, Map<String, String> responseParam);
	
	public void razorpayServerPaymentResponseHandler(NamespaceZoneEM namespaceZone, JSONObject data);

	public JSONArray getReportConfig(AuthDTO authDTO);

	public List<UserDTO> getUsers(AuthDTO authDTO, IntegrationDTO integration);

	public UserDTO getAuthToken(IntegrationDTO integration, UserDTO user);

	public Map<String, String> verifyAuthToken(IntegrationDTO integrationDTO, String authToken);

	public List<NamespaceDTO> getBitsGatewayNamespace();

	public List<FareRuleDetailsDTO> syncVertexFareRule(String fareRuleCode);

	public List<FareRuleDetailsDTO> getZoneSyncFareRuleDetails(AuthDTO authDTO, String bitsAuthtoken, String fareRuleCode, String syncDate);

	public List<StationOtaPartnerDTO> getZoneSyncStationOtaPartner(AuthDTO authDTO, String bitsAuthtoken, String syncDate);
	
	public List<CalendarAnnouncementDTO> getZoneSyncCalendarAnnouncement(AuthDTO authDTO, String bitsAuthtoken, String syncDate);

}
