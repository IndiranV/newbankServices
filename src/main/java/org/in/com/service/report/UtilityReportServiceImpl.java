package org.in.com.service.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.DBQueryParamDTO;
import org.in.com.dto.ReportQueryDTO;
import org.in.com.service.ReportQueryService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

import hirondelle.date4j.DateTime;

@Component
public class UtilityReportServiceImpl {
	@Autowired
	ReportQueryService reportQueryService;

	public Map<String, Object> getSalesSummaryDetails(AuthDTO authDTO, DateTime tripDate) {
		Map<String, Object> dataModel = Maps.newHashMap();

		List<DBQueryParamDTO> paramList = new ArrayList<DBQueryParamDTO>();
		DBQueryParamDTO namespaceParam = new DBQueryParamDTO();
		namespaceParam.setParamName("namespaceId");
		namespaceParam.setValue(String.valueOf(authDTO.getNamespace().getId()));
		paramList.add(namespaceParam);

		DBQueryParamDTO fromDateParam = new DBQueryParamDTO();
		fromDateParam.setParamName("fromDate");
		fromDateParam.setValue(DateUtil.convertDate(DateUtil.NOW()));
		paramList.add(fromDateParam);

		DBQueryParamDTO toDateParam = new DBQueryParamDTO();
		toDateParam.setParamName("toDate");
		toDateParam.setValue(DateUtil.convertDate(DateUtil.NOW()));
		paramList.add(toDateParam);

		ReportQueryDTO reportQueryDTO = new ReportQueryDTO();
		reportQueryDTO.setQuery("CALL EZEE_SP_RPT_DASHBOARD_STATISTIC(:namespaceId, :fromDate, :toDate)");
		List<Map<String, ?>> listMapData = reportQueryService.getQueryResultsMap(authDTO, reportQueryDTO, paramList);

		Map<String, Map<String, String>> summaryMap = new HashMap<String, Map<String, String>>();
		for (Map<String, ?> map : listMapData) {
			if (summaryMap.get(String.valueOf(map.get("trip_code"))) != null) {
				Map<String, String> entityMap = summaryMap.get(String.valueOf(map.get("trip_code")));
				entityMap.put(String.valueOf(map.get("attribute_type")), String.valueOf(map.get("attribute_value")));
				summaryMap.put(String.valueOf(map.get("trip_code")), entityMap);
			}
			else {
				Map<String, String> entityMap = new HashMap<String, String>();
				entityMap.put(String.valueOf(map.get("attribute_type")), String.valueOf(map.get("attribute_value")));
				summaryMap.put(String.valueOf(map.get("trip_code")), entityMap);
			}
		}

		int totalTripCount = 0;
		int totalTripSeatCount = 0;
		int bookedCount = 0;
		int phoneBookedCount = 0;
		int cancelledCount = 0;
		BigDecimal bookedAmount = BigDecimal.ZERO;
		BigDecimal phoneBookedAmount = BigDecimal.ZERO;
		BigDecimal cancelAmount = BigDecimal.ZERO;
		Map<String, Integer> groupWiseSeatMap = new HashMap<String, Integer>();
		Map<String, BigDecimal> groupWiseAmtMap = new HashMap<String, BigDecimal>();

		for (Entry<String, Map<String, String>> entry : summaryMap.entrySet()) {
			Map<String, String> salesInfo = entry.getValue();

			int tripSeatCount = Integer.valueOf(salesInfo.get("TRIP_ST_CNT"));
			List<String> groupList = Arrays.asList(salesInfo.get("GR_LIST").split(Text.COMMA));
			for (String group : groupList) {
				group = group.replaceAll(Text.SINGLE_SPACE, Text.EMPTY);
				// Overall Summary
				int groupBookedCount = 0;
				int groupPhoneBookedCount = 0;
				int groupCancelledCount = 0;

				if (salesInfo.get("BO_COUNT_" + group) != null) {
					groupBookedCount = Integer.valueOf(salesInfo.get("BO_COUNT_" + group));
				}
				if (salesInfo.get("PH_COUNT_" + group) != null) {
					groupPhoneBookedCount = Integer.valueOf(salesInfo.get("PH_COUNT_" + group));
				}
				if (salesInfo.get("CA_COUNT_" + group) != null) {
					groupCancelledCount = Integer.valueOf(salesInfo.get("CA_COUNT_" + group));
				}

				bookedCount = bookedCount + groupBookedCount;
				phoneBookedCount = phoneBookedCount + groupPhoneBookedCount;
				cancelledCount = cancelledCount + groupCancelledCount;

				// Group wise
				if (groupWiseSeatMap.get(group) != null) {
					int count = groupWiseSeatMap.get(group);
					int subTotal = count + groupBookedCount + groupPhoneBookedCount;
					groupWiseSeatMap.put(group, subTotal);
				}
				else {
					groupWiseSeatMap.put(group, groupBookedCount + groupPhoneBookedCount);
				}

				// Amount
				if (groupWiseAmtMap.get(group) != null) {
					BigDecimal seatAmount = BigDecimal.ZERO;
					if (salesInfo.get("BO_AMT_" + group) != null) {
						seatAmount = new BigDecimal(String.valueOf(salesInfo.get("BO_AMT_" + group)));
						bookedAmount = bookedAmount.add(seatAmount);
					}
					if (salesInfo.get("PH_BO_AMT_" + group) != null) {
						BigDecimal subTotal = new BigDecimal(String.valueOf(salesInfo.get("PH_BO_AMT_" + group)));
						seatAmount = seatAmount.add(subTotal);
						phoneBookedAmount = phoneBookedAmount.add(subTotal);
					}

					BigDecimal amount = new BigDecimal(String.valueOf(groupWiseAmtMap.get(group)));
					groupWiseAmtMap.put(group, amount.add(seatAmount));
				}
				else {
					BigDecimal seatAmount = BigDecimal.ZERO;
					if (salesInfo.get("BO_AMT_" + group) != null) {
						seatAmount = new BigDecimal(String.valueOf(salesInfo.get("BO_AMT_" + group)));
						bookedAmount = bookedAmount.add(seatAmount);
					}
					if (salesInfo.get("PH_BO_AMT_" + group) != null) {
						BigDecimal subTotal = new BigDecimal(String.valueOf(salesInfo.get("PH_BO_AMT_" + group)));
						seatAmount = seatAmount.add(subTotal);
						phoneBookedAmount = phoneBookedAmount.add(seatAmount);
					}
					groupWiseAmtMap.put(group, seatAmount);
				}
				if (salesInfo.get("CA_AMT_" + group) != null) {
					cancelAmount = cancelAmount.add(new BigDecimal(String.valueOf(salesInfo.get("CA_AMT_" + group))));
				}
			}

			totalTripSeatCount = totalTripSeatCount + tripSeatCount;
			totalTripCount = totalTripCount + 1;
		}
		double totalBookPercentage = (Float.valueOf(bookedCount + phoneBookedCount) * 100) / totalTripSeatCount;
		totalBookPercentage = Math.round(totalBookPercentage);
		dataModel.put("header", totalTripCount + " Trips, " + (bookedCount + phoneBookedCount) + "/" + totalTripSeatCount + Text.SINGLE_SPACE + totalBookPercentage + "%");

		List<String> contentList = new ArrayList<String>();
		if (bookedCount > 0) {
			String amount = Text.DOUBLE_QUOTE;
			double bookPercentage = (Float.valueOf(bookedCount) * 100) / (bookedCount + phoneBookedCount);
			bookPercentage = Math.round(bookPercentage * 100.0) / 100.0;
			if (bookedAmount.compareTo(new BigDecimal("100000")) != -1) {
				bookedAmount = bookedAmount.divide(new BigDecimal(100000)).setScale(2, RoundingMode.HALF_UP);
				amount = bookedAmount + "L";
			}
			else if (bookedAmount.compareTo(new BigDecimal("1000")) != -1) {
				bookedAmount = bookedAmount.divide(new BigDecimal(1000)).setScale(2, RoundingMode.HALF_UP);
				amount = bookedAmount + "K";
			}
			contentList.add(bookedCount + " BO " + bookPercentage + "%" + Text.SINGLE_SPACE + amount);
		}
		if (phoneBookedCount > 0) {
			String amount = Text.DOUBLE_QUOTE;
			double phoneBookPercentage = (Float.valueOf(phoneBookedCount) * 100) / (bookedCount + phoneBookedCount);
			phoneBookPercentage = Math.round(phoneBookPercentage * 100.0) / 100.0;
			if (phoneBookedAmount.compareTo(new BigDecimal("100000")) != -1) {
				phoneBookedAmount = phoneBookedAmount.divide(new BigDecimal(100000)).setScale(2, RoundingMode.HALF_UP);
				amount = phoneBookedAmount + "L";
			}
			else if (phoneBookedAmount.compareTo(new BigDecimal("1000")) != -1) {
				phoneBookedAmount = phoneBookedAmount.divide(new BigDecimal(1000)).setScale(2, RoundingMode.HALF_UP);
				amount = phoneBookedAmount + "K";
			}

			contentList.add(phoneBookedCount + " PB " + phoneBookPercentage + "% " + phoneBookedAmount + Text.SINGLE_SPACE + amount);
		}
		dataModel.put("content", contentList);

		Map<String, String> salesSummary = new HashMap<String, String>();
		List<String> summary = new ArrayList<String>();
		for (Entry<String, Integer> entry : groupWiseSeatMap.entrySet()) {
			int count = entry.getValue();
			int totalBook = bookedCount + phoneBookedCount;
			double percentage = (Float.valueOf(count) * 100) / totalBook;
			percentage = Math.round(percentage * 100.0) / 100.0;
			salesSummary.put(entry.getKey(), StringUtil.substring(entry.getKey(), 10) + " - " + count + ", " + percentage + "%, ");
		}

		for (Entry<String, BigDecimal> entry : groupWiseAmtMap.entrySet()) {
			String amount = "";
			BigDecimal amountInLakh = entry.getValue();
			amount = String.valueOf(amountInLakh);
			if (amountInLakh.compareTo(new BigDecimal("100000")) != -1) {
				amountInLakh = entry.getValue().divide(new BigDecimal(100000)).setScale(2, RoundingMode.HALF_UP);
				amount = amountInLakh + "L";
			}
			else if (amountInLakh.compareTo(new BigDecimal("1000")) != -1) {
				amountInLakh = entry.getValue().divide(new BigDecimal(1000)).setScale(2, RoundingMode.HALF_UP);
				amount = amountInLakh + "K";
			}

			String text = "NA";
			if (salesSummary.get(entry.getKey()) != null) {
				text = salesSummary.get(entry.getKey());
				summary.add(text + " Rs " + amount);
			}
			else {
				summary.add(text + " Rs " + amount);
			}
		}
		dataModel.put("summary", summary);
		dataModel.put("TOTAL_TRIP_CNT", totalTripCount);
		dataModel.put("TOTAL_BO_CNT", bookedCount + phoneBookedCount);
		dataModel.put("TOTAL_CA_CNT", cancelledCount);
		dataModel.put("cancel", cancelAmount.setScale(2, RoundingMode.HALF_UP) + " Rs");
		return dataModel;
	}

