package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OperatorIO extends BaseIO {
	private int mobileTicketFlag;
	private String displayName;
}
