package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.GalleryIO;
import org.in.com.controller.web.io.GalleryImageIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GalleryDTO;
import org.in.com.dto.GalleryImageDTO;
import org.in.com.service.GalleryImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/{authtoken}/gallery")
public class ImageGalleryController extends BaseController {
	@Autowired
	GalleryImageService imageService;

	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<GalleryIO>> getAllGallery(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "-1") int activeFlag) throws Exception {
		List<GalleryIO> galleryList = new ArrayList<GalleryIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<GalleryDTO> list = (List<GalleryDTO>) imageService.getAll(authDTO);
		for (GalleryDTO galleryDTO : list) {
			if (activeFlag != -1 && activeFlag != galleryDTO.getActiveFlag()) {
				continue;
			}
			GalleryIO gallery = new GalleryIO();
			gallery.setCode(galleryDTO.getCode());
			gallery.setName(galleryDTO.getName());
			gallery.setActiveFlag(galleryDTO.getActiveFlag());
			galleryList.add(gallery);
		}
		return ResponseIO.success(galleryList);
	}

	@RequestMapping(value = "/{code}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<GalleryIO>> getGallery(@PathVariable("authtoken") String authtoken, @PathVariable("code") String code) throws Exception {
		List<GalleryIO> galleryList = new ArrayList<GalleryIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		GalleryDTO dto = new GalleryDTO();
		dto.setCode(code);
		List<GalleryDTO> list = (List<GalleryDTO>) imageService.get(authDTO, dto);
		for (GalleryDTO galleryDTO : list) {
			GalleryIO gallery = new GalleryIO();
			gallery.setCode(galleryDTO.getCode());
			gallery.setName(galleryDTO.getName());
			gallery.setActiveFlag(galleryDTO.getActiveFlag());
			galleryList.add(gallery);
		}
		return ResponseIO.success(galleryList);
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> updateGalleryUID(@PathVariable("authtoken") String authtoken, @RequestBody GalleryIO gallery) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		GalleryDTO galleryDTO = new GalleryDTO();
		galleryDTO.setCode(gallery.getCode());
		galleryDTO.setName(gallery.getName());
		galleryDTO.setActiveFlag(gallery.getActiveFlag());
		imageService.Update(authDTO, galleryDTO);
		BaseIO baseIO = new BaseIO();
		baseIO.setCode(galleryDTO.getCode());
		return ResponseIO.success(baseIO);
	}

	@RequestMapping(value = "/{galleryCode}/image", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<GalleryImageIO>> getGalleryImage(@PathVariable("authtoken") String authtoken, @PathVariable("galleryCode") String galleryCode) throws Exception {
		List<GalleryImageIO> imageList = new ArrayList<GalleryImageIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		GalleryDTO galleryDTO = new GalleryDTO();
		galleryDTO.setCode(galleryCode);
		List<GalleryImageDTO> list = imageService.getGalleryImageDTO(authDTO, galleryDTO);
		for (GalleryImageDTO galleryImageDTO : list) {
			GalleryImageIO galleryImage = new GalleryImageIO();
			galleryImage.setCode(galleryImageDTO.getCode());
			galleryImage.setName(galleryImageDTO.getName());
			galleryImage.setImageURL(galleryImageDTO.getImageURL());
			galleryImage.setActiveFlag(galleryImageDTO.getActiveFlag());
			imageList.add(galleryImage);
		}
		return ResponseIO.success(imageList);
	}

	@RequestMapping(value = "/{galleryCode}/image/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> updateGalleryImageUID(@PathVariable("authtoken") String authtoken, @PathVariable("galleryCode") String galleryCode, @RequestBody GalleryImageIO galleryImage) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		GalleryDTO galleryDTO = new GalleryDTO();
		galleryDTO.setCode(galleryCode);
		GalleryImageDTO imageDTO = new GalleryImageDTO();
		imageDTO.setCode(galleryImage.getCode());
		imageDTO.setName(galleryImage.getName());
		imageDTO.setImageURL(galleryImage.getImageURL());
		imageDTO.setActiveFlag(galleryImage.getActiveFlag());

		imageService.UpdateImage(authDTO, galleryDTO, imageDTO);

		BaseIO baseIO = new BaseIO();
		baseIO.setCode(imageDTO.getCode());
		return ResponseIO.success(baseIO);
	}

}