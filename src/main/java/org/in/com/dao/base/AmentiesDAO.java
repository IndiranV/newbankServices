package org.in.com.dao.base;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import lombok.Cleanup;

import org.in.com.dao.ConnectDAO;
import org.in.com.dto.AmenitiesDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.exception.ServiceException;

public class AmentiesDAO extends GenericDao implements DaoFactory<AmenitiesDTO> {
	public AmentiesDAO() {
		tableName = "amenities";
	}

	public List<AmenitiesDTO> getAll(AuthDTO authDTO) {
		List<AmenitiesDTO> list = new ArrayList<AmenitiesDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code,name,active_flag FROM amenities where active_flag < 2");
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				AmenitiesDTO amentiesDTO = new AmenitiesDTO();
				amentiesDTO.setCode(selectRS.getString("code"));
				amentiesDTO.setName(selectRS.getString("name"));
				amentiesDTO.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(amentiesDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public AmenitiesDTO Update(AuthDTO authDTO, AmenitiesDTO amentiesDTO) {
		return amentiesDTO;
	}

	@Override
	public AmenitiesDTO get(AuthDTO authDTO, AmenitiesDTO dto) {
		return null;
	}

	@Override
	public AmenitiesDTO insert(AuthDTO authDTO, AmenitiesDTO dto) {
		return null;
	}

	@Override
	public AmenitiesDTO delete(AuthDTO authDTO, AmenitiesDTO dto) {
		return null;
	}

	@Override
	public AmenitiesDTO disable(AuthDTO authDTO, AmenitiesDTO dto) {
		return null;
	}

	@Override
	public AmenitiesDTO enable(AuthDTO authDTO, AmenitiesDTO dto) {
		return null;
	}

}
