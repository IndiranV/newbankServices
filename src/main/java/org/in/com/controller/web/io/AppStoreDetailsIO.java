package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AppStoreDetailsIO extends BaseIO {
	private String udid;
	private String gcmToken;
	private String os;
	private String model;
	private String brand;
	private BaseIO deviceMedium;
	private String createdAt;
}
