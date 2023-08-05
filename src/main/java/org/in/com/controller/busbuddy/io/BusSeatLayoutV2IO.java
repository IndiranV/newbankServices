package org.in.com.controller.busbuddy.io;

import lombok.Data;

@Data
public class BusSeatLayoutV2IO {
	private String seatCode;
	private String seatName;
	private BaseIO busSeatType;
	private int rowPos;
	private int colPos;
	private int layer;
	private int sequence;
	private int orientation;
}
