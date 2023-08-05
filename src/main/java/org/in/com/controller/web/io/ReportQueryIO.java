package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReportQueryIO extends BaseIO {
	private String description;
	private String query;
	private int daysLimit;
}
