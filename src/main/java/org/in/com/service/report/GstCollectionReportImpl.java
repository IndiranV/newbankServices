package org.in.com.service.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.DBQueryParamDTO;
import org.in.com.dto.ReportQueryDTO;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.service.ReportInterface;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class GstCollectionReportImpl implements ReportInterface {

	@Override
	public Map<String, Object> createWorkBook(AuthDTO authDTO, JSONObject jsonObject, List<Map<String, ?>> results, String fromDate, String toDate) {

		String tempfileName = String.valueOf(jsonObject.getString("reportName"));
		String type = String.valueOf(jsonObject.getString("datePeriod")).trim().toUpperCase();

		String fileName = ExportExcelHelper.generateFileName(tempfileName, fromDate, toDate, type);

		List<Map<String, ?>> bookings = new ArrayList<>();
		List<Map<String, ?>> cancellations = new ArrayList<>();

		for (Map<String, ?> resultMap : results) {
			if (resultMap.get("ticket_status_code") == null) {
				continue;
			}

			if (TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getCode().equals(resultMap.get("ticket_status_code")) || TicketStatusEM.TRIP_CANCELLED.getCode().equals(resultMap.get("ticket_status_code")) || TicketStatusEM.TICKET_TRANSFERRED.getCode().equals(resultMap.get("ticket_status_code"))) {
				cancellations.add(resultMap);
			}
			else if (TicketStatusEM.CONFIRM_BOOKED_TICKETS.getCode().equals(resultMap.get("ticket_status_code")) || TicketStatusEM.PHONE_BLOCKED_TICKET.getCode().equals(resultMap.get("ticket_status_code"))) {
				bookings.add(resultMap);
			}
		}
		// Create Excel
		List<String> csvDataList = createExcelV2(authDTO, bookings, cancellations, fileName);
		Map<String, Object> workBookMap = new HashMap<>();
		workBookMap.put(Text.WORK_BOOK, csvDataList);
		workBookMap.put(Text.FILE_NAME, fileName);
		return workBookMap;
	}

	private List<String> createExcelV2(AuthDTO authDTO, List<Map<String, ?>> bookings, List<Map<String, ?>> cancellations, String fileName) {
		List<String> finalList = new ArrayList<String>();

		JSONObject bookingKeyword = getKeywords().getJSONObject("booking");
		JSONArray bookingKeys = bookingKeyword.names();
		StringBuilder BookingHeader = new StringBuilder();
		for (int pos = 0; pos < bookingKeys.size(); pos++) {
			if (BookingHeader.length() > 0) {
				BookingHeader.append(Text.COMMA);
			}
			BookingHeader.append(bookingKeyword.getString(bookingKeys.getString(pos)));
		}

		StringBuilder cancelHeader = new StringBuilder();
		JSONObject cancelKeyword = getKeywords().getJSONObject("cancel");
		JSONArray cancelKeys = cancelKeyword.names();
		for (int pos = 0; pos < cancelKeys.size(); pos++) {
			if (cancelHeader.length() > 0) {
				cancelHeader.append(Text.COMMA);
			}
			cancelHeader.append(cancelKeyword.getString(cancelKeys.getString(pos)));
		}

		List<String> bookList = new ArrayList<String>();
		if (!bookings.isEmpty()) {
			for (Map<String, ?> resultMap : bookings) {
				int cellnum = 0;
				StringBuilder bookingValue = new StringBuilder();
				for (int pos = 0; pos < bookingKeys.size(); pos++) {
					String columnName = bookingKeys.getString(cellnum);
					if (bookingValue.length() > 0) {
						bookingValue.append(Text.COMMA);
					}
					String value = String.valueOf(resultMap.get(columnName));
					if (DateUtil.isValidDateV2(value)) {
						if (DateUtil.isValidDate(value)) {
							bookingValue.append(DateUtil.getDateTime(value).format("DD/MM/YYYY"));
						}
						else {
							bookingValue.append(DateUtil.getDateTime(value).format("DD/MM/YYYY hh:mm"));
						}
					}
					else {
						bookingValue.append(StringUtil.isNotNull(value) ? value.replaceAll(Text.COMMA, Text.SINGLE_SPACE) : Text.EMPTY);
					}
					cellnum++;
				}
				bookList.addAll(Arrays.asList(bookingValue.toString()));
			}
		}

		List<String> cancelList = new ArrayList<String>();
		if (!cancellations.isEmpty()) {
			for (Map<String, ?> resultMap : cancellations) {
				int cellnum = 0;
				StringBuilder cancelValue = new StringBuilder();
				for (int pos = 0; pos < cancelKeys.size(); pos++) {
					String columnName = cancelKeys.getString(cellnum);
					if (cancelValue.length() > 0) {
						cancelValue.append(Text.COMMA);
					}
					String value = String.valueOf(resultMap.get(columnName));
					if (DateUtil.isValidDateV2(value)) {
						if (DateUtil.isValidDate(value)) {
							cancelValue.append(DateUtil.getDateTime(value).format("DD/MM/YYYY"));
						}
						else {
							cancelValue.append(DateUtil.getDateTime(value).format("DD/MM/YYYY hh:mm"));
						}
					}
					else {
						cancelValue.append(StringUtil.isNotNull(value) ? value.replaceAll(Text.COMMA, Text.SINGLE_SPACE) : Text.EMPTY);
					}
					cellnum++;
				}
				cancelList.addAll(Arrays.asList(cancelValue.toString()));
			}
		}
		finalList.addAll(Arrays.asList(fileName));
		finalList.addAll(Arrays.asList("Booking"));
		finalList.add(BookingHeader.toString());
		finalList.addAll(bookList);
		finalList.addAll(Arrays.asList(Text.COMMA));

		finalList.addAll(Arrays.asList("Cancellation"));
		finalList.add(cancelHeader.toString());
		finalList.addAll(cancelList);
		return finalList;
	}

	@Override
	public List<DBQueryParamDTO> getParams(AuthDTO authDTO, ReportQueryDTO reportQueryDTO, String fromDate, String toDate, String filterDateType) {
		List<DBQueryParamDTO> params = new ArrayList<>();
		if (reportQueryDTO.getQuery().contains(":namespaceId")) {
			DBQueryParamDTO paramDTO = new DBQueryParamDTO();
			paramDTO.setParamName("namespaceId");
			paramDTO.setValue(String.valueOf(authDTO.getNamespace().getId()));
			params.add(paramDTO);
		}
		if (reportQueryDTO.getQuery().contains(":fromDate")) {
			DBQueryParamDTO paramDTO = new DBQueryParamDTO();
			paramDTO.setParamName("fromDate");
			paramDTO.setValue(fromDate);
			params.add(paramDTO);
		}
		if (reportQueryDTO.getQuery().contains(":toDate")) {
			DBQueryParamDTO paramDTO = new DBQueryParamDTO();
			paramDTO.setParamName("toDate");
			paramDTO.setValue(toDate);
			params.add(paramDTO);
		}
		if (reportQueryDTO.getQuery().contains(":travelDateFlag")) {
			DBQueryParamDTO paramDTO = new DBQueryParamDTO();
			paramDTO.setParamName("travelDateFlag");
			paramDTO.setValue(Numeric.ONE);
			params.add(paramDTO);
		}
		if (reportQueryDTO.getQuery().contains(":userCode")) {
			DBQueryParamDTO paramDTO = new DBQueryParamDTO();
			paramDTO.setParamName("userCode");
			paramDTO.setValue(Text.NA);
			params.add(paramDTO);
		}
		if (reportQueryDTO.getQuery().contains(":groupCode")) {
			DBQueryParamDTO paramDTO = new DBQueryParamDTO();
			paramDTO.setParamName("groupCode");
			paramDTO.setValue(Text.NA);
			params.add(paramDTO);
		}
		if (reportQueryDTO.getQuery().contains(":scheduleCode")) {
			DBQueryParamDTO paramDTO = new DBQueryParamDTO();
			paramDTO.setParamName("scheduleCode");
			paramDTO.setValue(Text.NA);
			params.add(paramDTO);
		}
		if (reportQueryDTO.getQuery().contains(":fromStationCode")) {
			DBQueryParamDTO paramDTO = new DBQueryParamDTO();
			paramDTO.setParamName("fromStationCode");
			paramDTO.setValue(Text.NA);
			params.add(paramDTO);
		}
		if (reportQueryDTO.getQuery().contains(":toStationCode")) {
			DBQueryParamDTO paramDTO = new DBQueryParamDTO();
			paramDTO.setParamName("toStationCode");
			paramDTO.setValue(Text.NA);
			params.add(paramDTO);
		}
		return params;
	}

	private JSONObject getKeywords() {
		JSONObject json = new JSONObject();
		JSONObject bookingJson = new JSONObject();
		bookingJson.put("ticket_code", "PNR");
		bookingJson.put("booking_code", "Booking Code");
		bookingJson.put("passenger_name", "Passenger Name");
		bookingJson.put("transaction_date", "Booking Date");
		bookingJson.put("travel_date", "Travel Date");
		bookingJson.put("from_station_name", "From Station");
		bookingJson.put("to_station_name", "To Station");
		bookingJson.put("user_name", "Booked User");
		bookingJson.put("user_group_name", "Booked User Group");
		bookingJson.put("ticket_amount", "Ticket Amount");
		bookingJson.put("addons_amount", "Discount Amount");
		bookingJson.put("ac_bus_tax", "GST");
		bookingJson.put("commission_amount", "Commission Amount");
		bookingJson.put("tds_tax", "TDS");
		bookingJson.put("transaction_amount", "Transaction Amount");
		bookingJson.put("ticket_status_code", "Ticket Status");
		bookingJson.put("payment_gateway_name", "Payment Gateway");
		bookingJson.put("bus_name", "Bus");
		bookingJson.put("service_number", "Service No.");
		bookingJson.put("from_state_name", "State");
		bookingJson.put("operator_gstin", "Operator GSTIN");
		bookingJson.put("operator_trade_name", "Operator Trade Name");
		bookingJson.put("customer_gstin", "Customer GSTIN");
		bookingJson.put("customer_trade_name", "Customer Trade Name");
		json.put("booking", bookingJson);

		JSONObject cancelJson = new JSONObject();
		cancelJson.put("ticket_code", "PNR");
		cancelJson.put("booking_code", "Booking Code");
		cancelJson.put("seat_name", "Seats");
		cancelJson.put("ticket_status_code", "Ticket Status");
		cancelJson.put("user_name", "Booked User");
		cancelJson.put("user_group_name", "Booked User Group");
		cancelJson.put("cancel_user_name", "Cancelled User");
		cancelJson.put("cancel_user_group_name", "Cancelled User Group");
		cancelJson.put("travel_date", "Travel Date");
		cancelJson.put("cancelled_date", "Cancelled Date");
		cancelJson.put("from_station_name", "From Station");
		cancelJson.put("to_station_name", "To Station");
		cancelJson.put("boarding_point_name", "Borading Point");
		cancelJson.put("ticket_amount", "Cancel Ticket Amount");
		cancelJson.put("cancellation_charges", "Cancel Charge Amount");
		cancelJson.put("charge_tax_amount", "Charge Tax Amount");
		cancelJson.put("ac_bus_tax", "GST");
		cancelJson.put("tds_tax", "TDS");
		cancelJson.put("addons_amount", "Cancel Discount");
		cancelJson.put("refund_amount", "Refund Amount");
		cancelJson.put("payment_gateway_name", "Payment Gateway");
		cancelJson.put("bus_name", "Bus");
		cancelJson.put("from_state_name", "State");
		json.put("cancel", cancelJson);
		return json;
	}
}
