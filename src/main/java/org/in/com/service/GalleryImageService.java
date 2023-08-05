package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.GalleryDTO;
import org.in.com.dto.GalleryImageDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.TripDTO;

public interface GalleryImageService extends BaseService<GalleryDTO> {

	void UpdateImage(AuthDTO authDTO, GalleryDTO galleryDTO, GalleryImageDTO imageDTO);

	public List<GalleryImageDTO> getGalleryImageDTO(AuthDTO authDTO, GalleryDTO galleryDTO);

	List<GalleryDTO> getScheduleGallery(AuthDTO authDTO, ScheduleDTO scheduleDTO);

	List<GalleryImageDTO> getScheduleGalleryImage(AuthDTO authDTO, TripDTO tripDTO);

	GalleryDTO mapScheduleGallery(AuthDTO authDTO, ScheduleDTO scheduleDTO, GalleryDTO galleryDTO);

}
