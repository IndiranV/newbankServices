package org.in.com.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.dto.enumeration.DeviceMediumEM;

@Data
@EqualsAndHashCode(callSuper = true)
public class AppStoreDetailsDTO extends BaseDTO<AppStoreDetailsDTO> {
	private String udid;
	private String gcmToken;
	private String os;
	private String model;
	private String brand;
	private DeviceMediumEM deviceMedium;
	private AuditDTO audit;

}
