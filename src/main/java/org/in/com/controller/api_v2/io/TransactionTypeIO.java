package org.in.com.controller.api_v2.io;

import lombok.Data;

@Data
public class TransactionTypeIO  {
	private String code;
	private String creditDebitFlag;
	private String name;
}
