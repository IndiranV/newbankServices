package org.in.com.controller.web.io;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserStationPointIO extends BaseIO {
	private UserIO user;
	private StationIO station;
	private List<GroupIO> groupList;
	private BigDecimal boardingCommission;

}
