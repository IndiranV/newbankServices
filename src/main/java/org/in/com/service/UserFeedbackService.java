package org.in.com.service;

import hirondelle.date4j.DateTime;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.UserFeedbackDTO;
import org.in.com.dto.UserRegistrationDTO;

public interface UserFeedbackService {

	public List<UserFeedbackDTO> getAll(AuthDTO authDTO, DateTime fromDate, DateTime toDate);

	public UserFeedbackDTO Update(AuthDTO authDTO, UserFeedbackDTO dto);

	public List<UserRegistrationDTO> getUserRegistrationRequest(AuthDTO authDTO, DateTime fromDate, DateTime toDate);

	public UserRegistrationDTO addUserRegistrationRequest(AuthDTO authDTO, UserRegistrationDTO registrationDTO);

	public String sendReplyToUserFeedback(AuthDTO authDTO, UserFeedbackDTO userFeedback);

	void sendFeedBackSMS(AuthDTO authDTO, DateTime tripDate);

}
