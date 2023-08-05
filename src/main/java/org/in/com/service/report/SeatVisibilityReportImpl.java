package org.in.com.service.report;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.stereotype.Service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class SeatVisibilityReportImpl implements ReportInterface {

	@Override
	public Map<String, Object> createWorkBook(AuthDTO authDTO, JSONObject jsonObject, List<Map<String, ?>> results, String fromDate, String toDate) {
		XSSFWorkbook workbook = new XSSFWorkbook();

		String tempfileName = String.valueOf(jsonObject.getString("reportName"));

		String sheetName = ExportExcelHelper.generateSheetName(tempfileName, fromDate, toDate);
		String fileName = ExportExcelHelper.generateFileName(tempfileName, fromDate, toDate, null);
		// Create a blank sheet
		XSSFSheet sheet = workbook.createSheet(sheetName);

		createExcel(authDTO, workbook, sheet, jsonObject, results, fileName);

		Map<String, Object> workBookMap = new HashMap<>();
		workBookMap.put(Text.WORK_BOOK, workbook);
		workBookMap.put(Text.FILE_NAME, fileName);

		return workBookMap;
	}

	private void createExcel(AuthDTO authDTO, XSSFWorkbook workbook, XSSFSheet sheet, JSONObject jsonObject, List<Map<String, ?>> seatVisibilityList, String fileName) {
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

		if (seatVisibilityList != null && !seatVisibilityList.isEmpty()) {
			for (Map<String, ?> resultMap : seatVisibilityList) {
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
	}

	@Override
	public List<DBQueryParamDTO> getParams(AuthDTO authDTO, ReportQueryDTO reportQueryDTO, String fromDate, String toDate, String filterDateType) {
		return null;
	}

	private JSONObject getKeywords() {
		JSONObject jsonOject = new JSONObject();
		jsonOject.put("scheduleName", "Schedule");
		jsonOject.put("code", "Visibility Code");
		jsonOject.put("serviceNumber", "Service Number");
		jsonOject.put("roleType", "Role");
		jsonOject.put("userNames", "Users");
		jsonOject.put("groupNames", "Groups");
		jsonOject.put("routes", "Routes");
		jsonOject.put("tripCode", "Trip Code");
		jsonOject.put("busType", "Bus Type");
		jsonOject.put("visibilityType", "Visibility Type");
		jsonOject.put("remarks", "Remarks");
		jsonOject.put("seatNames", "Seats");
		jsonOject.put("updatedBy", "Updated By");
		jsonOject.put("updatedAt", "Updated Date");
		return jsonOject;
	}
}
