package org.in.com.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CashbookVendorDTO extends BaseDTO<CashbookVendorDTO> {
	private String mobileNumber;
	private String email;
	private String address;
	private String bankDetails;
}
