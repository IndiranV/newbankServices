package org.in.com.service.impl;

import net.sf.ehcache.Element;

import org.in.com.aggregator.sms.SMSService;
import org.in.com.cache.EhcacheManager;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.enumeration.NotificationSubscriptionTypeEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.NotificationPushService;
import org.in.com.service.TransactionOTPService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionOTPImpl implements TransactionOTPService {
	@Autowired
	SMSService smsService;
	@Autowired
	NotificationPushService notificationPushService;

	public int generateOTP(AuthDTO authDTO, String transactionCode, String mobileNumber, boolean smsNotificationFlag) {
		int newOTP = BitsUtil.generateOTPNumber(mobileNumber);
		if (EhcacheManager.getFreshRequestEhCache().get("OTP" + mobileNumber) == null) {
			if (smsNotificationFlag) {
				smsService.sendTransactionOTP(authDTO, newOTP, mobileNumber);
			}
			if (authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.OTP_LOGIN)) {
				notificationPushService.pushOTPNotification(authDTO, newOTP);
			}
			String cacheKey = authDTO.getNamespaceCode() + Text.UNDER_SCORE + transactionCode + Text.UNDER_SCORE + mobileNumber;
			EhcacheManager.getOTPCache().put(new Element(cacheKey, newOTP));
			EhcacheManager.getFreshRequestEhCache().put(new Element("OTP" + mobileNumber, 1));
		}
		else {
			throw new ServiceException(ErrorCode.OTP_ALREADY_SENT);
		}
		return newOTP;
	}

	public boolean validateOTP(AuthDTO authDTO, String transactionCode, String mobileNumber, int OTP) {
		if (OTP == 0 || StringUtil.isNull(transactionCode) || StringUtil.isNull(mobileNumber)) {
			return false;
		}
		String cacheKey = authDTO.getNamespaceCode() + Text.UNDER_SCORE + transactionCode + Text.UNDER_SCORE + mobileNumber;
		Element element = EhcacheManager.getOTPCache().get(cacheKey);
		int cacheOTP = 0;
		if (element != null) {
			cacheOTP = (int) element.getObjectValue();

		}
		if (OTP == cacheOTP) {
			EhcacheManager.getOTPCache().remove(cacheKey);
			EhcacheManager.getFreshRequestEhCache().remove("OTP" + mobileNumber);
			return true;
		}
		return false;
	}

	public boolean checkOTP(AuthDTO authDTO, String transactionCode, String mobileNumber, int OTP) {
		if (OTP == 0 || StringUtil.isNull(transactionCode) || StringUtil.isNull(mobileNumber)) {
			return false;
		}
		String cacheKey = authDTO.getNamespaceCode() + Text.UNDER_SCORE + transactionCode + Text.UNDER_SCORE + mobileNumber;
		Element element = EhcacheManager.getOTPCache().get(cacheKey);
		int cacheOTP = 0;
		if (element != null) {
			cacheOTP = (int) element.getObjectValue();

		}
		if (OTP == cacheOTP) {
			return true;
		}
		return false;
	}

}
