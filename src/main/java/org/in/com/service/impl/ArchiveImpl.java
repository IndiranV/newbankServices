package org.in.com.service.impl;

import hirondelle.date4j.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.com.aggregator.bits.BitsService;
import org.in.com.cache.CacheCentral;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.ArchiveDAO;
import org.in.com.dto.DBQueryParamDTO;
import org.in.com.dto.ReportQueryDTO;
import org.in.com.service.ArchiveService;
import org.in.com.service.ScheduleTripService;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ArchiveImpl extends CacheCentral implements ArchiveService {
	public static Logger ARCHIVE_LOGGER = LoggerFactory.getLogger("org.in.com.service.impl.ArchiveImpl");

	@Autowired
	ScheduleTripService scheduleTripService;
	@Autowired
	BitsService bitsService;
	@Autowired
	ArchiveDAO archiveDAO;

	@Override
	public Map<String, List<Map<String, ?>>> getArchiveReport(String tableName, String fromDate, String toDate) {

		Map<String, List<Map<String, ?>>> finalMassData = new HashMap<String, List<Map<String, ?>>>();
		List<Map<String, ?>> finalList = new ArrayList<Map<String, ?>>();
		try {
			Map<String, String> namespaceCodeMap = new HashMap<String, String>();
			List<Map<String, ?>> overallDataList = archiveDAO.getTableForDrill(tableName, fromDate, toDate);

			for (Map<String, ?> overallData : overallDataList) {
				Map<String, Object> finalDataMap = new HashMap<String, Object>();
				finalDataMap.putAll(overallData);

				String namespaceCode = (String) overallData.get("namespace_code");
				if (StringUtil.isNull(namespaceCode)) {
					if ("ticket_detail".equals(tableName) || "ticket_cancel_detail".equals(tableName) || "ticket_cancel_transaction".equals(tableName) || "ticket_addons_detail".equals(tableName)) {
						namespaceCode = archiveDAO.getNamesapce("ticket", (String) overallData.get("ticket_code"), namespaceCodeMap);
						finalDataMap.put("namespace_code", namespaceCode);
					}
					else {
						ARCHIVE_LOGGER.error("Namespace not found - NamespaceCode:" + namespaceCode + " Table:" + tableName + " Date : " + fromDate + " To " + toDate);
					}
				}

				Object value = null;
				if (overallData.get("user_code") != null && StringUtil.isNumeric(overallData.get("user_code").toString())) {
					value = getUniqueCode("user", Integer.valueOf((String) overallData.get("user_code")));
					finalDataMap.put("user_code", value);
				}
				if (overallData.get("group_code") != null && StringUtil.isNumeric(overallData.get("group_code").toString())) {
					value = getUniqueCode("user_group", Integer.valueOf((String) overallData.get("group_code")));
					finalDataMap.put("group_code", value);
				}
				if (overallData.get("for_user_code") != null && StringUtil.isNumeric(overallData.get("for_user_code").toString())) {
					value = getUniqueCode("user", Integer.valueOf((String) overallData.get("for_user_code")));
					finalDataMap.put("for_user_code", value);
				}
				if (overallData.get("station_code") != null && StringUtil.isNumeric(overallData.get("station_code").toString())) {
					value = getUniqueCode("station", Integer.valueOf((String) overallData.get("station_code")));
					finalDataMap.put("from_station_code", value);
				}
				if (overallData.get("from_station_code") != null && StringUtil.isNumeric(overallData.get("from_station_code").toString())) {
					value = getUniqueCode("station", Integer.valueOf((String) overallData.get("from_station_code")));
					finalDataMap.put("from_station_code", value);
				}
				if (overallData.get("to_station_code") != null && StringUtil.isNumeric(overallData.get("to_station_code").toString())) {
					value = getUniqueCode("station", Integer.valueOf((String) overallData.get("to_station_code")));
					finalDataMap.put("to_station_code", value);
				}
				if (overallData.get("boarding_point_code") != null && StringUtil.isNumeric(overallData.get("boarding_point_code").toString())) {
					value = getUniqueCode("station_point", Integer.valueOf((String) overallData.get("boarding_point_code")));
					finalDataMap.put("boarding_point_code", value);
				}
				if (overallData.get("dropping_point_code") != null && StringUtil.isNumeric(overallData.get("dropping_point_code").toString())) {
					value = getUniqueCode("station_point", Integer.valueOf((String) overallData.get("dropping_point_code")));
					finalDataMap.put("dropping_point_code", value);
				}
				if (overallData.get("bus_code") != null && StringUtil.isNumeric(overallData.get("bus_code").toString())) {
					value = getUniqueCode("bus", Integer.valueOf((String) overallData.get("bus_code")));
					finalDataMap.put("bus_code", value);
				}
				if (overallData.get("tax_code") != null && StringUtil.isNotNull(namespaceCode) && StringUtil.isNumeric(overallData.get("tax_code").toString())) {
					value = getUniqueCode("namespace_tax", Integer.valueOf((String) overallData.get("tax_code")));
					finalDataMap.put("tax_code", value);
				}
				if (overallData.get("schedule_code") != null && StringUtil.isNumeric(overallData.get("schedule_code").toString())) {
					value = getUniqueCode("schedule", Integer.valueOf((String) overallData.get("schedule_code")));
					finalDataMap.put("schedule_code", value);
				}
				finalList.add(finalDataMap);
			}

			for (Map<String, ?> overallData : finalList) {
				String key = (String) overallData.get("namespace_code");
				if (finalMassData == null || finalMassData.size() == Numeric.ZERO_INT || finalMassData.get(key) == null) {
					List<Map<String, ?>> namespaceData = new ArrayList<Map<String, ?>>();
					namespaceData.add(overallData);
					finalMassData.put(key, namespaceData);
				}
				else {
					List<Map<String, ?>> namespaceDataList = finalMassData.get(key);
					namespaceDataList.add(overallData);
					finalMassData.put(key, namespaceDataList);
				}

			}
		}
		catch (Exception e) {
			ARCHIVE_LOGGER.error("Export failed! Table:" + tableName + " Date : " + fromDate + " To " + toDate + " Error Msg:" + e.getMessage());
			e.printStackTrace();
		}
		return finalMassData;
	}

	private Object getUniqueCode(String tableName, int id) {
		Object value = null;
		try {
			value = archiveDAO.getCode(tableName, id);
			if (value == null) {
				value = "NULL";
			}
		}
		catch (Exception e) {
			ARCHIVE_LOGGER.info("No data found - Table:" + tableName + " Id:" + id + " Error Msg:" + e.getMessage());
		}
		return value;
	}

	@Override
	public Map<String, List<Map<String, ?>>> getMasterForDrill(String tableName, String fromDate, String toDate) {
		List<Map<String, ?>> overallDataList = archiveDAO.getMasterForDrill(tableName, fromDate, toDate);
		Map<String, List<Map<String, ?>>> masterDataList = new HashMap<String, List<Map<String, ?>>>();
		masterDataList.put("master", overallDataList);
		return masterDataList;
	}

	@Override
	public Map<String, List<Map<String, ?>>> getBitsTicketTransaction(DateTime fromDate, DateTime toDate) {
		System.out.println("Overall " + fromDate.format(Text.DATE_DATE4J) + " To " + toDate.format(Text.DATE_DATE4J));
		String query = "CALL EZEE_SP_RPT_OVERALL_TICKET_TRANSACTION(:fromDate, :toDate)";

		Map<String, List<Map<String, ?>>> finalMapList = new HashMap<String, List<Map<String, ?>>>();
		// Booking and Cancellation
		List<DBQueryParamDTO> paramList = new ArrayList<DBQueryParamDTO>();
		DBQueryParamDTO fromDateParam = new DBQueryParamDTO();
		fromDateParam.setParamName("fromDate");
		fromDateParam.setValue(fromDate.format(Text.DATE_DATE4J));
		paramList.add(fromDateParam);

		DBQueryParamDTO toDateParam = new DBQueryParamDTO();
		toDateParam.setParamName("toDate");
		toDateParam.setValue(toDate.format(Text.DATE_DATE4J));
		paramList.add(toDateParam);

		ReportQueryDTO reportQueryDTO = new ReportQueryDTO();
		reportQueryDTO.setQuery(query);

		List<Map<String, ?>> dataMapList = getQueryResultsMap(reportQueryDTO, paramList);

		for (Map<String, ?> overallData : dataMapList) {
			String key = (String) overallData.get("namespace_code");
			if (finalMapList == null || finalMapList.size() == Numeric.ZERO_INT || finalMapList.get(key) == null) {
				List<Map<String, ?>> namespaceData = new ArrayList<Map<String, ?>>();
				namespaceData.add(overallData);
				finalMapList.put(key, namespaceData);
			}
			else {
				List<Map<String, ?>> namespaceDataList = finalMapList.get(key);
				namespaceDataList.add(overallData);
				finalMapList.put(key, namespaceDataList);
			}
		}
		return finalMapList;
	}

	public List<Map<String, ?>> getQueryResultsMap(ReportQueryDTO reportQueryDTO, List<DBQueryParamDTO> params) {
		List<Map<String, ?>> listMapData = archiveDAO.getQueryResultsMap(reportQueryDTO.getQuery(), params);
		return listMapData;
	}

}
