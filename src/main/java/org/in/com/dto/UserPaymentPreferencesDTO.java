package org.in.com.dto;

import org.in.com.dto.enumeration.FrequencyModeEM;
import org.in.com.dto.enumeration.PreferenceTypeEM;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserPaymentPreferencesDTO extends BaseDTO<UserPaymentPreferencesDTO> {
	private UserDTO user;
	private int travelDateFlag;
	private FrequencyModeEM frequencyMode;
	private PreferenceTypeEM preferenceType;
	private String dayOfWeek;
	private int dayOfMonth;
	private String dayOfTime;
	private String emailAddress;
}
