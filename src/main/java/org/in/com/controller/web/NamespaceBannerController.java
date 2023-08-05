package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.in.com.constants.Text;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.GroupIO;
import org.in.com.controller.web.io.NamespaceBannerDetailsIO;
import org.in.com.controller.web.io.NamespaceBannerIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.NamespaceBannerDTO;
import org.in.com.dto.NamespaceBannerDetailsDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.MediaTypeEM;
import org.in.com.service.NamespaceBannerService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
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
@RequestMapping("{authtoken}/namespace/banner")
public class NamespaceBannerController extends BaseController {

	@Autowired
	NamespaceBannerService bannerService;

	@RequestMapping(value = "/{bannerCode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<NamespaceBannerIO> getBanner(@PathVariable("authtoken") String authtoken, @PathVariable("bannerCode") String bannerCode) {
		NamespaceBannerIO bannerIO = new NamespaceBannerIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		NamespaceBannerDTO bannerDTO = new NamespaceBannerDTO();
		bannerDTO.setCode(bannerCode);
		bannerDTO = bannerService.getNamespaceBanner(authDTO, bannerDTO);
		bannerIO.setCode(bannerDTO.getCode());
		bannerIO.setName(bannerDTO.getName());

		List<GroupIO> groupList = new ArrayList<GroupIO>();
		for (GroupDTO group : bannerDTO.getGroup()) {
			GroupIO groupIO = new GroupIO();
			groupIO.setCode(group.getCode());
			groupIO.setName(group.getName());
			groupList.add(groupIO);
		}
		bannerIO.setGroup(groupList);
		bannerIO.setDisplayModel(bannerDTO.getDisplayModel());

		List<BaseIO> deviceMediumList = new ArrayList<BaseIO>();
		for (DeviceMediumEM deviceMedium : bannerDTO.getDeviceMedium()) {
			BaseIO deviceMediumIO = new BaseIO();
			deviceMediumIO.setCode(deviceMedium.getCode());
			deviceMediumIO.setName(deviceMedium.getName());
			deviceMediumList.add(deviceMediumIO);
		}
		bannerIO.setDeviceMedium(deviceMediumList);

		bannerIO.setFromDate(DateUtil.convertDate(bannerDTO.getFromDate()));
		bannerIO.setToDate(DateUtil.convertDate(bannerDTO.getToDate()));
		bannerIO.setDayOfWeek(bannerDTO.getDayOfWeek());
		bannerIO.setColor(bannerDTO.getColor());
		List<NamespaceBannerDetailsIO> bannerDetailsList = new ArrayList<NamespaceBannerDetailsIO>();
		for (NamespaceBannerDetailsDTO bannerDetailsDTO : bannerDTO.getBannerDetails()) {
			NamespaceBannerDetailsIO bannerDetailsIO = new NamespaceBannerDetailsIO();
			bannerDetailsIO.setCode(bannerDetailsDTO.getCode());
			bannerDetailsIO.setUrl(bannerDetailsDTO.getUrl());
			bannerDetailsIO.setRedirectUrl(bannerDetailsDTO.getRedirectUrl());
			bannerDetailsIO.setAlternateText(bannerDetailsDTO.getAlternateText());
			bannerDetailsIO.setSequence(bannerDetailsDTO.getSequence());
			bannerDetailsIO.setActiveFlag(bannerDetailsDTO.getActiveFlag());
			
			BaseIO messageType = new BaseIO();
			if (bannerDetailsDTO.getMediaType() != null) {
				messageType.setCode(bannerDetailsDTO.getMediaType().getCode());
				messageType.setName(bannerDetailsDTO.getMediaType().getName());
			}
			bannerDetailsIO.setMessageType(messageType);
			
			bannerDetailsList.add(bannerDetailsIO);
		}
		bannerIO.setBannerDetails(bannerDetailsList);
		bannerIO.setUpdatedAt(DateUtil.convertDateTime(bannerDTO.getUpdatedAt()));
		bannerIO.setActiveFlag(bannerDTO.getActiveFlag());

		return ResponseIO.success(bannerIO);
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<NamespaceBannerIO> updateBanner(@PathVariable("authtoken") String authtoken, @RequestBody NamespaceBannerIO banner) {
		NamespaceBannerIO bannerIO = new NamespaceBannerIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		NamespaceBannerDTO bannerDTO = new NamespaceBannerDTO();
		bannerDTO.setCode(banner.getCode());
		bannerDTO.setName(banner.getName());

		List<GroupDTO> groupList = new ArrayList<GroupDTO>();
		for (GroupIO groupIO : banner.getGroup()) {
			GroupDTO groupDTO = new GroupDTO();
			groupDTO.setCode(groupIO.getCode());
			groupList.add(groupDTO);
		}
		bannerDTO.setGroup(groupList);
		bannerDTO.setDisplayModel(StringUtil.isNull(banner.getDisplayModel(), Text.P_UPPER));

		List<DeviceMediumEM> deviceMediumList = new ArrayList<DeviceMediumEM>();
		for (BaseIO deiviceMedium : banner.getDeviceMedium()) {
			deviceMediumList.add(DeviceMediumEM.getDeviceMediumEM(deiviceMedium.getCode()));
		}
		bannerDTO.setDeviceMedium(deviceMediumList);

		bannerDTO.setFromDate(DateUtil.getDateTime(banner.getFromDate()));
		bannerDTO.setToDate(DateUtil.getDateTime(banner.getToDate()));
		bannerDTO.setActiveFlag(banner.getActiveFlag());
		bannerDTO.setDayOfWeek(banner.getDayOfWeek());
		bannerDTO.setColor(banner.getColor());
		NamespaceBannerDTO namespaceBanner = bannerService.Update(authDTO, bannerDTO);
		bannerIO.setCode(namespaceBanner.getCode());
		bannerIO.setActiveFlag(namespaceBanner.getActiveFlag());

		return ResponseIO.success(bannerIO);
	}

	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<NamespaceBannerIO>> getAllBanner(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "-1") int activeFlag) {
		List<NamespaceBannerIO> bannerList = new ArrayList<NamespaceBannerIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<NamespaceBannerDTO> bannerDTOList = bannerService.getAll(authDTO);
		for (NamespaceBannerDTO bannerDTO : bannerDTOList) {
			if (activeFlag != -1 && activeFlag != bannerDTO.getActiveFlag()) {
				continue;
			}
			NamespaceBannerIO bannerIO = new NamespaceBannerIO();
			bannerIO.setCode(bannerDTO.getCode());
			bannerIO.setName(bannerDTO.getName());

			List<GroupIO> groupList = new ArrayList<GroupIO>();
			for (GroupDTO group : bannerDTO.getGroup()) {
				GroupIO groupIO = new GroupIO();
				groupIO.setCode(group.getCode());
				groupIO.setName(group.getName());
				groupList.add(groupIO);
			}
			bannerIO.setGroup(groupList);
			bannerIO.setDisplayModel(bannerDTO.getDisplayModel());

			List<BaseIO> deviceMediumList = new ArrayList<BaseIO>();
			for (DeviceMediumEM deviceMedium : bannerDTO.getDeviceMedium()) {
				BaseIO deviceMediumIO = new BaseIO();
				deviceMediumIO.setCode(deviceMedium.getCode());
				deviceMediumIO.setName(deviceMedium.getName());
				deviceMediumList.add(deviceMediumIO);
			}
			bannerIO.setDeviceMedium(deviceMediumList);

			bannerIO.setFromDate(DateUtil.convertDate(bannerDTO.getFromDate()));
			bannerIO.setToDate(DateUtil.convertDate(bannerDTO.getToDate()));
			bannerIO.setDayOfWeek(bannerDTO.getDayOfWeek());
			bannerIO.setColor(bannerDTO.getColor());
			List<NamespaceBannerDetailsIO> bannerDetailsList = new ArrayList<NamespaceBannerDetailsIO>();
			for (NamespaceBannerDetailsDTO bannerDetailsDTO : bannerDTO.getBannerDetails()) {
				NamespaceBannerDetailsIO bannerDetailsIO = new NamespaceBannerDetailsIO();
				bannerDetailsIO.setCode(bannerDetailsDTO.getCode());
				bannerDetailsIO.setUrl(bannerDetailsDTO.getUrl());
				bannerDetailsIO.setRedirectUrl(bannerDetailsDTO.getRedirectUrl());
				bannerDetailsIO.setAlternateText(bannerDetailsDTO.getAlternateText());
				bannerDetailsIO.setSequence(bannerDetailsDTO.getSequence());
				
				BaseIO messageType = new BaseIO();
				if (bannerDetailsDTO.getMediaType() != null) {
					messageType.setCode(bannerDetailsDTO.getMediaType().getCode());
					messageType.setName(bannerDetailsDTO.getMediaType().getName());
				}
				bannerDetailsIO.setMessageType(messageType);
				bannerDetailsIO.setActiveFlag(bannerDetailsDTO.getActiveFlag());
				bannerDetailsList.add(bannerDetailsIO);
			}
			bannerIO.setBannerDetails(bannerDetailsList);
			bannerIO.setUpdatedAt(DateUtil.convertDateTime(bannerDTO.getUpdatedAt()));
			bannerIO.setActiveFlag(bannerDTO.getActiveFlag());
			bannerList.add(bannerIO);
		}

		return ResponseIO.success(bannerList);
	}

