package org.in.com.controller.app.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.controller.web.io.BaseIO;

@Data
@EqualsAndHashCode(callSuper = true)
public class AddonTypeIO extends BaseIO {
	private String creditDebitFlag;
}