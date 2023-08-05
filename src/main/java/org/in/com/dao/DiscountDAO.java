package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Cleanup;

import org.apache.commons.lang3.StringUtils;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.DiscountCategoryDTO;
import org.in.com.dto.DiscountCouponDTO;
import org.in.com.dto.DiscountCriteriaDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.UserCustomerDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.SeatGendarEM;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

public class DiscountDAO {

	/**
	 * Here we are getting all the discount details
	 * 
	 * @param namespaceDTO
	 * @return
	 * @throws Exception
	 */
	public List<DiscountCategoryDTO> getAllDiscountCategory(AuthDTO authDTO) {

		List<DiscountCategoryDTO> list = new ArrayList<DiscountCategoryDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code,name,description,active_flag FROM addons_discount_category where namespace_id = ? and active_flag < 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				DiscountCategoryDTO discountdto = new DiscountCategoryDTO();
				discountdto.setCode(selectRS.getString("code"));
				discountdto.setName(selectRS.getString("name"));
				discountdto.setDescription(selectRS.getString("description"));
				discountdto.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(discountdto);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public void getDiscountCategory(AuthDTO authDTO, DiscountCategoryDTO discountCategory) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, name, description, active_flag FROM addons_discount_category WHERE namespace_id = ? AND id = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, discountCategory.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				discountCategory.setCode(selectRS.getString("code"));
				discountCategory.setName(selectRS.getString("name"));
				discountCategory.setDescription(selectRS.getString("description"));
				discountCategory.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	/**
	 * Here we are updating the addons_discount_category based on the code
	 * 
	 * @param namespaceDTO
	 * @param amentiesDTO
	 * @return
	 */
	public DiscountCategoryDTO updateDiscountCategoryCode(AuthDTO authDTO, DiscountCategoryDTO discountDto) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_DISCOUNT_CATEGORY_IUD( ?,?,?,? ,?,?,?,?)}");
			callableStatement.setString(++pindex, discountDto.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setInt(++pindex, discountDto.getActiveFlag());
			callableStatement.setString(++pindex, discountDto.getName());
			callableStatement.setString(++pindex, discountDto.getDescription());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				discountDto.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return discountDto;
	}

	public List<DiscountCouponDTO> getAllDiscountCoupon(AuthDTO authDTO, String discountType) {
		// TODO Auto-generated method stub
		Map<Integer, DiscountCouponDTO> discountMap = new HashMap<Integer, DiscountCouponDTO>();
		List<DiscountCouponDTO> overrideList = new ArrayList<DiscountCouponDTO>();
		try {
			String userCustomer = StringUtil.isNotNull(discountType) ? "AND cup.user_customer_id != 0 " : "AND cup.user_customer_id = 0 ";
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT cup.id,cup.code,cup.coupon,cup.user_customer_id,cup.active_description,cup.error_description,cup.lookup_id,cup.used_count,cup.active_flag,cat.code,cat.name FROM addons_discount_coupon cup, addons_discount_category cat WHERE cup.namespace_id = ? " + userCustomer + " AND cat.namespace_id = ? AND cat.active_flag = 1 AND cat.id = cup.addons_discount_category_id AND cup.active_flag  = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				DiscountCouponDTO discountCoupondto = new DiscountCouponDTO();
				discountCoupondto.setId(selectRS.getInt("cup.id"));
				discountCoupondto.setCode(selectRS.getString("cup.code"));
				discountCoupondto.setCoupon(selectRS.getString("cup.coupon"));
				discountCoupondto.setActiveDesription(selectRS.getString("cup.active_description"));
				discountCoupondto.setErrorDescription(selectRS.getString("cup.error_description"));
				discountCoupondto.setLookupCode(selectRS.getString("cup.lookup_id"));
				discountCoupondto.setUsedCount(selectRS.getInt("cup.used_count"));
				discountCoupondto.setActiveFlag(selectRS.getInt("cup.active_flag"));
				DiscountCategoryDTO discountCategoryDTO = new DiscountCategoryDTO();
				discountCategoryDTO.setCode(selectRS.getString("cat.code"));
				discountCategoryDTO.setName(selectRS.getString("cat.name"));
				discountCoupondto.setDiscountCategory(discountCategoryDTO);

				UserCustomerDTO userCustomerDTO = new UserCustomerDTO();
				userCustomerDTO.setId(selectRS.getInt("cup.user_customer_id"));
				discountCoupondto.setUserCustomer(userCustomerDTO);

				if (discountCoupondto.getLookupCode().equals("0")) {
					discountMap.put(discountCoupondto.getId(), discountCoupondto);
				}
				else {
					overrideList.add(discountCoupondto);
				}
			}
			for (DiscountCouponDTO discountDTO : overrideList) {
				if (discountMap.get(Integer.parseInt(discountDTO.getLookupCode())) != null) {
					DiscountCouponDTO dto = discountMap.get(Integer.parseInt(discountDTO.getLookupCode()));
					dto.getOverrideList().add(discountDTO);
					discountMap.put(dto.getId(), dto);
				}
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return new ArrayList<DiscountCouponDTO>(discountMap.values());
	}

	public DiscountCouponDTO updateDiscountCouponCode(AuthDTO authDTO, DiscountCouponDTO discountCoupondto) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_DISCOUNT_COUPON_IUD(?,?,?,?,? ,?,?,?,?,?, ?,?)}");
			callableStatement.setString(++pindex, discountCoupondto.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, discountCoupondto.getDiscountCategory() != null ? discountCoupondto.getDiscountCategory().getCode() : null);
			callableStatement.setString(++pindex, discountCoupondto.getCoupon());
			callableStatement.setString(++pindex, discountCoupondto.getActiveDesription());
			callableStatement.setString(++pindex, discountCoupondto.getErrorDescription());
			callableStatement.setInt(++pindex, discountCoupondto.getUserCustomer() != null ? discountCoupondto.getUserCustomer().getId() : 0);
			callableStatement.setString(++pindex, discountCoupondto.getLookupCode());
			callableStatement.setInt(++pindex, discountCoupondto.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				discountCoupondto.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return discountCoupondto;
	}

