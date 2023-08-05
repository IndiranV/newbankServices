package org.in.com.service.impl;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.in.com.aggregator.fcm.FCMService;
import org.in.com.aggregator.wallet.WalletService;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AppStoreDetailsDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.StageFareDTO;
import org.in.com.dto.TicketAddonsDetailsDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.UserCustomerDTO;
import org.in.com.dto.UserTransactionDTO;
import org.in.com.dto.enumeration.AddonsTypeEM;
import org.in.com.dto.enumeration.SeatStatusEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.BusmapService;
import org.in.com.service.UserCustomerService;
import org.in.com.service.UserWalletService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserWalletServiceImpl implements UserWalletService {
	@Autowired
	WalletService walletService;
	@Autowired
	BusmapService busmapService;
	@Autowired
	UserCustomerService userCustomerService;
	@Autowired
	FCMService fcmService;

	@Override
	public Map<String, Object> getCurrentCreditBalace(AuthDTO authDTO, UserCustomerDTO userCustomerDTO) {
		userCustomerService.getUserCustomer(authDTO, userCustomerDTO);
		if (StringUtil.isNull(userCustomerDTO.getWalletCode())) {
			walletService.updateWalletUser(authDTO, userCustomerDTO);
			if (StringUtil.isNotNull(userCustomerDTO.getWalletCode())) {
				userCustomerService.updateUserCustomer(authDTO, userCustomerDTO);
			}
		}
		if (StringUtil.isNull(userCustomerDTO.getWalletCode())) {
			throw new ServiceException(ErrorCode.INVALID_USER_CODE);
		}
		Map<String, Object> currentBalanceDetails = walletService.getCurrentCreditBalace(authDTO, userCustomerDTO);
		return currentBalanceDetails;
	}

	@Override
	public Map<String, Object> validateWalletCoupon(AuthDTO authDTO, BookingDTO bookingDTO) {
		getSeatFare(authDTO, bookingDTO);
		userCustomerService.getUserCustomer(authDTO, authDTO.getUserCustomer());
		Map<String, Object> addonDetailMap = walletService.validateWalletCoupon(authDTO, bookingDTO, authDTO.getUserCustomer());
		return addonDetailMap;
	}

	private void getSeatFare(AuthDTO authDTO, BookingDTO bookingDTO) {
		for (TicketDTO ticketDTO : bookingDTO.getTicketList()) {
			TripDTO returnTripDTO = busmapService.getSearchBusmapV3(authDTO, ticketDTO.getTripDTO());
			ticketDTO.setTripDTO(returnTripDTO);
			// Get seatFare
			Map<String, BusSeatLayoutDTO> fareMap = new HashMap<String, BusSeatLayoutDTO>();
			List<BusSeatLayoutDTO> seatLayoutDTOList = returnTripDTO.getBus().getBusSeatLayoutDTO().getList();

			Map<String, StageFareDTO> getFareMap = new HashMap<String, StageFareDTO>();
			// Group Wise Fare and Default Fare
			for (StageFareDTO fareDTO : returnTripDTO.getStage().getStageFare()) {
				if (fareDTO.getGroup().getId() != 0) {
					getFareMap.put(fareDTO.getGroup().getId() + fareDTO.getBusSeatType().getCode(), fareDTO);
				}
				else {
					getFareMap.put(fareDTO.getBusSeatType().getCode(), fareDTO);
				}
			}
			// Get Group Wise Fare and Default Fare
			for (BusSeatLayoutDTO seatLayoutDTO : seatLayoutDTOList) {
				if (seatLayoutDTO.getSeatStatus() == SeatStatusEM.ALLOCATED_YOU || seatLayoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_ALL || seatLayoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_MALE || seatLayoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_FEMALE) {
					if (getFareMap.get(authDTO.getGroup().getId() + seatLayoutDTO.getBusSeatType().getCode()) != null) {

						// Seat Fare
						if (seatLayoutDTO.getFare() == null) {
							seatLayoutDTO.setFare(getFareMap.get(authDTO.getGroup().getId() + seatLayoutDTO.getBusSeatType().getCode()).getFare());
						}
						fareMap.put(seatLayoutDTO.getCode(), seatLayoutDTO);
					}
					else if (getFareMap.get(seatLayoutDTO.getBusSeatType().getCode()) != null) {
						if (seatLayoutDTO.getFare() == null) {
							seatLayoutDTO.setFare(getFareMap.get(seatLayoutDTO.getBusSeatType().getCode()).getFare());
						}
						fareMap.put(seatLayoutDTO.getCode(), seatLayoutDTO);
					}
				}
			}

			// Get seatFare
			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				BusSeatLayoutDTO seatLayoutDTO = fareMap.get(ticketDetailsDTO.getSeatCode());
				if (seatLayoutDTO == null) {
					throw new ServiceException(ErrorCode.SEAT_ALREADY_BLOCKED);
				}
				ticketDetailsDTO.setSeatFare(seatLayoutDTO.getFare());
				ticketDetailsDTO.setSeatCode(seatLayoutDTO.getCode());
				ticketDetailsDTO.setSeatName(seatLayoutDTO.getName());
				ticketDetailsDTO.setAcBusTax(seatLayoutDTO.getFare().divide(Numeric.ONE_HUNDRED, 2, RoundingMode.CEILING).multiply(returnTripDTO.getSchedule().getTax().getServiceTax()));
				ticketDetailsDTO.setSeatType(seatLayoutDTO.getBusSeatType().getCode());
				ticketDetailsDTO.setActiveFlag(1);
			}
		}
	}

	@Override
	public Map<String, String> getWalletRedeemDetails(AuthDTO authDTO, String cashCouponCode) {
		Map<String, String> redeemDetails = walletService.getWalletRedeemDetails(authDTO, cashCouponCode);
		return redeemDetails;
	}

	@Override
	public void processWalletTransaction(AuthDTO authDTO, TicketDTO ticketDTO, UserCustomerDTO userCustomerDTO) {
		try {
			BookingDTO bookingDTO = new BookingDTO();
			bookingDTO.setCode(ticketDTO.getCode());

			List<TicketDTO> ticketList = new ArrayList<TicketDTO>();
			ticketList.add(ticketDTO);
			bookingDTO.setTicketList(ticketList);

			String walletRedeemCode = Text.EMPTY;
			String cashCouponCode = Text.EMPTY;
			for (TicketAddonsDetailsDTO ticketAddonsDetailsDTO : ticketDTO.getTicketAddonsDetails()) {
				if (AddonsTypeEM.WALLET_REDEEM.getId() == ticketAddonsDetailsDTO.getAddonsType().getId()) {
					walletRedeemCode = ticketAddonsDetailsDTO.getRefferenceCode();
				}
				else if (AddonsTypeEM.WALLET_COUPON.getId() == ticketAddonsDetailsDTO.getAddonsType().getId()) {
					cashCouponCode = ticketAddonsDetailsDTO.getRefferenceCode();
				}
			}
			if (StringUtil.isNull(walletRedeemCode) && StringUtil.isNull(cashCouponCode)) {
				throw new ServiceException(ErrorCode.UNABLE_PROCESS, "Wallet required field is null");
			}
			Map<String, String> additionalAttributes = new HashMap<String, String>();
			additionalAttributes.put(Text.WALLET_REDREEM, walletRedeemCode);
			additionalAttributes.put(Text.WALLET_COUPON_CODE, cashCouponCode);
			bookingDTO.setAdditionalAttributes(additionalAttributes);

			// User Customer
			userCustomerService.getUserCustomer(authDTO, userCustomerDTO);

			walletService.processWalletTransaction(authDTO, bookingDTO, userCustomerDTO);
		}
		catch (ServiceException e) {
			e.printStackTrace();
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<Map<String, Object>> getUserCachCoupons(AuthDTO authDTO, String mobileNumber) {
		List<Map<String, Object>> couponList = walletService.getUserCachCoupons(authDTO, mobileNumber);
		return couponList;
	}

	@Override
	public JSONArray userTransactionHistory(AuthDTO authDTO, String fromDate, String toDate, String mobileNumber) {
		if (StringUtil.isNull(mobileNumber) && authDTO.getUserCustomer() != null) {
			userCustomerService.getUserCustomer(authDTO, authDTO.getUserCustomer());
			mobileNumber = authDTO.getUserCustomer().getMobile();
		}
		if (StringUtil.isNull(mobileNumber)) {
			throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
		}
		JSONArray transactionList = walletService.userTransactionHistory(authDTO, fromDate, toDate, mobileNumber);
		return transactionList;
	}

	@Override
	public List<Map<String, String>> getUserBalanceDetails(AuthDTO authDTO) {
		List<Map<String, String>> userBalanceList = walletService.getUserBalanceDetails(authDTO);
		return userBalanceList;
	}

	@Override
	public void addAfterTravelTransaction(AuthDTO authDTO, TicketDTO ticketDTO, String eventType) {
		UserTransactionDTO userTransactionDTO = new UserTransactionDTO();
		userTransactionDTO.setRefferenceCode(ticketDTO.getCode());
		userTransactionDTO.setTransactionDate(DateUtil.NOW().format(Text.DATE_DATE4J));
		userTransactionDTO.setTransactionAmount(ticketDTO.getTicketFareWithAddons().subtract(ticketDTO.getAcBusTax()));
		userTransactionDTO.setActiveFlag(Numeric.ONE_INT);

		if (ticketDTO.getTicketForUser() != null && ticketDTO.getTicketForUser().getId() != 0) {
			UserCustomerDTO userCustomerDTO = new UserCustomerDTO();
			userCustomerDTO.setId(ticketDTO.getTicketForUser().getId());
			userCustomerService.getUserCustomer(authDTO, userCustomerDTO);
			walletService.addAfterTravelTransaction(authDTO, userTransactionDTO, userCustomerDTO, eventType);
		}
	}

	@Override
	public Map<String, String> getUserReferral(AuthDTO authDTO, UserCustomerDTO userCustomerDTO) {
		userCustomerService.getUserCustomer(authDTO, userCustomerDTO);
		return walletService.getUserReferral(authDTO, userCustomerDTO);
	}

	@Override
	public void applyUserReferral(AuthDTO authDTO, String referralCode) {
		try {
			userCustomerService.getUserCustomer(authDTO, authDTO.getUserCustomer());

			Map<String, String> userReferralMap = walletService.applyUserReferral(authDTO, authDTO.getUserCustomer().getMobile(), referralCode);

			if (userReferralMap != null && StringUtil.isValidMobileNumber(userReferralMap.get("referralUserMobile")) && StringUtil.isValidMobileNumber(userReferralMap.get("mobileNumber"))) {
				UserCustomerDTO userCustomerDTO = new UserCustomerDTO();
				userCustomerDTO.setMobile(userReferralMap.get("referralUserMobile"));
				userCustomerService.getUserCustomer(authDTO, userCustomerDTO);

				UserCustomerDTO userCustomer = new UserCustomerDTO();
				userCustomer.setMobile(userReferralMap.get("mobileNumber"));
				userCustomerService.getUserCustomer(authDTO, userCustomer);

				String title = "Hi " + userCustomerDTO.getName();
				String content = "Your Referral Code is used by " + userCustomer.getName();

				pushNotification(authDTO, userCustomerDTO, title, content);

				String benificiaryTitle = "Hi " + userCustomer.getName();
				String benificiaryContent = "Referral Code used successfully.";

				pushNotification(authDTO, userCustomer, benificiaryTitle, benificiaryContent);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void pushNotification(AuthDTO authDTO, UserCustomerDTO userCustomerDTO, String title, String content) {
		List<AppStoreDetailsDTO> appStoreDetailsList = userCustomerService.getAppStoreDetails(authDTO, userCustomerDTO);
		for (AppStoreDetailsDTO appStoreDetailsDTO : appStoreDetailsList) {
			JSONObject data = new JSONObject();
			data.put("title", title);
			data.put("body", content);
			data.put("datetime", DateUtil.NOW().format("YYYY-MM-DD hh:mm:ss"));
			fcmService.pushNotification(authDTO, authDTO.getNamespaceCode(), appStoreDetailsDTO.getGcmToken(), title, content, Text.EMPTY, data);
		}
	}

	@Override
	public void verifyBenificiaryReferral(AuthDTO authDTO, String referralCode) {
		boolean isUserExist = walletService.verifyBenificiaryReferral(authDTO, referralCode);
		if (!isUserExist) {
			throw new ServiceException(ErrorCode.INVALID_CODE);
		}
	}
}