	public List<String> getTravelStatusSummary(AuthDTO authDTO) {
		List<String> travelStatusSummary = new ArrayList<>();

		List<DBQueryParamDTO> paramList = new ArrayList<DBQueryParamDTO>();
		DBQueryParamDTO namespaceParam = new DBQueryParamDTO();
		namespaceParam.setParamName("namespaceId");
		namespaceParam.setValue(String.valueOf(authDTO.getNamespace().getId()));
		paramList.add(namespaceParam);

		DBQueryParamDTO fromDateParam = new DBQueryParamDTO();
		fromDateParam.setParamName("fromDate");
		fromDateParam.setValue(DateUtil.convertDate(DateUtil.NOW()));
		paramList.add(fromDateParam);

		DBQueryParamDTO toDateParam = new DBQueryParamDTO();
		toDateParam.setParamName("toDate");
		toDateParam.setValue(DateUtil.convertDate(DateUtil.NOW()));
		paramList.add(toDateParam);

		DBQueryParamDTO filterParam = new DBQueryParamDTO();
		filterParam.setParamName("filterCode");
		filterParam.setValue("SUMM");
		paramList.add(filterParam);

		DBQueryParamDTO scheduleParam = new DBQueryParamDTO();
		scheduleParam.setParamName("scheduleCode");
		scheduleParam.setValue(Text.NA);
		paramList.add(scheduleParam);

		DBQueryParamDTO vehicleParam = new DBQueryParamDTO();
		vehicleParam.setParamName("vehicleCode");
		vehicleParam.setValue(Text.NA);
		paramList.add(vehicleParam);

		DBQueryParamDTO tagParam = new DBQueryParamDTO();
		tagParam.setParamName("tagCode");
		tagParam.setValue(Text.NA);
		paramList.add(tagParam);

		ReportQueryDTO reportQueryDTO = new ReportQueryDTO();
		reportQueryDTO.setQuery("CALL EZEE_SP_RPT_TRAVEL_STATUS(:namespaceId,:fromDate,:toDate,:filterCode,:scheduleCode,:vehicleCode,:tagCode)");

		List<Map<String, ?>> response = reportQueryService.getQueryResultsMap(authDTO, reportQueryDTO, paramList);

		String text = Text.EMPTY;
		for (Map<String, ?> map : response) {
			text = map.get("travel_status_code") + " - " + map.get("seat_count") + "\n";
			travelStatusSummary.add(text);
		}
		return travelStatusSummary;
	}

}
