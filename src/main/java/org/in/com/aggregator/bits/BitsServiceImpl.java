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
import org.in.com.dto.enumeration.IntegrationTypeEM;
import org.in.com.dto.enumeration.NamespaceZoneEM;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
@EnableAsync
public class BitsServiceImpl implements BitsService {

	@Override
	public List<AmenitiesDTO> getZoneSyncAmenties(AuthDTO authDTO, String bitsAuthtoken, String syncDate) {
		BitsCommunicator communicator = new BitsCommunicator();
		JSONObject jsonData = communicator.getZoneSyncAmenties(bitsAuthtoken, syncDate);
		BitsDataConvertor convertor = new BitsDataConvertor();
		return convertor.getZoneSyncAmenties(jsonData);
	}

	@Override
	public List<StationDTO> getZoneSyncStation(AuthDTO authDTO, String bitsAuthtoken, String syncDate) {
		BitsCommunicator communicator = new BitsCommunicator();
		JSONObject jsonData = communicator.getZoneSyncStation(bitsAuthtoken, syncDate);
		BitsDataConvertor convertor = new BitsDataConvertor();
		return convertor.getZoneSyncStation(jsonData);
	}

	@Override
	public List<ReportQueryDTO> getZoneSyncReportQuery(AuthDTO authDTO, String bitsAuthtoken, String syncDate) {
		BitsCommunicator communicator = new BitsCommunicator();
		JSONObject jsonData = communicator.getZoneSyncReportQuery(bitsAuthtoken, syncDate);
		BitsDataConvertor convertor = new BitsDataConvertor();
		return convertor.getZoneSyncReportQuery(jsonData);
	}

	@Override
	public List<MenuDTO> getZoneSyncMenu(AuthDTO authDTO, String bitsAuthtoken, String syncDate) {
		BitsCommunicator communicator = new BitsCommunicator();
		JSONObject jsonData = communicator.getZoneSyncMenu(bitsAuthtoken, syncDate);
		BitsDataConvertor convertor = new BitsDataConvertor();
		return convertor.getZoneSyncMenu(jsonData);
	}

	@Override
	public JSONObject getBitsConfigure(AuthDTO authDTO) {
		BitsCommunicator communicator = new BitsCommunicator();
		JSONObject jsonData = communicator.getBitsConfigure(authDTO);
		BitsDataConvertor convertor = new BitsDataConvertor();
		return convertor.getBitsConfigure(jsonData);
	}

	@Override
	public JSONObject getNamespaceConfigure(AuthDTO authDTO) {
		BitsCommunicator communicator = new BitsCommunicator();
		JSONObject jsonData = communicator.getNamespaceConfigure(authDTO);
		BitsDataConvertor convertor = new BitsDataConvertor();
		return convertor.getNamespaceConfigure(authDTO, jsonData);
	}

	@Override
	public List<NamespaceDTO> getAllNamespace() {
		BitsCommunicator communicator = new BitsCommunicator();
		AuthDTO authDTO = new AuthDTO();
		JSONObject jsonData = communicator.getNamespaceConfigure(authDTO);
		BitsDataConvertor convertor = new BitsDataConvertor();
		return convertor.getAllNamespace(jsonData);
	}

	@Override
	public List<NamespaceDTO> getBitsGatewayNamespace() {
		BitsCommunicator communicator = new BitsCommunicator();
		AuthDTO authDTO = new AuthDTO();
		JSONObject jsonData = communicator.getNamespaceConfigure(authDTO);
		BitsDataConvertor convertor = new BitsDataConvertor();
		return convertor.getBitsGatewayNamespace(jsonData);
	}

	@Override
	public JSONObject getNotificationConfigure() {
		BitsCommunicator communicator = new BitsCommunicator();
		JSONObject jsonData = communicator.getNotificationConfigure();
		BitsDataConvertor convertor = new BitsDataConvertor();
		return convertor.getNotificationConfigure(jsonData);
	}

	@Override
	public void serverPaymentResponseHandler(NamespaceZoneEM namespaceZone, Map<String, String> responseParam) {
		BitsCommunicator communicator = new BitsCommunicator();
		communicator.serverPaymentResponseHandler(namespaceZone, responseParam);
	}

	@Override
	public void razorpayServerPaymentResponseHandler(NamespaceZoneEM namespaceZone, JSONObject data) {
		BitsCommunicator communicator = new BitsCommunicator();
		communicator.razorpayServerPaymentResponseHandler(namespaceZone, data);
	}

