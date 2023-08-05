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
import org.in.com.dto.GalleryDTO;
import org.in.com.dto.GalleryImageDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GalleryImageDAO {
	Logger logger = LoggerFactory.getLogger(GalleryImageDAO.class);

	public List<GalleryDTO> getGallery(AuthDTO authDTO, GalleryDTO galleryDTO) {
		List<GalleryDTO> list = new ArrayList<GalleryDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, name, active_flag FROM namespace_gallery where code  = ? AND namespace_id = ? AND active_flag = 1");
			selectPS.setString(1, galleryDTO.getCode());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				GalleryDTO dto = new GalleryDTO();
				dto.setCode(selectRS.getString("code"));
				dto.setName(selectRS.getString("name"));
				dto.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(dto);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public List<GalleryDTO> getAllGallery(AuthDTO authDTO) {
		List<GalleryDTO> list = new ArrayList<GalleryDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, name, active_flag FROM namespace_gallery WHERE namespace_id = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				GalleryDTO dto = new GalleryDTO();
				dto.setCode(selectRS.getString("code"));
				dto.setName(selectRS.getString("name"));
				dto.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(dto);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public GalleryDTO Update(AuthDTO authDTO, GalleryDTO galleryDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_NAMESPACE_GALLERY_IUD( ?,?,?,? ,?,?,?)}");
			callableStatement.setString(++pindex, galleryDTO.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, galleryDTO.getName());
			callableStatement.setInt(++pindex, galleryDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				galleryDTO.setCode(callableStatement.getString("pcrCode"));
				galleryDTO.setActiveFlag(callableStatement.getInt("pitRowCount"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return galleryDTO;
	}

	public void UpdateImage(AuthDTO authDTO, GalleryDTO galleryDTO, GalleryImageDTO imageDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_NAMESPACE_GALLERY_IMAGE_IUD( ?,?,?,?,?  ,?,?,?,?)}");
			callableStatement.setString(++pindex, imageDTO.getCode());
			callableStatement.setString(++pindex, imageDTO.getName());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, galleryDTO.getCode());
			callableStatement.setString(++pindex, imageDTO.getImageURL());
			callableStatement.setInt(++pindex, imageDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				imageDTO.setCode(callableStatement.getString("pcrCode"));
				imageDTO.setActiveFlag(callableStatement.getInt("pitRowCount"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public List<GalleryImageDTO> getGalleryImageDTO(AuthDTO authDTO, GalleryDTO galleryDTO) {

		List<GalleryImageDTO> list = new ArrayList<GalleryImageDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT img.code, image_url, img.active_flag FROM namespace_gallery nsga, namespace_gallery_image img WHERE nsga.code  = ? AND nsga.id = img.namespace_gallery_id AND img.namespace_id = ? AND img.active_flag = 1");
			selectPS.setString(1, galleryDTO.getCode());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				GalleryImageDTO dto = new GalleryImageDTO();
				dto.setCode(selectRS.getString("code"));
				dto.setImageURL(selectRS.getString("image_url"));
				dto.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(dto);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;

	}

	public List<GalleryDTO> getScheduleGallery(AuthDTO authDTO, ScheduleDTO scheduleDTO) {

		List<GalleryDTO> list = new ArrayList<GalleryDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT img.code, img.name, img.active_flag FROM namespace_gallery img, schedule_image_gallery sig, schedule sche WHERE sche.code  = ? AND sche.id = sig.schedule_id AND img.id = sig.gallery_id AND img.namespace_id = ? AND img.active_flag = 1");
			selectPS.setString(1, scheduleDTO.getCode());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				GalleryDTO dto = new GalleryDTO();
				dto.setCode(selectRS.getString("code"));
				dto.setName(selectRS.getString("name"));
				dto.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(dto);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public GalleryDTO mapScheduleGallery(AuthDTO authDTO, ScheduleDTO scheduleDTO, GalleryDTO galleryDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_SCHEDULE_GALLERY_IUD( ?,?,?,  ?,?,?,?)}");
			callableStatement.setString(++pindex, scheduleDTO.getCode());
			callableStatement.setString(++pindex, galleryDTO.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setInt(++pindex, galleryDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				galleryDTO.setActiveFlag(callableStatement.getInt("pitRowCount"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return galleryDTO;
	}

	public List<GalleryImageDTO> getScheduleGalleryImage(AuthDTO authDTO, ScheduleDTO scheduleDTO) {

		List<GalleryImageDTO> list = new ArrayList<GalleryImageDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT nsga.name,img.code, image_url, img.active_flag FROM namespace_gallery nsga, namespace_gallery_image img,schedule_image_gallery sche WHERE sche.schedule_id = ? AND sche.gallery_id = nsga.id AND nsga.id = img.namespace_gallery_id AND sche.namespace_id = ? AND sche.namespace_id = img.namespace_id AND sche.namespace_id = nsga.namespace_id AND nsga.active_flag = 1 AND img.active_flag = 1");
			selectPS.setInt(1, scheduleDTO.getId());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				GalleryImageDTO dto = new GalleryImageDTO();
				dto.setName(selectRS.getString("name"));
				dto.setCode(selectRS.getString("code"));
				dto.setImageURL(selectRS.getString("image_url"));
				dto.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(dto);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;

	}
}
