package org.in.com.dto;

import org.in.com.dto.enumeration.DeviceMediumEM;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserDeviceDTO extends BaseDTO<UserDeviceDTO> {

	public UserDeviceDTO() {
		super();
	}

	public UserDeviceDTO(String uniqueCode, String deviceCode, String version, DeviceMediumEM deviceMedium) {
		super();
		this.uniqueCode = uniqueCode;
		this.deviceCode = deviceCode;
		this.version = version;
		this.deviceMedium = deviceMedium;
	}

	private String uniqueCode;
	private String deviceCode;
	private String version;
	private DeviceMediumEM deviceMedium;

}
