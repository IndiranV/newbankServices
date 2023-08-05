package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.sf.json.JSONObject;

@Data
@EqualsAndHashCode(callSuper = true)
public class BusBreakevenSettingsIO extends BaseIO {
	private BusIO bus;
	private JSONObject breakevenDetails;
}
