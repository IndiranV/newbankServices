package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TermIO extends BaseIO {
	private int sequence;
	private List<String> tagList;
	private List<ScheduleIO> schedule;
	private BaseIO transactionType;
}
