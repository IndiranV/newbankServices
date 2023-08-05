package org.in.com.service.report;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class SalesReportImpl implements ReportInterface {

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

		List<String> summaryDataList = getSummaryDetails(bookings, cancellations);

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
		finalList.addAll(summaryDataList);

		finalList.addAll(Arrays.asList("Booking"));
		finalList.add(BookingHeader.toString());
		finalList.addAll(bookList);
		finalList.addAll(Arrays.asList(Text.COMMA));

		finalList.addAll(Arrays.asList("Cancellation"));
		finalList.add(cancelHeader.toString());
		finalList.addAll(cancelList);
		return finalList;
	}

	private List<String> getSummaryDetails(List<Map<String, ?>> bookings, List<Map<String, ?>> cancellations) {
		List<Map<String, ?>> bookCancelDetails = new ArrayList<>();
		bookCancelDetails.addAll(bookings);
		bookCancelDetails.addAll(cancellations);

		StringBuilder summaryHeader = new StringBuilder();
		JSONObject summary = getSummaryKeywords();
		JSONArray summaryKeys = summary.names();

		for (int pos = 0; pos < summaryKeys.size(); pos++) {
			if (summaryHeader.length() > 0) {
				summaryHeader.append(Text.COMMA);
			}
			summaryHeader.append(summary.getString(summaryKeys.getString(pos)));
		}

		BigDecimal totBookingFare = BigDecimal.ZERO;
		BigDecimal totBoookingDiscount = BigDecimal.ZERO;
		BigDecimal totBookingAcBusTax = BigDecimal.ZERO;
		BigDecimal totBookingCgst = BigDecimal.ZERO;
		BigDecimal totBookingSgst = BigDecimal.ZERO;
		BigDecimal totBookingIgst = BigDecimal.ZERO;
		BigDecimal totBookingcommission = BigDecimal.ZERO;
		BigDecimal totBookinGgstOnComm = BigDecimal.ZERO;
		BigDecimal totBookingTdsOnComm = BigDecimal.ZERO;
		BigDecimal totBookingTdsTaxRevenue = BigDecimal.ZERO;
		BigDecimal totBookingTcs = BigDecimal.ZERO;

		BigDecimal totCancelFare = BigDecimal.ZERO;
		BigDecimal totCancelDiscount = BigDecimal.ZERO;
		BigDecimal totCancelAcBusTax = BigDecimal.ZERO;
		BigDecimal totCancelCgst = BigDecimal.ZERO;
		BigDecimal totCancelSgst = BigDecimal.ZERO;
		BigDecimal totCancelIgst = BigDecimal.ZERO;
		BigDecimal totCancelCommission = BigDecimal.ZERO;
		BigDecimal totRevokeCommission = BigDecimal.ZERO;
		BigDecimal totCancelGgstOnComm = BigDecimal.ZERO;
		BigDecimal totCancelBookGgstOnComm = BigDecimal.ZERO;
		BigDecimal totCancelTdsOnComm = BigDecimal.ZERO;
		BigDecimal totCancelTdsTaxRevenue = BigDecimal.ZERO;
		BigDecimal totCancelTcs = BigDecimal.ZERO;

		BigDecimal totCancelChargeFare = BigDecimal.ZERO;
		BigDecimal totCancelChargeDiscount = BigDecimal.ZERO;
		BigDecimal totCancelChargeAcBusTax = BigDecimal.ZERO;
		BigDecimal totCancelChargeCommission = BigDecimal.ZERO;
		BigDecimal totCancelChargeGgstOnComm = BigDecimal.ZERO;
		BigDecimal totCancelChargeTdsOnComm = BigDecimal.ZERO;
		BigDecimal totCancelChargeTdsTaxRevenue = BigDecimal.ZERO;
		BigDecimal totCancelChargeTcs = BigDecimal.ZERO;

		BigDecimal totTripCancelFare = BigDecimal.ZERO;
		BigDecimal totTripCancelDiscount = BigDecimal.ZERO;
		BigDecimal totTripCancelAcBusTax = BigDecimal.ZERO;
		BigDecimal totTripCancelCgst = BigDecimal.ZERO;
		BigDecimal totTripCancelSgst = BigDecimal.ZERO;
		BigDecimal totTripCancelIgst = BigDecimal.ZERO;
		BigDecimal totTripCancelCommission = BigDecimal.ZERO;
		BigDecimal totTripCancelGgstOnComm = BigDecimal.ZERO;
		BigDecimal totTripCancelTdsOnComm = BigDecimal.ZERO;
		BigDecimal totTripCancelTdsTaxRevenue = BigDecimal.ZERO;
		BigDecimal totTripCancelTcs = BigDecimal.ZERO;

		List<String> bookList = new ArrayList<String>();
		StringBuilder bookingValue = new StringBuilder();

		if (!bookCancelDetails.isEmpty()) {
			for (Map<String, ?> resultMaps : bookCancelDetails) {
				Map<String, String> resultMap = (Map<String, String>) resultMaps;

				String ticketStatusCode = String.valueOf(resultMap.get("ticket_status_code"));

				if (ticketStatusCode.equals("BO") || ticketStatusCode.equals("PBL")) {
					BigDecimal bookingFare = BigDecimal.ZERO;
					BigDecimal bookingDiscount = BigDecimal.ZERO;
					BigDecimal bookingAcBusTax = BigDecimal.ZERO;
					BigDecimal bookingCgst = BigDecimal.ZERO;
					BigDecimal bookingSgst = BigDecimal.ZERO;
					BigDecimal bookingIgst = BigDecimal.ZERO;
					BigDecimal bookingCommission = BigDecimal.ZERO;
					BigDecimal bookinGgstOnComm = BigDecimal.ZERO;
					BigDecimal bookingTdsOnComm = BigDecimal.ZERO;
					BigDecimal bookingTdsTaxRevenue = BigDecimal.ZERO;
					BigDecimal bookingTcs = BigDecimal.ZERO;

					bookingFare = bookingFare.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("ticket_amount"))));
					bookingDiscount = bookingDiscount.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("addons_amount"))));
					bookingAcBusTax = bookingAcBusTax.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("ac_bus_tax"))));
					bookingCgst = bookingCgst.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("cgst_amount"))));
					bookingSgst = bookingSgst.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("sgst_amount"))));
					bookingIgst = bookingIgst.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("igst_amount"))));
					bookingCommission = bookingCommission.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("commission_amount"))));
					bookingTdsOnComm = bookingTdsOnComm.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("tds_tax"))));
					bookinGgstOnComm = bookinGgstOnComm.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("service_tax_amount"))));
					bookingTdsTaxRevenue = bookingTdsTaxRevenue.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("tds_tax_revenue"))));
					bookingTcs = bookingTcs.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("tcs_tax"))));

					resultMap.put("payable", String.valueOf(bookingFare.add(bookingCgst).add(bookingSgst).add(bookingIgst).subtract(bookingDiscount).subtract(bookingCommission).subtract(bookingTdsOnComm).subtract(bookingTcs).subtract(bookingTdsTaxRevenue)));
					resultMap.put("commission_amount", String.valueOf(bookingCommission.subtract(bookinGgstOnComm)));
					
					totBookingFare = totBookingFare.add(bookingFare);
					totBoookingDiscount = totBoookingDiscount.add(bookingDiscount);
					totBookingAcBusTax = totBookingAcBusTax.add(bookingAcBusTax);
					totBookingCgst = totBookingCgst.add(bookingCgst);
					totBookingSgst = totBookingSgst.add(bookingSgst);
					totBookingIgst = totBookingIgst.add(bookingIgst);
					totBookingcommission = totBookingcommission.add(bookingCommission.subtract(bookinGgstOnComm));
					totBookinGgstOnComm = totBookinGgstOnComm.add(bookinGgstOnComm);
					totBookingTdsOnComm = totBookingTdsOnComm.add(bookingTdsOnComm);
					totBookingTdsTaxRevenue = totBookingTdsTaxRevenue.add(bookingTdsTaxRevenue);
					totBookingTcs = totBookingTcs.add(bookingTcs);
				}
				else if (ticketStatusCode.equals("CA") || ticketStatusCode.equals("TCKTR")) {
					BigDecimal cancelFare = BigDecimal.ZERO;
					BigDecimal cancelDiscount = BigDecimal.ZERO;
					BigDecimal cancelAcBusTax = BigDecimal.ZERO;
					BigDecimal cancelCgst = BigDecimal.ZERO;
					BigDecimal cancelSgst = BigDecimal.ZERO;
					BigDecimal cancelIgst = BigDecimal.ZERO;
					BigDecimal cancelCommission = BigDecimal.ZERO;
					BigDecimal cancelBookGgstOnComm = BigDecimal.ZERO;
					BigDecimal cancelGgstOnComm = BigDecimal.ZERO;
					BigDecimal cancelTdsOnComm = BigDecimal.ZERO;
					BigDecimal cancelTdsTaxRevenue = BigDecimal.ZERO;
					BigDecimal cancelTcs = BigDecimal.ZERO;

					BigDecimal revokeCommission = BigDecimal.ZERO;
					BigDecimal refundAmount = BigDecimal.ZERO;

					BigDecimal cancelCharges = BigDecimal.ZERO;
					BigDecimal cancelChargeCommission = BigDecimal.ZERO;
					BigDecimal cancelChargeTdsOnComm = BigDecimal.ZERO;

					cancelFare = cancelFare.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("ticket_amount"))));
					cancelDiscount = cancelDiscount.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("addons_amount"))));
					cancelAcBusTax = cancelAcBusTax.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("ac_bus_tax"))));
					cancelCgst = cancelCgst.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("cgst_amount"))));
					cancelSgst = cancelSgst.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("sgst_amount"))));
					cancelIgst = cancelIgst.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("igst_amount"))));
					cancelCommission = cancelCommission.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("commission_amount"))));
					cancelTdsOnComm = cancelTdsOnComm.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("tds_tax"))));
					cancelTdsTaxRevenue = cancelTdsTaxRevenue.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("cancel_tds_tax_revenue"))));
					cancelBookGgstOnComm = cancelBookGgstOnComm.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("service_tax_amount"))));
					cancelGgstOnComm = cancelGgstOnComm.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("cancel_service_tax_amount"))));
					cancelTcs = cancelTcs.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("cancel_tcs_tax"))));

					cancelCharges = cancelCharges.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("cancellation_charges"))));
					cancelChargeCommission = cancelChargeCommission.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("cancel_commission"))));
					cancelChargeTdsOnComm = cancelChargeTdsOnComm.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("cancel_tds_tax"))));

					revokeCommission = revokeCommission.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("revoke_commission_amount"))));
					refundAmount = refundAmount.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("refund_amount"))));

					resultMap.put("refund", String.valueOf(cancelFare.add(cancelCgst).add(cancelSgst).add(cancelIgst).subtract(cancelDiscount).subtract(cancelCommission).add(cancelTdsOnComm).subtract(cancelCharges).add(cancelChargeCommission).subtract(cancelChargeTdsOnComm)));
					resultMap.put("commission_amount", String.valueOf(cancelCommission.subtract(cancelBookGgstOnComm)));
					
					totCancelFare = totCancelFare.add(cancelFare);
					totCancelDiscount = totCancelDiscount.add(cancelDiscount);
					totCancelAcBusTax = totCancelAcBusTax.add(cancelAcBusTax);
					totCancelCgst = totCancelCgst.add(cancelCgst);
					totCancelSgst = totCancelSgst.add(cancelSgst);
					totCancelIgst = totCancelIgst.add(cancelIgst);
					totCancelCommission = totCancelCommission.add(cancelCommission.subtract(cancelBookGgstOnComm));
					totRevokeCommission = totRevokeCommission.add(revokeCommission);
					totCancelGgstOnComm = totCancelGgstOnComm.add(cancelGgstOnComm);
					totCancelBookGgstOnComm = totCancelBookGgstOnComm.add(cancelBookGgstOnComm);
					totCancelTdsOnComm = totCancelTdsOnComm.add(cancelTdsOnComm);
					totCancelTdsTaxRevenue = totCancelTdsTaxRevenue.add(cancelTdsTaxRevenue);
					totCancelTcs = totCancelTcs.add(cancelTcs);

					totCancelChargeFare = totCancelChargeFare.add(cancelCharges);
					totCancelChargeCommission = totCancelChargeCommission.add(cancelChargeCommission);
					totCancelChargeTdsOnComm = totCancelChargeTdsOnComm.add(cancelChargeTdsOnComm);
				}
				else if (ticketStatusCode.equals("TCA")) {
					BigDecimal tripCancelFare = BigDecimal.ZERO;
					BigDecimal tripCancelDiscount = BigDecimal.ZERO;
					BigDecimal tripCancelAcBusTax = BigDecimal.ZERO;
					BigDecimal tripCancelCgst = BigDecimal.ZERO;
					BigDecimal tripCancelSgst = BigDecimal.ZERO;
					BigDecimal tripCancelIgst = BigDecimal.ZERO;
					BigDecimal tripCancelCommission = BigDecimal.ZERO;
					BigDecimal tripCancelTdsOnComm = BigDecimal.ZERO;
					BigDecimal tripCancelTdsTaxevenue = BigDecimal.ZERO;
					BigDecimal tripCancelGgstOnComm = BigDecimal.ZERO;
					BigDecimal tripCancelTcs = BigDecimal.ZERO;

					tripCancelFare = tripCancelFare.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("ticket_amount"))));
					tripCancelDiscount = tripCancelDiscount.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("addons_amount"))));
					tripCancelAcBusTax = tripCancelAcBusTax.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("ac_bus_tax"))));
					tripCancelCgst = tripCancelCgst.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("cgst_amount"))));
					tripCancelSgst = tripCancelSgst.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("sgst_amount"))));
					tripCancelIgst = tripCancelIgst.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("igst_amount"))));
					tripCancelCommission = tripCancelCommission.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("commission_amount"))));
					tripCancelTdsOnComm = tripCancelTdsOnComm.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("tds_tax"))));
					tripCancelTdsTaxevenue = tripCancelTdsTaxevenue.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("cancel_tds_tax_revenue"))));
					tripCancelGgstOnComm = tripCancelGgstOnComm.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("service_tax_amount"))));
					tripCancelTcs = tripCancelTcs.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("tcs_tax"))));

					resultMap.put("refund", String.valueOf(tripCancelFare.add(tripCancelCgst).add(tripCancelSgst).add(tripCancelIgst).subtract(tripCancelDiscount).subtract(tripCancelCommission).add(tripCancelTdsOnComm).add(tripCancelGgstOnComm).subtract(tripCancelTcs).subtract(tripCancelTdsTaxevenue)));
					resultMap.put("commission_amount", String.valueOf(tripCancelCommission.subtract(tripCancelGgstOnComm)));
					
					totTripCancelFare = totTripCancelFare.add(tripCancelFare);
					totTripCancelAcBusTax = totTripCancelAcBusTax.add(tripCancelAcBusTax);
					totTripCancelCgst = totTripCancelCgst.add(tripCancelCgst);
					totTripCancelSgst = totTripCancelSgst.add(tripCancelSgst);
					totTripCancelIgst = totTripCancelIgst.add(tripCancelIgst);
					totTripCancelDiscount = totTripCancelDiscount.add(tripCancelDiscount);
					totTripCancelCommission = totTripCancelCommission.add(tripCancelCommission.subtract(tripCancelGgstOnComm));
					totTripCancelTdsOnComm = totTripCancelTdsOnComm.add(tripCancelTdsOnComm);
					totTripCancelTdsTaxRevenue = totTripCancelTdsTaxRevenue.add(tripCancelTdsTaxevenue);
					totTripCancelGgstOnComm = totTripCancelGgstOnComm.add(tripCancelGgstOnComm);
					totTripCancelTcs = totTripCancelTcs.add(tripCancelTcs);
				}
			}
		}

		bookList.addAll(Arrays.asList("Summary"));
		bookList.add(summaryHeader.toString());

		bookingValue.append("Booking (A)");
		bookingValue.append(Text.COMMA);
		bookingValue.append(totBookingFare);
		bookingValue.append(Text.COMMA);
		bookingValue.append(totBoookingDiscount);
		bookingValue.append(Text.COMMA);
		bookingValue.append(totBookingCgst.add(totBookingSgst).add(totBookingIgst));
		bookingValue.append(Text.COMMA);
		bookingValue.append(totBookingcommission);
		bookingValue.append(Text.COMMA);
		bookingValue.append(totBookinGgstOnComm);
		bookingValue.append(Text.COMMA);
		bookingValue.append(totBookingTdsOnComm);
		bookingValue.append(Text.COMMA);
		bookingValue.append(totBookingTcs);
		bookingValue.append(Text.COMMA);

		BigDecimal bookingSubTotal = BigDecimal.ZERO;
		bookingSubTotal = totBookingFare.subtract(totBoookingDiscount).add(totBookingCgst).add(totBookingSgst).add(totBookingIgst).subtract(totBookingcommission).subtract(totBookinGgstOnComm).subtract(totBookingTdsOnComm).subtract(totBookingTcs).subtract(totBookingTdsTaxRevenue);
		bookingValue.append(bookingSubTotal);

		bookList.addAll(Arrays.asList(bookingValue.toString()));

		bookingValue = new StringBuilder();
		bookingValue.append("Cancel (B)");
		bookingValue.append(Text.COMMA);
		bookingValue.append(totCancelFare);
		bookingValue.append(Text.COMMA);
		bookingValue.append(totCancelDiscount);
		bookingValue.append(Text.COMMA);
		bookingValue.append(totCancelCgst.add(totCancelSgst).add(totCancelIgst));
		bookingValue.append(Text.COMMA);
		bookingValue.append(totCancelCommission);
		bookingValue.append(Text.COMMA);
		bookingValue.append(totCancelBookGgstOnComm);
		bookingValue.append(Text.COMMA);
		bookingValue.append(totCancelTdsOnComm);
		bookingValue.append(Text.COMMA);
		bookingValue.append(totCancelTcs);
		bookingValue.append(Text.COMMA);

		BigDecimal cancelSubTotal = BigDecimal.ZERO;
		cancelSubTotal = totCancelFare.subtract(totCancelDiscount).add(totCancelCgst).add(totCancelSgst).add(totCancelIgst).subtract(totCancelCommission).add(totCancelGgstOnComm).add(totCancelTdsOnComm).subtract(totCancelTcs).subtract(totCancelTdsTaxRevenue);
		bookingValue.append(cancelSubTotal);

		bookList.addAll(Arrays.asList(bookingValue.toString()));

		bookingValue = new StringBuilder();
		bookingValue.append("Cancel Charges (C)");
		bookingValue.append(Text.COMMA);
		bookingValue.append(totCancelChargeFare);
		bookingValue.append(Text.COMMA);
		bookingValue.append(totCancelChargeDiscount);
		bookingValue.append(Text.COMMA);
		bookingValue.append(totCancelChargeAcBusTax);
		bookingValue.append(Text.COMMA);
		bookingValue.append(totCancelChargeCommission);
		bookingValue.append(Text.COMMA);
		bookingValue.append(totCancelChargeGgstOnComm);
		bookingValue.append(Text.COMMA);
		bookingValue.append(totCancelChargeTdsOnComm);
		bookingValue.append(Text.COMMA);
		bookingValue.append(totCancelChargeTcs);
		bookingValue.append(Text.COMMA);

		BigDecimal cancelChargeSubTotal = BigDecimal.ZERO;
		cancelChargeSubTotal = totCancelChargeFare.subtract(totCancelChargeDiscount).add(totCancelChargeAcBusTax).subtract(totCancelChargeCommission).add(totCancelChargeGgstOnComm).add(totCancelChargeTdsOnComm).subtract(totCancelChargeTcs).subtract(totCancelChargeTdsTaxRevenue);
		bookingValue.append(cancelChargeSubTotal);
		bookList.addAll(Arrays.asList(bookingValue.toString()));

		bookingValue = new StringBuilder();
		bookingValue.append("Trip Cancel (D)");
		bookingValue.append(Text.COMMA);
		bookingValue.append(totTripCancelFare);
		bookingValue.append(Text.COMMA);
		bookingValue.append(totTripCancelDiscount);
		bookingValue.append(Text.COMMA);
		bookingValue.append(totTripCancelCgst.add(totTripCancelSgst).add(totTripCancelIgst));
		bookingValue.append(Text.COMMA);
		bookingValue.append(totTripCancelCommission);
		bookingValue.append(Text.COMMA);
		bookingValue.append(totTripCancelGgstOnComm);
		bookingValue.append(Text.COMMA);
		bookingValue.append(totTripCancelTdsOnComm);
		bookingValue.append(Text.COMMA);
		bookingValue.append(totTripCancelTcs);
		bookingValue.append(Text.COMMA);

		BigDecimal tripCancelSubTotal = BigDecimal.ZERO;
		tripCancelSubTotal = totTripCancelFare.add(totTripCancelCgst).add(totTripCancelSgst).add(totTripCancelIgst).subtract(totTripCancelDiscount).subtract(totTripCancelCommission).add(totTripCancelTdsOnComm).add(totTripCancelGgstOnComm).subtract(totTripCancelTcs).subtract(totTripCancelTdsTaxRevenue);
		bookingValue.append(tripCancelSubTotal);
		bookList.addAll(Arrays.asList(bookingValue.toString()));

		bookingValue = new StringBuilder();
		bookingValue.append("Net Amount (A-B+C-D)");
		bookingValue.append(Text.COMMA);
		bookingValue.append(totBookingFare.subtract(totCancelFare).add(totCancelChargeFare).subtract(totTripCancelFare));
		bookingValue.append(Text.COMMA);
		bookingValue.append(totBoookingDiscount.subtract(totCancelDiscount).add(totCancelChargeDiscount).subtract(totTripCancelDiscount));
		bookingValue.append(Text.COMMA);
		bookingValue.append((totBookingCgst.add(totBookingSgst).add(totBookingIgst)).subtract(totCancelCgst.add(totCancelSgst).add(totCancelIgst)).add(totCancelChargeAcBusTax).subtract(totTripCancelCgst.add(totTripCancelSgst).add(totTripCancelIgst)));
		bookingValue.append(Text.COMMA);
		bookingValue.append(totBookingcommission.subtract(totCancelCommission).add(totCancelChargeCommission).subtract(totTripCancelCommission));
		bookingValue.append(Text.COMMA);
		bookingValue.append(totBookinGgstOnComm.subtract(totCancelGgstOnComm).add(totCancelChargeGgstOnComm).subtract(totTripCancelGgstOnComm));
		bookingValue.append(Text.COMMA);
		bookingValue.append(totBookingTdsOnComm.subtract(totCancelTdsOnComm).add(totCancelChargeTdsOnComm).subtract(totTripCancelTdsOnComm));
		bookingValue.append(Text.COMMA);
		bookingValue.append(totBookingTcs.subtract(totCancelTcs).add(totCancelChargeTcs).subtract(totTripCancelTcs));
		bookingValue.append(Text.COMMA);
		bookingValue.append(bookingSubTotal.subtract(cancelSubTotal).add(cancelChargeSubTotal).subtract(tripCancelSubTotal));
		bookList.addAll(Arrays.asList(bookingValue.toString()));

		bookList.addAll(Arrays.asList(Text.COMMA));

		return bookList;
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
		if (reportQueryDTO.getQuery().contains(":filterDateType")) {
			DBQueryParamDTO paramDTO = new DBQueryParamDTO();
			paramDTO.setParamName("filterDateType");
			paramDTO.setValue(filterDateType);
			params.add(paramDTO);
		}
		return params;
	}

	private JSONObject getKeywords() {
		JSONObject json = new JSONObject();
		JSONObject bookingJson = new JSONObject();
		bookingJson.put("travel_date", "Travel Date");
		bookingJson.put("transaction_date", "Transaction Date");
		bookingJson.put("user_group_name", "Group Name");
		bookingJson.put("user_name", "User Name");
		bookingJson.put("route", "Route");
		bookingJson.put("ticket_code", "PNR");
		bookingJson.put("bus_name", "RC Owner");
		bookingJson.put("schedule_tag", "Collection Owner");
		bookingJson.put("registation_number", "Vehicle Number");
		bookingJson.put("bus_type", "Bus Type");
		bookingJson.put("ticket_status_code", "Ticket Status");
		bookingJson.put("operator_gstin", "Operator GST");
		bookingJson.put("from_state_name", "Operator GST State");
		bookingJson.put("customer_gstin", "Customer GST");
		bookingJson.put("customer_trade_name", "Customer GST Trade Name");
		bookingJson.put("seat_count", "Seat Count");
		bookingJson.put("ticket_amount", "Fare");
		bookingJson.put("ac_bus_tax", "A/C GST");
		bookingJson.put("cgst_amount", "CGST");
		bookingJson.put("sgst_amount", "SGST");
		bookingJson.put("igst_amount", "IGST");
		bookingJson.put("addons_amount", "Discount");
		bookingJson.put("commission_amount", "Commission");
		bookingJson.put("service_tax_amount", "GST On Comm");
		bookingJson.put("tcs_tax", "TCS");
		bookingJson.put("tds_tax_revenue", "TDS 194-0");
		bookingJson.put("payable", "Payable");
		json.put("booking", bookingJson);

		JSONObject cancelJson = new JSONObject();
		cancelJson.put("travel_date", "Travel Date");
		cancelJson.put("transaction_date", "Transaction Date");
		cancelJson.put("user_group_name", "Group Name");
		cancelJson.put("user_name", "User Name");
		cancelJson.put("route", "Route");
		cancelJson.put("ticket_code", "PNR");
		cancelJson.put("bus_name", "RC Owner");
		cancelJson.put("schedule_tag", "Collection Owner");
		cancelJson.put("registation_number", "Vehicle Number");
		cancelJson.put("bus_type", "Bus Type");
		cancelJson.put("ticket_status_code", "Ticket Status");
		cancelJson.put("operator_gstin", "Operator GST");
		cancelJson.put("from_state_name", "Operator GST State");
		cancelJson.put("customer_gstin", "Customer GST");
		cancelJson.put("customer_trade_name", "Customer GST Trade Name");
		cancelJson.put("seat_count", "Seat Count");
		cancelJson.put("ticket_amount", "Fare");
		cancelJson.put("ac_bus_tax", "A/C GST");
		cancelJson.put("cgst_amount", "CGST");
		cancelJson.put("sgst_amount", "SGST");
		cancelJson.put("igst_amount", "IGST");
		cancelJson.put("addons_amount", "Discount");
		cancelJson.put("commission_amount", "Commission");
		cancelJson.put("service_tax_amount", "GST On Comm");
		cancelJson.put("cancellation_charges", "Cancel Charge");
		cancelJson.put("cancel_commission", "Cancel Comm/Share");
		cancelJson.put("cancel_tds_tax_revenue", "TDS 194-0");
		cancelJson.put("cancel_tcs_tax", "TCS");
		cancelJson.put("refund", "Refund");
		json.put("cancel", cancelJson);
		return json;
	}

	private JSONObject getSummaryKeywords() {
		JSONObject summaryJson = new JSONObject();
		summaryJson.put("category", "Category");
		summaryJson.put("fare", "Fare (1) ");
		summaryJson.put("discount", "Discount (2)");
		summaryJson.put("ac_bus_gst", "Ac Bus GST (3)");
		summaryJson.put("commission", "Commisssion (4)");
		summaryJson.put("gst_on_commission", "GST On Commission (5)");
		summaryJson.put("tds_on_commission", "TDS On Commission (6)");
		summaryJson.put("tcs", "TCS (7)");
		summaryJson.put("total", "Total (1-2+3-4+5+6-7)");

		return summaryJson;
	}
}
