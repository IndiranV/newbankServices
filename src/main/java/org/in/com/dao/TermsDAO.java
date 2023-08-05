package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.TermDTO;
import org.in.com.dto.enumeration.TransactionTypeEM;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

import lombok.Cleanup;

public class TermsDAO {
	
	public List<TermDTO> getAllCancellationTerms(AuthDTO authDTO, TermDTO dto) {
		List<TermDTO> list = new ArrayList<TermDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement(" SELECT code, terms, sequence_id, tag, schedule_code, transaction_type_id, active_flag  FROM  namespace_terms terms WHERE  namespace_id = ? AND transaction_type_id = ? and active_flag = 1 ORDER BY sequence_id ASC");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, dto.getTransactionType().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				TermDTO termDTO = new TermDTO();
				termDTO.setSequenceId(selectRS.getInt("sequence_id"));
				termDTO.setCode(selectRS.getString("code"));
				termDTO.setName(selectRS.getString("terms"));
				String tag = selectRS.getString("tag");
				String tags[] = tag.split(",");
				List<String> tagList = new ArrayList<String>();
				for (String tag1 : tags) {
					if (StringUtil.isNotNull(tag1)) {
						tagList.add(tag1);
					}
				}
				termDTO.setTagList(tagList);
				String schedule = selectRS.getString("schedule_code");
				String scheduleCode[] = schedule.split(",");
				List<ScheduleDTO> scheduleList = new ArrayList<>();
				for(String code : scheduleCode) {
					if(StringUtil.isNotNull(code)) {
						ScheduleDTO scheduleDTO = new ScheduleDTO();
						scheduleDTO.setCode(code);
						scheduleList.add(scheduleDTO);
					}
				}
				termDTO.setSchedule(scheduleList);
				termDTO.setActiveFlag(selectRS.getInt("active_flag"));
				termDTO.setTransactionType(TransactionTypeEM.getTransactionTypeEM(selectRS.getInt("transaction_type_id")));
				list.add(termDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}
	
	public TermDTO getTermsIUD(AuthDTO authDTO, TermDTO termDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			StringBuilder tagBuilder = new StringBuilder();
			StringBuilder scheduleBuilder = new StringBuilder();
			for (String tag : termDTO.getTagList()) {
				tagBuilder.append(tag);
				tagBuilder.append(",");
			}
			for (ScheduleDTO schedule : termDTO.getSchedule()) {
				scheduleBuilder.append(schedule.getCode());
				scheduleBuilder.append(",");
			
			}
			int pindex = 0;
			@Cleanup
			CallableStatement termSt = connection.prepareCall("{call   EZEE_SP_NAMESPACE_TERMS_IUD( ?,?,?,?,?, ?,?,?,?,?, ?)}");
			termSt.setString(++pindex, termDTO.getCode());
			termSt.setString(++pindex, termDTO.getName());
			termSt.setInt(++pindex, termDTO.getSequenceId());
			termSt.setInt(++pindex, authDTO.getNamespace().getId());
			termSt.setString(++pindex, tagBuilder.toString());
			termSt.setString(++pindex, scheduleBuilder.toString());
			termSt.setInt(++pindex, termDTO.getTransactionType().getId());
			termSt.setInt(++pindex, termDTO.getActiveFlag());
			termSt.setInt(++pindex, authDTO.getUser().getId());
			termSt.setInt(++pindex, 0);
			termSt.registerOutParameter(++pindex, Types.INTEGER);
			termSt.execute();

			if (termSt.getInt("pitRowCount") > 0) {
				termDTO.setCode(termSt.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return termDTO;
	}

	public List<TermDTO> getAllTermsAndConditions(AuthDTO authDTO) {
		List<TermDTO> termList = new ArrayList<TermDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement(" SELECT code, terms, sequence_id, tag, schedule_code, transaction_type_id, active_flag  FROM  namespace_terms WHERE  namespace_id = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				TermDTO termDTO1 = new TermDTO();
				termDTO1.setSequenceId(selectRS.getInt("sequence_id"));
				termDTO1.setCode(selectRS.getString("code"));
				termDTO1.setName(selectRS.getString("terms"));
				String tag = selectRS.getString("tag");
				String tags[] = tag.split(",");
				List<String> tagList = new ArrayList<String>();
				for (String tag1 : tags) {
					if (StringUtil.isNotNull(tag1)) {
						tagList.add(tag1);
					}
				}
				termDTO1.setTagList(tagList);
				String schedule = selectRS.getString("schedule_code");
				String scheduleCode[] = schedule.split(",");
				List<ScheduleDTO> scheduleList = new ArrayList<>();
				for(String code : scheduleCode) {
					if(StringUtil.isNotNull(code)) {
						ScheduleDTO dto = new ScheduleDTO();
						dto.setCode(code);
						scheduleList.add(dto);
					}
				}
				termDTO1.setSchedule(scheduleList);
				termDTO1.setActiveFlag(selectRS.getInt("active_flag"));
				termDTO1.setTransactionType(TransactionTypeEM.getTransactionTypeEM(selectRS.getInt("transaction_type_id")));
				termList.add(termDTO1);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return termList;
	}
}
