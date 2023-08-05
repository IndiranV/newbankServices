package org.in.com.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.sf.json.JSONObject;

@Data
@EqualsAndHashCode(callSuper = true)
public class BusBreakevenSettingsDTO extends BaseDTO<BusBreakevenSettingsDTO> {
	private BusDTO bus;
	private JSONObject breakevenDetails;
	private double fuelPrice = 00.0;
	private String breakevenKey;

	public String getBreakeven() {
		String json = "{}";
		if (breakevenDetails != null) {
			json = breakevenDetails.toString();
		}
		return json;
	}
}
