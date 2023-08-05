package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.NamespaceTabletSettingsIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.NamespaceTabletSettingsDTO;
import org.in.com.service.NamespaceTabletSettingsService;
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
@RequestMapping(value = "/{authtoken}/namespace/tablet/settings")
public class NamespaceTabletSettingsController extends BaseController {

	@Autowired
	NamespaceTabletSettingsService tabletSettingsService;

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> updateTabletSettings(@PathVariable("authtoken") String authtoken, @RequestBody NamespaceTabletSettingsIO tabletSettings) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		NamespaceTabletSettingsDTO tabletSettingsDTO = new NamespaceTabletSettingsDTO();
		List<String> tabs = new ArrayList<>();
		if (tabletSettings.getTabs() != null) {
			for (String tab : tabletSettings.getTabs()) {
				if (StringUtil.isNull(tab)) {
					continue;
				}
				tabs.add(tab);
			}
		}
		tabletSettingsDTO.setTabs(tabs);
		tabletSettingsDTO.setTripSyncPeriod(tabletSettings.getTripSyncPeriod());
		tabletSettingsDTO.setFlagCodes(tabletSettings.getFlagCodes());
		tabletSettingsDTO.setBookingOpenMinutes(tabletSettings.getBookingOpenMinutes());
		tabletSettingsDTO.setBookingType(tabletSettings.getBookingType());
		tabletSettingsDTO.setMaxDiscountAmount(tabletSettings.getMaxDiscountAmount());
		tabletSettingsDTO.setMaxServiceChargePerSeat(tabletSettings.getMaxServiceChargePerSeat());
		tabletSettingsDTO.setHideBookedTicketFare(tabletSettings.getHideBookedTicketFare());
		tabletSettingsDTO.setForceReleaseFlag(tabletSettings.getForceReleaseFlag());
		tabletSettingsDTO.setTripChartOpenMinutes(tabletSettings.getTripChartOpenMinutes());
		tabletSettingsDTO.setActiveFlag(tabletSettings.getActiveFlag());
		tabletSettingsService.Update(authDTO, tabletSettingsDTO);
		return ResponseIO.success();
	}

	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<NamespaceTabletSettingsIO> getTabletSettings(@PathVariable("authtoken") String authtoken) throws Exception {
		NamespaceTabletSettingsIO tabletSetttingsIO = new NamespaceTabletSettingsIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		NamespaceTabletSettingsDTO tabletSettingsDTO = tabletSettingsService.getNamespaceTabletSetting(authDTO);
		tabletSetttingsIO.setTabs(tabletSettingsDTO.getTabs());
		tabletSetttingsIO.setTripSyncPeriod(tabletSettingsDTO.getTripSyncPeriod());
		tabletSetttingsIO.setFlagCodes(tabletSettingsDTO.getFlagCodes());
		tabletSetttingsIO.setBookingOpenMinutes(tabletSettingsDTO.getBookingOpenMinutes());
		tabletSetttingsIO.setBookingType(tabletSettingsDTO.getBookingType());
		tabletSetttingsIO.setMaxDiscountAmount(tabletSettingsDTO.getMaxDiscountAmount());
		tabletSetttingsIO.setMaxServiceChargePerSeat(tabletSettingsDTO.getMaxServiceChargePerSeat());
		tabletSetttingsIO.setHideBookedTicketFare(tabletSettingsDTO.getHideBookedTicketFare());
		tabletSetttingsIO.setForceReleaseFlag(tabletSettingsDTO.getForceReleaseFlag());
		tabletSetttingsIO.setTripChartOpenMinutes(tabletSettingsDTO.getTripChartOpenMinutes());
		tabletSetttingsIO.setActiveFlag(tabletSettingsDTO.getActiveFlag());
		return ResponseIO.success(tabletSetttingsIO);
	}
}
