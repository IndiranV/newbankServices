package org.in.com.service.impl;

import java.util.List;

import org.in.com.dao.GalleryImageDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GalleryDTO;
import org.in.com.dto.GalleryImageDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.TripDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.GalleryImageService;
import org.in.com.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GalleryImageImpl extends BaseImpl implements GalleryImageService {
	@Autowired
	TripService tripService;

	public List<GalleryDTO> get(AuthDTO authDTO, GalleryDTO dto) {
		GalleryImageDAO imageDAO = new GalleryImageDAO();
		return imageDAO.getGallery(authDTO, dto);
	}

	public List<GalleryDTO> getAll(AuthDTO authDTO) {
		GalleryImageDAO imageDAO = new GalleryImageDAO();
		return imageDAO.getAllGallery(authDTO);
	}

	public GalleryDTO Update(AuthDTO authDTO, GalleryDTO galleryDTO) {
		GalleryImageDAO imageDAO = new GalleryImageDAO();
		return imageDAO.Update(authDTO, galleryDTO);
	}

	public void UpdateImage(AuthDTO authDTO, GalleryDTO galleryDTO, GalleryImageDTO imageDTO) {
		GalleryImageDAO imageDAO = new GalleryImageDAO();
		imageDAO.UpdateImage(authDTO, galleryDTO, imageDTO);
	}

	public List<GalleryImageDTO> getGalleryImageDTO(AuthDTO authDTO, GalleryDTO galleryDTO) {
		GalleryImageDAO imageDAO = new GalleryImageDAO();
		return imageDAO.getGalleryImageDTO(authDTO, galleryDTO);
	}

	public List<GalleryDTO> getScheduleGallery(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		GalleryImageDAO imageDAO = new GalleryImageDAO();
		return imageDAO.getScheduleGallery(authDTO, scheduleDTO);
	}

	public GalleryDTO mapScheduleGallery(AuthDTO authDTO, ScheduleDTO scheduleDTO, GalleryDTO galleryDTO) {
		GalleryImageDAO imageDAO = new GalleryImageDAO();
		return imageDAO.mapScheduleGallery(authDTO, scheduleDTO, galleryDTO);
	}

	public List<GalleryImageDTO> getScheduleGalleryImage(AuthDTO authDTO, TripDTO tripDTO) {
		tripDTO = tripService.getTripDTO(authDTO, tripDTO);
		if (tripDTO.getSchedule().getId() == 0) {
			throw new ServiceException(ErrorCode.INVALID_TRIP_CODE);
		}
		GalleryImageDAO imageDAO = new GalleryImageDAO();
		return imageDAO.getScheduleGalleryImage(authDTO, tripDTO.getSchedule());
	}
}
