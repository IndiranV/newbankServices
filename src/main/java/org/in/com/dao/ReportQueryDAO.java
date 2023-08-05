package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.DBQueryParamDTO;
import org.in.com.dto.ExportReportDetailsDTO;
import org.in.com.dto.ReportQueryDTO;
import org.in.com.exception.ServiceException;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.Cleanup;

public class ReportQueryDAO {
	private static final Logger reportlogger = LoggerFactory.getLogger("org.in.com.controller.report");

	/**
	 * Here we are getting all the amenities
	 * 
	 * @param namespaceDTO
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<ReportQueryDTO> getAll(AuthDTO authDTO) {

		List<ReportQueryDTO> list = new ArrayList<ReportQueryDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code,name,description,query,days_limit,active_flag FROM report_query WHERE active_flag = 1");
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ReportQueryDTO queryDTO = new ReportQueryDTO();
				queryDTO.setCode(selectRS.getString("code"));
				queryDTO.setName(selectRS.getString("name"));
				queryDTO.setDescription(selectRS.getString("description"));
				queryDTO.setQuery(selectRS.getString("query"));
				queryDTO.setDaysLimit(selectRS.getInt("days_limit"));
				queryDTO.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(queryDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	/**
	 * Here we are updating the report queries based on the code
	 * 
	 * @param namespaceDTO
	 * @param reportQueryDTO
	 * @return
	 * @sample
	 *         select code from ticket where id = :{your parameter name}
	 */

	public ReportQueryDTO getUpdate(AuthDTO authDTO, ReportQueryDTO reportQueryDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_REPORT_QUERY_IUD(?,?,?,?,?,? ,?,?,?)}");
			callableStatement.setString(++pindex, reportQueryDTO.getCode());
			callableStatement.setString(++pindex, reportQueryDTO.getName());
			callableStatement.setString(++pindex, reportQueryDTO.getDescription());
			callableStatement.setString(++pindex, reportQueryDTO.getQuery());
			callableStatement.setInt(++pindex, reportQueryDTO.getDaysLimit());
			callableStatement.setInt(++pindex, reportQueryDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				reportQueryDTO.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return reportQueryDTO;
	}

