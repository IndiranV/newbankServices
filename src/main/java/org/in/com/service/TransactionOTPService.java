package org.in.com.service;

import org.in.com.dto.AuthDTO;

public interface TransactionOTPService {

	public int generateOTP(AuthDTO authDTO, String transactionCode, String mobileNumber, boolean smsNotificationFlag);

	public boolean validateOTP(AuthDTO authDTO, String transactionCode, String mobileNumber, int OTP);

	public boolean checkOTP(AuthDTO authDTO, String transactionCode, String mobileNumber, int OTP);

}
