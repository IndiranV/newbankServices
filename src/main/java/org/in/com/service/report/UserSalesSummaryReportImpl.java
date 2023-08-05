package org.in.com.service.report;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.beanutils.BeanComparator;
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
import org.in.com.dto.ReportSummaryDTO;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.service.ReportInterface;
import org.in.com.utils.StringUtil;
import org.springframework.stereotype.Service;

@Service
public class UserSalesSummaryReportImpl implements ReportInterface {

	@Override
	public Map<String, Object> createWorkBook(AuthDTO authDTO, JSONObject jsonObject, List<Map<String, ?>> results, String fromDate, String toDate) {
		// Blank workbook
		XSSFWorkbook workbook = new XSSFWorkbook();

		String tempfileName = String.valueOf(jsonObject.getString("reportName"));
		String type = String.valueOf(jsonObject.getString("datePeriod")).trim().toUpperCase();

		String sheetName = ExportExcelHelper.generateSheetName(tempfileName, fromDate, toDate);
		String fileName = ExportExcelHelper.generateFileName(tempfileName, fromDate, toDate, type);

		// Create a blank sheet
		XSSFSheet sheet = workbook.createSheet(sheetName);

		List<Map<String, ?>> bookings = new ArrayList<>();

		Map<String, Map<String, Object>> userMap = new HashMap<>();
		List<String> cancelledUsers = new ArrayList<String>();

		for (Map<String, ?> resultMap : results) {
			if (TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getCode().equals(resultMap.get("ticket_status_code")) || TicketStatusEM.TRIP_CANCELLED.getCode().equals(resultMap.get("ticket_status_code"))) {
				String userCode = String.valueOf(resultMap.get("user_code"));
				cancelledUsers.add(userCode);
			}
		}
		for (Map<String, ?> resultMap : results) {
			if (resultMap.get("ticket_status_code") == null) {
				continue;
			}
			boolean userExist = false;
			Map<String, Object> subMap = new HashMap<>();

			BigDecimal subPayableAmount = BigDecimal.ZERO;
			BigDecimal subGrossAmount = BigDecimal.ZERO;

			subGrossAmount = StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("ticket_amount"))).add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("ac_bus_tax")))).subtract(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("addon_amount")))).subtract(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("commission_amount"))));
			subMap.putAll(resultMap);
			subMap.put("cancel_seats", BigDecimal.ZERO.setScale(0));
			subMap.put("gross_amount", subGrossAmount);
			subMap.put("payable_amount", subGrossAmount);
			subMap.put("agent_share", BigDecimal.ZERO.setScale(2));
			subMap.put("cancel_amount", BigDecimal.ZERO.setScale(2));
			subMap.put("cancel_discount", BigDecimal.ZERO.setScale(2));
			subMap.put("cancel_ac_bus_tax", BigDecimal.ZERO.setScale(2));
			subMap.put("cancel_tds_tax", BigDecimal.ZERO.setScale(2));
			subMap.put("cancel_charge_tax_amount", BigDecimal.ZERO.setScale(2));
			subMap.put("revenue", BigDecimal.ZERO.setScale(2));

			if (userMap.get(String.valueOf(resultMap.get("user_code"))) != null) {
				subMap = userMap.get(String.valueOf(resultMap.get("user_code")));
				subPayableAmount = StringUtil.getBigDecimalValue(String.valueOf(subMap.get("gross_amount"))).subtract(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("refund_amount"))));

				userMap.remove(String.valueOf(resultMap.get("user_code")));

				subMap.put("cancel_seats", resultMap.get("seat_count"));
				subMap.put("cancel_amount", resultMap.get("ticket_amount"));
				subMap.put("revoke_commission_amount", resultMap.get("revoke_commission_amount"));
				subMap.put("cancel_discount", resultMap.get("addon_amount"));
				subMap.put("cancellation_charges", resultMap.get("cancellation_charges"));
				subMap.put("revenue", resultMap.get("cancellation_charges"));
				subMap.put("refund_amount", resultMap.get("refund_amount"));
				subMap.put("cancel_ac_bus_tax", resultMap.get("ac_bus_tax"));
				subMap.put("cancel_charge_tax_amount", resultMap.get("charge_tax_amount"));
				subMap.put("cancel_tds_tax", resultMap.get("tds_tax"));
				subMap.put("payable_amount", subPayableAmount);
				userMap = new HashMap<>();
				userExist = true;
			}
			userMap.put(String.valueOf(resultMap.get("user_code")), subMap);

			if (!userExist && !cancelledUsers.contains(String.valueOf(resultMap.get("user_code")))) {
				userExist = true;
			}
			if (userExist) {
				bookings.add(userMap);
				userMap = new HashMap<>();
			}
		}

		// Create Excel
		createExcel(authDTO, workbook, sheet, jsonObject, bookings, null, null, fileName);

		Map<String, Object> workBookMap = new HashMap<>();
		workBookMap.put(Text.WORK_BOOK, workbook);
		workBookMap.put(Text.FILE_NAME, fileName);
		return workBookMap;
	}

	private void createExcel(AuthDTO authDTO, XSSFWorkbook workbook, XSSFSheet sheet, JSONObject jsonObject, List<Map<String, ?>> bookings, List<Map<String, ?>> cancellations, Map<String, BigDecimal> grandTotalMap, String fileName) {
		CellStyle headerStyle = ExportExcelHelper.createHeaderStyle(workbook);
		CellStyle subHeaderBookingStyle = ExportExcelHelper.createBookingStyle(workbook);
		CellStyle subHeaderCancelStyle = ExportExcelHelper.createCancelStyle(workbook);
		CellStyle blueStyle = ExportExcelHelper.createBlueHighlightStyle(workbook);
		CellStyle cellStyle = ExportExcelHelper.createCellStyle(workbook);

		int rownum = 0;

		Row fileHeaderRow = sheet.createRow(rownum++);
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 21));
		Cell fileHeaderCell = fileHeaderRow.createCell(0);
		fileHeaderCell.setCellStyle(subHeaderBookingStyle);
		fileHeaderCell.setCellValue(fileName);

		sheet.createRow(rownum++);

		Row bookingRow = sheet.createRow(rownum++);
		sheet.addMergedRegion(new CellRangeAddress(2, 2, 2, 9));
		Cell bookingCell = bookingRow.createCell(2);
		bookingCell.setCellStyle(subHeaderBookingStyle);
		bookingCell.setCellValue("Bookings");

		sheet.addMergedRegion(new CellRangeAddress(2, 2, 10, 18));
		Cell cancelCell = bookingRow.createCell(10);
		cancelCell.setCellStyle(subHeaderCancelStyle);
		cancelCell.setCellValue("Cancellation");

		JSONObject keyword = getKeywords().getJSONObject("keyword");
		JSONArray keys = keyword.names();
		Row header = sheet.createRow(rownum++);
		Map<String, Integer> position = new HashMap<>();
		int col = 0;
		for (int pos = 0; pos < keys.size(); pos++) {
			Cell cell = header.createCell(col++);
			cell.setCellStyle(headerStyle);
			cell.setCellValue(keyword.getString(keys.getString(pos)));
			position.put(keys.getString(pos), pos);
		}

		Map<String, ReportSummaryDTO> totalMap = ExportExcelHelper.getTotalKeywords(getKeywords(), position);
		if (!bookings.isEmpty()) {

			for (Map<String, ?> resultParentMap : bookings) {
				Set<String> userCodes = resultParentMap.keySet();
				Row row = sheet.createRow(rownum++);
				for (String userCode : userCodes) {
					int cellnum = 0;
					Map<String, Object> resultMap = (Map<String, Object>) resultParentMap.get(userCode);
					for (int pos = 0; pos < keys.size(); pos++) {
						String columnName = keys.getString(cellnum);
						Cell cell = row.createCell(cellnum++);
						cell.setCellStyle(cellStyle);
						if (String.valueOf(resultMap.get(columnName)).matches("-?\\d+(\\.\\d+)?")) {
							cell.setCellValue(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get(columnName))).setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue());
						}
						else {
							cell.setCellValue(String.valueOf(resultMap.get(columnName)));
						}

						ReportSummaryDTO reportSummaryDTO = totalMap.get(columnName);
						if (reportSummaryDTO != null) {
							BigDecimal value = StringUtil.getBigDecimalValue(String.valueOf(resultMap.get(columnName)));
							reportSummaryDTO.setValue(StringUtil.isNotNull(reportSummaryDTO.getValue()) ? reportSummaryDTO.getValue().add(value).setScale(2) : value.setScale(2));
							totalMap.put(columnName, reportSummaryDTO);
						}
					}
				}
			}
			sheet.createRow(rownum++);

			Row summaryHeaderRow = sheet.createRow(rownum++);
			List<ReportSummaryDTO> summaryList = new ArrayList<>(totalMap.values());
			// Sorting
			Comparator<ReportSummaryDTO> comp = new BeanComparator("column");
			Collections.sort(summaryList, comp);

			for (ReportSummaryDTO reportSummaryDTO : summaryList) {
				Cell cell = summaryHeaderRow.createCell(reportSummaryDTO.getColumn());
				cell.setCellValue(reportSummaryDTO.getValue().setScale(2).doubleValue());
				cell.setCellStyle(blueStyle);
			}
		}
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
		return params;
	}
	
	private JSONObject getKeywords() {
		JSONObject json = new JSONObject();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("user_group_name", "Group Name");
		jsonObject.put("user_name", "User Name");
		jsonObject.put("seat_count", "Seats");
		jsonObject.put("ticket_amount", "Fare");
		jsonObject.put("addon_amount", "Discount");
		jsonObject.put("ac_bus_tax", "GST");
		jsonObject.put("tds_tax", "TDS");
		jsonObject.put("charge_tax_amount", "Charge Tax Amount");
		jsonObject.put("commission_amount", "Commission Amount");
		jsonObject.put("gross_amount", "Gross");
		jsonObject.put("cancel_seats", "Seats");
		jsonObject.put("cancel_amount", "Fare");
		jsonObject.put("cancel_discount", "Discount");
		jsonObject.put("cancel_ac_bus_tax", "GST");
		jsonObject.put("cancel_tds_tax", "Cancel TDS");
		jsonObject.put("cancel_charge_tax_amount", "Cancel Charge Tax Amount");
		jsonObject.put("revoke_commission_amount", "Revoke Commision Amount");
		jsonObject.put("cancellation_charges", "Cancellation Charge");
		jsonObject.put("agent_share", "Agent Share");
		jsonObject.put("revenue", "Revenue");
		jsonObject.put("refund_amount", "Refund Amount");
		jsonObject.put("payable_amount", "Payable");
		json.put("keyword", jsonObject);
		
		JSONArray jsonArray = new JSONArray();
		jsonArray.add("seat_count");
		jsonArray.add("ticket_amount");
		jsonArray.add("addon_amount");
		jsonArray.add("ac_bus_tax");
		jsonArray.add("tds_tax");
		jsonArray.add("charge_tax_amount");
		jsonArray.add("commission_amount");
		jsonArray.add("gross_amount");
		jsonArray.add("cancel_seats");
		jsonArray.add("cancel_amount");
		jsonArray.add("cancel_discount");
		jsonArray.add("cancel_ac_bus_tax");
		jsonArray.add("cancel_tds_tax");
		jsonArray.add("cancel_charge_tax_amount");
		jsonArray.add("revoke_commission_amount");
		jsonArray.add("cancellation_charges");
		jsonArray.add("agent_share");
		jsonArray.add("revenue");
		jsonArray.add("refund_amount");
		jsonArray.add("payable_amount");
		
		json.put("total", jsonArray);
		return json;
	}
}