	public void addReportQueryAuditLog(AuthDTO authDTO, ReportQueryDTO reportQueryDTO, String params, int runTime, int resultRowCount) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO audit_report_log(namespace_id, report_code, user_id, parameter_log, execution_time, result_row_count, status_log, active_flag, updated_at)VALUES(?,?,?,?,?, ?,?,1,NOW())");
			preparedStatement.setInt(++pindex, authDTO.getNamespace().getId());
			preparedStatement.setString(++pindex, reportQueryDTO.getCode());
			preparedStatement.setInt(++pindex, authDTO.getUser().getId());
			preparedStatement.setString(++pindex, params);
			preparedStatement.setInt(++pindex, runTime);
			preparedStatement.setInt(++pindex, resultRowCount);
			preparedStatement.setString(++pindex, reportQueryDTO.getDescription());
			preparedStatement.execute();
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void get(AuthDTO authDTO, ReportQueryDTO queryDTO) {

		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, name, description, query, days_limit, active_flag FROM report_query WHERE code = ?  AND active_flag = 1");
			selectPS.setString(1, queryDTO.getCode());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				queryDTO.setId(selectRS.getInt("id"));
				queryDTO.setName(selectRS.getString("name"));
				queryDTO.setDescription(selectRS.getString("description"));
				queryDTO.setQuery(selectRS.getString("query"));
				queryDTO.setDaysLimit(selectRS.getInt("days_limit"));
				queryDTO.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}

	}

	public List<List<String>> getQueryResults(String query, List<DBQueryParamDTO> params) {
		try {
			@Cleanup
			Connection connection = ConnectReportDAO.getReportConnection();
			@Cleanup
			NamedPrepareStatement statement = new NamedPrepareStatement(connection, query);
			if (params != null) {
				for (DBQueryParamDTO param : params) {
					String name = param.getParamName();
					String value = param.getValue();
					if (value != null && StringUtils.isNumeric(value) && value.length() <= 8) {
						statement.setInt(name, Integer.parseInt(value));
					}
					else if (StringUtil.isNotNull(value) && DateUtil.isValidDate(value)) {
						statement.setString(name, value);
					}
					else if (value != null && StringUtils.isAlphanumeric(value)) {
						statement.setString(name, value);
					}
					else {
						reportlogger.error("Unknown data type for JDBC query parameter: {} \n {} {}", query, name, value);
					}
				}
			}
			@Cleanup
			ResultSet resultSet = statement.executeQuery();
			return getResultList(resultSet);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public List<Map<String, ?>> getQueryResultsMap(String query, List<DBQueryParamDTO> params) {
		try {
			@Cleanup
			Connection connection = ConnectReportDAO.getReportConnection();
			@Cleanup
			NamedPrepareStatement statement = new NamedPrepareStatement(connection, query);
			if (params != null) {
				for (DBQueryParamDTO param : params) {
					String name = param.getParamName().trim();
					String value = param.getValue().trim();
					if (StringUtil.isNotNull(value) && StringUtils.isNumeric(value) && value.length() <= 8) {
						statement.setInt(name, Integer.parseInt(value));
					}
					else if (StringUtil.isNotNull(value) && name.contains("Date") && DateUtil.isValidDate(value)) {
						statement.setString(name, value);
					}
					else if (StringUtil.isNotNull(value) && StringUtils.isAlphanumeric(value)) {
						statement.setString(name, value);
					}
					else if (!StringUtils.isBlank(value)) {
						statement.setString(name, value);
					}
					else {
						reportlogger.error("Unknown data type for JDBC query parameter: {} \n {} {}", query, name, value);
					}
				}
			}
			@Cleanup
			ResultSet resultSet = statement.executeQuery();
			return getResultMaps(resultSet);
		}
		catch (Exception e) {
			reportlogger.error("RPTMAP DAO {} {}", query, Lists.transform(params, Functions.toStringFunction()));
			System.out.println("query " + query + Lists.transform(params, Functions.toStringFunction()));
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public List<Map<String, ?>> executeDynamicQuery(AuthDTO authDTO, String query) {
		List<Map<String, ?>> resultMapList = new ArrayList<>();
		try {
			@Cleanup
			Connection connection = ConnectReportDAO.getReportConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement(query);
			@Cleanup
			ResultSet resultSet = selectPS.executeQuery(query);
			resultMapList = getResultMaps(resultSet);
		}
		catch (Exception e) {
			System.out.println(DateUtil.NOW() + " Report Dynamic query Error :" + query + Text.SINGLE_SPACE + authDTO.getUserCode());
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return resultMapList;
	}

	private List<Map<String, ?>> getResultMaps(ResultSet resultSet) throws Exception {
		ResultSetMetaData metaData = resultSet.getMetaData();
		int columnCount = metaData.getColumnCount();
		Map<String, String> columns = Maps.newLinkedHashMap();
		for (int i = 1; i <= columnCount; i++) {
			columns.put(metaData.getColumnLabel(i), metaData.getColumnClassName(i));
		}

		List<Map<String, ?>> resultMapList = new ArrayList<>();
		while (resultSet.next()) {
			Object value = null;
			Map<String, Object> resultsMap = Maps.newLinkedHashMap();
			for (String columnName : columns.keySet()) {
				String dataType = columns.get(columnName);
				switch (dataType) {
					case "java.lang.String":
						value = resultSet.getString(columnName);
						break;
					case "java.lang.Integer":
						value = resultSet.getInt(columnName);
						break;
					case "java.lang.Float":
						value = resultSet.getFloat(columnName);
						break;
					case "java.lang.Double":
						value = resultSet.getDouble(columnName);
						break;
					case "java.math.BigDecimal":
						value = resultSet.getBigDecimal(columnName);
						break;
					case "java.lang.Long":
						value = resultSet.getLong(columnName);
						break;
					case "java.sql.Date":
						value = resultSet.getDate(columnName);
						break;
					case "java.sql.Timestamp":
						value = DateUtil.getTimestamp(resultSet.getTimestamp(columnName));
						break;
					case "java.sql.Time":
						value = resultSet.getTime(columnName);
						break;
					case "java.time.LocalDateTime":
						value = DateUtil.getTimestamp(resultSet.getTimestamp(columnName));
						break;
					default:
						continue;
				}
				resultsMap.put(columnName, value);
			}
			resultMapList.add(resultsMap);
		}
		return resultMapList;
	}

	private List<List<String>> getResultList(ResultSet resultSet) throws Exception {
		ResultSetMetaData metaData = resultSet.getMetaData();
		List<String> columnList = new ArrayList<>();
		for (int i = 1; i <= metaData.getColumnCount(); i++) {
			columnList.add(metaData.getColumnLabel(i));
		}
		// Add Header
		List<List<String>> resultList = new ArrayList<>();
		resultList.add(columnList);
		// Collect Data
		while (resultSet.next()) {
			List<String> dataList = new ArrayList<String>();
			for (String columnName : columnList) {
				try {
					dataList.add(resultSet.getString(columnName));
				}
				catch (Exception e) {
					dataList.add(null);
				}
			}
			resultList.add(dataList);
		}
		return resultList;
	}

	public List<ReportQueryDTO> getAllforZoneSync(AuthDTO authDTO, String syncDate) {

		List<ReportQueryDTO> list = new ArrayList<ReportQueryDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code,name,description,query,days_limit,active_flag FROM report_query WHERE DATE(updated_at) >= ?");
			selectPS.setString(1, syncDate);
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ReportQueryDTO queryDTO = new ReportQueryDTO();
				queryDTO.setCode(selectRS.getString("code"));
				queryDTO.setName(selectRS.getString("name"));
				queryDTO.setDescription(selectRS.getString("description"));
				queryDTO.setQuery(selectRS.getString("query"));
				queryDTO.setDaysLimit(selectRS.getInt("days_limit"));
				queryDTO.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(queryDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public String getZoneSyncDate(AuthDTO authDTO) {
		String zoneSyncDate = null;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT MAX(updated_at) as zoneSyncDate FROM report_query");
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				zoneSyncDate = selectRS.getString("zoneSyncDate");
			}
			if (StringUtil.isNull(zoneSyncDate)) {
				zoneSyncDate = "2014-02-12 03:49:03";
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return zoneSyncDate;
	}

	public List<ReportQueryDTO> updateZoneSync(AuthDTO authDTO, List<ReportQueryDTO> list) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_REPORT_QUERY_ZONESYNC( ?,?,?,?,? ,?,? )}");
			for (ReportQueryDTO reportQueryDTO : list) {
				int pindex = 0;
				callableStatement.setString(++pindex, reportQueryDTO.getCode());
				callableStatement.setString(++pindex, reportQueryDTO.getName());
				callableStatement.setString(++pindex, reportQueryDTO.getDescription());
				callableStatement.setString(++pindex, reportQueryDTO.getQuery());
				callableStatement.setInt(++pindex, reportQueryDTO.getDaysLimit());
				callableStatement.setInt(++pindex, reportQueryDTO.getActiveFlag());
				callableStatement.setInt(++pindex, authDTO.getUser().getId());
				callableStatement.execute();
				callableStatement.clearParameters();
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public List<ExportReportDetailsDTO> getReportDetailsByStatus(AuthDTO authDTO, String status) {
		List<ExportReportDetailsDTO> list = new ArrayList<>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, name, service_name, parameter, status, requested_time, encrypt_data, active_flag FROM export_report_details WHERE status = ? AND active_flag = 1");
			selectPS.setString(1, status);
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ExportReportDetailsDTO reportDetailsDTO = new ExportReportDetailsDTO();
				reportDetailsDTO.setCode(selectRS.getString("code"));
				reportDetailsDTO.setName(selectRS.getString("name"));
				reportDetailsDTO.setServiceName(selectRS.getString("service_name"));
				reportDetailsDTO.setParameter(selectRS.getString("parameter"));
				reportDetailsDTO.setStatus(selectRS.getString("status"));
				reportDetailsDTO.setRequestedTime(DateUtil.getDateTime(selectRS.getString("requested_time")));
				reportDetailsDTO.setEncryptData(selectRS.getString("encrypt_data"));
				reportDetailsDTO.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(reportDetailsDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public void updateReportDetailsStatus(AuthDTO authDTO, ExportReportDetailsDTO dto) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("UPDATE export_report_details SET status = ?, encrypt_data = ?, updated_at = NOW() WHERE code = ? AND active_flag = 1");
			selectPS.setString(1, dto.getStatus());
			selectPS.setString(2, StringUtil.isNotNull(dto.getEncryptData()) ? dto.getEncryptData() : Text.NA);
			selectPS.setString(3, dto.getCode());
			selectPS.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public List<ExportReportDetailsDTO> getAllExportReportDetails(AuthDTO authDTO) {
		List<ExportReportDetailsDTO> list = new ArrayList<>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, name, service_name, parameter, status, requested_time, encrypt_data, active_flag FROM export_report_details WHERE active_flag = 1");
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ExportReportDetailsDTO reportDetailsDTO = new ExportReportDetailsDTO();
				reportDetailsDTO.setCode(selectRS.getString("code"));
				reportDetailsDTO.setName(selectRS.getString("name"));
				reportDetailsDTO.setServiceName(selectRS.getString("service_name"));
				reportDetailsDTO.setParameter(selectRS.getString("parameter"));
				reportDetailsDTO.setStatus(selectRS.getString("status"));
				reportDetailsDTO.setRequestedTime(DateUtil.getDateTime(selectRS.getString("requested_time")));
				reportDetailsDTO.setEncryptData(selectRS.getString("encrypt_data"));
				reportDetailsDTO.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(reportDetailsDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public ExportReportDetailsDTO getReportDetailsByStatus(AuthDTO authDTO, String status, String reportCode) {
		ExportReportDetailsDTO reportDetailsDTO = new ExportReportDetailsDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, name, service_name, parameter, status, requested_time, encrypt_data, active_flag FROM export_report_details WHERE code = ? AND status = ? AND active_flag = 1");
			selectPS.setString(1, reportCode);
			selectPS.setString(2, status);
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				reportDetailsDTO.setCode(selectRS.getString("code"));
				reportDetailsDTO.setName(selectRS.getString("name"));
				reportDetailsDTO.setServiceName(selectRS.getString("service_name"));
				reportDetailsDTO.setParameter(selectRS.getString("parameter"));
				reportDetailsDTO.setStatus(selectRS.getString("status"));
				reportDetailsDTO.setRequestedTime(DateUtil.getDateTime(selectRS.getString("requested_time")));
				reportDetailsDTO.setEncryptData(selectRS.getString("encrypt_data"));
				reportDetailsDTO.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return reportDetailsDTO;
	}
}
