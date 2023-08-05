package org.in.com.service.impl;

import java.util.List;

import org.in.com.aggregator.sms.SMSService;
import org.in.com.aggregator.wallet.WalletService;
import org.in.com.constants.Constants;
import org.in.com.constants.Text;
import org.in.com.dao.UserCustomerDAO;
import org.in.com.dto.AppStoreDetailsDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.DiscountCategoryDTO;
import org.in.com.dto.DiscountCouponDTO;
import org.in.com.dto.DiscountCriteriaDTO;
import org.in.com.dto.UserCustomerAuthDTO;
import org.in.com.dto.UserCustomerDTO;
import org.in.com.dto.enumeration.WalletAccessEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.DiscountCategoryService;
import org.in.com.service.DiscountCouponService;
import org.in.com.service.DiscountService;
import org.in.com.service.TicketService;
import org.in.com.service.UserCustomerService;
import org.in.com.utils.StringUtil;
import org.in.com.utils.TokenGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@Service
@EnableAsync
public class UserCustomerServiceImpl implements UserCustomerService {
	@Autowired
	WalletService walletService;
	@Autowired
	DiscountService discountService;
	@Autowired
	DiscountCouponService discountCouponService;
	@Autowired
	DiscountCategoryService discountCategoryService;
	@Autowired
	SMSService smsService;
	@Autowired
	TicketService ticketService;

	@Override
	public UserCustomerDTO getUserCustomer(AuthDTO authDTO, UserCustomerDTO userCustomerDTO) {
		UserCustomerDAO userCustomerDAO = new UserCustomerDAO();
		userCustomerDAO.getUserCustomer(authDTO, userCustomerDTO);
		return userCustomerDTO;
	}

	@Override
	public UserCustomerDTO updateUserCustomer(AuthDTO authDTO, UserCustomerDTO userCustomerDTO) {
		UserCustomerDAO userCustomerDAO = new UserCustomerDAO();
		userCustomerDAO.updateUserCustomer(authDTO, userCustomerDTO);

		// Update Wallet User
		if (WalletAccessEM.getWalletAccessEM(authDTO.getNamespaceCode()) != null) {
			updateWalletUser(authDTO, userCustomerDTO);
		}
		return userCustomerDTO;
	}

	@Async
	public void updateWalletUser(AuthDTO authDTO, UserCustomerDTO userCustomerDTO) {
		UserCustomerDAO userCustomerDAO = new UserCustomerDAO();
		walletService.updateWalletUser(authDTO, userCustomerDTO);
		if (StringUtil.isNotNull(userCustomerDTO.getWalletCode())) {
			userCustomerDAO.updateUserCustomer(authDTO, userCustomerDTO);
		}
	}

	@Override
	public UserCustomerDTO checkUserCustomer(AuthDTO authDTO, String mobileNumber) {
		UserCustomerDAO userCustomerDAO = new UserCustomerDAO();
		return userCustomerDAO.checkUserCustomer(authDTO, mobileNumber);
	}

	@Override
	public void appStoreUpdate(AuthDTO authDTO, UserCustomerDTO userCustomer) {
		UserCustomerDAO userCustomerDAO = new UserCustomerDAO();
		if (!userCustomerDAO.isUserAppStoreExist(authDTO, userCustomer)) {
			userCustomerDAO.addAppStoreDetails(authDTO, userCustomer);
		}
		else {
			userCustomerDAO.updateUserAppStoreDetails(authDTO, userCustomer);
		}
	}

	@Override
	public List<AppStoreDetailsDTO> getAppStoreDetails(AuthDTO authDTO, UserCustomerDTO userCustomerDTO) {
		UserCustomerDAO userCustomerDAO = new UserCustomerDAO();
		return userCustomerDAO.getAppStoreDetails(authDTO, userCustomerDTO);
	}

