package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BusIO extends BaseIO {
	private String categoryCode;
	private String displayName;
	private int seatCount;
	private String busType;
	private int totalSeatCount;
	private List<BusSeatLayoutIO> seatLayoutList;

}
