package org.in.com.controller.api_v3.io;

import lombok.Data;

@Data
public class TransactionTypeIO  {
	private String code;
	private String creditDebitFlag;
	private String name;
}
