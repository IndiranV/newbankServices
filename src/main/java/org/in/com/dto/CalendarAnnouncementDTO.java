package org.in.com.dto;

import java.util.List;

import org.in.com.dto.enumeration.CalendarAnnouncementCategoryEM;

import hirondelle.date4j.DateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class CalendarAnnouncementDTO extends BaseDTO<CalendarAnnouncementDTO> {
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private List<DateTime> dates;
	private List<StateDTO> states;
	private CalendarAnnouncementCategoryEM category;
	
}
