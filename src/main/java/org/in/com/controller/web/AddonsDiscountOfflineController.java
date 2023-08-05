package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.in.com.controller.web.io.AddonsDiscountOfflineIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.dto.AddonsDiscountOfflineDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.service.AddonsDiscountOfflineService;
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
@RequestMapping(value = "/{authtoken}/addons/discount")
public class AddonsDiscountOfflineController extends BaseController {
	@Autowired
	AddonsDiscountOfflineService offlineDiscountService;

	@RequestMapping(value = "/offline/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<AddonsDiscountOfflineIO> updateOfflineDiscount(@PathVariable("authtoken") String authtoken, @RequestBody AddonsDiscountOfflineIO discount) throws Exception {
		AddonsDiscountOfflineIO addonsOfflineDiscount = new AddonsDiscountOfflineIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			AddonsDiscountOfflineDTO offlineDiscountDTO = new AddonsDiscountOfflineDTO();
			offlineDiscountDTO.setActiveFlag(discount.getActiveFlag());
			offlineDiscountDTO.setActiveFrom(discount.getActiveFrom());
			offlineDiscountDTO.setActiveTo(discount.getActiveTo());
			offlineDiscountDTO.setCode(discount.getCode());
			offlineDiscountDTO.setDayOfWeek(discount.getDayOfWeek());
			offlineDiscountDTO.setGroupCode(discount.getGroupCode());
			offlineDiscountDTO.setMaxDiscountAmount(discount.getMaxDiscountAmount());
			offlineDiscountDTO.setMinSeatCount(discount.getMinSeatCount());
			offlineDiscountDTO.setMinTicketFare(discount.getMinTicketFare());
			offlineDiscountDTO.setName(discount.getName());
			offlineDiscountDTO.setPercentageFlag(discount.isPercentageFlag());
			offlineDiscountDTO.setRouteCode(discount.getRouteCode());
			offlineDiscountDTO.setScheduleCode(discount.getScheduleCode());
			offlineDiscountDTO.setTravelDateFlag(discount.isTravelDateFlag());
			offlineDiscountDTO.setValue(discount.getValue());

			offlineDiscountService.Update(authDTO, offlineDiscountDTO);
			addonsOfflineDiscount.setCode(offlineDiscountDTO.getCode());
			addonsOfflineDiscount.setActiveFlag(offlineDiscountDTO.getActiveFlag());
		}
		return ResponseIO.success(addonsOfflineDiscount);
	}

	@RequestMapping(value = "/offline/{discountCode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<AddonsDiscountOfflineIO> getOfflineDiscount(@PathVariable("authtoken") String authtoken, @PathVariable("discountCode") String discountCode) throws Exception {
		AddonsDiscountOfflineIO addonsOfflineDiscount = new AddonsDiscountOfflineIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			AddonsDiscountOfflineDTO offlineDiscountDTO = offlineDiscountService.getOfflineDiscount(authDTO, discountCode);
			if (offlineDiscountDTO != null) {
				addonsOfflineDiscount.setCode(offlineDiscountDTO.getCode());
				addonsOfflineDiscount.setActiveFlag(offlineDiscountDTO.getActiveFlag());
				addonsOfflineDiscount.setActiveFrom(offlineDiscountDTO.getActiveFrom());
				addonsOfflineDiscount.setActiveTo(offlineDiscountDTO.getActiveTo());
				addonsOfflineDiscount.setDayOfWeek(offlineDiscountDTO.getDayOfWeek());
				addonsOfflineDiscount.setGroupCode(offlineDiscountDTO.getGroupCode());
				addonsOfflineDiscount.setMaxDiscountAmount(offlineDiscountDTO.getMaxDiscountAmount());
				addonsOfflineDiscount.setMinSeatCount(offlineDiscountDTO.getMinSeatCount());
				addonsOfflineDiscount.setMinTicketFare(offlineDiscountDTO.getMinTicketFare());
				addonsOfflineDiscount.setName(offlineDiscountDTO.getName());
				addonsOfflineDiscount.setPercentageFlag(offlineDiscountDTO.isPercentageFlag());
				addonsOfflineDiscount.setRouteCode(offlineDiscountDTO.getRouteCode());
				addonsOfflineDiscount.setScheduleCode(offlineDiscountDTO.getScheduleCode());
				addonsOfflineDiscount.setTravelDateFlag(offlineDiscountDTO.isTravelDateFlag());
				addonsOfflineDiscount.setValue(offlineDiscountDTO.getValue());
				addonsOfflineDiscount.setActiveFlag(offlineDiscountDTO.getActiveFlag());
			}
		}
		return ResponseIO.success(addonsOfflineDiscount);
	}

	@RequestMapping(value = "/offline", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<AddonsDiscountOfflineIO>> getAllOfflineDiscount(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "-1") int activeFlag) throws Exception {
		List<AddonsDiscountOfflineIO> addonsOfflineDiscounts = new ArrayList<AddonsDiscountOfflineIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<AddonsDiscountOfflineDTO> list = offlineDiscountService.getAll(authDTO);
			for (AddonsDiscountOfflineDTO offlineDiscountDTO : list) {
				if (activeFlag != -1 && activeFlag != offlineDiscountDTO.getActiveFlag()) {
					continue;
				}
				AddonsDiscountOfflineIO addonsOfflineDiscount = new AddonsDiscountOfflineIO();
				addonsOfflineDiscount.setCode(offlineDiscountDTO.getCode());
				addonsOfflineDiscount.setActiveFlag(offlineDiscountDTO.getActiveFlag());
				addonsOfflineDiscount.setActiveFrom(offlineDiscountDTO.getActiveFrom());
				addonsOfflineDiscount.setActiveTo(offlineDiscountDTO.getActiveTo());
				addonsOfflineDiscount.setDayOfWeek(offlineDiscountDTO.getDayOfWeek());
				addonsOfflineDiscount.setGroupCode(offlineDiscountDTO.getGroupCode());
				addonsOfflineDiscount.setMaxDiscountAmount(offlineDiscountDTO.getMaxDiscountAmount());
				addonsOfflineDiscount.setMinSeatCount(offlineDiscountDTO.getMinSeatCount());
				addonsOfflineDiscount.setMinTicketFare(offlineDiscountDTO.getMinTicketFare());
				addonsOfflineDiscount.setName(offlineDiscountDTO.getName());
				addonsOfflineDiscount.setPercentageFlag(offlineDiscountDTO.isPercentageFlag());
				addonsOfflineDiscount.setRouteCode(offlineDiscountDTO.getRouteCode());
				addonsOfflineDiscount.setScheduleCode(offlineDiscountDTO.getScheduleCode());
				addonsOfflineDiscount.setTravelDateFlag(offlineDiscountDTO.isTravelDateFlag());
				addonsOfflineDiscount.setValue(offlineDiscountDTO.getValue());
				addonsOfflineDiscount.setActiveFlag(offlineDiscountDTO.getActiveFlag());
				addonsOfflineDiscounts.add(addonsOfflineDiscount);
			}
		}
		return ResponseIO.success(addonsOfflineDiscounts);
	}
}
