package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.ImageDetailsIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.ImageDetailsDTO;
import org.in.com.dto.enumeration.ImageCategoryEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.ImageDetailsService;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/{authtoken}/image/details")
public class ImageDetailsController extends BaseController {

	@Autowired
	ImageDetailsService imageDetailsService;

	@RequestMapping(value = "/{referenceCode}/{imageCategoryCode}/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> updateImageDetails(@PathVariable("authtoken") String authtoken, @PathVariable("referenceCode") String referenceCode, @PathVariable("imageCategoryCode") String imageCategoryCode, @RequestBody List<ImageDetailsIO> galleryImageList) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ImageCategoryEM imageCategoryEM = ImageCategoryEM.getImageCategoryEM(imageCategoryCode);
		if (imageCategoryEM == null) {
			throw new ServiceException(ErrorCode.INVAID_IMAGE_CATEGORY);
		}

		List<ImageDetailsDTO> imagesDetails = new ArrayList<ImageDetailsDTO>();
		if (galleryImageList != null) {
			for (ImageDetailsIO imageDetailsIO : galleryImageList) {
				if (StringUtil.isNull(imageDetailsIO.getImageUrlSlug())) {
					continue;
				}
				ImageDetailsDTO imageDetailsDTO = new ImageDetailsDTO();
				imageDetailsDTO.setImageUrlSlug(imageDetailsIO.getImageUrlSlug());
				imageDetailsDTO.setImageCategory(imageCategoryEM);
				imagesDetails.add(imageDetailsDTO);
			}
		}

		imageDetailsService.updateImageDetails(authDTO, imagesDetails, referenceCode, imageCategoryEM);
		return ResponseIO.success();

	}

	@RequestMapping(value = "/{referenceCode}/{imageCategoryCode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<ImageDetailsIO>> getImageDetails(@PathVariable("authtoken") String authtoken, @PathVariable("referenceCode") String referenceCode, @PathVariable("imageCategoryCode") String imageCategoryCode) throws Exception {
		List<ImageDetailsIO> imageDetailsList = new ArrayList<ImageDetailsIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ImageCategoryEM imageCategoryEM = ImageCategoryEM.getImageCategoryEM(imageCategoryCode);
		if (imageCategoryEM == null) {
			throw new ServiceException(ErrorCode.INVAID_IMAGE_CATEGORY);
		}
		if (StringUtil.isNull(referenceCode)) {
			throw new ServiceException(ErrorCode.CODE_INVALID);
		}
		List<ImageDetailsDTO> imageDetails = imageDetailsService.getImageDetails(authDTO, referenceCode, imageCategoryEM);
		if (imageDetails != null) {
			for (ImageDetailsDTO imageDetailsDTO : imageDetails) {
				ImageDetailsIO imageDetailsIO = new ImageDetailsIO();
				imageDetailsIO.setImageUrlSlug(imageDetailsDTO.getImageUrlSlug());
				imageDetailsIO.setImageCategory(imageDetailsDTO.getImageCategory().getCode());
				imageDetailsList.add(imageDetailsIO);
			}
		}
		return ResponseIO.success(imageDetailsList);
	}
}
