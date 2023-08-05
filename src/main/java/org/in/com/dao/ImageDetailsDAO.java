package org.in.com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ImageDetailsDTO;
import org.in.com.dto.enumeration.ImageCategoryEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;

import lombok.Cleanup;

public class ImageDetailsDAO {

	public void updateImageDetails(AuthDTO authDTO, List<ImageDetailsDTO> imageDetailsList) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("INSERT INTO image_details(namespace_id, image_url_slug, image_category_id, active_flag, created_at) VALUES (?,?,?,1,NOW())", PreparedStatement.RETURN_GENERATED_KEYS);
			for (ImageDetailsDTO imageDetailsDTO : imageDetailsList) {
				int psCount = 0;
				ps.setInt(++psCount, authDTO.getNamespace().getId());
				ps.setString(++psCount, imageDetailsDTO.getImageUrlSlug());
				ps.setInt(++psCount, imageDetailsDTO.getImageCategory().getId());
				ps.executeUpdate();
				@Cleanup
				ResultSet rs = ps.getGeneratedKeys();
				if (rs.next()) {
					imageDetailsDTO.setId(rs.getInt(1));
				}
				else {
					throw new ServiceException(ErrorCode.UPDATE_FAIL);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public void getImageDetails(AuthDTO authDTO, List<ImageDetailsDTO> imageDetailsList) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT image_url_slug, image_category_id, active_flag FROM image_details WHERE id = ? AND namespace_id = ? AND active_flag = 1");
			for (ImageDetailsDTO imageDetailsDTO : imageDetailsList) {
				selectPS.setInt(1, imageDetailsDTO.getId());
				selectPS.setInt(2, authDTO.getNamespace().getId());
				@Cleanup
				ResultSet selectRS = selectPS.executeQuery();
				if (selectRS.next()) {
					imageDetailsDTO.setImageUrlSlug(selectRS.getString("image_url_slug"));
					imageDetailsDTO.setImageCategory(ImageCategoryEM.getImageCategoryEM(selectRS.getInt("image_category_id")));
					imageDetailsDTO.setActiveFlag(selectRS.getInt("active_flag"));
				}
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

}
