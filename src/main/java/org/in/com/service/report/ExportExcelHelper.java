package org.in.com.service.report;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.in.com.constants.Text;
import org.in.com.dto.ReportSummaryDTO;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;

/**
 *
 * @author Arun
 */
public class ExportExcelHelper {

	public static Map<String, ReportSummaryDTO> getTotalKeywords(JSONObject jsonObject, Map<String, Integer> position) {
		JSONArray total = jsonObject.getJSONArray("total");

		Map<String, ReportSummaryDTO> bookingSummaryMap = new HashMap<>();
		for (Object summaryJSON : total) {
			String name = (String) summaryJSON;

			ReportSummaryDTO reportSummaryDTO = new ReportSummaryDTO();
			reportSummaryDTO.setKeyword(name);
			reportSummaryDTO.setColumn(position.get(name));
			bookingSummaryMap.put(name, reportSummaryDTO);
		}
		return bookingSummaryMap;
	}

	public static CellStyle createHeaderStyle(XSSFWorkbook workbook) {
		Font sheetTitleFont = workbook.createFont();
		CellStyle headerStyle = workbook.createCellStyle();
		sheetTitleFont.setBold(true);
		sheetTitleFont.setFontHeightInPoints((short) 12);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerStyle.setFont(sheetTitleFont);
		return headerStyle;
	}

	public static CellStyle createGreenHighlightStyle(XSSFWorkbook workbook) {
		Font sheetTitleFont = workbook.createFont();
		CellStyle headerStyle = workbook.createCellStyle();
		sheetTitleFont.setBold(true);
		sheetTitleFont.setFontHeightInPoints((short) 12);
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setFont(sheetTitleFont);
		return headerStyle;
	}

	public static CellStyle createRedHighlightStyle(XSSFWorkbook workbook) {
		Font sheetTitleFont = workbook.createFont();
		CellStyle headerStyle = workbook.createCellStyle();
		sheetTitleFont.setBold(true);
		sheetTitleFont.setFontHeightInPoints((short) 12);
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setFont(sheetTitleFont);
		return headerStyle;
	}

	public static CellStyle createBlueHighlightStyle(XSSFWorkbook workbook) {
		Font sheetTitleFont = workbook.createFont();
		CellStyle headerStyle = workbook.createCellStyle();
		sheetTitleFont.setBold(true);
		sheetTitleFont.setFontHeightInPoints((short) 12);
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setFont(sheetTitleFont);
		return headerStyle;
	}

	public static CellStyle createBookingStyle(XSSFWorkbook workbook) {
		Font sheetTitleFont = workbook.createFont();
		CellStyle headerStyle = workbook.createCellStyle();
		sheetTitleFont.setBold(true);
		sheetTitleFont.setFontHeightInPoints((short) 11);
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setFont(sheetTitleFont);
		return headerStyle;
	}

	public static CellStyle createCancelStyle(XSSFWorkbook workbook) {
		Font sheetTitleFont = workbook.createFont();
		CellStyle headerStyle = workbook.createCellStyle();
		sheetTitleFont.setBold(true);
		sheetTitleFont.setFontHeightInPoints((short) 11);
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setFont(sheetTitleFont);
		return headerStyle;
	}

	public static CellStyle createCellStyle(XSSFWorkbook workbook) {
		Font cellFont = workbook.createFont();
		CellStyle cellStyle = workbook.createCellStyle();
		cellFont.setFontHeightInPoints((short) 11);
		cellStyle.setAlignment(HorizontalAlignment.LEFT);
		return cellStyle;
	}

	public static String generateSheetName(String sheetName, String fromdate, String todate) {
		sheetName = sheetName.split(Text.HYPHEN)[0].replace("Report", Text.EMPTY).trim().replaceAll("\\s+", Text.UNDER_SCORE).toLowerCase();

		return sheetName;
	}

	public static String generateFileName(String reportName, String fromDate, String toDate, String type) {
		reportName = reportName.split(Text.HYPHEN)[0].replace("Report", Text.EMPTY).trim().toUpperCase();

		StringBuilder fileName = new StringBuilder();

		String monthName = DateUtil.getDateTime(fromDate).format("MMMM", Locale.ENGLISH);
		fileName.append(reportName);
		fileName.append(Text.SINGLE_SPACE);
		fileName.append(Text.HYPHEN);
		fileName.append(Text.SINGLE_SPACE);
		if (StringUtil.isNotNull(type)) {
			fileName.append(type);
			fileName.append(Text.SINGLE_SPACE);
		}
		fileName.append(fromDate.split(Text.HYPHEN)[2]);
		if (StringUtil.isNotNull(type) && !type.equals("DAILY")) {
			fileName.append(Text.HYPHEN);
			fileName.append(toDate.split(Text.HYPHEN)[2]);
		}
		fileName.append(Text.HYPHEN);
		fileName.append(monthName);
		fileName.append(Text.HYPHEN);
		fileName.append(fromDate.split(Text.HYPHEN)[0]);

		return fileName.toString();
	}
}
