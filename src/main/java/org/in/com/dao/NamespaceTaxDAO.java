package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.NamespaceTaxDTO;
import org.in.com.dto.StateDTO;
import org.in.com.dto.enumeration.ProductTypeEM;
import org.in.com.exception.ServiceException;

import lombok.Cleanup;

public class NamespaceTaxDAO {
	public NamespaceTaxDTO Update(AuthDTO authDTO, NamespaceTaxDTO taxDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_NAMESPACE_TAX_IUD(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?)}");
			callableStatement.setString(++pindex, taxDTO.getCode());
			callableStatement.setString(++pindex, taxDTO.getName());
			callableStatement.setString(++pindex, taxDTO.getState() != null ? taxDTO.getState().getCode() : null);
			callableStatement.setInt(++pindex, taxDTO.getProductType().getId());
			callableStatement.setString(++pindex, taxDTO.getTradeName());
			callableStatement.setString(++pindex, taxDTO.getGstin());
			callableStatement.setBigDecimal(++pindex, taxDTO.getCgstValue());
			callableStatement.setBigDecimal(++pindex, taxDTO.getSgstValue());
			callableStatement.setBigDecimal(++pindex, taxDTO.getUgstValue());
			callableStatement.setBigDecimal(++pindex, taxDTO.getIgstValue());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, taxDTO.getSacNumber());
			callableStatement.setInt(++pindex, taxDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				taxDTO.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return taxDTO;
	}

	public List<NamespaceTaxDTO> getAll(AuthDTO authDTO) {
		List<NamespaceTaxDTO> list = new ArrayList<NamespaceTaxDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT ta.name, ta.code, st.name, st.code, ta.product_id, ta.trade_name, ta.gstin, ta.cgst, ta.sgst, ta.ugst, ta.igst, ta.sac_number, ta.active_flag FROM namespace_tax ta, state st WHERE ta.state_id = st.id AND ta.namespace_id = ? AND st.active_flag = 1 AND ta.active_flag < 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				NamespaceTaxDTO taxDTO = new NamespaceTaxDTO();
				taxDTO.setName(selectRS.getString("ta.name"));
				taxDTO.setCode(selectRS.getString("ta.code"));

				StateDTO stateDTO = new StateDTO();
				stateDTO.setName(selectRS.getString("st.name"));
				stateDTO.setCode(selectRS.getString("st.code"));
				taxDTO.setState(stateDTO);

				taxDTO.setProductType(ProductTypeEM.getProductTypeEM(selectRS.getInt("ta.product_id")));
				taxDTO.setTradeName(selectRS.getString("ta.trade_name"));
				taxDTO.setGstin(selectRS.getString("ta.gstin"));
				taxDTO.setCgstValue(selectRS.getBigDecimal("ta.cgst"));
				taxDTO.setSgstValue(selectRS.getBigDecimal("ta.sgst"));
				taxDTO.setUgstValue(selectRS.getBigDecimal("ta.ugst"));
				taxDTO.setIgstValue(selectRS.getBigDecimal("ta.igst"));
				taxDTO.setSacNumber(selectRS.getString("ta.sac_number"));
				taxDTO.setActiveFlag(selectRS.getInt("ta.active_flag"));
				list.add(taxDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public NamespaceTaxDTO getTax(AuthDTO authDTO, NamespaceTaxDTO taxDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT ta.name, ta.code, st.name, st.code, ta.product_id, ta.trade_name, ta.gstin, ta.cgst, ta.sgst, ta.ugst, ta.igst, ta.sac_number, ta.active_flag FROM namespace_tax ta, state st WHERE ta.namespace_id = ? AND ta.state_id = st.id AND ta.code = ? AND st.active_flag = 1 AND ta.active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, taxDTO.getCode());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				taxDTO.setName(selectRS.getString("ta.name"));
				taxDTO.setCode(selectRS.getString("ta.code"));

				StateDTO stateDTO = new StateDTO();
				stateDTO.setName(selectRS.getString("st.name"));
				stateDTO.setCode(selectRS.getString("st.code"));
				taxDTO.setState(stateDTO);

				taxDTO.setProductType(ProductTypeEM.getProductTypeEM(selectRS.getInt("ta.product_id")));
				taxDTO.setTradeName(selectRS.getString("ta.trade_name"));
				taxDTO.setGstin(selectRS.getString("ta.gstin"));
				taxDTO.setCgstValue(selectRS.getBigDecimal("ta.cgst"));
				taxDTO.setSgstValue(selectRS.getBigDecimal("ta.sgst"));
				taxDTO.setUgstValue(selectRS.getBigDecimal("ta.ugst"));
				taxDTO.setIgstValue(selectRS.getBigDecimal("ta.igst"));
				taxDTO.setSacNumber(selectRS.getString("ta.sac_number"));
				taxDTO.setActiveFlag(selectRS.getInt("ta.active_flag"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return taxDTO;
	}

	public void getNamespaceTaxIdCode(AuthDTO authDTO, NamespaceTaxDTO taxDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (taxDTO.getId() != 0) {
				selectPS = connection.prepareStatement("SELECT id, name, code,state_id, trade_name, gstin, cgst, sgst, ugst, igst, sac_number, active_flag FROM namespace_tax WHERE namespace_id = ? AND id = ?");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, taxDTO.getId());
			}
			else {
				selectPS = connection.prepareStatement("SELECT id, name, code, state_id, trade_name, gstin, cgst, sgst, ugst, igst, sac_number, active_flag FROM namespace_tax WHERE namespace_id = ? AND code = ?");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, taxDTO.getCode());
			}

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				taxDTO.setId(selectRS.getInt("id"));
				taxDTO.setName(selectRS.getString("name"));
				taxDTO.setCode(selectRS.getString("code"));

				StateDTO stateDTO = new StateDTO();
				stateDTO.setId(selectRS.getInt("state_id"));
				taxDTO.setState(stateDTO);

				taxDTO.setTradeName(selectRS.getString("trade_name"));
				taxDTO.setGstin(selectRS.getString("gstin"));
				taxDTO.setCgstValue(selectRS.getBigDecimal("cgst"));
				taxDTO.setSgstValue(selectRS.getBigDecimal("sgst"));
				taxDTO.setUgstValue(selectRS.getBigDecimal("ugst"));
				taxDTO.setIgstValue(selectRS.getBigDecimal("igst"));
				taxDTO.setSacNumber(selectRS.getString("sac_number"));
				taxDTO.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public List<String> getNamespaceTaxbyStateCode(AuthDTO authDTO, StateDTO stateDTO) {
		List<String> taxList = new ArrayList<String>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT tax.code FROM namespace_tax tax, state sta WHERE tax.namespace_id = ? AND tax.state_id = sta.id AND tax.active_flag = 1 AND sta.code = ? AND product_id = ?");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, stateDTO.getCode());
			selectPS.setInt(3, ProductTypeEM.BITS.getId());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				taxList.add(selectRS.getString("code"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return taxList;
	}
	
	public boolean checkNamespaceTaxFound(AuthDTO authDTO, StateDTO stateDTO, ProductTypeEM productTypeEM) {
		boolean isFound = false;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT 1 FROM namespace_tax tax, state sta WHERE tax.namespace_id = ? AND tax.state_id = sta.id AND tax.active_flag = 1 AND sta.code = ? AND product_id = ?");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, stateDTO.getCode());
			selectPS.setInt(3, productTypeEM.getId());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				isFound = true;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return isFound;
	}
}
