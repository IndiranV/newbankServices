package org.in.com.aggregator.costiv;

import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.in.com.constants.Numeric;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.IntegrationDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.TripDTO;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.HttpServiceClient;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CostivCommunicator {
	private static final Logger LOGGER = LoggerFactory.getLogger("costivLog");

	public void saveTrip(AuthDTO authDTO, TripDTO tripDTO, IntegrationDTO integration) {
		try {
			LOGGER.info("Costiv save the trip process begin..");
			String url = integration.getAccessUrl() + "/internal/bits/trip/details/add";
			LOGGER.info("URL {}", url);

			JSONObject trip = new JSONObject();
			trip.put("code", tripDTO.getCode());
			trip.put("name", tripDTO.getSchedule().getName());
			trip.put("serviceNumber", tripDTO.getSchedule().getServiceNumber() + "|" + tripDTO.getSchedule().getCode());
			trip.put("namespaceCode", integration.getAccount());

			ScheduleStationDTO fromStationDTO = BitsUtil.getOriginStation(tripDTO.getStationList());
			ScheduleStationDTO toStationDTO = BitsUtil.getDestinationStation(tripDTO.getStationList());

			JSONObject fromStation = new JSONObject();
			fromStation.put("code", fromStationDTO.getStation().getCode());
			fromStation.put("dateTime", DateUtil.convertDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), fromStationDTO.getMinitues())));
			trip.put("fromStation", fromStation);

			JSONObject toStation = new JSONObject();
			toStation.put("code", toStationDTO.getStation().getCode());
			toStation.put("dateTime", DateUtil.convertDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), toStationDTO.getMinitues())));
			trip.put("toStation", toStation);

			trip.put("tripDate", DateUtil.convertDate(tripDTO.getTripDate()));

			JSONObject primaryDriverContact = new JSONObject();
			primaryDriverContact.put("code", tripDTO.getTripInfo().getPrimaryDriver().getCode());

			JSONObject secondaryDriverContact = new JSONObject();
			secondaryDriverContact.put("code", tripDTO.getTripInfo().getSecondaryDriver().getCode());

			JSONObject attendantContact = new JSONObject();
			attendantContact.put("code", tripDTO.getTripInfo().getAttendant().getCode());

			JSONArray contactList = new JSONArray();
			contactList.add(primaryDriverContact);
			contactList.add(secondaryDriverContact);
			contactList.add(attendantContact);

			trip.put("contactList", contactList);

			JSONObject vehicle = new JSONObject();
			vehicle.put("name", tripDTO.getTripInfo().getBusVehicle().getName());
			vehicle.put("registrationNumber", tripDTO.getTripInfo().getBusVehicle().getRegistationNumber());
			vehicle.put("registrationDate", tripDTO.getTripInfo().getBusVehicle().getRegistrationDate());
			vehicle.put("licNumber", tripDTO.getTripInfo().getBusVehicle().getLicNumber());

			JSONObject vehicleType = new JSONObject();
			vehicleType.put("code", tripDTO.getTripInfo().getBusVehicle().getVehicleType().getCode());
			vehicle.put("vehicleType", vehicleType);

			vehicle.put("vehicleTag", "BITS");
			vehicle.put("climatize", 0);
			vehicle.put("displayName", tripDTO.getTripInfo().getBusVehicle().getName());
			vehicle.put("seatCount", tripDTO.getTripInfo().getBusVehicle().getBus().getSeatCount());
			trip.put("vehicle", vehicle);
			LOGGER.info("Request {}", trip);

			HttpServiceClient httpClient = new HttpServiceClient();
			httpClient.post(url, trip.toString(), "application/json");
		}
		catch (Exception e) {
			LOGGER.info("", e);
			e.printStackTrace();
		}
		LOGGER.info("Costiv save the trip process end..");
	}

	public void updateTripOdometer(AuthDTO authDTO, IntegrationDTO integration, Map<String, String> additionalAttribute) {
		try {
			LOGGER.info("Costiv update the odometer process begin..");
			String url = integration.getAccessUrl() + "/internal/bits/odometer/update?actionType=" + additionalAttribute.get("actionType");
			LOGGER.info("URL {}", url);

			JSONObject tripDetails = new JSONObject();
			tripDetails.put("code", additionalAttribute.get("tripCode"));
			tripDetails.put("namespaceCode", integration.getAccount());
			tripDetails.put("startOdometer", additionalAttribute.get("startOdometer"));
			tripDetails.put("endOdometer", additionalAttribute.get("endOdometer"));
			tripDetails.put("actualStartDateTime", additionalAttribute.get("dateTime"));
			tripDetails.put("actualEndDateTime", additionalAttribute.get("dateTime"));

			JSONArray images = new JSONArray();
			if (StringUtil.isNotNull(additionalAttribute.get("driverImage"))) {
				JSONObject driverImage = new JSONObject();
				driverImage.put("imageUrl", additionalAttribute.get("driverImage"));
				if ("START".equals(additionalAttribute.get("actionType"))) {
					driverImage.put("attribute", "Trip Start Driver");
				}
				else {
					driverImage.put("attribute", "Trip End Driver");
				}
				images.add(driverImage);
			}
			if (StringUtil.isNotNull(additionalAttribute.get("startOdometerImage"))) {
				JSONObject startOdometerImage = new JSONObject();
				startOdometerImage.put("imageUrl", additionalAttribute.get("startOdometerImage"));
				startOdometerImage.put("attribute", "Start Odometer");
				images.add(startOdometerImage);
			}
			if (StringUtil.isNotNull(additionalAttribute.get("endOdometerImage"))) {
				JSONObject endOdometerImage = new JSONObject();
				endOdometerImage.put("imageUrl", additionalAttribute.get("endOdometerImage"));
				endOdometerImage.put("attribute", "End Odometer");
				images.add(endOdometerImage);
			}
			tripDetails.put("images", images);

			LOGGER.info("Request {}", tripDetails);

			HttpServiceClient httpClient = new HttpServiceClient();
			httpClient.post(url, tripDetails.toString(), "application/json");
		}
		catch (Exception e) {
			LOGGER.info("", e);
			e.printStackTrace();
		}
		LOGGER.info("Costiv update the odometer process end..");
	}

	public JSONObject saveTripIncomeExpense(AuthDTO authDTO, TripDTO tripDTO, IntegrationDTO integration, Map<String, String> transactionDetails) {
		JSONObject responseJSON = null;
		try {
			LOGGER.info("Costiv save the trip income/expense begin..");
			String url = integration.getAccessUrl() + "/internal/bits/trip/income/expense/update";
			LOGGER.info("URL {}", url);

			JSONObject tripTransaction = new JSONObject();
			tripTransaction.put("code", transactionDetails.get("transactionCode"));
			tripTransaction.put("namespaceCode", integration.getAccount());
			tripTransaction.put("bitsTripCode", tripDTO.getCode());
			tripTransaction.put("amount", transactionDetails.get("amount"));
			tripTransaction.put("remarks", transactionDetails.get("remarks"));
			tripTransaction.put("tripDate", DateUtil.convertDate(tripDTO.getTripDate()));
			tripTransaction.put("activeFlag", Numeric.ONE_INT);

			JSONObject paymentBy = new JSONObject();
			paymentBy.put("code", transactionDetails.get("paymentContact"));
			tripTransaction.put("paymentContact", paymentBy);

			JSONObject paymentTo = new JSONObject();
			paymentTo.put("code", transactionDetails.get("vendorContact"));
			tripTransaction.put("vendorContact", paymentTo);

			JSONObject expenseType = new JSONObject();
			expenseType.put("code", transactionDetails.get("expenseType"));
			tripTransaction.put("expenseType", expenseType);

			JSONObject paymentMode = new JSONObject();
			paymentMode.put("code", "CASH");
			tripTransaction.put("paymentMode", paymentMode);
			LOGGER.info("Request {}", tripTransaction);

			HttpServiceClient httpClient = new HttpServiceClient();
			String response = httpClient.post(url, tripTransaction.toString(), "application/json");
			responseJSON = JSONObject.fromObject(response);
			LOGGER.info("Response {}", responseJSON);
		}
		catch (Exception e) {
			LOGGER.info("", e);
			e.printStackTrace();
		}
		LOGGER.info("Costiv save the trip income/expense end..");
		return responseJSON;
	}

	public JSONObject getTripIncomeExpenses(AuthDTO authDTO, TripDTO tripDTO, IntegrationDTO integration) {
		JSONObject responseJSON = null;
		try {
			LOGGER.info("Costiv get the trip income/expense begin..");
			String url = integration.getAccessUrl() + "/internal/bits/trip/income/expenses";
			LOGGER.info("URL {}", url);

			JSONObject tripTransaction = new JSONObject();
			tripTransaction.put("namespaceCode", integration.getAccount());
			tripTransaction.put("bitsTripCode", tripDTO.getCode());
			LOGGER.info("Request {}", tripTransaction);

			HttpServiceClient httpClient = new HttpServiceClient();
			String response = httpClient.post(url, tripTransaction.toString(), "application/json");
			responseJSON = JSONObject.fromObject(response);
			LOGGER.info("Response {}", responseJSON);
		}
		catch (Exception e) {
			LOGGER.info("", e);
			e.printStackTrace();
		}
		LOGGER.info("Costiv get the trip income/expense end..");
		return responseJSON;
	}

	public JSONObject saveFuelExpense(AuthDTO authDTO, TripDTO tripDTO, IntegrationDTO integration, Map<String, String> transactionDetails) {
		JSONObject responseJSON = null;
		try {
			LOGGER.info("Costiv save the fuel expense begin..");
			String url = integration.getAccessUrl() + "/internal/bits/fuel/expense/update";
			LOGGER.info("URL {}", url);

			JSONObject fuelExpense = new JSONObject();
			fuelExpense.put("code", transactionDetails.get("transactionCode"));
			fuelExpense.put("namespaceCode", integration.getAccount());
			fuelExpense.put("litres", transactionDetails.get("litres"));
			fuelExpense.put("pricePerLitre", transactionDetails.get("pricePerLitre"));
			fuelExpense.put("billNumber", transactionDetails.get("billNumber"));
			fuelExpense.put("odometer", transactionDetails.get("odometer"));
			fuelExpense.put("remarks", transactionDetails.get("remarks"));
			fuelExpense.put("activeFlag", Numeric.ONE_INT);

			JSONObject paymentBy = new JSONObject();
			paymentBy.put("code", transactionDetails.get("paymentContact"));
			fuelExpense.put("paymentContact", paymentBy);

			JSONObject vehicle = new JSONObject();
			vehicle.put("registrationNumber", tripDTO.getTripInfo().getBusVehicle().getRegistationNumber());
			fuelExpense.put("vehicle", vehicle);

			JSONObject busTripDetails = new JSONObject();
			busTripDetails.put("code", tripDTO.getCode());
			fuelExpense.put("busTripDetails", busTripDetails);

			JSONObject paymentMode = new JSONObject();
			paymentMode.put("code", "CASH");
			fuelExpense.put("paymentMode", paymentMode);
			LOGGER.info("Request {}", fuelExpense);

			HttpServiceClient httpClient = new HttpServiceClient();
			String response = httpClient.post(url, fuelExpense.toString(), "application/json");
			responseJSON = JSONObject.fromObject(response);
			LOGGER.info("Response {}", responseJSON);
		}
		catch (Exception e) {
			LOGGER.info("", e);
			e.printStackTrace();
		}
		LOGGER.info("Costiv save the fuel expense end..");
		return responseJSON;
	}

	public JSONObject getFuelExpense(AuthDTO authDTO, TripDTO tripDTO, IntegrationDTO integration) {
		JSONObject responseJson = null;
		try {
			LOGGER.info("Costiv get the fuel expense begin..");
			String url = integration.getAccessUrl() + "/internal/bits/fuel/expenses";
			LOGGER.info("URL {}", url);

			JSONObject fuelExpense = new JSONObject();
			fuelExpense.put("namespaceCode", integration.getAccount());

			JSONObject busTripDetails = new JSONObject();
			busTripDetails.put("code", tripDTO.getCode());
			fuelExpense.put("busTripDetails", busTripDetails);
			LOGGER.info("Request {}", fuelExpense);

			HttpServiceClient httpClient = new HttpServiceClient();
			String response = httpClient.post(url, fuelExpense.toString(), "application/json");
			responseJson = JSONObject.fromObject(response);
			LOGGER.info("Response {}", responseJson);
		}
		catch (Exception e) {
			LOGGER.info("", e);
			e.printStackTrace();
		}
		LOGGER.info("Costiv get the fuel expense end..");
		return responseJson;
	}

	public JSONObject getExpenseTypes(AuthDTO authDTO, IntegrationDTO integration) {
		JSONObject responseJson = null;
		try {
			LOGGER.info("Costiv get the expense types begin..");
			String url = integration.getAccessUrl() + "/internal/bits/expense/types";
			LOGGER.info("URL {}", url);

			JSONObject fuelExpense = new JSONObject();
			fuelExpense.put("namespaceCode", integration.getAccount());
			LOGGER.info("Request {}", fuelExpense);

			HttpServiceClient httpClient = new HttpServiceClient();
			String response = httpClient.post(url, fuelExpense.toString(), "application/json");
			responseJson = JSONObject.fromObject(response);
			LOGGER.info("Response {}", responseJson);
		}
		catch (Exception e) {
			LOGGER.info("", e);
			e.printStackTrace();
		}
		LOGGER.info("Costiv get the expense types end..");
		return responseJson;
	}

	public JSONObject getContacts(AuthDTO authDTO, IntegrationDTO integration) {
		JSONObject responseJson = null;
		try {
			LOGGER.info("Costiv get the contacts begin..");
			String url = integration.getAccessUrl() + "/internal/bits/contacts";
			LOGGER.info("URL {}", url);

			JSONObject fuelExpense = new JSONObject();
			fuelExpense.put("namespaceCode", integration.getAccount());
			LOGGER.info("Request {}", fuelExpense);

			HttpServiceClient httpClient = new HttpServiceClient();
			String response = httpClient.post(url, fuelExpense.toString(), "application/json");
			responseJson = JSONObject.fromObject(response);
			LOGGER.info("Response {}", responseJson);
		}
		catch (Exception e) {
			LOGGER.info("", e);
			e.printStackTrace();
		}
		LOGGER.info("Costiv get the contacts end..");
		return responseJson;
	}
}