	public void updateDiscountCouponUsage(AuthDTO authDTO, DiscountCouponDTO discountCoupondto) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("UPDATE addons_discount_coupon SET used_count = ? WHERE namespace_id = ? AND code = ? AND active_flag = 1");
			selectPS.setInt(1, discountCoupondto.getUsedCount());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			selectPS.setString(3, discountCoupondto.getCode());
			selectPS.executeUpdate();
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public DiscountCouponDTO getDiscountCoupon(AuthDTO authDTO, DiscountCouponDTO discountCouponDTO) {
		DiscountCouponDTO discountCoupondto = null;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, code, coupon, used_count, active_flag FROM addons_discount_coupon WHERE namespace_id = ? AND coupon = ? AND active_flag  = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, discountCouponDTO.getCoupon());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				discountCoupondto = new DiscountCouponDTO();
				discountCoupondto.setId(selectRS.getInt("id"));
				discountCoupondto.setCode(selectRS.getString("code"));
				discountCoupondto.setCoupon(selectRS.getString("coupon"));
				discountCoupondto.setUsedCount(selectRS.getInt("used_count"));
				discountCoupondto.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return discountCoupondto;
	}

	public UserCustomerDTO checkCustomerCoupon(AuthDTO authDTO, DiscountCouponDTO dto) {
		UserCustomerDTO userCustomerDTO = null;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT user_customer_id FROM addons_discount_coupon WHERE coupon = ? AND namespace_id = ? AND user_customer_id != 0 AND active_flag = 1");
			selectPS.setString(1, dto.getCoupon());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				userCustomerDTO = new UserCustomerDTO();
				userCustomerDTO.setId(selectRS.getInt("user_customer_id"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return userCustomerDTO;
	}

	public List<DiscountCriteriaDTO> getAllByCouponCode(AuthDTO authDTO, DiscountCouponDTO dto) {
		List<DiscountCriteriaDTO> list = new ArrayList<DiscountCriteriaDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT dis.id, dis.code, coupon.code, coupon.coupon, coupon.used_count, coupon.user_customer_id, addons_discount_coupon_id, addons_discount_category_id, user_group_id, active_from, active_to, day_of_week, value, is_percentage_flag, is_travel_date_flag, is_registered_user_flag, is_show_offer_page_flag, max_usage_limit_per_user, max_discount_amount, min_seat_count, before_booking_minitues, after_booking_minitues, min_ticket_fare, max_usage_limit_per_user, device_medium, schedule_code, route_code, is_round_trip_flag, slab_details, seat_gender_id, age, mobile_number, service_timing, dis.active_flag FROM addons_discount_criteria dis,addons_discount_coupon coupon WHERE dis.addons_discount_coupon_id = coupon.id and coupon.coupon = ? and dis.namespace_id = ? and coupon.namespace_id = ? and dis.active_flag = 1 and coupon.active_flag =1");
			selectPS.setString(1, dto.getCoupon());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			selectPS.setInt(3, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				DiscountCriteriaDTO discountdto = new DiscountCriteriaDTO();
				discountdto.setId(selectRS.getInt("dis.id"));
				discountdto.setCode(selectRS.getString("code"));

				DiscountCouponDTO coupon = new DiscountCouponDTO();
				coupon.setCode(selectRS.getString("coupon.code"));
				coupon.setCoupon(selectRS.getString("coupon.coupon"));
				coupon.setId(selectRS.getInt("addons_discount_coupon_id"));
				coupon.setUsedCount(selectRS.getInt("coupon.used_count"));

				DiscountCategoryDTO discountCategory = new DiscountCategoryDTO();
				discountCategory.setId(selectRS.getInt("addons_discount_category_id"));
				coupon.setDiscountCategory(discountCategory);

				UserCustomerDTO userCustomerDTO = new UserCustomerDTO();
				userCustomerDTO.setId(selectRS.getInt("coupon.user_customer_id"));
				dto.setUserCustomer(userCustomerDTO);

				discountdto.setDiscountCoupon(coupon);

				List<GroupDTO> groupList = convertGroupList(selectRS.getString("user_group_id"));
				discountdto.setGroupList(groupList);

				discountdto.setActiveFrom(selectRS.getString("active_from"));
				discountdto.setActiveTo(selectRS.getString("active_to"));
				discountdto.setDayOfWeek(selectRS.getString("day_of_week"));
				discountdto.setValue(selectRS.getFloat("value"));
				discountdto.setPercentageFlag(selectRS.getBoolean("is_percentage_flag"));
				discountdto.setTravelDateFlag(selectRS.getBoolean("is_travel_date_flag"));
				discountdto.setRoundTripFlag(selectRS.getBoolean("is_round_trip_flag"));
				discountdto.setRegisteredUserFlag(selectRS.getBoolean("is_registered_user_flag"));
				discountdto.setShowOfferPageFlag(selectRS.getBoolean("is_show_offer_page_flag"));
				discountdto.setMaxDiscountAmount(selectRS.getInt("max_discount_amount"));
				discountdto.setMinSeatCount(selectRS.getInt("min_seat_count"));
				discountdto.setBeforeBookingMinitues(selectRS.getInt("before_booking_minitues"));
				discountdto.setAfterBookingMinitues(selectRS.getInt("after_booking_minitues"));
				discountdto.setMinTicketFare(selectRS.getInt("min_ticket_fare"));

				List<DeviceMediumEM> deviceMediumList = convertDeviceMediumList(selectRS.getString("device_medium"));
				discountdto.setDeviceMedium(deviceMediumList);

				discountdto.setMaxUsageLimitPerUser(selectRS.getInt("max_usage_limit_per_user"));
				String scheduleCodes = selectRS.getString("schedule_code");
				String routeCodes = selectRS.getString("route_code");
				if (StringUtil.isNotNull(scheduleCodes)) {
					discountdto.setScheduleCode(Arrays.asList(scheduleCodes.split(Text.COMMA)));
				}
				if (StringUtil.isNotNull(routeCodes)) {
					discountdto.setRouteCode(Arrays.asList(routeCodes.split(Text.COMMA)));
				}
				String slabDetails = selectRS.getString("slab_details");
				discountdto.setSlabList(discountdto.getSlabDetails(slabDetails));

				List<SeatGendarEM> genderList = convertGenderList(selectRS.getString("seat_gender_id"));
				discountdto.setSeatGender(genderList);

				discountdto.setAge(selectRS.getString("age"));
				discountdto.setMobileNumber(selectRS.getString("mobile_number"));
				discountdto.setServiceTiming(selectRS.getString("service_timing"));
				discountdto.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(discountdto);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public List<DiscountCriteriaDTO> getAllByCoupon(AuthDTO authDTO, DiscountCouponDTO dto) {
		List<DiscountCriteriaDTO> list = new ArrayList<DiscountCriteriaDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT dis.code, cate.code, cate.name, coupon.code, coupon.coupon, addons_discount_coupon_id, addons_discount_category_id, user_group_id, active_from, active_to, day_of_week, value, is_percentage_flag, is_travel_date_flag, is_registered_user_flag, is_show_offer_page_flag, max_usage_limit_per_user, max_discount_amount, min_seat_count, before_booking_minitues, after_booking_minitues, min_ticket_fare, max_usage_limit_per_user, device_medium, schedule_code, route_code, is_round_trip_flag, slab_details, seat_gender_id, age, mobile_number, service_timing, dis.active_flag from addons_discount_criteria dis,addons_discount_coupon coupon,addons_discount_category cate where dis.addons_discount_coupon_id = coupon.id and coupon.addons_discount_category_id = cate.id and coupon.code = ? and dis.namespace_id = ? and coupon.namespace_id = ? and cate.namespace_id = ? and dis.active_flag = 1 and coupon.active_flag =1 and cate.active_flag = 1");
			selectPS.setString(1, dto.getCode());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			selectPS.setInt(3, authDTO.getNamespace().getId());
			selectPS.setInt(4, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				DiscountCriteriaDTO discountdto = new DiscountCriteriaDTO();
				discountdto.setCode(selectRS.getString("code"));
				DiscountCategoryDTO cate = new DiscountCategoryDTO();
				cate.setCode(selectRS.getString("cate.code"));
				cate.setName(selectRS.getString("cate.name"));
				DiscountCouponDTO coupon = new DiscountCouponDTO();
				coupon.setDiscountCategory(cate);
				coupon.setCode(selectRS.getString("coupon.code"));
				coupon.setCoupon(selectRS.getString("coupon.coupon"));
				discountdto.setDiscountCoupon(coupon);

				List<GroupDTO> groupList = convertGroupList(selectRS.getString("user_group_id"));
				discountdto.setGroupList(groupList);

				discountdto.setActiveFrom(selectRS.getString("active_from"));
				discountdto.setActiveTo(selectRS.getString("active_to"));
				discountdto.setDayOfWeek(selectRS.getString("day_of_week"));
				discountdto.setValue(selectRS.getFloat("value"));
				discountdto.setPercentageFlag(selectRS.getBoolean("is_percentage_flag"));
				discountdto.setTravelDateFlag(selectRS.getBoolean("is_travel_date_flag"));
				discountdto.setRoundTripFlag(selectRS.getBoolean("is_round_trip_flag"));
				discountdto.setRegisteredUserFlag(selectRS.getBoolean("is_registered_user_flag"));
				discountdto.setShowOfferPageFlag(selectRS.getBoolean("is_show_offer_page_flag"));
				discountdto.setMaxDiscountAmount(selectRS.getInt("max_discount_amount"));
				discountdto.setMinSeatCount(selectRS.getInt("min_seat_count"));
				discountdto.setBeforeBookingMinitues(selectRS.getInt("before_booking_minitues"));
				discountdto.setAfterBookingMinitues(selectRS.getInt("after_booking_minitues"));
				discountdto.setMinTicketFare(selectRS.getInt("min_ticket_fare"));

				List<DeviceMediumEM> deviceMediumList = convertDeviceMediumList(selectRS.getString("device_medium"));
				discountdto.setDeviceMedium(deviceMediumList);

				discountdto.setMaxUsageLimitPerUser(selectRS.getInt("max_usage_limit_per_user"));
				String scheduleCodes = selectRS.getString("schedule_code");
				String routeCodes = selectRS.getString("route_code");
				if (StringUtil.isNotNull(scheduleCodes)) {
					discountdto.setScheduleCode(Arrays.asList(scheduleCodes.split(Text.COMMA)));
				}
				if (StringUtil.isNotNull(routeCodes)) {
					discountdto.setRouteCode(Arrays.asList(routeCodes.split(Text.COMMA)));
				}
				String slabDetails = selectRS.getString("slab_details");
				discountdto.setSlabList(discountdto.getSlabDetails(slabDetails));

				List<SeatGendarEM> genderList = convertGenderList(selectRS.getString("seat_gender_id"));
				discountdto.setSeatGender(genderList);

				discountdto.setAge(selectRS.getString("age"));
				discountdto.setMobileNumber(selectRS.getString("mobile_number"));
				discountdto.setServiceTiming(selectRS.getString("service_timing"));
				discountdto.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(discountdto);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public List<DiscountCriteriaDTO> getAllByCategory(AuthDTO authDTO, DiscountCategoryDTO dto) {
		List<DiscountCriteriaDTO> list = new ArrayList<DiscountCriteriaDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT dis.id, dis.code, cate.code, cate.name, coupon.code, coupon.coupon, addons_discount_coupon_id, addons_discount_category_id, user_group_id, active_from, active_to, day_of_week, value, is_percentage_flag, is_travel_date_flag, is_show_offer_page_flag, max_usage_limit_per_user, max_discount_amount, min_seat_count, before_booking_minitues, after_booking_minitues, min_ticket_fare, max_usage_limit_per_user, device_medium, schedule_code, route_code, slab_details, seat_gender_id, age, mobile_number, service_timing, dis.active_flag FROM addons_discount_criteria dis,addons_discount_coupon coupon,addons_discount_category cate where dis.addons_discount_coupon_id = coupon.id and coupon.addons_discount_category_id = cate.id and cate.code = ? and dis.namespace_id = ? and coupon.namespace_id = ? and cate.namespace_id = ? and dis.active_flag = 1 and coupon.active_flag =1 and cate.active_flag = 1");
			selectPS.setString(1, dto.getCode());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			selectPS.setInt(3, authDTO.getNamespace().getId());
			selectPS.setInt(4, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				DiscountCriteriaDTO discountdto = new DiscountCriteriaDTO();
				discountdto.setId(selectRS.getInt("dis.id"));
				discountdto.setCode(selectRS.getString("code"));
				DiscountCategoryDTO cate = new DiscountCategoryDTO();
				cate.setCode(selectRS.getString("cate.code"));
				cate.setName(selectRS.getString("cate.name"));
				DiscountCouponDTO coupon = new DiscountCouponDTO();
				coupon.setDiscountCategory(cate);
				coupon.setCode(selectRS.getString("coupon.code"));
				coupon.setCoupon(selectRS.getString("coupon.coupon"));
				discountdto.setDiscountCoupon(coupon);

				List<GroupDTO> groupList = convertGroupList(selectRS.getString("user_group_id"));
				discountdto.setGroupList(groupList);

				discountdto.setActiveFrom(selectRS.getString("active_from"));
				discountdto.setActiveTo(selectRS.getString("active_to"));
				discountdto.setDayOfWeek(selectRS.getString("day_of_week"));
				discountdto.setValue(selectRS.getFloat("value"));
				discountdto.setPercentageFlag(selectRS.getBoolean("is_percentage_flag"));
				discountdto.setTravelDateFlag(selectRS.getBoolean("is_travel_date_flag"));
				discountdto.setRegisteredUserFlag(selectRS.getBoolean("is_registered_user_flag"));
				discountdto.setShowOfferPageFlag(selectRS.getBoolean("is_show_offer_page_flag"));
				discountdto.setMaxDiscountAmount(selectRS.getInt("max_discount_amount"));
				discountdto.setMinSeatCount(selectRS.getInt("min_seat_count"));
				discountdto.setBeforeBookingMinitues(selectRS.getInt("before_booking_minitues"));
				discountdto.setAfterBookingMinitues(selectRS.getInt("after_booking_minitues"));
				discountdto.setMinTicketFare(selectRS.getInt("min_ticket_fare"));

				List<DeviceMediumEM> deviceMediumList = convertDeviceMediumList(selectRS.getString("device_medium"));
				discountdto.setDeviceMedium(deviceMediumList);

				discountdto.setMaxUsageLimitPerUser(selectRS.getInt("max_usage_limit_per_user"));
				String scheduleCodes = selectRS.getString("schedule_code");
				String routeCodes = selectRS.getString("route_code");
				if (StringUtil.isNotNull(scheduleCodes)) {
					discountdto.setScheduleCode(Arrays.asList(scheduleCodes.split(Text.COMMA)));
				}
				if (StringUtil.isNotNull(routeCodes)) {
					discountdto.setRouteCode(Arrays.asList(routeCodes.split(Text.COMMA)));
				}
				String slabDetails = selectRS.getString("slab_details");
				discountdto.setSlabList(discountdto.getSlabDetails(slabDetails));

				List<SeatGendarEM> genderList = convertGenderList(selectRS.getString("seat_gender_id"));
				discountdto.setSeatGender(genderList);

				discountdto.setAge(selectRS.getString("age"));
				discountdto.setMobileNumber(selectRS.getString("mobile_number"));
				discountdto.setServiceTiming(selectRS.getString("service_timing"));
				discountdto.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(discountdto);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public DiscountCriteriaDTO updateDiscount(AuthDTO authDTO, DiscountCriteriaDTO dto) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_DISCOUNT_CRITERIA_IUD(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,? ,?,?,?,?,?, ?)}");
			callableStatement.setString(++pindex, dto.getCode());
			callableStatement.setString(++pindex, dto.getDiscountCoupon().getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, getGroups(dto.getGroupList()));
			callableStatement.setString(++pindex, dto.getActiveFrom());
			callableStatement.setString(++pindex, dto.getActiveTo());
			callableStatement.setString(++pindex, dto.getDayOfWeek());
			callableStatement.setFloat(++pindex, dto.getValue());
			callableStatement.setInt(++pindex, dto.isPercentageFlag() ? 1 : 0);
			callableStatement.setInt(++pindex, dto.isTravelDateFlag() ? 1 : 0);
			callableStatement.setInt(++pindex, dto.isRoundTripFlag() ? 1 : 0);
			callableStatement.setInt(++pindex, dto.isRegisteredUserFlag() ? 1 : 0);
			callableStatement.setInt(++pindex, dto.isShowOfferPageFlag() ? 1 : 0);

			callableStatement.setInt(++pindex, dto.getMaxDiscountAmount());
			callableStatement.setInt(++pindex, dto.getMinSeatCount());
			callableStatement.setInt(++pindex, dto.getBeforeBookingMinitues());
			callableStatement.setInt(++pindex, dto.getAfterBookingMinitues());
			callableStatement.setInt(++pindex, dto.getMinTicketFare());

			callableStatement.setInt(++pindex, dto.getMaxUsageLimitPerUser());
			callableStatement.setString(++pindex, dto.getDeviceMediums());
			callableStatement.setString(++pindex, StringUtils.join(dto.getScheduleCode(), ','));
			callableStatement.setString(++pindex, StringUtils.join(dto.getRouteCode(), ','));
			callableStatement.setString(++pindex, dto.getSlabDetails());
			callableStatement.setString(++pindex, getGenders(dto.getSeatGender()));
			callableStatement.setString(++pindex, dto.getAge());
			callableStatement.setString(++pindex, dto.getMobileNumber());
			callableStatement.setString(++pindex, dto.getServiceTiming());
			callableStatement.setInt(++pindex, dto.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				dto.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return dto;
	}

	public List<DiscountCriteriaDTO> getAll(AuthDTO authDTO) {
		List<DiscountCriteriaDTO> list = new ArrayList<DiscountCriteriaDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT dis.code, cate.code, cate.name, coupon.code, coupon.coupon, coupon.user_customer_id, addons_discount_coupon_id, addons_discount_category_id, user_group_id, active_from, active_to, day_of_week, value, is_percentage_flag, is_travel_date_flag, is_show_offer_page_flag, is_registered_user_flag, max_usage_limit_per_user, max_discount_amount, min_seat_count, before_booking_minitues, min_ticket_fare, device_medium, after_booking_minitues, schedule_code, route_code, slab_details, seat_gender_id, age, mobile_number, service_timing, dis.active_flag FROM addons_discount_criteria dis,addons_discount_coupon coupon,addons_discount_category cate where dis.addons_discount_coupon_id = coupon.id and coupon.addons_discount_category_id = cate.id and dis.namespace_id = ? and coupon.namespace_id = ? and cate.namespace_id = ? and dis.active_flag = 1 and coupon.active_flag =1 and cate.active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			selectPS.setInt(3, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				DiscountCriteriaDTO discountdto = new DiscountCriteriaDTO();
				discountdto.setCode(selectRS.getString("code"));
				DiscountCategoryDTO cate = new DiscountCategoryDTO();
				cate.setCode(selectRS.getString("cate.code"));
				cate.setName(selectRS.getString("cate.name"));
				DiscountCouponDTO coupon = new DiscountCouponDTO();
				coupon.setDiscountCategory(cate);
				coupon.setCode(selectRS.getString("coupon.code"));
				coupon.setCoupon(selectRS.getString("coupon.coupon"));

				UserCustomerDTO userCustomerDTO = new UserCustomerDTO();
				userCustomerDTO.setId(selectRS.getInt("coupon.user_customer_id"));
				coupon.setUserCustomer(userCustomerDTO);

				discountdto.setDiscountCoupon(coupon);

				List<GroupDTO> groupList = convertGroupList(selectRS.getString("user_group_id"));
				discountdto.setGroupList(groupList);

				discountdto.setActiveFrom(selectRS.getString("active_from"));
				discountdto.setActiveTo(selectRS.getString("active_to"));
				discountdto.setDayOfWeek(selectRS.getString("day_of_week"));
				discountdto.setValue(selectRS.getFloat("value"));
				discountdto.setPercentageFlag(selectRS.getBoolean("is_percentage_flag"));
				discountdto.setTravelDateFlag(selectRS.getBoolean("is_travel_date_flag"));
				discountdto.setRegisteredUserFlag(selectRS.getBoolean("is_registered_user_flag"));
				discountdto.setShowOfferPageFlag(selectRS.getBoolean("is_show_offer_page_flag"));

				discountdto.setMaxDiscountAmount(selectRS.getInt("max_discount_amount"));
				discountdto.setMinSeatCount(selectRS.getInt("min_seat_count"));
				discountdto.setBeforeBookingMinitues(selectRS.getInt("before_booking_minitues"));
				discountdto.setAfterBookingMinitues(selectRS.getInt("after_booking_minitues"));
				discountdto.setMinTicketFare(selectRS.getInt("min_ticket_fare"));

				List<DeviceMediumEM> deviceMediumList = convertDeviceMediumList(selectRS.getString("device_medium"));
				discountdto.setDeviceMedium(deviceMediumList);

				discountdto.setMaxUsageLimitPerUser(selectRS.getInt("max_usage_limit_per_user"));
				String scheduleCodes = selectRS.getString("schedule_code");
				String routeCodes = selectRS.getString("route_code");
				if (StringUtil.isNotNull(scheduleCodes)) {
					discountdto.setScheduleCode(Arrays.asList(scheduleCodes.split(Text.COMMA)));
				}
				if (StringUtil.isNotNull(routeCodes)) {
					discountdto.setRouteCode(Arrays.asList(routeCodes.split(Text.COMMA)));
				}
				String slabDetails = selectRS.getString("slab_details");
				discountdto.setSlabList(discountdto.getSlabDetails(slabDetails));

				List<SeatGendarEM> genderList = convertGenderList(selectRS.getString("seat_gender_id"));
				discountdto.setSeatGender(genderList);

				discountdto.setAge(selectRS.getString("age"));
				discountdto.setMobileNumber(selectRS.getString("mobile_number"));
				discountdto.setServiceTiming(selectRS.getString("service_timing"));
				discountdto.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(discountdto);
			}
		}
		catch (Exception e) {
			System.out.println("Error: ");
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public List<DiscountCriteriaDTO> getAllAvailableDiscountOfferPage(AuthDTO authDTO) {
		List<DiscountCriteriaDTO> list = new ArrayList<DiscountCriteriaDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT dis.code, coupon.code, coupon.coupon, addons_discount_coupon_id, addons_discount_category_id, user_group_id, active_from, active_to, day_of_week, value, is_percentage_flag, is_travel_date_flag, is_registered_user_flag, is_show_offer_page_flag, max_discount_amount, min_seat_count, before_booking_minitues, after_booking_minitues, min_ticket_fare, device_medium, max_usage_limit_per_user, schedule_code, route_code, slab_details, seat_gender_id, age, mobile_number, service_timing, dis.active_flag FROM addons_discount_criteria dis,addons_discount_coupon coupon WHERE dis.addons_discount_coupon_id = coupon.id  AND dis.namespace_id = ? AND coupon.namespace_id = ? AND dis.active_flag = 1 AND coupon.active_flag = 1 AND is_show_offer_page_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				DiscountCriteriaDTO discountdto = new DiscountCriteriaDTO();
				discountdto.setCode(selectRS.getString("code"));
				DiscountCouponDTO coupon = new DiscountCouponDTO();
				coupon.setCode(selectRS.getString("coupon.code"));
				coupon.setCoupon(selectRS.getString("coupon.coupon"));
				coupon.setId(selectRS.getInt("addons_discount_coupon_id"));
				discountdto.setDiscountCoupon(coupon);

				List<GroupDTO> groupList = convertGroupList(selectRS.getString("user_group_id"));
				discountdto.setGroupList(groupList);

				discountdto.setActiveFrom(selectRS.getString("active_from"));
				discountdto.setActiveTo(selectRS.getString("active_to"));
				discountdto.setDayOfWeek(selectRS.getString("day_of_week"));
				discountdto.setValue(selectRS.getFloat("value"));
				discountdto.setPercentageFlag(selectRS.getBoolean("is_percentage_flag"));
				discountdto.setTravelDateFlag(selectRS.getBoolean("is_travel_date_flag"));
				discountdto.setRegisteredUserFlag(selectRS.getBoolean("is_registered_user_flag"));
				discountdto.setShowOfferPageFlag(selectRS.getBoolean("is_show_offer_page_flag"));

				discountdto.setMaxDiscountAmount(selectRS.getInt("max_discount_amount"));
				discountdto.setMinSeatCount(selectRS.getInt("min_seat_count"));
				discountdto.setBeforeBookingMinitues(selectRS.getInt("before_booking_minitues"));
				discountdto.setAfterBookingMinitues(selectRS.getInt("after_booking_minitues"));
				discountdto.setMinTicketFare(selectRS.getInt("min_ticket_fare"));

				List<DeviceMediumEM> deviceMediumList = convertDeviceMediumList(selectRS.getString("device_medium"));
				discountdto.setDeviceMedium(deviceMediumList);

				discountdto.setMaxUsageLimitPerUser(selectRS.getInt("max_usage_limit_per_user"));
				String scheduleCodes = selectRS.getString("schedule_code");
				String routeCodes = selectRS.getString("route_code");
				if (StringUtil.isNotNull(scheduleCodes)) {
					discountdto.setScheduleCode(Arrays.asList(scheduleCodes.split(Text.COMMA)));
				}
				if (StringUtil.isNotNull(routeCodes)) {
					discountdto.setRouteCode(Arrays.asList(routeCodes.split(Text.COMMA)));
				}
				String slabDetails = selectRS.getString("slab_details");
				discountdto.setSlabList(discountdto.getSlabDetails(slabDetails));

				List<SeatGendarEM> genderList = convertGenderList(selectRS.getString("seat_gender_id"));
				discountdto.setSeatGender(genderList);

				discountdto.setAge(selectRS.getString("age"));
				discountdto.setMobileNumber(selectRS.getString("mobile_number"));
				discountdto.setServiceTiming(selectRS.getString("service_timing"));
				discountdto.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(discountdto);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public DiscountCriteriaDTO getCustomerDiscountCoupon(AuthDTO authDTO, UserCustomerDTO userCustomer) {
		DiscountCriteriaDTO discountdto = new DiscountCriteriaDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT dis.code, cate.code, cate.name, coupon.code, coupon.coupon, addons_discount_coupon_id, addons_discount_category_id, user_group_id, active_from, active_to, day_of_week, value, is_percentage_flag, is_travel_date_flag, is_registered_user_flag, is_show_offer_page_flag, max_usage_limit_per_user, max_discount_amount, min_seat_count, before_booking_minitues, after_booking_minitues, min_ticket_fare, device_medium, max_usage_limit_per_user, schedule_code, route_code, slab_details, seat_gender_id, age, mobile_number, service_timing, dis.active_flag FROM addons_discount_criteria dis,addons_discount_coupon coupon,addons_discount_category cate, user_customer cust WHERE dis.addons_discount_coupon_id = coupon.id AND coupon.addons_discount_category_id = cate.id AND dis.namespace_id = ? AND coupon.namespace_id = ? AND cate.namespace_id = ? AND coupon.namespace_id = cust.namespace_id AND coupon.user_customer_id = cust.id AND cust.mobile = ? AND dis.active_flag = 1 AND coupon.active_flag = 1 AND cate.active_flag = 1 AND cust.active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			selectPS.setInt(3, authDTO.getNamespace().getId());
			selectPS.setString(4, userCustomer.getMobile());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				discountdto.setCode(selectRS.getString("code"));
				DiscountCategoryDTO cate = new DiscountCategoryDTO();
				cate.setCode(selectRS.getString("cate.code"));
				cate.setName(selectRS.getString("cate.name"));

				DiscountCouponDTO coupon = new DiscountCouponDTO();
				coupon.setDiscountCategory(cate);
				coupon.setCode(selectRS.getString("coupon.code"));
				coupon.setCoupon(selectRS.getString("coupon.coupon"));
				discountdto.setDiscountCoupon(coupon);

				List<GroupDTO> groupList = convertGroupList(selectRS.getString("user_group_id"));
				discountdto.setGroupList(groupList);

				discountdto.setActiveFrom(selectRS.getString("active_from"));
				discountdto.setActiveTo(selectRS.getString("active_to"));
				discountdto.setDayOfWeek(selectRS.getString("day_of_week"));
				discountdto.setValue(selectRS.getFloat("value"));
				discountdto.setPercentageFlag(selectRS.getBoolean("is_percentage_flag"));
				discountdto.setTravelDateFlag(selectRS.getBoolean("is_travel_date_flag"));
				discountdto.setRegisteredUserFlag(selectRS.getBoolean("is_registered_user_flag"));
				discountdto.setShowOfferPageFlag(selectRS.getBoolean("is_show_offer_page_flag"));

				discountdto.setMaxDiscountAmount(selectRS.getInt("max_discount_amount"));
				discountdto.setMinSeatCount(selectRS.getInt("min_seat_count"));
				discountdto.setBeforeBookingMinitues(selectRS.getInt("before_booking_minitues"));
				discountdto.setAfterBookingMinitues(selectRS.getInt("after_booking_minitues"));
				discountdto.setMinTicketFare(selectRS.getInt("min_ticket_fare"));

				List<DeviceMediumEM> deviceMediumList = convertDeviceMediumList(selectRS.getString("device_medium"));
				discountdto.setDeviceMedium(deviceMediumList);

				discountdto.setMaxUsageLimitPerUser(selectRS.getInt("max_usage_limit_per_user"));
				String scheduleCodes = selectRS.getString("schedule_code");
				String routeCodes = selectRS.getString("route_code");
				if (StringUtil.isNotNull(scheduleCodes)) {
					discountdto.setScheduleCode(Arrays.asList(scheduleCodes.split(Text.COMMA)));
				}
				if (StringUtil.isNotNull(routeCodes)) {
					discountdto.setRouteCode(Arrays.asList(routeCodes.split(Text.COMMA)));
				}
				String slabDetails = selectRS.getString("slab_details");
				discountdto.setSlabList(discountdto.getSlabDetails(slabDetails));

				List<SeatGendarEM> genderList = convertGenderList(selectRS.getString("seat_gender_id"));
				discountdto.setSeatGender(genderList);

				discountdto.setAge(selectRS.getString("age"));
				discountdto.setMobileNumber(selectRS.getString("mobile_number"));
				discountdto.setServiceTiming(selectRS.getString("service_timing"));
				discountdto.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return discountdto;
	}

	private List<DeviceMediumEM> convertDeviceMediumList(String deviceMediumCodes) {
		List<DeviceMediumEM> deviceMediumList = new ArrayList<>();
		if (StringUtil.isNotNull(deviceMediumCodes)) {
			List<String> deviceMediums = Arrays.asList(deviceMediumCodes.split(Text.COMMA));
			if (deviceMediums != null) {
				for (String deviceMedium : deviceMediums) {
					if (StringUtil.isNull(deviceMedium)) {
						continue;
					}
					deviceMediumList.add(DeviceMediumEM.getDeviceMediumEM(deviceMedium));
				}
			}
		}
		return deviceMediumList;
	}

	private List<SeatGendarEM> convertGenderList(String genderIds) {
		List<SeatGendarEM> genderList = new ArrayList<>();
		if (StringUtil.isNotNull(genderIds)) {
			List<String> genders = Arrays.asList(genderIds.split(Text.COMMA));
			if (genders != null) {
				for (String genderId : genders) {
					if (StringUtil.isNull(genderId) || genderId.equals(Numeric.ZERO)) {
						continue;
					}
					genderList.add(SeatGendarEM.getSeatGendarEM(StringUtil.getIntegerValue(genderId)));
				}
			}
		}
		return genderList;
	}

	private String getGenders(List<SeatGendarEM> genderList) {
		StringBuilder gender = new StringBuilder();
		if (genderList != null) {
			for (SeatGendarEM seatGendarEM : genderList) {
				gender.append(seatGendarEM.getId());
				gender.append(Text.COMMA);
			}
		}
		return StringUtil.isNotNull(gender.toString()) ? gender.toString() : Text.NA;
	}

	private List<GroupDTO> convertGroupList(String groups) {
		List<GroupDTO> groupList = new ArrayList<>();
		if (StringUtil.isNotNull(groups)) {
			List<String> groupIds = Arrays.asList(groups.split(Text.COMMA));
			if (groupIds != null) {
				for (String groupId : groupIds) {
					if (StringUtil.isNull(groupId) || groupId.equals(Numeric.ZERO)) {
						continue;
					}
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setId(StringUtil.getIntegerValue(groupId));
					groupList.add(groupDTO);
				}
			}
		}
		return groupList;
	}

	private String getGroups(List<GroupDTO> groupList) {
		StringBuilder group = new StringBuilder();
		if (groupList != null) {
			for (GroupDTO groupDTO : groupList) {
				if (groupDTO.getId() == 0) {
					continue;
				}
				group.append(groupDTO.getId());
				group.append(Text.COMMA);
			}
		}
		return StringUtil.isNotNull(group.toString()) ? group.toString() : Text.NA;
	}
}
