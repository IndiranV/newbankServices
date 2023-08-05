package org.in.com.service.report;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.in.com.aggregator.aws.S3Service;
import org.in.com.aggregator.bits.BitsService;
import org.in.com.aggregator.mail.EmailService;
import org.in.com.config.ApplicationConfig;
import org.in.com.constants.Constants;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.DBQueryParamDTO;
import org.in.com.dto.ExportReportDTO;
import org.in.com.dto.ExportReportDetailsDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.ReportQueryDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.NamespaceService;
import org.in.com.service.ReportInterface;
import org.in.com.service.ReportQueryService;
import org.in.com.service.SeatVisibilityReportService;
import org.in.com.utils.BitsEnDecrypt;
import org.in.com.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Iterables;

import hirondelle.date4j.DateTime;
import hirondelle.date4j.DateTime.DayOverflow;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class ExportReportServiceImpl implements ExportReportService {

	private static final String MONTHLY = "MONTHLY";
	private static final String WEEKLY = "WEEKLY";
	private static final String DAILY = "DAILY";
	private static final String DATE = "DATE";
	private static final String BOOK_HEAD = "Booking";
	private static final String CANCEL_HEAD = "Cancellation";
	private static final String SUMMARY_HEAD = "Summary";
	private static final String CSV_SPLITTER = "\\,";

	@Autowired
	BitsService bitsService;
	@Autowired
	ReportQueryService reportQueryService;
	@Autowired
	S3Service s3Service;
	@Autowired
	EmailService mailService;
	@Autowired
	SeatVisibilityReportService seatVisibilityReportService;
	@Autowired
	ReportQueryService reportQuery;
	@Autowired
	org.in.com.service.ExportReportService exportReportService;
	@Autowired
	NamespaceService namespaceService;

	private static final Logger reportlogger = LoggerFactory.getLogger("org.in.com.controller.report");

	@Override
	public void exportReport(AuthDTO authDTO, JSONObject json) {
		DateTime currentDate = DateUtil.NOW();
		JSONArray jsonArray = new JSONArray();
		if (json == null) {
			bitsService.getReportConfig(authDTO);
		}
		else {
			jsonArray.add(json);
		}
		for (Object object : jsonArray) {
			JSONObject jsonObject = (JSONObject) object;
			try {
				String fileName = jsonObject.getString("reportName").split(Text.HYPHEN)[0].replaceAll(Text.SINGLE_SPACE, Text.EMPTY);
				String frequency = jsonObject.getString("frequency");

				if (frequency.equals(WEEKLY) && currentDate.getWeekDay() != Numeric.ONE_INT) {
					continue;
				}
				// if (frequency.equals(MONTHLY) && currentDate.getDay() !=
				// currentDate.getStartOfMonth().getDay()) {
				// continue;
				// }

				generateReport(authDTO, fileName, jsonObject, currentDate);
			}
			catch (ServiceException e) {
				throw new ServiceException(e.getErrorCode(), e.getData());
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void generateReport(AuthDTO authDTO, String fileName, JSONObject jsonObject, DateTime currentDate) {
		DateTime now = DateUtil.NOW();

		String reportCode = jsonObject.getString("reportCode");
		String period = jsonObject.getString("datePeriod");
		String reportImpl = jsonObject.getString("reportImpl");
		String filterDateType = jsonObject.has("filterDateType") && jsonObject.getString("filterDateType").equals("TRP") ? "TRP" : "TXN";
		reportlogger.info("Exp Rpt:{} period:{} reportImpl:{} travelDateFlag:{}", reportCode, period, reportImpl, filterDateType);
		try {
			ReportInterface instance = getReportInstance(reportImpl);

			DateTime startDate = null;
			DateTime endDate = null;

			if (period.equals(DAILY)) {
				startDate = currentDate.minusDays(1);
				endDate = startDate;
			}
			else if (period.equals(WEEKLY)) {
				startDate = DateUtil.getWeekStartDate(currentDate).minusDays(Numeric.SEVEN_INT);
				endDate = DateUtil.getWeekEndDate(startDate);
			}
			else if (period.equals(MONTHLY)) {
				DateTime month = currentDate.minus(0, 1, 0, 0, 0, 0, 0, DayOverflow.FirstDay);
				startDate = month.getStartOfMonth();
				endDate = month.getEndOfMonth();
			}
			else if (period.equals(DATE)) {
				startDate = DateUtil.getDateTime(jsonObject.getString("fromDate")).getStartOfDay();
				endDate = DateUtil.getDateTime(jsonObject.getString("toDate")).getEndOfDay();
			}

			String fromDate = DateUtil.convertDate(startDate);
			String toDate = DateUtil.convertDate(endDate);
			List<Map<String, ?>> results = new ArrayList<Map<String, ?>>();

			reportlogger.info("Exp Rpt:{} Date{}-{}", reportCode, fromDate, toDate);
			switch (reportCode) {
				case "VISIBILITY":
					SearchDTO searchDTO = new SearchDTO();
					searchDTO.setTravelDate(DateUtil.getDateTime(fromDate));
					List<Map<String, String>> seatVisibilitylist = seatVisibilityReportService.getAllScheduleVisibility(authDTO, searchDTO);
					results.addAll(seatVisibilitylist);
					break;

				case "DYNAMIC":
					ReportQueryDTO reportQuery = new ReportQueryDTO();
					reportQuery.setCode("DYNAMICEXPORT");
					reportQuery.setQuery(jsonObject.getString("query"));
					JSONArray paramJsonArray = jsonObject.getJSONArray("parameter");
					List<DBQueryParamDTO> dyparams = instance.getParams(authDTO, reportQuery, fromDate, toDate, filterDateType);
					for (Object paramJson : paramJsonArray) {
						JSONObject paramJsonObject = (JSONObject) paramJson;
						DBQueryParamDTO queryParam = new DBQueryParamDTO();
						queryParam.setParamName(paramJsonObject.getString("paramName"));
						queryParam.setValue(paramJsonObject.getString("value"));
						dyparams.add(queryParam);
					}
					results = reportQueryService.getQueryResultsMap(authDTO, reportQuery, dyparams);
					break;

				default:
					// Get Request Params
					ReportQueryDTO reportQueryDTO = new ReportQueryDTO();
					reportQueryDTO.setCode(jsonObject.getString("reportCode"));
					reportQueryService.get(authDTO, reportQueryDTO);

					if (reportQueryDTO.getActiveFlag() != Numeric.ZERO_INT) {
						List<DBQueryParamDTO> params = instance.getParams(authDTO, reportQueryDTO, fromDate, toDate, filterDateType);
						// Get Report Result
						results = reportQueryService.getQueryResultsMap(authDTO, reportQueryDTO, params);
					}
					else {
						reportlogger.info("Exp Rpt:{} Not found", reportCode);
						throw new ServiceException(ErrorCode.INVALID_CODE, reportCode + " Report Not Found");
					}
					break;
			}
			reportlogger.info("Exp Rpt:{} rowcount:{}", reportCode, results.size());

			// Create WorkBook
			Map<String, Object> workBookMap = instance.createWorkBook(authDTO, jsonObject, results, fromDate, toDate);
			String reportName = jsonObject.getString("reportName");
			reportlogger.info("Exp Rpt:{} workBookMap:{}", reportCode, workBookMap.size());

			String downloadBaseUrl = "https://utility.ezeebits.com/download";

			String finalFileUrl = Text.NA;
			// Write Excel
			if (Constants.SERVER_ENV_LINUX.equals(ApplicationConfig.getServerEnv())) {
				finalFileUrl = s3Service.exportReport(authDTO, (List<String>) workBookMap.get(Text.WORK_BOOK), reportCode, String.valueOf(workBookMap.get(Text.FILE_NAME)));
			}
			else {
				exportCSV(authDTO, (List<String>) workBookMap.get(Text.WORK_BOOK), String.valueOf(workBookMap.get(Text.FILE_NAME)));
			}
			reportlogger.info("Exp Rpt:{} finalFileUrl:{}", reportCode, finalFileUrl);

			StringBuilder mailId = new StringBuilder();
			JSONArray jsonArray = jsonObject.getJSONArray("email");
			for (int i = 0; i < jsonArray.size(); i++) {
				String mail = (String) jsonArray.get(i);
				mailId.append(mail);
				mailId.append(Text.COMMA);
			}
			JSONObject data = new JSONObject();
			data.put("fileurl", finalFileUrl);
			data.put("reportCode", reportCode);
			data.put("namespaceCode", authDTO.getNamespaceCode());
			data.put("zoneCode", ApplicationConfig.getServerZoneCode());
			data.put("yearMonth", DateUtil.NOW().format("YYYY/MM"));
			String encryptData = BitsEnDecrypt.getEncoder(data.toString());
			String linkFileUrl = downloadBaseUrl + "?ns=" + authDTO.getNamespaceCode() + "&zone=" + ApplicationConfig.getServerZoneCode() + "&zoneurl=" + ApplicationConfig.getServerZoneUrl() + "&url=" + encryptData;
			mailService.sendReportEmail(authDTO, mailId.toString(), reportName + " " + workBookMap.get(Text.FILE_NAME), linkFileUrl);
			reportlogger.info("Exp Rpt:{} linkFileUrl:{}", reportCode, linkFileUrl);

		}
		catch (ServiceException e) {
			throw new ServiceException(e.getErrorCode(), e.getData());
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		finally {
			reportlogger.info("Report:{} time:{}", reportCode, DateUtil.getSecondsDifferent(now, DateUtil.NOW()));
		}
	}

	private ReportInterface getReportInstance(String reportName) {
		ReportInterface instance = null;
		try {
			String reportClassName = "org.in.com.service.report." + reportName;
			Class<?> reportClass = Class.forName(reportClassName);
			instance = (ReportInterface) reportClass.newInstance();

			if (instance == null) {
				throw new ServiceException("RP01", "No Report Found");
			}
		}
		catch (ClassNotFoundException e) {
			throw new ServiceException("RP01", "No Report Found");
		}
		catch (ServiceException e) {
			throw new ServiceException(e.getErrorCode(), e.getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return instance;
	}

	// https://www.programcreek.com/java-api-examples/?code=Contargo/iris/iris-master/src/main/java/net/contargo/iris/address/staticsearch/upload/csv/StaticAddressCsvService.java
	private void exportCSV(AuthDTO authDTO, List<String> dataList, String fileName) {
		CSVPrinter csvPrinter = null;
		try {
			File configDir = new File(System.getProperty("catalina.base"), "logs");
			File fileDir = new File(configDir, "report/" + authDTO.getNamespaceCode());
			if (!fileDir.exists()) {
				fileDir.mkdirs();
			}
			csvPrinter = new CSVPrinter(new FileWriter(new File(fileDir, fileName + ".csv")), CSVFormat.DEFAULT);
			for (String data : dataList) {
				csvPrinter.printRecord(data.split(CSV_SPLITTER));
				if (!data.equals(BOOK_HEAD) && !data.equals(SUMMARY_HEAD) && !data.equals(CANCEL_HEAD)) {
					continue;
				}
				csvPrinter.println();
			}
			csvPrinter.flush();
			csvPrinter.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	@Override
	public void exportReportV2(AuthDTO authDTO) {
		try {
			DateTime currentDateTime = DateUtil.NOW();
			boolean isRequestExists = false;
			List<ExportReportDetailsDTO> detailsList = reportQuery.getAllExportReportDetails(authDTO);
			ExportReportDetailsDTO detailsDTO = Iterables.getLast(detailsList, null);
			if (detailsDTO != null) {
				int minutes = DateUtil.getMinutiesDifferent(detailsDTO.getRequestedTime(), currentDateTime);
				if (minutes < 1440) {
					isRequestExists = true;
				}
			}
			if (!isRequestExists) {
				List<ExportReportDTO> reportList = exportReportService.getAllExportReport(authDTO);
				for (ExportReportDTO reportDTO : reportList) {
					for (NamespaceDTO namespaceDTO : reportDTO.getNamespace()) {
						AuthDTO authdto = new AuthDTO();
						NamespaceDTO namespace = new NamespaceDTO();
						namespace.setId(namespaceDTO.getId());
						namespaceService.getNamespace(namespace);
						authdto.setNamespace(namespace);
						authdto.setUser(authDTO.getUser());
						authdto.setNamespaceCode(namespace.getCode());
						LocalDate currentDate = LocalDate.now();
						Month currentMonth = currentDate.getMonth();
						int currentDay = currentDate.getDayOfMonth();
						if ((reportDTO.getFrequency().equals("FINANCIAL YEAR") && (!currentMonth.equals(Month.APRIL) || currentDay != 1)) || (reportDTO.getFrequency().equals("CALANDER YEAR") && (!currentMonth.equals(Month.JANUARY) || currentDay != 1))) {
							continue;
						}
						exportReportService.updateExportReportDetails(authdto, reportDTO);
						List<ExportReportDetailsDTO> reportDetailsList = reportQuery.getReportDetailsByStatus(authdto, Constants.REPORT_DETAILS_DEFAULT_STATUS);
						for (ExportReportDetailsDTO dto : reportDetailsList) {
							List<Map<String, ?>> results = new ArrayList<Map<String, ?>>();
							String filterDateType = null;
							String[] values = dto.getParameter().split(",");
							String datePeriod = values[0].substring(values[0].indexOf(":") + 1);
							String reportImpl = dto.getServiceName();
							String fromDate = values[1].substring(values[1].indexOf(":") + 1);
							String toDate = values[2].substring(values[2].indexOf(":") + 1);
							String reportCode = values[3].substring(values[3].indexOf(":") + 1);
							int filterDateTypeFlag = Integer.valueOf(values[4].substring(values[4].indexOf(":") + 1));

							if (filterDateTypeFlag == 1) {
								filterDateType = "TRP";
							}
							else {
								filterDateType = "TXN";
							}

							ReportInterface instance = getReportInstance(reportImpl);
							dto.setStatus("INPROGRESS");
							reportQuery.updateReportDetailsStatus(authdto, dto);

							// Get Request Params
							ReportQueryDTO reportQueryDTO = new ReportQueryDTO();
							reportQueryDTO.setCode(reportCode);
							reportQueryService.get(authdto, reportQueryDTO);

							if (reportQueryDTO.getActiveFlag() != Numeric.ZERO_INT) {
								List<DBQueryParamDTO> params = instance.getParams(authdto, reportQueryDTO, fromDate, toDate, filterDateType);
								// Get Report Result
								results = reportQueryService.getQueryResultsMap(authdto, reportQueryDTO, params);
							}
							else {
								reportlogger.info("Exp Rpt:{} Not found", reportCode);
								throw new ServiceException(ErrorCode.INVALID_CODE, reportCode + " Report Not Found");
							}
							reportlogger.info("Exp Rpt:{} rowcount:{}", reportCode, results.size());

							JSONObject jsonObject = new JSONObject();
							jsonObject.put("reportName", dto.getName());
							jsonObject.put("reportCode", reportCode);
							jsonObject.put("datePeriod", datePeriod);

							// Create WorkBook
							Map<String, Object> workBookMap = instance.createWorkBook(authdto, jsonObject, results, fromDate, toDate);
							reportlogger.info("Exp Rpt:{} workBookMap:{}", reportCode, workBookMap.size());

							String finalFileUrl = Text.NA;
							// Write Excel
							if (Constants.SERVER_ENV_LINUX.equals(ApplicationConfig.getServerEnv())) {
								finalFileUrl = s3Service.exportReport(authdto, (List<String>) workBookMap.get(Text.WORK_BOOK), reportImpl, String.valueOf(workBookMap.get(Text.FILE_NAME)));
							}
							else {
								exportCSV(authdto, (List<String>) workBookMap.get(Text.WORK_BOOK), String.valueOf(workBookMap.get(Text.FILE_NAME)));
							}
							reportlogger.info("Exp Rpt:{} finalFileUrl:{}", reportCode, finalFileUrl);

							JSONObject data = new JSONObject();
							data.put("fileurl", finalFileUrl);
							data.put("reportCode", reportCode);
							data.put("namespaceCode", authdto.getNamespaceCode());
							data.put("zoneCode", ApplicationConfig.getServerZoneCode());
							data.put("yearMonth", DateUtil.NOW().format("YYYY/MM"));
							String encryptData = BitsEnDecrypt.getEncoder(data.toString());
							dto.setEncryptData(encryptData);
							dto.setStatus("COMPLETE");
							reportQuery.updateReportDetailsStatus(authdto, dto);
						}
					}
				}
			}
			else {
				throw new ServiceException(ErrorCode.REQUEST_ALREADY_PROCESSED);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
