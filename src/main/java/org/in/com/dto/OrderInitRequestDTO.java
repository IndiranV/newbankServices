package org.in.com.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import org.in.com.dto.enumeration.OrderTypeEM;

import lombok.Data;

@Data
public class OrderInitRequestDTO implements Serializable {

	private static final long serialVersionUID = 8694551267862935278L;
	private String firstName;
	private String lastName;
	private String email;
	private String mobile;
	private String transactionCode;
	private BigDecimal amount;
	private String partnerCode;
	private String responseUrl;
	private String orderCode;
	private OrderTypeEM orderType;
	private String address1;
	private String address2;
	private String city = "Chennai";
	private String state = "Tamil Nadu";
	private String udf1;
	private String udf2;
	private String udf3;
	private String udf4;
	private String udf5;

}