	@RequestMapping(value = "/details/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> updateBannerDetails(@PathVariable("authtoken") String authtoken, @RequestBody NamespaceBannerIO banner) {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		NamespaceBannerDTO bannerDTO = new NamespaceBannerDTO();
		bannerDTO.setCode(banner.getCode());

		List<NamespaceBannerDetailsDTO> bannerDetailsList = new ArrayList<NamespaceBannerDetailsDTO>();
		for (NamespaceBannerDetailsIO bannerDetails : banner.getBannerDetails()) {
			NamespaceBannerDetailsDTO bannerDetailsDTO = new NamespaceBannerDetailsDTO();
			bannerDetailsDTO.setCode(bannerDetails.getCode());
			bannerDetailsDTO.setUrl(StringUtil.isNull(bannerDetails.getUrl(), Text.EMPTY));
			bannerDetailsDTO.setRedirectUrl(StringUtil.isNull(bannerDetails.getRedirectUrl(), Text.EMPTY));
			bannerDetailsDTO.setAlternateText(bannerDetails.getAlternateText());
			bannerDetailsDTO.setSequence(bannerDetails.getSequence());
			bannerDetailsDTO.setMediaType(bannerDetails.getMessageType() != null ? MediaTypeEM.getMediaTypeEM(bannerDetails.getMessageType().getCode()) : null);
			bannerDetailsDTO.setActiveFlag(bannerDetails.getActiveFlag());
			bannerDetailsList.add(bannerDetailsDTO);
		}
		bannerDTO.setBannerDetails(bannerDetailsList);
		bannerService.updateBannerDetails(authDTO, bannerDTO);
		return ResponseIO.success();
	}

}
