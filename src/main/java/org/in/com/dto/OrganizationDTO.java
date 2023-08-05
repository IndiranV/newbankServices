package org.in.com.dto;

import org.in.com.constants.Text;
import org.in.com.utils.StringUtil;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrganizationDTO extends BaseDTO<OrganizationDTO> {
	private String address1;
	private String address2;
	private String contact;
	private StationDTO station;
	private int userCount;
	private String pincode;
	private String latLon;
	private String shortCode;
	private int workingMinutes;

	public String getLatitude() {
		String latitude = Text.EMPTY;
		if (StringUtil.isNotNull(latLon)) {
			latitude = latLon.split(Text.COMMA)[0];
		}
		return latitude;
	}

	public String getLongitude() {
		String longitude = Text.EMPTY;
		if (StringUtil.isNotNull(latLon)) {
			longitude = latLon.split(Text.COMMA)[1];
		}
		return longitude;
	}
}
