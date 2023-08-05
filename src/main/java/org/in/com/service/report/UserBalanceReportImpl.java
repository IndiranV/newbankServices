package org.in.com.service.report;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.DBQueryParamDTO;
import org.in.com.dto.ReportQueryDTO;
import org.in.com.service.ReportInterface;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;

public class UserBalanceReportImpl implements ReportInterface {

	@Override
	public Map<String, Object> createWorkBook(AuthDTO authDTO, JSONObject jsonObject, List<Map<String, ?>> results, String fromDate, String toDate) {
		// Blank workbook
		XSSFWorkbook workbook = new XSSFWorkbook();

		String tempfileName = String.valueOf(jsonObject.getString("reportName"));

		String sheetName = ExportExcelHelper.generateSheetName(tempfileName, fromDate, toDate);
		String fileName = ExportExcelHelper.generateFileName(tempfileName, fromDate, toDate, null);
		// Create a blank sheet
		XSSFSheet sheet = workbook.createSheet(sheetName);

		List<Map<String, ?>> userBalanceList = new ArrayList<>();
		for (Map<String, ?> resultMap : results) {
			BigDecimal availableCreditLimit = BigDecimal.ZERO;

			Map<String, Object> subMap = new HashMap<String, Object>();

			subMap.put("name", StringUtil.isNotNull(String.valueOf(resultMap.get("first_name"))) ? String.valueOf(resultMap.get("first_name")) : Text.EMPTY);
			subMap.put("days_till", StringUtil.isNotNull(String.valueOf(resultMap.get("last_transaction_date"))) ? DateUtil.getDayDifferent(DateUtil.getDateTime(String.valueOf(resultMap.get("last_transaction_date"))), DateUtil.NOW()) : Text.EMPTY);

			availableCreditLimit = availableCreditLimit.add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("credit_limit")))).add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("current_balance"))));
			subMap.put("available_credit_limit", availableCreditLimit);

			subMap.putAll(resultMap);

			if (StringUtil.isNull(String.valueOf(subMap.get("last_transaction_date")))) {
				subMap.put("last_transaction_date", Text.EMPTY);
			}
			if (StringUtil.isNull(String.valueOf(subMap.get("mobile_number")))) {
				subMap.put("mobile_number", Text.EMPTY);
			}
			if (StringUtil.isNull(String.valueOf(subMap.get("user_group_name")))) {
				subMap.put("user_group_name", Text.EMPTY);
			}

			userBalanceList.add(subMap);
		}

		// Create Excel
		createExcel(authDTO, workbook, sheet, jsonObject, userBalanceList, results, null, fileName);

		Map<String, Object> workBookMap = new HashMap<>();
		workBookMap.put(Text.WORK_BOOK, workbook);
		workBookMap.put(Text.FILE_NAME, fileName);

		return workBookMap;
	}

	private void createExcel(AuthDTO authDTO, XSSFWorkbook workbook, XSSFSheet sheet, JSONObject jsonObject, List<Map<String, ?>> userBalanceList, List<Map<String, ?>> cancellations, Map<String, BigDecimal> grandTotalMap, String fileName) {
		CellStyle headerStyle = ExportExcelHelper.createHeaderStyle(workbook);
		CellStyle subHeaderBookingStyle = ExportExcelHelper.createBookingStyle(workbook);
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
		for (int pos = 0; pos < keys.size(); pos++) {
			Cell cell = header.createCell(col++);
			cell.setCellStyle(headerStyle);
			cell.setCellValue(keyword.getString(keys.getString(pos)));
		}

		if (userBalanceList != null && !userBalanceList.isEmpty()) {
			for (Map<String, ?> resultMap : userBalanceList) {
				int cellnum = 0;
				Row row = sheet.createRow(rownum++);
				for (int pos = 0; pos < keys.size(); pos++) {
					String columnName = keys.getString(cellnum);
					Cell cell = row.createCell(cellnum++);
					cell.setCellStyle(cellStyle);
					if (String.valueOf(resultMap.get(columnName)).matches("-?\\d+(\\.\\d+)?")) {
						cell.setCellValue(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get(columnName))).setScale(2).doubleValue());
					}
					else {
						cell.setCellValue(String.valueOf(resultMap.get(columnName)));
					}
				}
			}
			sheet.createRow(rownum++);
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
		if (reportQueryDTO.getQuery().contains(":transactionDate")) {
			DBQueryParamDTO paramDTO = new DBQueryParamDTO();
			paramDTO.setParamName("transactionDate");
			paramDTO.setValue(Text.NA);
			params.add(paramDTO);
		}
		return params;
	}
	
	private JSONObject getKeywords() {
		JSONObject jsonOject = new JSONObject();
		jsonOject.put("user_group_name", "Group Name");
		jsonOject.put("payment_type_code", "Payment Type");
		jsonOject.put("name", "Name");
		jsonOject.put("mobile_number", "Mobile");
		jsonOject.put("last_transaction_date", "Last Transaction Date");
		jsonOject.put("days_till", "Days Till Last Transaction");
		jsonOject.put("credit_limit","Credit Limit");
		jsonOject.put("current_balance", "Current Balance");
		jsonOject.put("booking_commission_value", "Booking Commision");
		jsonOject.put("cancel_agent_share", "Cancellation Share");
		jsonOject.put("user_tds", "User TDS");
		jsonOject.put("gst", "GST");
		jsonOject.put("cancel_commission_value", "Cancel Commission");
		jsonOject.put("available_credit_limit", "Available Credit Limit");
		return jsonOject;
	}
}
