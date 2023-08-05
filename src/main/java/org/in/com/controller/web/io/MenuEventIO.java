package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MenuEventIO extends BaseIO {
	private String permissionType;// V,S,U,ALL
	private String operationCode;
	private String attr1Value;
	private int exceptionFlag;
	private int enabledFlag;
    private BaseIO severity;
}