	@Override
	public JSONArray getReportConfig(AuthDTO authDTO) {
		BitsCommunicator communicator = new BitsCommunicator();
		return communicator.getReportConfigure(authDTO);
	}

	@Override
	public List<UserDTO> getUsers(AuthDTO authDTO, IntegrationDTO integration) {
		BitsCommunicator communicator = new BitsCommunicator();
		JSONObject json = communicator.getUsers(authDTO, integration);
		BitsDataConvertor convertor = new BitsDataConvertor();
		List<UserDTO> users = convertor.getUsers(json);
		return users;
	}

	@Override
	public UserDTO getAuthToken(IntegrationDTO integration, UserDTO user) {
		BitsCommunicator bitsCommunicator = new BitsCommunicator();
		JSONObject json = null;
		if (integration.getIntegrationtype().getId() == IntegrationTypeEM.COSTIV.getId() || integration.getIntegrationtype().getId() == IntegrationTypeEM.TOURONE.getId()) {
			json = bitsCommunicator.getAuthTokenV2(integration, user);
		}
		else {
			json = bitsCommunicator.getAuthToken(integration, user);
		}
		BitsDataConvertor bitsDataConvertor = new BitsDataConvertor();
		UserDTO bitsAuthDTO = bitsDataConvertor.getAuthToken(json);
		return bitsAuthDTO;
	}

	@Override
	public Map<String, String> verifyAuthToken(IntegrationDTO integrationDTO, String authToken) {
		BitsCommunicator bitsCommunicator = new BitsCommunicator();
		JSONObject json = null;
		if (integrationDTO.getIntegrationtype().getId() == IntegrationTypeEM.COSTIV.getId() || integrationDTO.getIntegrationtype().getId() == IntegrationTypeEM.TOURONE.getId()) {
			json = bitsCommunicator.getVerifyAuthTokenV2(integrationDTO, authToken);
		}
		else {
			json = bitsCommunicator.getVerifyAuthToken(integrationDTO, authToken);
		}
		BitsDataConvertor bitsDataConvertor = new BitsDataConvertor();
		Map<String, String> auth = bitsDataConvertor.getVerifyAuthToken(json);
		return auth;
	}

	@Override
	public List<FareRuleDetailsDTO> syncVertexFareRule(String fareRuleCode) {
		BitsCommunicator bitsCommunicator = new BitsCommunicator();
		JSONObject jsonObject = bitsCommunicator.syncVertexFareRule(fareRuleCode);
		BitsDataConvertor bitsDataConvertor = new BitsDataConvertor();
		return bitsDataConvertor.syncFareRule(jsonObject);
	}

	@Override
	public List<FareRuleDetailsDTO> getZoneSyncFareRuleDetails(AuthDTO authDTO, String bitsAuthtoken, String fareRuleCode, String syncDate) {
		BitsCommunicator communicator = new BitsCommunicator();
		JSONObject jsonData = communicator.getZoneSyncFareRuleDetails(bitsAuthtoken, fareRuleCode, syncDate);
		BitsDataConvertor convertor = new BitsDataConvertor();
		return convertor.getZoneSyncFareRuleDetails(jsonData);
	}

	@Override
	public List<StationOtaPartnerDTO> getZoneSyncStationOtaPartner(AuthDTO authDTO, String bitsAuthtoken, String syncDate) {
		BitsCommunicator communicator = new BitsCommunicator();
		JSONObject jsonData = communicator.getZoneSyncStationOtaPartner(bitsAuthtoken, syncDate);
		BitsDataConvertor convertor = new BitsDataConvertor();
		return convertor.getZoneSyncStationOtaPartner(jsonData);
	}

	@Override
	public List<StationAreaDTO> getZoneSyncStationArea(AuthDTO authDTO, String bitsAuthtoken, String syncDate) {
		BitsCommunicator communicator = new BitsCommunicator();
		JSONObject jsonData = communicator.getZoneSyncStationArea(bitsAuthtoken, syncDate);
		BitsDataConvertor convertor = new BitsDataConvertor();
		return convertor.getZoneSyncStationArea(jsonData);
	}

	@Override
	public List<CalendarAnnouncementDTO> getZoneSyncCalendarAnnouncement(AuthDTO authDTO, String bitsAccessToken, String syncDate) {
		BitsCommunicator communicator = new BitsCommunicator();
		JSONObject jsonData = communicator.getZoneSyncCalendarAnnouncement(bitsAccessToken, syncDate);
		BitsDataConvertor convertor = new BitsDataConvertor();
		return convertor.getCalendarAnnouncementForZoneSync(jsonData);
	}
}
