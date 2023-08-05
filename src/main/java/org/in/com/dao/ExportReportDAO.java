package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ExportReportDTO;
import org.in.com.dto.ExportReportDetailsDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.exception.ServiceException;
import org.in.com.utils.DateUtil;
import org.springframework.stereotype.Repository;

import lombok.Cleanup;

@Repository
public class ExportReportDAO {

	public ExportReportDTO updateExportReport(AuthDTO authDTO, ExportReportDTO reportDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			StringBuilder operatorBuilder = new StringBuilder();
			for (NamespaceDTO operator : reportDTO.getNamespace()) {
				operatorBuilder.append(operator.getId());
				operatorBuilder.append(",");
			}
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_EXPORT_REPORT_IUD(?,?,?,?,? ,?,?,?,?,?)}");
			callableStatement.setString(++pindex, reportDTO.getCode());
			callableStatement.setString(++pindex, operatorBuilder.toString());
			callableStatement.setString(++pindex, reportDTO.getReportName());
			callableStatement.setString(++pindex, reportDTO.getReportCode());
			callableStatement.setString(++pindex, reportDTO.getFrequency());
			callableStatement.setInt(++pindex, reportDTO.getFilterDateTypeFlag());
			callableStatement.setInt(++pindex, reportDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				reportDTO.setCode(callableStatement.getString("pcrCode"));
			}

		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}

		return reportDTO;
	}

	public List<ExportReportDTO> getAllExportReport(AuthDTO authDTO) {
		List<ExportReportDTO> list = new ArrayList<>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, operator, report_name, report_code, frequency, filter_datetype_flag, active_flag FROM export_report WHERE active_flag = 1");
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ExportReportDTO reportDTO = new ExportReportDTO();
				reportDTO.setCode(selectRS.getString("code"));
				String operator = selectRS.getString("operator");
				String operators[] = operator.split(",");
				List<NamespaceDTO> namespaceList = new ArrayList<>();
				for (String id : operators) {
					NamespaceDTO namespace = new NamespaceDTO();
					namespace.setId(Integer.valueOf(id));
					namespaceList.add(namespace);
				}
				reportDTO.setNamespace(namespaceList);
				reportDTO.setReportName(selectRS.getString("report_name"));
				reportDTO.setReportCode(selectRS.getString("report_code"));
				reportDTO.setFrequency(selectRS.getString("frequency"));
				reportDTO.setFilterDateTypeFlag(selectRS.getInt("filter_datetype_flag"));
				reportDTO.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(reportDTO);
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public void updateExportReportDetails(AuthDTO authDTO, ExportReportDTO reportDTO, ExportReportDetailsDTO queueDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_EXPORT_REPORT_DETAILS_IUD(?,?,?,?,? ,?,?,?,?,? ,?,?)}");
			for (NamespaceDTO namespace : reportDTO.getNamespace()) {
				int pindex = 0;
				ExportReportDetailsDTO dto = new ExportReportDetailsDTO();
				callableStatement.setString(++pindex, queueDTO.getCode());
				callableStatement.setInt(++pindex, namespace.getId());
				callableStatement.setString(++pindex, queueDTO.getName());
				callableStatement.setString(++pindex, queueDTO.getServiceName());
				callableStatement.setString(++pindex, queueDTO.getParameter());
				callableStatement.setString(++pindex, queueDTO.getStatus());
				callableStatement.setString(++pindex, DateUtil.convertDateTime(queueDTO.getRequestedTime()));
				callableStatement.setString(++pindex, queueDTO.getEncryptData());
				callableStatement.setInt(++pindex, reportDTO.getActiveFlag());
				callableStatement.setInt(++pindex, authDTO.getUser().getId());
				callableStatement.setInt(++pindex, 0);
				callableStatement.registerOutParameter(++pindex, Types.INTEGER);
				callableStatement.execute();
				if (callableStatement.getInt("pitRowCount") > 0) {
					dto.setCode(callableStatement.getString("pcrCode"));
				}
				callableStatement.clearParameters();
			}

		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}
}
