package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import lombok.Cleanup;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.BusTypeCategoryDTO;
import org.in.com.dto.BusTypeCategoryDetailsDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.enumeration.BusSeatTypeEM;
import org.in.com.exception.ServiceException;

public class BusDAO {

	/**
	 * Here we are getting all the amenities
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<BusTypeCategoryDTO> getCategotyDetails() {

		List<BusTypeCategoryDTO> list = new ArrayList<BusTypeCategoryDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id,code,name FROM bus_type_category WHERE active_flag = 1");
			@Cleanup
			PreparedStatement selectTypePS = connection.prepareStatement("SELECT id,code,name FROM bus_type_category_details WHERE bus_type_category_id = ? and active_flag = 1");
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				BusTypeCategoryDTO categoryDTO = new BusTypeCategoryDTO();
				categoryDTO.setCode(selectRS.getString("code"));
				categoryDTO.setName(selectRS.getString("name"));
				categoryDTO.setActiveFlag(1);
				selectTypePS.setInt(1, selectRS.getInt("id"));
				@Cleanup
				ResultSet selectTypeRS = selectTypePS.executeQuery();
				List<BusTypeCategoryDetailsDTO> DetailsList = new ArrayList<BusTypeCategoryDetailsDTO>();
				while (selectTypeRS.next()) {
					BusTypeCategoryDetailsDTO detailsDTO = new BusTypeCategoryDetailsDTO();
					detailsDTO.setCode(selectTypeRS.getString("code"));
					detailsDTO.setName(selectTypeRS.getString("name"));
					DetailsList.add(detailsDTO);
				}
				selectTypePS.clearParameters();
				categoryDTO.setCategoryList(DetailsList);
				list.add(categoryDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public List<BusDTO> getBus(AuthDTO authDTO) {

		List<BusDTO> list = new ArrayList<BusDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement namespaceBusPS = connection.prepareStatement(" SELECT id,code,name,category_code,display_name,active_flag FROM bus WHERE namespace_id = ? and active_flag = 1");
			namespaceBusPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			PreparedStatement selectTypePS = connection.prepareStatement(" SELECT count(1) as seatCount FROM bus_layout WHERE namespace_id = ? AND bus_id = ? AND active_flag = 1");
			@Cleanup
			ResultSet selectRS = namespaceBusPS.executeQuery();
			while (selectRS.next()) {
				BusDTO busDTO = new BusDTO();
				busDTO.setId(selectRS.getInt("id"));
				busDTO.setCode(selectRS.getString("code"));
				busDTO.setName(selectRS.getString("name"));
				busDTO.setCategoryCode(selectRS.getString("category_code"));
				busDTO.setDisplayName(selectRS.getString("display_name"));
				busDTO.setActiveFlag(selectRS.getInt("active_flag"));
				selectTypePS.setInt(1, authDTO.getNamespace().getId());
				selectTypePS.setInt(2, busDTO.getId());
				@Cleanup
				ResultSet selectTypeRS = selectTypePS.executeQuery();
				if (selectTypeRS.next()) {
					busDTO.setSeatCount(selectTypeRS.getInt("seatCount"));
				}
				selectTypePS.clearParameters();
				list.add(busDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public List<BusDTO> getBus(AuthDTO authDTO, BusDTO dto) {

		List<BusDTO> list = new ArrayList<BusDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement namespaceBusPS = connection.prepareStatement(" SELECT id,code,name,category_code,display_name,active_flag FROM bus WHERE namespace_id = ? and code = ? and active_flag = 1 ");
			namespaceBusPS.setInt(1, authDTO.getNamespace().getId());
			namespaceBusPS.setString(2, dto.getCode());
			@Cleanup
			PreparedStatement selectTypePS = connection.prepareStatement(" SELECT count(1) as seatCount FROM bus_layout WHERE namespace_id = ? AND bus_id = ? AND active_flag = 1");
			@Cleanup
			ResultSet selectRS = namespaceBusPS.executeQuery();
			while (selectRS.next()) {
				BusDTO busDTO = new BusDTO();
				busDTO.setId(selectRS.getInt("id"));
				busDTO.setCode(selectRS.getString("code"));
				busDTO.setName(selectRS.getString("name"));
				busDTO.setCategoryCode(selectRS.getString("category_code"));
				busDTO.setDisplayName(selectRS.getString("display_name"));
				busDTO.setActiveFlag(selectRS.getInt("active_flag"));

				selectTypePS.setInt(1, authDTO.getNamespace().getId());
				selectTypePS.setInt(2, dto.getId());
				@Cleanup
				ResultSet selectTypeRS = selectTypePS.executeQuery();
				if (selectTypeRS.next()) {
					busDTO.setSeatCount(selectTypeRS.getInt("seatCount"));
				}
				selectTypePS.clearParameters();
				list.add(busDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public void getBusDTO(AuthDTO authDTO, BusDTO busDTO) {

		List<BusSeatLayoutDTO> layoutDto = new ArrayList<BusSeatLayoutDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement namespaceBusPS = null;
			if (busDTO.getId() != 0) {
				namespaceBusPS = connection.prepareStatement(" SELECT id,code,name,category_code,display_name,seat_count,active_flag FROM bus WHERE namespace_id = ?  AND id = ? and active_flag  = 1");
				namespaceBusPS.setInt(1, authDTO.getNamespace().getId());
				namespaceBusPS.setInt(2, busDTO.getId());
			}
			else {
				namespaceBusPS = connection.prepareStatement(" SELECT id,code,name,category_code,display_name,seat_count,active_flag FROM bus WHERE namespace_id = ?  AND code = ? and active_flag  = 1 ");
				namespaceBusPS.setInt(1, authDTO.getNamespace().getId());
				namespaceBusPS.setString(2, busDTO.getCode());

			}
			@Cleanup
			PreparedStatement selectTypePS = connection.prepareStatement("   SELECT bout.id, bout.code, bout.row_pos, bout.column_pos, bout.seat_name, bout.layer, bout.bus_seat_type_id, bout.sequence, bout.orientation, bout.active_flag FROM bus_layout bout WHERE namespace_id = ? AND bout.bus_id = ? AND bout.active_flag = 1");
			@Cleanup
			ResultSet selectRS = namespaceBusPS.executeQuery();
			while (selectRS.next()) {
				busDTO.setId(selectRS.getInt("id"));
				busDTO.setCode(selectRS.getString("code"));
				busDTO.setName(selectRS.getString("name"));
				busDTO.setCategoryCode(selectRS.getString("category_code"));
				busDTO.setDisplayName(selectRS.getString("display_name"));
				busDTO.setActiveFlag(selectRS.getInt("active_flag"));
				busDTO.setSeatCount(selectRS.getInt("seat_count"));
				
				selectTypePS.setInt(1, authDTO.getNamespace().getId());
				selectTypePS.setInt(2, busDTO.getId());
				@Cleanup
				ResultSet selectTypeRS = selectTypePS.executeQuery();
				while (selectTypeRS.next()) {
					BusSeatLayoutDTO busLayoutDTO = new BusSeatLayoutDTO();
					busLayoutDTO.setId(selectTypeRS.getInt("bout.id"));
					busLayoutDTO.setCode(selectTypeRS.getString("bout.code"));
					busLayoutDTO.setRowPos(selectTypeRS.getInt("bout.row_pos"));
					busLayoutDTO.setColPos(selectTypeRS.getInt("bout.column_pos"));
					busLayoutDTO.setName(selectTypeRS.getString("bout.seat_name"));
					busLayoutDTO.setLayer(selectTypeRS.getInt("bout.layer"));
					busLayoutDTO.setSequence(selectTypeRS.getInt("bout.sequence"));
					busLayoutDTO.setOrientation(selectTypeRS.getInt("bout.orientation"));
					busLayoutDTO.setActiveFlag(selectTypeRS.getInt("bout.active_flag"));
					busLayoutDTO.setBusSeatType(BusSeatTypeEM.getBusSeatTypeEM(selectTypeRS.getInt("bus_seat_type_id")));
					layoutDto.add(busLayoutDTO);
				}
				selectTypePS.clearParameters();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		BusSeatLayoutDTO busLayoutDTO = new BusSeatLayoutDTO();
		busLayoutDTO.setList(layoutDto);
		busDTO.setBusSeatLayoutDTO(busLayoutDTO);
	}

	public List<BusSeatLayoutDTO> getBusLayout(AuthDTO authDTO, BusDTO dto) {

		List<BusSeatLayoutDTO> layoutDto = new ArrayList<BusSeatLayoutDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectTypePS = connection.prepareStatement(" SELECT lout.code, row_pos, column_pos, seat_name, layer, lout.bus_seat_type_id, sequence, lout.orientation , lout.active_flag FROM bus_layout lout  JOIN bus bs ON bs.id = lout.bus_id WHERE lout.namespace_id =  bs.namespace_id  AND lout.namespace_id = ? AND bs.code = ? AND lout.active_flag = 1 AND bs.active_flag = 1");
			selectTypePS.setInt(1, authDTO.getNamespace().getId());
			selectTypePS.setString(2, dto.getCode());
			@Cleanup
			ResultSet selectTypeRS = selectTypePS.executeQuery();
			while (selectTypeRS.next()) {
				BusSeatLayoutDTO busLayoutDTO = new BusSeatLayoutDTO();
				busLayoutDTO.setCode(selectTypeRS.getString("lout.code"));
				busLayoutDTO.setRowPos(selectTypeRS.getInt("row_pos"));
				busLayoutDTO.setColPos(selectTypeRS.getInt("column_pos"));
				busLayoutDTO.setName(selectTypeRS.getString("seat_name"));
				busLayoutDTO.setLayer(selectTypeRS.getInt("layer"));
				busLayoutDTO.setSequence(selectTypeRS.getInt("sequence"));
				busLayoutDTO.setOrientation(selectTypeRS.getInt("lout.orientation"));
				busLayoutDTO.setActiveFlag(selectTypeRS.getInt("active_flag"));
				busLayoutDTO.setBusSeatType(BusSeatTypeEM.getBusSeatTypeEM(selectTypeRS.getInt("bus_seat_type_id")));
				layoutDto.add(busLayoutDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return layoutDto;
	}

	public void getBusUID(AuthDTO authDTO, BusDTO busDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_BUS_IUD(?,?,?,?,? ,?,?,?,?)}");
			callableStatement.setString(++pindex, busDTO.getCode());
			callableStatement.setString(++pindex, busDTO.getName());
			callableStatement.setString(++pindex, busDTO.getCategoryCode());
			callableStatement.setString(++pindex, busDTO.getDisplayName());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setInt(++pindex, busDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				busDTO.setActiveFlag(callableStatement.getInt("pitRowCount"));
				busDTO.setCode(callableStatement.getString("pcrCode"));
			}

		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public List<BusSeatLayoutDTO> getBusLayoutUID(AuthDTO authDTO, BusDTO busDTO, List<BusSeatLayoutDTO> layoutList) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			PreparedStatement namespaceBusPS = connection.prepareStatement(" SELECT id from bus WHERE namespace_id = ? and code = ?");
			namespaceBusPS.setInt(1, authDTO.getNamespace().getId());
			namespaceBusPS.setString(2, busDTO.getCode());
			@Cleanup
			ResultSet selectRS = namespaceBusPS.executeQuery();
			if (selectRS.next()) {
				busDTO.setId(selectRS.getInt("id"));
			}

			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call EZEE_SP_BUS_LAYOUT_IUD(?,?,?,?,? ,?,?,?,?,? ,?,?,?)}");
			for (BusSeatLayoutDTO layoutDTO : layoutList) {
				pindex = 0;
				callableStatement.setString(++pindex, layoutDTO.getCode());
				callableStatement.setInt(++pindex, busDTO.getId());
				callableStatement.setInt(++pindex, layoutDTO.getRowPos());
				callableStatement.setInt(++pindex, layoutDTO.getColPos());
				callableStatement.setString(++pindex, layoutDTO.getName());
				callableStatement.setInt(++pindex, layoutDTO.getLayer());
				callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
				callableStatement.setInt(++pindex, layoutDTO.getBusSeatType().getId());
				callableStatement.setInt(++pindex, layoutDTO.getOrientation());
				callableStatement.setInt(++pindex, layoutDTO.getActiveFlag());
				callableStatement.setInt(++pindex, authDTO.getUser().getId());
				callableStatement.setInt(++pindex, 0);
				callableStatement.registerOutParameter(++pindex, Types.INTEGER);
				callableStatement.execute();
				if (callableStatement.getInt("pitRowCount") > 0) {
					layoutDTO.setActiveFlag(callableStatement.getInt("pitRowCount"));
					layoutDTO.setCode(callableStatement.getString("pcrCode"));
				}
				callableStatement.clearParameters();
			}
			@Cleanup
			CallableStatement callableSeatCount = connection.prepareCall("{call EZEE_SP_BUS_LAYOUT_SEAT_COUNT_UPDATE(?,?)}");
			callableSeatCount.setInt(1, authDTO.getNamespace().getId());
			callableSeatCount.setInt(2, busDTO.getId());
			callableSeatCount.execute();

		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return layoutList;
	}

	public void UpdateSeatSequence(AuthDTO authDTO, BusDTO busDTO, List<BusSeatLayoutDTO> busDTOList) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement layoutBusPS = connection.prepareStatement("update bus_layout SET sequence = ? WHERE code = ? AND namespace_id = ? and bus_id = ? AND active_flag = 1");
			for (BusSeatLayoutDTO layoutDTO : busDTOList) {
				layoutBusPS.setInt(1, layoutDTO.getSequence());
				layoutBusPS.setString(2, layoutDTO.getCode());
				layoutBusPS.setInt(3, authDTO.getNamespace().getId());
				layoutBusPS.setInt(4, busDTO.getId());
				layoutBusPS.execute();
				layoutBusPS.clearParameters();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public NamespaceDTO getNamespace(BusDTO busDTO) {
		NamespaceDTO namespaceDTO = new NamespaceDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT namespace_id FROM bus  WHERE code = ?");
			ps.setString(1, busDTO.getCode());
			@Cleanup
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				namespaceDTO.setId(rs.getInt("namespace_id"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return namespaceDTO;
	}
}