	@Override
	public void generateUserCustomerAuth(AuthDTO authDTO, UserCustomerDTO userCustomerDTO) {
		UserCustomerDAO userCustomerDAO = new UserCustomerDAO();
		List<UserCustomerAuthDTO> userCustomerAuthList = userCustomerDAO.getUserCustomerAuth(authDTO, userCustomerDTO);

		UserCustomerAuthDTO customerAuthDTO = null;
		for (UserCustomerAuthDTO userCustomerAuth : userCustomerAuthList) {
			if (userCustomerAuth.getDeviceMedium().getId() == authDTO.getDeviceMedium().getId()) {
				customerAuthDTO = userCustomerAuth;
				break;
			}
		}

		if (customerAuthDTO == null) {
			UserCustomerAuthDTO userCustomerAuth = new UserCustomerAuthDTO();
			userCustomerAuth.setSessionToken(TokenGenerator.generateCode(authDTO.getDeviceMedium().getCode()));
			userCustomerAuth.setDeviceMedium(authDTO.getDeviceMedium());
			userCustomerDTO.setUserCustomerAuth(userCustomerAuth);
			userCustomerDAO.addUserCustomerAuth(authDTO, userCustomerDTO);
		}
		else if (customerAuthDTO != null) {
			customerAuthDTO.setSessionToken(TokenGenerator.generateCode(authDTO.getDeviceMedium().getCode()));
			userCustomerDAO.updateUserCustomerAuth(authDTO, customerAuthDTO);
			userCustomerDTO.setUserCustomerAuth(customerAuthDTO);
		}
	}

	@Override
	public UserCustomerDTO checkUserCustomerAuthBySessionToken(AuthDTO authDTO, UserCustomerDTO userCustomer) {
		UserCustomerDAO userCustomerDAO = new UserCustomerDAO();
		userCustomerDAO.getUserCustomer(authDTO, userCustomer);
		if (userCustomer.getId() == 0) {
			throw new ServiceException(ErrorCode.INVALID_USERNAME);
		}
		List<UserCustomerAuthDTO> userCustomerAuthList = userCustomerDAO.getUserCustomerAuth(authDTO, userCustomer);

		if (userCustomerAuthList.isEmpty()) {
			throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
		}
		for (UserCustomerAuthDTO userCustomerAuth : userCustomerAuthList) {
			if (userCustomerAuth.getDeviceMedium().getId() == userCustomer.getUserCustomerAuth().getDeviceMedium().getId() && userCustomerAuth.getSessionToken().equalsIgnoreCase(userCustomer.getUserCustomerAuth().getSessionToken())) {
				userCustomerAuth.setSessionToken(TokenGenerator.generateCode(authDTO.getDeviceMedium().getCode()));
				userCustomerDAO.updateUserCustomerAuth(authDTO, userCustomerAuth);
				userCustomer.setUserCustomerAuth(userCustomerAuth);
			}
		}
		return userCustomer;
	}

	@Override
	public void updateUserCustomerAuth(AuthDTO authDTO, UserCustomerAuthDTO userCustomerAuthDTO) {
		UserCustomerDAO userCustomerDAO = new UserCustomerDAO();
		userCustomerDAO.updateUserCustomerAuth(authDTO, userCustomerAuthDTO);
	}

	@Override
	public UserCustomerDTO saveUserCustomer(AuthDTO authDTO, UserCustomerDTO userCustomerDTO) {
		updateUserCustomer(authDTO, userCustomerDTO);

		if (StringUtil.isContains(Constants.CUSTOMER_DISCOUNT, authDTO.getNamespace().getCode())) {
			DiscountCriteriaDTO discountCriteria = discountService.getCustomerDiscountCriteria(authDTO, userCustomerDTO);
			if (discountCriteria != null && discountCriteria.getId() != 0) {
				DiscountCouponDTO discountCouponDTO = discountCriteria.getDiscountCoupon();
				discountCouponDTO.setCode(null);
				discountCouponDTO.setActiveDesription("Dynamic Customer Discount");
				discountCouponDTO.setErrorDescription(Text.NA);
				discountCouponDTO.setActiveFlag(1);

				DiscountCategoryDTO categoryDTO = new DiscountCategoryDTO();
				categoryDTO.setId(discountCriteria.getDiscountCoupon().getDiscountCategory().getId());
				discountCategoryService.get(authDTO, categoryDTO);
				discountCouponDTO.setDiscountCategory(categoryDTO);

				discountCouponDTO.setCoupon(TokenGenerator.generateToken(8).toUpperCase());
				discountCouponDTO.setUserCustomer(userCustomerDTO);
				discountCouponService.Update(authDTO, discountCouponDTO);

				// Send SMS
				smsService.sendCustomerDiscountCoupon(authDTO, discountCouponDTO, userCustomerDTO);
			}
		}
		return userCustomerDTO;
	}

}
