package org.in.com.controller.api.io;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class VehicleAttendantIO extends BaseIO {
	@JsonInclude(Include.NON_DEFAULT)
	private int age;
	private String mobile;
	private String alternateMobile;
	private String joiningDate;
	private String address;
	private String remarks;

}
