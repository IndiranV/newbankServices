package org.in.com.controller.busbuddy.io;

import lombok.Data;

@Data
public class BaseIO {
	private String code;
	private String name;
	private int activeFlag = 1;
}
