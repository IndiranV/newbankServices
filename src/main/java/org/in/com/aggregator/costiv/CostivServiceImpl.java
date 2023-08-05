package org.in.com.aggregator.costiv;

import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.IntegrationDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.enumeration.IntegrationTypeEM;
import org.in.com.service.IntegrationService;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@Service
@EnableAsync
public class CostivServiceImpl implements CostivService {
	@Autowired
	IntegrationService integrationService;

	public void saveTrip(AuthDTO authDTO, TripDTO tripDTO) {
		try {
			IntegrationDTO integration = integrationService.getIntegration(authDTO, IntegrationTypeEM.COSTIV, Text.NA);
			if (integration != null && StringUtil.isNotNull(integration.getAccount()) && StringUtil.isNotNull(integration.getAccessUrl())) {
				CostivCommunicator communicator = new CostivCommunicator();
				communicator.saveTrip(authDTO, tripDTO, integration);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Async
	public void updateTripOdometer(AuthDTO authDTO, Map<String, String> additionalAttribute) {
		IntegrationDTO integration = integrationService.getIntegration(authDTO, IntegrationTypeEM.COSTIV, Text.NA);
		if (integration != null && StringUtil.isNotNull(integration.getAccount()) && StringUtil.isNotNull(integration.getAccessUrl())) {
			CostivCommunicator communicator = new CostivCommunicator();
			communicator.updateTripOdometer(authDTO, integration, additionalAttribute);
		}
	}

	@Override
	public void saveTripIncomeExpense(AuthDTO authDTO, TripDTO tripDTO, Map<String, String> transactionDetails) {
		IntegrationDTO integration = integrationService.getIntegration(authDTO, IntegrationTypeEM.COSTIV, Text.NA);
		if (integration != null && StringUtil.isNotNull(integration.getAccount()) && StringUtil.isNotNull(integration.getAccessUrl())) {
			CostivCommunicator communicator = new CostivCommunicator();
			JSONObject responseJSON = communicator.saveTripIncomeExpense(authDTO, tripDTO, integration, transactionDetails);
			if (responseJSON != null && responseJSON.has("data") && responseJSON.getJSONObject("data") != null) {
				transactionDetails.put("transactionCode", responseJSON.getJSONObject("data").has("code") ? responseJSON.getJSONObject("data").getString("code") : Text.NA);
			}
		}
	}

	@Override
	public JSONArray getTripIncomeExpenses(AuthDTO authDTO, TripDTO tripDTO) {
		IntegrationDTO integration = integrationService.getIntegration(authDTO, IntegrationTypeEM.COSTIV, Text.NA);
		CostivCommunicator communicator = new CostivCommunicator();
		JSONObject incomeExpenseJSON = communicator.getTripIncomeExpenses(authDTO, tripDTO, integration);
		JSONArray incomeExpenses = incomeExpenseJSON != null ? incomeExpenseJSON.getJSONArray("data") : new JSONArray();

		JSONObject responseJSON = communicator.getFuelExpense(authDTO, tripDTO, integration);
		JSONArray fuelResponse = responseJSON != null ? responseJSON.getJSONArray("data") : new JSONArray();
		for (Object object : fuelResponse) {
			JSONObject jsonObject = (JSONObject) object;
			jsonObject.put("isFuel", Text.TRUE);
			incomeExpenses.add(jsonObject);
		}
		return incomeExpenses;
	}

	@Override
	public void saveFuelExpense(AuthDTO authDTO, TripDTO tripDTO, Map<String, String> transactionDetails) {
		IntegrationDTO integration = integrationService.getIntegration(authDTO, IntegrationTypeEM.COSTIV, Text.NA);
		if (integration != null && StringUtil.isNotNull(integration.getAccount()) && StringUtil.isNotNull(integration.getAccessUrl())) {
			CostivCommunicator communicator = new CostivCommunicator();
			JSONObject responseJSON = communicator.saveFuelExpense(authDTO, tripDTO, integration, transactionDetails);
			if (responseJSON != null && responseJSON.has("data") && responseJSON.getJSONObject("data") != null) {
				transactionDetails.put("transactionCode", responseJSON.getJSONObject("data").has("code") ? responseJSON.getJSONObject("data").getString("code") : Text.NA);
			}
		}
	}

	@Override
	public JSONArray getFuelExpenses(AuthDTO authDTO, TripDTO tripDTO) {
		IntegrationDTO integration = integrationService.getIntegration(authDTO, IntegrationTypeEM.COSTIV, Text.NA);
		CostivCommunicator communicator = new CostivCommunicator();
		JSONObject responseJSON = communicator.getFuelExpense(authDTO, tripDTO, integration);
		return responseJSON != null ? responseJSON.getJSONArray("data") : null;
	}

	@Override
	public JSONArray getExpenseTypes(AuthDTO authDTO) {
		IntegrationDTO integration = integrationService.getIntegration(authDTO, IntegrationTypeEM.COSTIV, Text.NA);
		CostivCommunicator communicator = new CostivCommunicator();
		JSONObject responseJSON = communicator.getExpenseTypes(authDTO, integration);
		return responseJSON != null ? responseJSON.getJSONArray("data") : null;
	}

	@Override
	public JSONArray getContacts(AuthDTO authDTO) {
		IntegrationDTO integration = integrationService.getIntegration(authDTO, IntegrationTypeEM.COSTIV, Text.NA);
		CostivCommunicator communicator = new CostivCommunicator();
		JSONObject responseJSON = communicator.getContacts(authDTO, integration);
		return responseJSON != null ? responseJSON.getJSONArray("data") : null;
	}
}
