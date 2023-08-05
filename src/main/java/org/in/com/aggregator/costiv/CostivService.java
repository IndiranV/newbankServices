package org.in.com.aggregator.costiv;

import java.util.Map;

import net.sf.json.JSONArray;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.TripDTO;

public interface CostivService {

	public void saveTrip(AuthDTO authDTO, TripDTO tripDTO);

	public void updateTripOdometer(AuthDTO authDTO, Map<String, String> additionalAttribute);

	public void saveTripIncomeExpense(AuthDTO authDTO, TripDTO tripDTO, Map<String, String> transactionDetails);

	public JSONArray getTripIncomeExpenses(AuthDTO authDTO, TripDTO tripDTO);

	public void saveFuelExpense(AuthDTO authDTO, TripDTO tripDTO, Map<String, String> transactionDetails);

	public JSONArray getFuelExpenses(AuthDTO authDTO, TripDTO tripDTO);

	public JSONArray getExpenseTypes(AuthDTO authDTO);

	public JSONArray getContacts(AuthDTO authDTO);

}
