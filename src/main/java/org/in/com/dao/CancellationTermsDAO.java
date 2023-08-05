package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.CancellationPolicyDTO;
import org.in.com.dto.CancellationTermDTO;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

import lombok.Cleanup;

public class CancellationTermsDAO {
	public List<CancellationTermDTO> getAllCancellationTerms(AuthDTO authDTO) {
		List<CancellationTermDTO> list = new ArrayList<CancellationTermDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement(" SELECT terms.code, terms.name, cancellation_policy_group_key, terms.active_flag  FROM  namespace_cancellation_terms terms WHERE  namespace_id = ? and active_flag < 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			PreparedStatement selectGroupPS = connection.prepareStatement(" SELECT policy.id,from_value,to_value,deduction_amount,percentage_flag,policy_pattern FROM cancellation_policy policy,cancellation_policy_group grgr WHERE policy.id = grgr.cancellation_policy_id AND  grgr.group_key =?");
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				CancellationTermDTO cancellationTermDTO = new CancellationTermDTO();
				cancellationTermDTO.setPolicyGroupKey(selectRS.getString("cancellation_policy_group_key"));
				cancellationTermDTO.setCode(selectRS.getString("code"));
				cancellationTermDTO.setName(selectRS.getString("name"));
				cancellationTermDTO.setActiveFlag(selectRS.getInt("active_flag"));
				List<CancellationPolicyDTO> policyList = new ArrayList<>();
				Map<String, CancellationPolicyDTO> termMap = new HashMap<>();
				selectGroupPS.setString(1, cancellationTermDTO.getPolicyGroupKey());
				@Cleanup
				ResultSet resultSet = selectGroupPS.executeQuery();
				while (resultSet.next()) {
					CancellationPolicyDTO cancellationPolicyDTO = new CancellationPolicyDTO();
					cancellationPolicyDTO.setFromValue(resultSet.getInt("from_value"));
					cancellationPolicyDTO.setToValue(resultSet.getInt("to_value"));
					cancellationPolicyDTO.setDeductionValue(resultSet.getBigDecimal("deduction_amount"));
					cancellationPolicyDTO.setPercentageFlag(resultSet.getInt("percentage_flag"));
					cancellationPolicyDTO.setPolicyPattern(resultSet.getString("policy_pattern"));
					termMap.put(resultSet.getString("policy.id"), cancellationPolicyDTO);
				}
				String[] key = cancellationTermDTO.getPolicyGroupKey().split("F");
				for (String pkey : key) {
					pkey = pkey.replaceAll("[^0-9]+", "");
					policyList.add(termMap.get(pkey));
				}
				cancellationTermDTO.setPolicyList(policyList);
				list.add(cancellationTermDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public void getCancellationPolicyGroup(AuthDTO authDTO, CancellationTermDTO cancellationTermDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT group_key FROM cancellation_policy_group WHERE id = ?");
			selectPS.setInt(1, cancellationTermDTO.getPolicyGroupId());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				cancellationTermDTO.setPolicyGroupKey(selectRS.getString("group_key"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public void getCancellationPolicyGroupId(AuthDTO authDTO, CancellationTermDTO cancellationTermDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id FROM cancellation_policy_group WHERE group_key = ? LIMIT 1");
			selectPS.setString(1, cancellationTermDTO.getPolicyGroupKey());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				cancellationTermDTO.setPolicyGroupId(selectRS.getInt("id"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public CancellationTermDTO getCancellationTerms(AuthDTO authDTO, CancellationTermDTO cancellationTermDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (cancellationTermDTO.getId() != 0) {
				selectPS = connection.prepareStatement(" SELECT terms.code, terms.name, cancellation_policy_group_key, terms.active_flag  FROM  namespace_cancellation_terms terms WHERE  namespace_id = ? AND terms.id = ? AND active_flag < 2");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, cancellationTermDTO.getId());
			}
			else if (StringUtil.isNotNull(cancellationTermDTO.getPolicyGroupKey())) {
				selectPS = connection.prepareStatement(" SELECT terms.code, terms.name, cancellation_policy_group_key, terms.active_flag  FROM  namespace_cancellation_terms terms WHERE  namespace_id = ? AND terms.cancellation_policy_group_key = ? AND active_flag < 2");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, cancellationTermDTO.getPolicyGroupKey());
			}
			else {
				selectPS = connection.prepareStatement(" SELECT terms.code, terms.name, cancellation_policy_group_key, terms.active_flag  FROM  namespace_cancellation_terms terms WHERE  namespace_id = ? AND terms.code = ? AND active_flag < 2");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, cancellationTermDTO.getCode());
			}

			@Cleanup
			PreparedStatement selectGroupPS = connection.prepareStatement(" SELECT policy.id, from_value,to_value,deduction_amount,percentage_flag,policy_pattern FROM cancellation_policy policy,cancellation_policy_group grgr WHERE policy.id = grgr.cancellation_policy_id AND  grgr.group_key =?");
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				cancellationTermDTO.setPolicyGroupKey(selectRS.getString("cancellation_policy_group_key"));
				cancellationTermDTO.setCode(selectRS.getString("code"));
				cancellationTermDTO.setName(selectRS.getString("name"));
				cancellationTermDTO.setActiveFlag(selectRS.getInt("active_flag"));
				List<CancellationPolicyDTO> policyList = new ArrayList<>();
				selectGroupPS.setString(1, cancellationTermDTO.getPolicyGroupKey());
				Map<String, CancellationPolicyDTO> termMap = new HashMap<>();
				@Cleanup
				ResultSet resultSet = selectGroupPS.executeQuery();
				while (resultSet.next()) {
					CancellationPolicyDTO cancellationPolicyDTO = new CancellationPolicyDTO();
					cancellationPolicyDTO.setPolicyId(resultSet.getInt("policy.id"));
					cancellationPolicyDTO.setFromValue(resultSet.getInt("from_value"));
					cancellationPolicyDTO.setToValue(resultSet.getInt("to_value"));
					cancellationPolicyDTO.setDeductionValue(resultSet.getBigDecimal("deduction_amount"));
					cancellationPolicyDTO.setPercentageFlag(resultSet.getInt("percentage_flag"));
					cancellationPolicyDTO.setPolicyPattern(resultSet.getString("policy_pattern"));
					termMap.put(String.valueOf(cancellationPolicyDTO.getPolicyId()), cancellationPolicyDTO);
				}
				String[] key = cancellationTermDTO.getPolicyGroupKey().split("F");
				for (String pkey : key) {
					pkey = pkey.replaceAll("[^0-9]+", "");
					policyList.add(termMap.get(pkey));
				}

				cancellationTermDTO.setPolicyList(policyList);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return cancellationTermDTO;
	}
	
	public CancellationTermDTO getCancellationPolicyByGroupkey(AuthDTO authDTO, CancellationTermDTO cancellationTermDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			@Cleanup
			PreparedStatement selectGroupPS = connection.prepareStatement(" SELECT policy.id, from_value,to_value,deduction_amount,percentage_flag,policy_pattern FROM cancellation_policy policy,cancellation_policy_group grgr WHERE policy.id = grgr.cancellation_policy_id AND  grgr.group_key =?");
			selectGroupPS.setString(1, cancellationTermDTO.getPolicyGroupKey());
			@Cleanup
			ResultSet resultSet = selectGroupPS.executeQuery();
			List<CancellationPolicyDTO> policyList = new ArrayList<>();
			Map<String, CancellationPolicyDTO> termMap = new HashMap<>();
			while (resultSet.next()) {
				CancellationPolicyDTO cancellationPolicyDTO = new CancellationPolicyDTO();
				cancellationPolicyDTO.setPolicyId(resultSet.getInt("policy.id"));
				cancellationPolicyDTO.setFromValue(resultSet.getInt("from_value"));
				cancellationPolicyDTO.setToValue(resultSet.getInt("to_value"));
				cancellationPolicyDTO.setDeductionValue(resultSet.getBigDecimal("deduction_amount"));
				cancellationPolicyDTO.setPercentageFlag(resultSet.getInt("percentage_flag"));
				cancellationPolicyDTO.setPolicyPattern(resultSet.getString("policy_pattern"));
				termMap.put(String.valueOf(cancellationPolicyDTO.getPolicyId()), cancellationPolicyDTO);
			}
			String[] key = cancellationTermDTO.getPolicyGroupKey().split("F");
			for (String pkey : key) {
				pkey = pkey.replaceAll("[^0-9]+", "");
				policyList.add(termMap.get(pkey));
			}
			cancellationTermDTO.setPolicyList(policyList);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return cancellationTermDTO;
	}

	public CancellationTermDTO getCancellationTermsIUD(AuthDTO authDTO, CancellationTermDTO termDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			StringBuilder groupKey = null;
			if (termDTO.getActiveFlag() == 1) {
				for (CancellationPolicyDTO dto : termDTO.getPolicyList()) {
					pindex = 0;
					@Cleanup
					CallableStatement callableStatement = connection.prepareCall("{call   EZEE_SP_CANCELLATION_POLICY_IUD( ?,?,?,? ,?,?,?,?)}");
					callableStatement.setInt(++pindex, dto.getFromValue());
					callableStatement.setInt(++pindex, dto.getToValue());
					callableStatement.setInt(++pindex, dto.getPercentageFlag());
					callableStatement.setBigDecimal(++pindex, dto.getDeductionValue());
					callableStatement.setString(++pindex, dto.getPolicyPattern());
					callableStatement.setInt(++pindex, 0);
					callableStatement.registerOutParameter(++pindex, Types.INTEGER);
					callableStatement.registerOutParameter(++pindex, Types.INTEGER);
					callableStatement.execute();
					if (groupKey == null) {
						groupKey = new StringBuilder();
						groupKey.append(callableStatement.getString("pitId"));

					}
					else {
						groupKey.append(Text.COMMA);
						groupKey.append(callableStatement.getString("pitId"));
					}
				}
				pindex = 0;
				@Cleanup
				CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_CANCELLATION_POLICY_GROUP_IUD( ?,?,?,?)}");
				callableStatement.setString(++pindex, groupKey == null ? null : Text.S_UPPER + groupKey.toString().replaceAll(Text.COMMA, Text.G_UPPER) + Text.E_UPPER);
				callableStatement.setString(++pindex, groupKey == null ? null : groupKey.toString());
				callableStatement.setInt(++pindex, 0);
				callableStatement.registerOutParameter(++pindex, Types.INTEGER);
				callableStatement.execute();
			}
			pindex = 0;
			String TermsKey = groupKey == null ? null : Text.S_UPPER + groupKey.toString().replaceAll(Text.COMMA, Text.G_UPPER) + Text.E_UPPER;
			@Cleanup
			CallableStatement termSt = connection.prepareCall("{call   EZEE_SP_NAMESPACE_CANCELLATION_TERMS_IUD( ?, ?,?,?, ?,?,?,?)}");
			termSt.setString(++pindex, termDTO.getCode());
			termSt.setString(++pindex, termDTO.getName());
			termSt.setString(++pindex, TermsKey);
			termSt.setInt(++pindex, authDTO.getNamespace().getId());
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

}
