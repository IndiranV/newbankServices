package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CashbookVendorIO extends BaseIO {
	private String mobileNumber;
	private String address;
	private String email;
	private String bankDetails;
}
