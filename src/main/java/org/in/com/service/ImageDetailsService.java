package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ImageDetailsDTO;
import org.in.com.dto.enumeration.ImageCategoryEM;

public interface ImageDetailsService {

	public void updateImageDetails(AuthDTO authDTO, List<ImageDetailsDTO> imageDetailsList, String refernceCode, ImageCategoryEM imageCategory);

	public List<ImageDetailsDTO> getImageDetails(AuthDTO authDTO, String referenceCode, ImageCategoryEM imageCategory);

}
