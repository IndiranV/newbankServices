package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StationOtaPartnerIO extends BaseIO {
	private String otaStationCode;
	private String otaStationName;
	private BaseIO state;
	private List<BaseIO> stations;
	private BaseIO otaPartner;
	private List<StationOtaPartnerIO> otaStations;
}
