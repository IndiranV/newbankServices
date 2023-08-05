package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExtraCommissionSlabIO extends BaseIO {
	private BaseIO slabCalenderType;
	private BaseIO slabCalenderMode;
	private BaseIO slabMode;
	private int slabFromValue;
	private int slabToValue;
}
