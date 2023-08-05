package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CalendarAnnouncementIO extends BaseIO {
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private List<String> dates;
	private List<StateIO> states;
	private BaseIO category;
}
