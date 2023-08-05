package org.in.com.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserDetailsDTO extends BaseDTO<UserDetailsDTO> {
	private String address1;
	private String address2;
	private String landmark;
	private String pincode;
	private UserDTO user;
	private StationDTO station;
	private StateDTO state;
	private StationAreaDTO stationArea;

}
