package org.in.com.service.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.DBQueryParamDTO;
import org.in.com.dto.ReportQueryDTO;
import org.in.com.service.ReportInterface;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class DynamicReportServiceImpl implements ReportInterface {

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
		return params;
	}

	@Override
	public Map<String, Object> createWorkBook(AuthDTO authDTO, JSONObject jsonObject, List<Map<String, ?>> results, String fromDate, String toDate) {
		String tempfileName = String.valueOf(jsonObject.getString("reportName"));
		String fileName = tempfileName;

		List<Map<String, ?>> transactions = new ArrayList<>();
		Set<String> keys = !results.isEmpty() ? results.get(0).keySet() : new HashSet<>();
		List<String> headers = new ArrayList<>(keys);

		for (Map<String, ?> resultMap : results) {
			transactions.add(resultMap);
		}
		// Create Excel
		List<String> csvDataList = createExcelV2(authDTO, transactions, headers, fileName);
		Map<String, Object> workBookMap = new HashMap<>();
		workBookMap.put(Text.WORK_BOOK, csvDataList);
		workBookMap.put(Text.FILE_NAME, fromDate + Text.SPACE_HYPHEN + toDate);
		return workBookMap;
	}

	private List<String> createExcelV2(AuthDTO authDTO, List<Map<String, ?>> transactions, List<String> headerList, String fileName) {
		List<String> finalList = new ArrayList<String>();

		JSONObject bookingKeyword = getKeywords(headerList);
		JSONArray bookingKeys = bookingKeyword.names();
		StringBuilder transactionHeader = new StringBuilder();
		for (int pos = 0; pos < bookingKeys.size(); pos++) {
			if (transactionHeader.length() > 0) {
				transactionHeader.append(Text.COMMA);
			}
			transactionHeader.append(bookingKeyword.getString(bookingKeys.getString(pos)));
		}

		List<String> transactionList = new ArrayList<String>();
		if (!transactions.isEmpty()) {
			for (Map<String, ?> resultMap : transactions) {
				int cellnum = 0;
				StringBuilder transactionValue = new StringBuilder();
				for (int pos = 0; pos < bookingKeys.size(); pos++) {
					String columnName = bookingKeys.getString(cellnum);
					if (transactionValue.length() > 0) {
						transactionValue.append(Text.COMMA);
					}
					String value = String.valueOf(resultMap.get(columnName));
					if (DateUtil.isValidDateV2(value)) {
						if (DateUtil.isValidDate(value)) {
							transactionValue.append(DateUtil.getDateTime(value).format("DD/MM/YYYY"));
						}
						else {
							transactionValue.append(DateUtil.getDateTime(value).format("DD/MM/YYYY hh:mm"));
						}
					}
					else {
						transactionValue.append(StringUtil.isNotNull(value) ? value.replaceAll(Text.COMMA, Text.SINGLE_SPACE) : Text.EMPTY);
					}
					cellnum++;
				}
				transactionList.addAll(Arrays.asList(transactionValue.toString()));
			}
		}

		finalList.addAll(Arrays.asList(fileName));
		finalList.add(transactionHeader.toString());
		finalList.addAll(transactionList);
		return finalList;
	}

	private JSONObject getKeywords(List<String> headerList) {
		JSONObject json = new JSONObject();
		for (String headerKey : headerList) {
			StringBuilder headers = new StringBuilder();
			for (String key : headerKey.split("_")) {
				String firstLetter = key.substring(0, 1);
				firstLetter = firstLetter.toUpperCase();
				String remaingLetters = key.substring(1, key.length());
				if (headers.length() > 0) {
					headers.append(" ");
				}
				headers.append(firstLetter + remaingLetters);
			}
			if (StringUtil.isNotNull(headers.toString())) {
				json.put(headerKey, headers.toString());
			}
		}
		return json;
	}
}
