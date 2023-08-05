package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.in.com.constants.Numeric;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.DiscountCategoryIO;
import org.in.com.controller.web.io.DiscountCouponIO;
import org.in.com.controller.web.io.DiscountCriteriaIO;
import org.in.com.controller.web.io.DiscountCriteriaSlabIO;
import org.in.com.controller.web.io.GroupIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.UserCustomerIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.DiscountCategoryDTO;
import org.in.com.dto.DiscountCouponDTO;
import org.in.com.dto.DiscountCriteriaDTO;
import org.in.com.dto.DiscountCriteriaSlabDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.UserCustomerDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.FareTypeEM;
import org.in.com.dto.enumeration.SeatGendarEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.DiscountCategoryService;
import org.in.com.service.DiscountCouponService;
import org.in.com.service.DiscountService;
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
@RequestMapping(value = "/{authtoken}/addons")
public class DiscountController extends BaseController {

	@Autowired
	DiscountCategoryService discountCategoryService;

	@Autowired
	DiscountCouponService discountCouponService;

	@Autowired
	DiscountService discountService;

	@RequestMapping(value = "/discount/category", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<DiscountCategoryIO>> getAllCategory(String authToken, @RequestParam(required = false, defaultValue = "-1") int activeFlag) throws Exception {
		List<DiscountCategoryIO> discountlist = new ArrayList<DiscountCategoryIO>();
		AuthDTO authDTO = authService.getAuthDTO(authToken);
		if (authDTO != null) {
			List<DiscountCategoryDTO> list = discountCategoryService.getAll(authDTO);
			for (DiscountCategoryDTO discountdto : list) {
				if (activeFlag != -1 && activeFlag != discountdto.getActiveFlag()) {
					continue;
				}
				DiscountCategoryIO discountio = new DiscountCategoryIO();
				discountio.setCode(discountdto.getCode());
				discountio.setName(discountdto.getName());
				discountio.setDescription(discountdto.getDescription());
				discountio.setActiveFlag(discountdto.getActiveFlag());
				discountlist.add(discountio);
			}
		}
		return ResponseIO.success(discountlist);
	}

	@RequestMapping(value = "/discount/category/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<DiscountCategoryIO> updateDiscountCategory(@PathVariable("authtoken") String authtoken, @RequestBody DiscountCategoryIO discount) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		DiscountCategoryIO discountCategoryIO = new DiscountCategoryIO();
		if (authDTO != null) {
			DiscountCategoryDTO discountCategoryDTO = new DiscountCategoryDTO();
			discountCategoryDTO.setCode(discount.getCode());
			discountCategoryDTO.setName(discount.getName());
			discountCategoryDTO.setActiveFlag(discount.getActiveFlag());
			discountCategoryDTO.setDescription(discount.getDescription());
			discountCategoryService.Update(authDTO, discountCategoryDTO);
			discountCategoryIO.setCode(discountCategoryDTO.getCode());
			discountCategoryIO.setActiveFlag(discountCategoryDTO.getActiveFlag());
		}
		return ResponseIO.success(discountCategoryIO);

	}

	@RequestMapping(value = "/discount/coupon", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<DiscountCouponIO>> getAllCoupon(String authToken, @RequestParam(required = false, defaultValue = "-1") int activeFlag, @RequestParam(required = false, defaultValue = "NA") String discountType) throws Exception {
		List<DiscountCouponIO> discountCouponlist = new ArrayList<DiscountCouponIO>();
		AuthDTO authDTO = authService.getAuthDTO(authToken);
		if (authDTO != null) {
			List<DiscountCouponDTO> list = discountCouponService.getDiscountCoupons(authDTO, discountType);
			for (DiscountCouponDTO discountCoupondto : list) {
				if (activeFlag != -1 && activeFlag != discountCoupondto.getActiveFlag()) {
					continue;
				}
				DiscountCouponIO discountCouponio = new DiscountCouponIO();
				discountCouponio.setCode(discountCoupondto.getCode());
				discountCouponio.setCoupon(discountCoupondto.getCoupon());
				DiscountCategoryIO discountCategoryIO = new DiscountCategoryIO();
				discountCategoryIO.setCode(discountCoupondto.getDiscountCategory().getCode());
				discountCategoryIO.setName(discountCoupondto.getDiscountCategory().getName());
				discountCouponio.setDiscountCategory(discountCategoryIO);
				discountCouponio.setActiveDesription(discountCoupondto.getActiveDesription());
				discountCouponio.setErrorDescription(discountCoupondto.getErrorDescription());
				discountCouponio.setUsedCount(discountCoupondto.getUsedCount());
				discountCouponio.setActiveFlag(discountCoupondto.getActiveFlag());

				UserCustomerIO userCustomer = new UserCustomerIO();
				if (discountCoupondto.getUserCustomer() != null && discountCoupondto.getUserCustomer().getId() != 0) {
					userCustomer.setCode(discountCoupondto.getUserCustomer().getCode());
					userCustomer.setName(discountCoupondto.getUserCustomer().getName());
					userCustomer.setMobile(discountCoupondto.getUserCustomer().getMobile());
				}
				discountCouponio.setUserCustomer(userCustomer);

				List<DiscountCouponIO> overrideList = new ArrayList<DiscountCouponIO>();
				for (DiscountCouponDTO couponOverrideDTO : discountCoupondto.getOverrideList()) {
					DiscountCouponIO discountCouponOverrideIO = new DiscountCouponIO();
					discountCouponOverrideIO.setCode(couponOverrideDTO.getCode());
					discountCouponOverrideIO.setCoupon(couponOverrideDTO.getCoupon());

					DiscountCategoryIO discountCategoryOverride = new DiscountCategoryIO();
					discountCategoryOverride.setCode(couponOverrideDTO.getDiscountCategory().getCode());
					discountCategoryOverride.setName(couponOverrideDTO.getDiscountCategory().getName());
					discountCouponOverrideIO.setDiscountCategory(discountCategoryOverride);

					discountCouponOverrideIO.setActiveDesription(couponOverrideDTO.getActiveDesription());
					discountCouponOverrideIO.setErrorDescription(couponOverrideDTO.getErrorDescription());
					discountCouponOverrideIO.setUsedCount(couponOverrideDTO.getUsedCount());
					discountCouponOverrideIO.setActiveFlag(couponOverrideDTO.getActiveFlag());
					overrideList.add(discountCouponOverrideIO);
				}
				discountCouponio.setOverrideList(overrideList);
				discountCouponlist.add(discountCouponio);
			}
		}
		return ResponseIO.success(discountCouponlist);
	}

	@RequestMapping(value = "/discount/coupon/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<DiscountCouponIO> updateDiscountCoupon(@PathVariable("authtoken") String authtoken, @RequestBody DiscountCouponIO discountCoupon) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		DiscountCouponIO discountCouponIO = new DiscountCouponIO();
		if (authDTO != null) {
			DiscountCouponDTO discountCouponDTO = new DiscountCouponDTO();
			if (discountCoupon.getDiscountCategory() != null && StringUtil.isNotNull(discountCoupon.getDiscountCategory().getCode())) {
				DiscountCategoryDTO categoryDTO = new DiscountCategoryDTO();
				categoryDTO.setCode(discountCoupon.getDiscountCategory().getCode());
				discountCouponDTO.setDiscountCategory(categoryDTO);
			}
			discountCouponDTO.setCode(discountCoupon.getCode());
			discountCouponDTO.setCoupon(discountCoupon.getCoupon());
			discountCouponDTO.setActiveDesription(discountCoupon.getActiveDesription());
			discountCouponDTO.setErrorDescription(discountCoupon.getErrorDescription());
			discountCouponDTO.setLookupCode(discountCoupon.getLookupCode());
			discountCouponDTO.setActiveFlag(discountCoupon.getActiveFlag());
			discountCouponService.Update(authDTO, discountCouponDTO);
			discountCouponIO.setCode(discountCouponDTO.getCode());
			discountCouponIO.setActiveFlag(discountCouponDTO.getActiveFlag());
		}
		return ResponseIO.success(discountCouponIO);

	}

	@RequestMapping(value = "/discount", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<DiscountCriteriaIO>> getAllDiscount(String authToken, @RequestParam(required = false, defaultValue = "-1") int activeFlag) {
		List<DiscountCriteriaIO> discountList = new ArrayList<DiscountCriteriaIO>();
		AuthDTO authDTO = authService.getAuthDTO(authToken);
		if (authDTO != null) {
			List<DiscountCriteriaDTO> list = discountService.getAll(authDTO);
			for (DiscountCriteriaDTO discountDto : list) {
				if (activeFlag != -1 && activeFlag != discountDto.getActiveFlag()) {
					continue;
				}
				DiscountCriteriaIO discountio = new DiscountCriteriaIO();
				discountio.setCode(discountDto.getCode());
				DiscountCouponIO discountCouponIO = new DiscountCouponIO();
				discountCouponIO.setCode(discountDto.getDiscountCoupon().getCode());
				discountCouponIO.setCoupon(discountDto.getDiscountCoupon().getCoupon());

				UserCustomerIO userCustomer = new UserCustomerIO();
				if (discountDto.getDiscountCoupon().getUserCustomer() != null && discountDto.getDiscountCoupon().getUserCustomer().getId() != 0) {
					userCustomer.setCode(discountDto.getDiscountCoupon().getUserCustomer().getCode());
					userCustomer.setName(discountDto.getDiscountCoupon().getUserCustomer().getName());
					userCustomer.setMobile(discountDto.getDiscountCoupon().getUserCustomer().getMobile());
				}
				discountCouponIO.setUserCustomer(userCustomer);

				discountio.setDiscountCoupon(discountCouponIO);

				List<GroupIO> groupList = new ArrayList<GroupIO>();
				for (GroupDTO groupDTO : discountDto.getGroupList()) {
					GroupIO groupIO = new GroupIO();
					groupIO.setCode(groupDTO.getCode());
					groupIO.setName(groupDTO.getName());
					groupIO.setLevel(groupDTO.getLevel());
					groupList.add(groupIO);
				}
				if (groupList.isEmpty()) {
					GroupIO groupIO = new GroupIO();
					groupIO.setName("All Group");
					groupList.add(groupIO);
				}
				discountio.setUserGroup(groupList);

				discountio.setActiveFrom(discountDto.getActiveFrom());
				discountio.setActiveTo(discountDto.getActiveTo());
				discountio.setDayOfWeek(discountDto.getDayOfWeek());
				discountio.setValue(discountDto.getValue());
				discountio.setPercentageFlag(discountDto.isPercentageFlag());
				discountio.setTravelDateFlag(discountDto.isTravelDateFlag());
				discountio.setShowOfferPageFlag(discountDto.isShowOfferPageFlag());
				discountio.setMaxUsageLimitPerUser(discountDto.getMaxUsageLimitPerUser());
				discountio.setDeviceMedium(discountDto.getDeviceMediumCodes());
				discountio.setBeforeBookingMinitues(discountDto.getBeforeBookingMinitues());
				discountio.setAfterBookingMinitues(discountDto.getAfterBookingMinitues());
				discountio.setMinTicketFare(discountDto.getMinTicketFare());
				discountio.setMaxDiscountAmount(discountDto.getMaxDiscountAmount());
				discountio.setMinSeatCount(discountDto.getMinSeatCount());
				discountio.setAge(discountDto.getAge());
				discountio.setMobileNumber(discountDto.getMobileNumber());
				discountio.setServiceTiming(discountDto.getServiceTiming());
				discountio.setScheduleCode(discountDto.getScheduleCode());
				discountio.setRouteCode(discountDto.getRouteCode());

				List<BaseIO> genderList = new ArrayList<BaseIO>();
				for (SeatGendarEM genderDTO : discountDto.getSeatGender()) {
					BaseIO gender = new BaseIO();
					gender.setCode(genderDTO.getCode());
					gender.setName(genderDTO.getName());
					genderList.add(gender);
				}
				discountio.setSeatGender(genderList);

				discountio.setActiveFlag(discountDto.getActiveFlag());
				discountList.add(discountio);
			}
		}
		return ResponseIO.success(discountList);
	}

	@RequestMapping(value = "/discount/filter/coupon", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<DiscountCriteriaIO>> getAllDiscountByCoupon(String authToken, String couponCode) throws Exception {
		List<DiscountCriteriaIO> discountList = new ArrayList<DiscountCriteriaIO>();
		AuthDTO authDTO = authService.getAuthDTO(authToken);
		if (authDTO != null) {
			DiscountCouponDTO couponDTO = new DiscountCouponDTO();
			couponDTO.setCode(couponCode);
			List<DiscountCriteriaDTO> list = discountService.getAllDiscountByCoupon(authDTO, couponDTO);
			for (DiscountCriteriaDTO discountDto : list) {
				DiscountCriteriaIO discountio = new DiscountCriteriaIO();
				discountio.setCode(discountDto.getCode());
				DiscountCouponIO discountCouponIO = new DiscountCouponIO();
				DiscountCategoryIO discountCategoryIO = new DiscountCategoryIO();
				discountCategoryIO.setCode(discountDto.getDiscountCoupon().getDiscountCategory().getCode());
				discountCategoryIO.setName(discountDto.getDiscountCoupon().getDiscountCategory().getName());
				discountCouponIO.setCode(discountDto.getDiscountCoupon().getCode());
				discountCouponIO.setCoupon(discountDto.getDiscountCoupon().getCoupon());
				discountCouponIO.setDiscountCategory(discountCategoryIO);
				discountio.setDiscountCoupon(discountCouponIO);

				List<GroupIO> groupList = new ArrayList<GroupIO>();
				for (GroupDTO groupDTO : discountDto.getGroupList()) {
					GroupIO groupIO = new GroupIO();
					groupIO.setCode(groupDTO.getCode());
					groupIO.setName(groupDTO.getName());
					groupIO.setLevel(groupDTO.getLevel());
					groupList.add(groupIO);
				}
				if (groupList.isEmpty()) {
					GroupIO groupIO = new GroupIO();
					groupIO.setName("All Group");
					groupList.add(groupIO);
				}
				discountio.setUserGroup(groupList);

				discountio.setActiveFrom(discountDto.getActiveFrom());
				discountio.setActiveTo(discountDto.getActiveTo());
				discountio.setDayOfWeek(discountDto.getDayOfWeek());
				discountio.setValue(discountDto.getValue());
				discountio.setPercentageFlag(discountDto.isPercentageFlag());
				discountio.setTravelDateFlag(discountDto.isTravelDateFlag());
				discountio.setShowOfferPageFlag(discountDto.isShowOfferPageFlag());
				discountio.setMaxUsageLimitPerUser(discountDto.getMaxUsageLimitPerUser());
				discountio.setDeviceMedium(discountDto.getDeviceMediumCodes());
				discountio.setBeforeBookingMinitues(discountDto.getBeforeBookingMinitues());
				discountio.setAfterBookingMinitues(discountDto.getAfterBookingMinitues());
				discountio.setMinTicketFare(discountDto.getMinTicketFare());
				discountio.setMaxDiscountAmount(discountDto.getMaxDiscountAmount());
				discountio.setMinSeatCount(discountDto.getMinSeatCount());
				discountio.setAge(discountDto.getAge());
				discountio.setMobileNumber(discountDto.getMobileNumber());
				discountio.setServiceTiming(discountDto.getServiceTiming());
				discountio.setScheduleCode(discountDto.getScheduleCode());
				discountio.setRouteCode(discountDto.getRouteCode());
				// Discount Criteria Slab
				List<DiscountCriteriaSlabIO> slabList = new ArrayList<DiscountCriteriaSlabIO>();
				for (DiscountCriteriaSlabDTO slabDTO : discountDto.getSlabList()) {
					DiscountCriteriaSlabIO slab = new DiscountCriteriaSlabIO();
					slab.setSlabFromValue(slabDTO.getSlabFromValue());
					slab.setSlabToValue(slabDTO.getSlabToValue());
					slab.setSlabValue(slabDTO.getSlabValue());
					slab.setSlabValueType(slabDTO.getSlabValueType().getCode());
					slabList.add(slab);
				}
				List<BaseIO> genderList = new ArrayList<BaseIO>();
				for (SeatGendarEM genderDTO : discountDto.getSeatGender()) {
					BaseIO gender = new BaseIO();
					gender.setCode(genderDTO.getCode());
					gender.setName(genderDTO.getName());
					genderList.add(gender);
				}
				discountio.setSeatGender(genderList);
				discountio.setDiscountSlab(slabList);
				discountio.setActiveFlag(discountDto.getActiveFlag());
				discountList.add(discountio);
			}
		}
		return ResponseIO.success(discountList);
	}

	@RequestMapping(value = "/discount/filter/category", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<DiscountCriteriaIO>> getAllDiscountByCategory(String authToken, String categoryCode) throws Exception {
		List<DiscountCriteriaIO> discountList = new ArrayList<DiscountCriteriaIO>();
		AuthDTO authDTO = authService.getAuthDTO(authToken);
		if (authDTO != null) {
			DiscountCategoryDTO categoryDTO = new DiscountCategoryDTO();
			categoryDTO.setCode(categoryCode);
			List<DiscountCriteriaDTO> list = discountService.getAllDiscountByCategory(authDTO, categoryDTO);
			for (DiscountCriteriaDTO discountDto : list) {
				DiscountCriteriaIO discountio = new DiscountCriteriaIO();
				discountio.setCode(discountDto.getCode());
				DiscountCouponIO discountCouponIO = new DiscountCouponIO();
				discountCouponIO.setCode(discountDto.getDiscountCoupon().getCode());
				discountCouponIO.setCoupon(discountDto.getDiscountCoupon().getCoupon());
				discountio.setDiscountCoupon(discountCouponIO);

				List<GroupIO> groupList = new ArrayList<GroupIO>();
				for (GroupDTO groupDTO : discountDto.getGroupList()) {
					GroupIO groupIO = new GroupIO();
					groupIO.setCode(groupDTO.getCode());
					groupIO.setName(groupDTO.getName());
					groupIO.setLevel(groupDTO.getLevel());
					groupList.add(groupIO);
				}
				if (groupList.isEmpty()) {
					GroupIO groupIO = new GroupIO();
					groupIO.setName("All Group");
					groupList.add(groupIO);
				}
				discountio.setUserGroup(groupList);

				discountio.setActiveFrom(discountDto.getActiveFrom());
				discountio.setActiveTo(discountDto.getActiveTo());
				discountio.setDayOfWeek(discountDto.getDayOfWeek());
				discountio.setValue(discountDto.getValue());
				discountio.setPercentageFlag(discountDto.isPercentageFlag());
				discountio.setTravelDateFlag(discountDto.isTravelDateFlag());
				discountio.setShowOfferPageFlag(discountDto.isShowOfferPageFlag());
				discountio.setMaxUsageLimitPerUser(discountDto.getMaxUsageLimitPerUser());
				discountio.setDeviceMedium(discountDto.getDeviceMediumCodes());
				discountio.setBeforeBookingMinitues(discountDto.getBeforeBookingMinitues());
				discountio.setAfterBookingMinitues(discountDto.getAfterBookingMinitues());
				discountio.setMinTicketFare(discountDto.getMinTicketFare());
				discountio.setMaxDiscountAmount(discountDto.getMaxDiscountAmount());
				discountio.setMinSeatCount(discountDto.getMinSeatCount());
				discountio.setAge(discountDto.getAge());
				discountio.setMobileNumber(discountDto.getMobileNumber());
				discountio.setServiceTiming(discountDto.getServiceTiming());
				discountio.setScheduleCode(discountDto.getScheduleCode());
				discountio.setRouteCode(discountDto.getRouteCode());
				// Discount Criteria Slab
				List<DiscountCriteriaSlabIO> slabList = new ArrayList<DiscountCriteriaSlabIO>();
				for (DiscountCriteriaSlabDTO slabDTO : discountDto.getSlabList()) {
					DiscountCriteriaSlabIO slab = new DiscountCriteriaSlabIO();
					slab.setSlabFromValue(slabDTO.getSlabFromValue());
					slab.setSlabToValue(slabDTO.getSlabToValue());
					slab.setSlabValue(slabDTO.getSlabValue());
					slab.setSlabValueType(slabDTO.getSlabValueType().getCode());
					slabList.add(slab);
				}
				discountio.setDiscountSlab(slabList);

				List<BaseIO> genderList = new ArrayList<BaseIO>();
				for (SeatGendarEM genderDTO : discountDto.getSeatGender()) {
					BaseIO gender = new BaseIO();
					gender.setCode(genderDTO.getCode());
					gender.setName(genderDTO.getName());
					genderList.add(gender);
				}
				discountio.setSeatGender(genderList);
				discountio.setActiveFlag(discountDto.getActiveFlag());
				discountList.add(discountio);
			}
		}
		return ResponseIO.success(discountList);
	}

	@RequestMapping(value = "/discount/criteria/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<DiscountCriteriaIO> updateDiscount(@PathVariable("authtoken") String authtoken, @RequestBody DiscountCriteriaIO discount) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		DiscountCriteriaIO discountIO = new DiscountCriteriaIO();
		if (authDTO != null) {
			DiscountCriteriaDTO discountdto = new DiscountCriteriaDTO();

			DiscountCouponDTO discountCouponDTO = new DiscountCouponDTO();
			if (discount.getDiscountCoupon() != null && StringUtil.isNotNull(discount.getDiscountCoupon().getCode())) {
				discountCouponDTO.setCode(discount.getDiscountCoupon().getCode());
			}
			discountdto.setDiscountCoupon(discountCouponDTO);
			discountdto.setCode(discount.getCode());

			List<GroupDTO> groupList = new ArrayList<>();

			if (discount.getUserGroup() != null) {
				for (GroupIO group : discount.getUserGroup()) {
					if (StringUtil.isNull(group.getCode())) {
						continue;
					}
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setCode(group.getCode());
					groupList.add(groupDTO);
				}
			}

			discountdto.setGroupList(groupList);
			discountdto.setActiveFrom(discount.getActiveFrom());
			discountdto.setActiveTo(discount.getActiveTo());
			discountdto.setDayOfWeek(discount.getDayOfWeek());
			discountdto.setValue(discount.getValue());
			discountdto.setPercentageFlag(discount.isPercentageFlag());
			discountdto.setTravelDateFlag(discount.isTravelDateFlag());
			discountdto.setRoundTripFlag(discount.isRoundTripFlag());
			discountdto.setShowOfferPageFlag(discount.isShowOfferPageFlag());
			discountdto.setMaxUsageLimitPerUser(discount.getMaxUsageLimitPerUser());
			discountdto.setMaxDiscountAmount(discount.getMaxDiscountAmount());
			discountdto.setMinTicketFare(discount.getMinTicketFare());
			discountdto.setAge(discount.getAge());
			discountdto.setMobileNumber(discount.getMobileNumber());
			discountdto.setServiceTiming(discount.getServiceTiming());
			discountdto.setMinSeatCount(discount.getMinSeatCount());
			discountdto.setAfterBookingMinitues(discount.getAfterBookingMinitues());
			discountdto.setBeforeBookingMinitues(discount.getBeforeBookingMinitues());

			List<DeviceMediumEM> deviceMediumList = new ArrayList<>();
			if (discount.getDeviceMedium() != null) {
				for (String code : discount.getDeviceMedium()) {
					deviceMediumList.add(DeviceMediumEM.getDeviceMediumEM(code));
				}
			}

			discountdto.setDeviceMedium(deviceMediumList);
			discountdto.setScheduleCode(discount.getScheduleCode());
			discountdto.setRouteCode(discount.getRouteCode());
			discountdto.setActiveFlag(discount.getActiveFlag());

			// Discount Criteria Slab
			List<DiscountCriteriaSlabDTO> slabList = new ArrayList<DiscountCriteriaSlabDTO>();
			if (discount.getDiscountSlab() != null && !discount.getDiscountSlab().isEmpty()) {
				for (DiscountCriteriaSlabIO slabIO : discount.getDiscountSlab()) {
					DiscountCriteriaSlabDTO criteriaSlabDTO = new DiscountCriteriaSlabDTO();
					criteriaSlabDTO.setSlabFromValue(slabIO.getSlabFromValue());
					criteriaSlabDTO.setSlabToValue(slabIO.getSlabToValue());
					criteriaSlabDTO.setSlabValue(slabIO.getSlabValue());
					criteriaSlabDTO.setSlabValueType(FareTypeEM.getFareTypeEM(slabIO.getSlabValueType()));
					slabList.add(criteriaSlabDTO);
				}
			}
			discountdto.setSlabList(slabList);

			List<SeatGendarEM> genderList = new ArrayList<SeatGendarEM>();
			if (discount.getSeatGender() != null && !discount.getSeatGender().isEmpty()) {
				for (BaseIO gender : discount.getSeatGender()) {
					genderList.add(SeatGendarEM.getSeatGendarEM(gender.getCode()));
				}
			}
			discountdto.setSeatGender(genderList);
			discountService.Update(authDTO, discountdto);
			discountIO.setCode(discountdto.getCode());
			discountIO.setActiveFlag(discountdto.getActiveFlag());

		}
		return ResponseIO.success(discountIO);

	}

	@RequestMapping(value = "/discount/customer/coupon", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<DiscountCriteriaIO> getCustomerDiscountCoupon(String authToken, String mobile) throws Exception {
		DiscountCriteriaIO discountIO = new DiscountCriteriaIO();
		AuthDTO authDTO = authService.getAuthDTO(authToken);
		if (authDTO != null) {
			if (!StringUtil.isValidMobileNumber(mobile)) {
				throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
			}
			UserCustomerDTO customerDTO = new UserCustomerDTO();
			customerDTO.setMobile(mobile);
			DiscountCriteriaDTO discountCriteria = discountService.getCustomerDiscountCoupon(authDTO, customerDTO);

			if (discountCriteria.getActiveFlag() != Numeric.ZERO_INT) {
				discountIO.setCode(discountCriteria.getCode());

				DiscountCategoryIO discountCategoryIO = new DiscountCategoryIO();
				discountCategoryIO.setCode(discountCriteria.getDiscountCoupon().getDiscountCategory().getCode());
				discountCategoryIO.setName(discountCriteria.getDiscountCoupon().getDiscountCategory().getName());

				DiscountCouponIO discountCouponIO = new DiscountCouponIO();
				discountCouponIO.setCode(discountCriteria.getDiscountCoupon().getCode());
				discountCouponIO.setCoupon(discountCriteria.getDiscountCoupon().getCoupon());
				discountCouponIO.setDiscountCategory(discountCategoryIO);
				discountIO.setDiscountCoupon(discountCouponIO);

				List<GroupIO> groupList = new ArrayList<GroupIO>();
				for (GroupDTO groupDTO : discountCriteria.getGroupList()) {
					GroupIO groupIO = new GroupIO();
					groupIO.setCode(groupDTO.getCode());
					groupIO.setName(groupDTO.getName());
					groupIO.setLevel(groupDTO.getLevel());
					groupList.add(groupIO);
				}
				if (groupList.isEmpty()) {
					GroupIO groupIO = new GroupIO();
					groupIO.setName("All Group");
					groupList.add(groupIO);
				}
				discountIO.setUserGroup(groupList);

				discountIO.setActiveFrom(discountCriteria.getActiveFrom());
				discountIO.setActiveTo(discountCriteria.getActiveTo());
				discountIO.setDayOfWeek(discountCriteria.getDayOfWeek());
				discountIO.setValue(discountCriteria.getValue());
				discountIO.setPercentageFlag(discountCriteria.isPercentageFlag());
				discountIO.setTravelDateFlag(discountCriteria.isTravelDateFlag());
				discountIO.setShowOfferPageFlag(discountCriteria.isShowOfferPageFlag());
				discountIO.setMaxUsageLimitPerUser(discountCriteria.getMaxUsageLimitPerUser());
				discountIO.setDeviceMedium(discountCriteria.getDeviceMediumCodes());
				discountIO.setBeforeBookingMinitues(discountCriteria.getBeforeBookingMinitues());
				discountIO.setAfterBookingMinitues(discountCriteria.getAfterBookingMinitues());
				discountIO.setMinTicketFare(discountCriteria.getMinTicketFare());
				discountIO.setMaxDiscountAmount(discountCriteria.getMaxDiscountAmount());
				discountIO.setMinSeatCount(discountCriteria.getMinSeatCount());
				discountIO.setAge(discountCriteria.getAge());
				discountIO.setMobileNumber(discountCriteria.getMobileNumber());
				discountIO.setServiceTiming(discountCriteria.getServiceTiming());
				discountIO.setScheduleCode(discountCriteria.getScheduleCode());
				discountIO.setRouteCode(discountCriteria.getRouteCode());
				// Discount Criteria Slab
				List<DiscountCriteriaSlabIO> slabList = new ArrayList<DiscountCriteriaSlabIO>();
				for (DiscountCriteriaSlabDTO slabDTO : discountCriteria.getSlabList()) {
					DiscountCriteriaSlabIO slab = new DiscountCriteriaSlabIO();
					slab.setSlabFromValue(slabDTO.getSlabFromValue());
					slab.setSlabToValue(slabDTO.getSlabToValue());
					slab.setSlabValue(slabDTO.getSlabValue());
					slab.setSlabValueType(slabDTO.getSlabValueType().getCode());
					slabList.add(slab);
				}
				List<BaseIO> genderList = new ArrayList<BaseIO>();
				for (SeatGendarEM genderDTO : discountCriteria.getSeatGender()) {
					BaseIO gender = new BaseIO();
					gender.setCode(genderDTO.getCode());
					gender.setName(genderDTO.getName());
					genderList.add(gender);
				}
				discountIO.setSeatGender(genderList);
				discountIO.setDiscountSlab(slabList);
				discountIO.setActiveFlag(discountCriteria.getActiveFlag());
			}
		}
		return ResponseIO.success(discountIO);
	}
}
