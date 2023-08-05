package org.in.com.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PhoneNumberBlockDTO extends BaseDTO<PhoneNumberBlockDTO> {
	private String mobile;
	private String remarks;
}
