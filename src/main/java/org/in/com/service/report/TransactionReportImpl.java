package org.in.com.service.report;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.DBQueryParamDTO;
import org.in.com.dto.ReportQueryDTO;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.service.ReportInterface;
import org.in.com.utils.StringUtil;
import org.springframework.stereotype.Service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class TransactionReportImpl implements ReportInterface {

	@Override
	public Map<String, Object> createWorkBook(AuthDTO authDTO, JSONObject jsonObject, List<Map<String, ?>> results, String fromDate, String toDate) {
		// Blank workbook
		XSSFWorkbook workbook = new XSSFWorkbook();

		String tempfileName = jsonObject.getString("reportName");
		String type = String.valueOf(jsonObject.getString("datePeriod")).trim().toUpperCase();

		String sheetName = ExportExcelHelper.generateSheetName(tempfileName, fromDate, toDate);
		String fileName = ExportExcelHelper.generateFileName(tempfileName, fromDate, toDate, type);
		// Create a blank sheet
		XSSFSheet sheet = workbook.createSheet(sheetName);

		List<Map<String, ?>> bookings = new ArrayList<>();
		List<Map<String, ?>> cancellations = new ArrayList<>();

		BigDecimal totaltransactionAmount = BigDecimal.ZERO;
		BigDecimal totalCommissionAmount = BigDecimal.ZERO;
		BigDecimal totalRevokeCommissionAmount = BigDecimal.ZERO;
		BigDecimal totalTripCancelAmount = BigDecimal.ZERO;
		BigDecimal totalAcBusTax = BigDecimal.ZERO;
		BigDecimal totalTds = BigDecimal.ZERO;
		BigDecimal totalChargeTaxAmount = BigDecimal.ZERO;
		BigDecimal totalOffer = BigDecimal.ZERO;
		BigDecimal totalRevokeAcBusTax = BigDecimal.ZERO;
		BigDecimal totalCancelTds = BigDecimal.ZERO;
		BigDecimal totalRevokeChargeTaxAmount = BigDecimal.ZERO;
		BigDecimal totalRevokeOffer = BigDecimal.ZERO;
		BigDecimal totalCancellationCharge = BigDecimal.ZERO;
		BigDecimal totalCancelledAmount = BigDecimal.ZERO;
		BigDecimal totalGrossAmount = BigDecimal.ZERO;
		int totalSeatCount = 0;
		BigDecimal totalPhoneBookingAmountSub = BigDecimal.ZERO;

		for (Map<String, ?> resultMap : results) {
			if (resultMap.get("ticket_status_code") == null) {
				continue;
			}

			if (TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getCode().equals(resultMap.get("ticket_status_code")) || TicketStatusEM.TRIP_CANCELLED.getCode().equals(resultMap.get("ticket_status_code"))) {
				totalRevokeAcBusTax = totalRevokeAcBusTax.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("ac_bus_tax"))));
				totalRevokeOffer = totalRevokeOffer.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("addons_amount"))));
				totalRevokeCommissionAmount = totalRevokeCommissionAmount.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("revoke_commission_amount"))));
				totalCancellationCharge = totalCancellationCharge.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("cancellation_charges"))));
				totalCancelledAmount = totalCancelledAmount.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("ticket_amount"))));
				totalCancelTds = totalCancelTds.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("tds_tax"))));
				totalRevokeChargeTaxAmount = totalRevokeChargeTaxAmount.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("charge_tax_amount"))));
				cancellations.add(resultMap);
			}
			else if (TicketStatusEM.CONFIRM_BOOKED_TICKETS.getCode().equals(resultMap.get("ticket_status_code")) || TicketStatusEM.PHONE_BLOCKED_TICKET.getCode().equals(resultMap.get("ticket_status_code")) || TicketStatusEM.TICKET_TRANSFERRED.getCode().equals(resultMap.get("ticket_status_code"))) {
				totaltransactionAmount = TicketStatusEM.CONFIRM_BOOKED_TICKETS.getCode().equals(resultMap.get("ticket_status_code")) ? totaltransactionAmount.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("ticket_amount")))) : BigDecimal.ZERO;
				totalPhoneBookingAmountSub = TicketStatusEM.PHONE_BLOCKED_TICKET.getCode().equals(resultMap.get("ticket_status_code")) ? totalPhoneBookingAmountSub.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("ticket_amount")))) : BigDecimal.ZERO;
				totalCommissionAmount = totalCommissionAmount.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("commission_amount"))));
				totalAcBusTax = totalAcBusTax.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("ac_bus_tax"))));
				totalOffer = totalOffer.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("addons_amount"))));
				totalGrossAmount = totalGrossAmount.add(totaltransactionAmount).add(totalAcBusTax).subtract(totalCommissionAmount).subtract(totalOffer);
				totalTds = totalTds.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("tds_tax"))));
				totalChargeTaxAmount = totalChargeTaxAmount.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("charge_tax_amount"))));

				bookings.add(resultMap);
			}

			totalSeatCount = totalSeatCount + StringUtil.getIntegerValue(String.valueOf(resultMap.get("seat_count")));
		}

		Map<String, BigDecimal> grandTotalMap = new HashMap<String, BigDecimal>();
		grandTotalMap.put("Online", totaltransactionAmount);
		grandTotalMap.put("A/C Bus Tax", totalAcBusTax);
		grandTotalMap.put("TDS", totalTds);
		grandTotalMap.put("Charge Tax Amount", totalChargeTaxAmount);
		grandTotalMap.put("Discount Offer", totalOffer);
		BigDecimal bookingSubAmount = totaltransactionAmount.add(totalAcBusTax).subtract(totalOffer).subtract(totalCommissionAmount);
		grandTotalMap.put("Booking Sub Total", bookingSubAmount);
		grandTotalMap.put("Revoke A/C Bus Tax", totalRevokeAcBusTax);
		grandTotalMap.put("Revoke Discount Offer", totalRevokeOffer);
		grandTotalMap.put("Cancellation Charge", totalCancellationCharge);
		grandTotalMap.put("Cancel TDS", totalCancelTds);
		grandTotalMap.put("Cancel Charge Tax Amount", totalRevokeChargeTaxAmount);
		grandTotalMap.put("Cancellation Amount", totalCancelledAmount);
		grandTotalMap.put("Commission Amount", totalCommissionAmount);
		grandTotalMap.put("Revoke Commission Amount", totalRevokeCommissionAmount);
		BigDecimal finalSubTotal = totalCancelledAmount.subtract(totalRevokeOffer).add(totalRevokeAcBusTax).subtract(totalRevokeCommissionAmount);
		grandTotalMap.put("Cancel Sub Total", finalSubTotal);
		grandTotalMap.put("Net Amount", bookingSubAmount.subtract(finalSubTotal).add(totalCancellationCharge).subtract(totalTripCancelAmount));

		// Create Excel
		createExcel(authDTO, workbook, sheet, jsonObject, bookings, cancellations, grandTotalMap, fileName);

		Map<String, Object> workBookMap = new HashMap<>();
		workBookMap.put(Text.WORK_BOOK, workbook);
		workBookMap.put(Text.FILE_NAME, fileName);
		return workBookMap;
	}

	private void createExcel(AuthDTO authDTO, XSSFWorkbook workbook, XSSFSheet sheet, JSONObject jsonObject, List<Map<String, ?>> bookings, List<Map<String, ?>> cancellations, Map<String, BigDecimal> grandTotalMap, String fileName) {
		CellStyle headerStyle = ExportExcelHelper.createHeaderStyle(workbook);
		CellStyle subHeaderBookingStyle = ExportExcelHelper.createBookingStyle(workbook);
		CellStyle subHeaderCancelStyle = ExportExcelHelper.createCancelStyle(workbook);
		CellStyle greenStyle = ExportExcelHelper.createGreenHighlightStyle(workbook);
		CellStyle redStyle = ExportExcelHelper.createRedHighlightStyle(workbook);
		CellStyle blueStyle = ExportExcelHelper.createBlueHighlightStyle(workbook);
		CellStyle cellStyle = ExportExcelHelper.createCellStyle(workbook);

		int rownum = 0;

		JSONObject keyword = getKeywords();
		JSONArray keys = keyword.names();

		Row fileHeaderRow = sheet.createRow(rownum++);
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, keys.size() - 1));
		Cell fileHeaderCell = fileHeaderRow.createCell(0);
		fileHeaderCell.setCellStyle(subHeaderBookingStyle);
		fileHeaderCell.setCellValue(fileName);

		sheet.createRow(rownum++);

		Row header = sheet.createRow(rownum++);
		int col = 0;
		Map<String, Integer> position = new HashMap<>();
		for (int pos = 0; pos < keys.size(); pos++) {
			Cell cell = header.createCell(col++);
			cell.setCellStyle(headerStyle);
			cell.setCellValue(keyword.getString(keys.getString(pos)));
			position.put(keys.getString(pos), pos);
		}

		if (!bookings.isEmpty()) {
			Row bookingRow = sheet.createRow(rownum++);
			Cell bookingCell = bookingRow.createCell(0);
			bookingCell.setCellStyle(subHeaderBookingStyle);
			bookingCell.setCellValue("Bookings");

			for (Map<String, ?> resultMap : bookings) {
				int cellnum = 0;
				Row row = sheet.createRow(rownum++);

				for (int pos = 0; pos < keys.size(); pos++) {
					String columnName = keys.getString(cellnum);
					Cell cell = row.createCell(cellnum++);
					cell.setCellStyle(cellStyle);
					cell.setCellValue(String.valueOf(resultMap.get(columnName)));
				}
			}
			sheet.createRow(rownum++);
		}
		if (!cancellations.isEmpty()) {
			Row cancellationRow = sheet.createRow(rownum++);
			Cell cancelCell = cancellationRow.createCell(0);
			cancelCell.setCellStyle(subHeaderCancelStyle);
			cancelCell.setCellValue("Cancellations");

			for (Map<String, ?> resultMap : cancellations) {
				int cellnum = 0;
				Row row = sheet.createRow(rownum++);

				for (int pos = 0; pos < keys.size(); pos++) {
					String columnName = keys.getString(cellnum);
					Cell cell = row.createCell(cellnum++);
					cell.setCellStyle(cellStyle);
					cell.setCellValue(String.valueOf(resultMap.get(columnName)));
				}
			}
			sheet.createRow(rownum++);
			sheet.createRow(rownum++);
			sheet.createRow(rownum++);
		}

		sheet.createRow(rownum++);

		Row summaryRow = sheet.createRow(rownum++);
		Cell cell = summaryRow.createCell(1);
		cell.setCellStyle(greenStyle);
		cell.setCellValue("Summary");

		Row bookingRow = sheet.createRow(rownum++);
		Cell bookingCell = bookingRow.createCell(1);
		bookingCell.setCellStyle(cellStyle);
		bookingCell.setCellValue("Booking Fare ( A )");

		Cell bookingValueCell = bookingRow.createCell(2);
		bookingValueCell.setCellStyle(cellStyle);
		bookingValueCell.setCellValue(grandTotalMap.get("Online").setScale(0, BigDecimal.ROUND_DOWN).doubleValue());

		Row taxRow = sheet.createRow(rownum++);
		Cell taxCell = taxRow.createCell(1);
		taxCell.setCellStyle(cellStyle);
		taxCell.setCellValue("A/C Bus GST ( B )");

		Cell taxValueCell = taxRow.createCell(2);
		taxValueCell.setCellStyle(cellStyle);
		taxValueCell.setCellValue(grandTotalMap.get("A/C Bus Tax").setScale(0, BigDecimal.ROUND_DOWN).doubleValue());

		Row tdsRow = sheet.createRow(rownum++);
		Cell tdsCell = tdsRow.createCell(1);
		tdsCell.setCellStyle(cellStyle);
		tdsCell.setCellValue("TDS ( C )");

		Cell tdsValueCell = tdsRow.createCell(2);
		tdsValueCell.setCellStyle(cellStyle);
		tdsValueCell.setCellValue(grandTotalMap.get("TDS").setScale(0, BigDecimal.ROUND_DOWN).doubleValue());

		Row chargeTaxRow = sheet.createRow(rownum++);
		Cell chargeTaxCell = chargeTaxRow.createCell(1);
		chargeTaxCell.setCellStyle(cellStyle);
		chargeTaxCell.setCellValue("Charge Tax Amount ( D )");

		Cell chargeTaxCellValueCell = chargeTaxRow.createCell(2);
		chargeTaxCellValueCell.setCellStyle(cellStyle);
		chargeTaxCellValueCell.setCellValue(grandTotalMap.get("Charge Tax Amount").setScale(0, BigDecimal.ROUND_DOWN).doubleValue());

		Row discountRow = sheet.createRow(rownum++);
		Cell discountCell = discountRow.createCell(1);
		discountCell.setCellStyle(cellStyle);
		discountCell.setCellValue("Discount ( E )");

		Cell discountValueCell = discountRow.createCell(2);
		discountValueCell.setCellStyle(cellStyle);
		discountValueCell.setCellValue(grandTotalMap.get("Discount Offer").setScale(0, BigDecimal.ROUND_DOWN).doubleValue());

		Row commissionRow = sheet.createRow(rownum++);
		Cell commissionCell = commissionRow.createCell(1);
		commissionCell.setCellStyle(cellStyle);
		commissionCell.setCellValue("Commission ( F )");

		Cell commissionValueCell = commissionRow.createCell(2);
		commissionValueCell.setCellStyle(cellStyle);
		commissionValueCell.setCellValue(grandTotalMap.get("Commission Amount").setScale(0, BigDecimal.ROUND_DOWN).doubleValue());

		Row netAmountRow = sheet.createRow(rownum++);
		Cell netAmountCell = netAmountRow.createCell(1);
		netAmountCell.setCellStyle(greenStyle);
		netAmountCell.setCellValue("Booking Sub Total ( G = A+B+C+D-E-F )");

		Cell netAmountValueCell = netAmountRow.createCell(2);
		netAmountValueCell.setCellStyle(greenStyle);
		netAmountValueCell.setCellValue(grandTotalMap.get("Booking Sub Total").setScale(0, BigDecimal.ROUND_DOWN).doubleValue());

		Row cancellationAmountRow = sheet.createRow(rownum++);
		Cell cancellationAmountCell = cancellationAmountRow.createCell(1);
		cancellationAmountCell.setCellStyle(cellStyle);
		cancellationAmountCell.setCellValue("Cancellation Fare ( H )");

		Cell cancellationAmountValueCell = cancellationAmountRow.createCell(2);
		cancellationAmountValueCell.setCellStyle(cellStyle);
		cancellationAmountValueCell.setCellValue(grandTotalMap.get("Cancellation Amount").setScale(0, BigDecimal.ROUND_DOWN).doubleValue());

		Row revokeTaxRow = sheet.createRow(rownum++);
		Cell revokeTaxCell = revokeTaxRow.createCell(1);
		revokeTaxCell.setCellStyle(cellStyle);
		revokeTaxCell.setCellValue("Revoke A/C Bus GST ( I )");

		Cell revokeTaxValueCell = revokeTaxRow.createCell(2);
		revokeTaxValueCell.setCellStyle(cellStyle);
		revokeTaxValueCell.setCellValue(grandTotalMap.get("Revoke A/C Bus Tax").setScale(0, BigDecimal.ROUND_DOWN).doubleValue());

		Row revokeTdsRow = sheet.createRow(rownum++);
		Cell revokeTdsCell = revokeTdsRow.createCell(1);
		revokeTdsCell.setCellStyle(cellStyle);
		revokeTdsCell.setCellValue("Cancel TDS ( J )");

		Cell revokeTdsValueCell = revokeTdsRow.createCell(2);
		revokeTdsValueCell.setCellStyle(cellStyle);
		revokeTdsValueCell.setCellValue(grandTotalMap.get("Cancel TDS").setScale(0, BigDecimal.ROUND_DOWN).doubleValue());

		Row revokeChargeTaxRow = sheet.createRow(rownum++);
		Cell revokeChargeTaxCell = revokeChargeTaxRow.createCell(1);
		revokeChargeTaxCell.setCellStyle(cellStyle);
		revokeChargeTaxCell.setCellValue("Cancel Charge Tax Amount ( K )");

		Cell revokeChargeTaxValueCell = revokeChargeTaxRow.createCell(2);
		revokeChargeTaxValueCell.setCellStyle(cellStyle);
		revokeChargeTaxValueCell.setCellValue(grandTotalMap.get("Cancel Charge Tax Amount").setScale(0, BigDecimal.ROUND_DOWN).doubleValue());

		Row revokeDiscountRow = sheet.createRow(rownum++);
		Cell revokeDiscountCell = revokeDiscountRow.createCell(1);
		revokeDiscountCell.setCellStyle(cellStyle);
		revokeDiscountCell.setCellValue("Revoke Discount ( L )");

		Cell revokeDiscountValueCell = revokeDiscountRow.createCell(2);
		revokeDiscountValueCell.setCellStyle(cellStyle);
		revokeDiscountValueCell.setCellValue(grandTotalMap.get("Revoke Discount Offer").setScale(0, BigDecimal.ROUND_DOWN).doubleValue());

		Row cancelCommissionRow = sheet.createRow(rownum++);
		Cell cancelCommissionCell = cancelCommissionRow.createCell(1);
		cancelCommissionCell.setCellStyle(cellStyle);
		cancelCommissionCell.setCellValue("Revoke Commission ( M )");

		Cell cancelCommissionValueCell = cancelCommissionRow.createCell(2);
		cancelCommissionValueCell.setCellStyle(cellStyle);
		cancelCommissionValueCell.setCellValue(grandTotalMap.get("Revoke Commission Amount").setScale(0, BigDecimal.ROUND_DOWN).doubleValue());

		Row subTotalRow = sheet.createRow(rownum++);
		Cell subTotalCell = subTotalRow.createCell(1);
		subTotalCell.setCellStyle(redStyle);
		subTotalCell.setCellValue("Cancel Sub Total ( J = F-G-H-I )");

		Cell subTotalValueCell = subTotalRow.createCell(2);
		subTotalValueCell.setCellStyle(redStyle);
		subTotalValueCell.setCellValue(grandTotalMap.get("Cancel Sub Total").setScale(0, BigDecimal.ROUND_DOWN).doubleValue());

		Row cancellationChargeRow = sheet.createRow(rownum++);
		Cell cancellationChargeCell = cancellationChargeRow.createCell(1);
		cancellationChargeCell.setCellStyle(cellStyle);
		cancellationChargeCell.setCellValue("Cancellation Charge ( K )");

		Cell cancellationChargeValueCell = cancellationChargeRow.createCell(2);
		cancellationChargeValueCell.setCellStyle(cellStyle);
		cancellationChargeValueCell.setCellValue(grandTotalMap.get("Cancellation Charge").setScale(0, BigDecimal.ROUND_DOWN).doubleValue());

		Row totalRow = sheet.createRow(rownum++);
		Cell totalCell = totalRow.createCell(1);
		totalCell.setCellStyle(blueStyle);
		totalCell.setCellValue("Net Amount ( L = G-J+K )");

		Cell totalValueCell = totalRow.createCell(2);
		totalValueCell.setCellStyle(blueStyle);
		totalValueCell.setCellValue(grandTotalMap.get("Net Amount").setScale(0, BigDecimal.ROUND_DOWN).doubleValue());
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
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("travel_date", "Travel Date");
		jsonObject.put("transaction_date", "Transaction Date");
		jsonObject.put("user_group_name", "Group Name");
		jsonObject.put("user_name", "User Name");
		jsonObject.put("from_station_name", "From Station Name");
		jsonObject.put("to_station_name", "To Station Name");
		jsonObject.put("ticket_code", "PNR");
		jsonObject.put("ticket_status_code", "Ticket Status Code");
		jsonObject.put("seat_count", "Seat Count");
		jsonObject.put("ticket_amount", "Fare");
		jsonObject.put("ac_bus_tax", "GST");
		jsonObject.put("tds_tax", "TDS");
		jsonObject.put("charge_tax_amount", "Charge Tax Amount");
		jsonObject.put("addons_amount", "Discount");
		jsonObject.put("commission_amount", "Commission Amount");
		jsonObject.put("cancellation_charges", "Cancellation Charge");
		jsonObject.put("refund_amount", "Refund Amount");
		return jsonObject;
	}
}
