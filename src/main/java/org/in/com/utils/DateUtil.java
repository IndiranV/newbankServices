package org.in.com.utils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import hirondelle.date4j.DateTime;

public class DateUtil {
	public static final DateTimeFormatter JODA_DATE_TIME_FORMATE = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
	public static final DateTimeFormatter JODA_DATE_FORMATE = DateTimeFormat.forPattern("yyyy-MM-dd");
	public static final DateTimeFormatter DATE_FORMATE_ZONE = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
	public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	public static boolean isValidDate(String inDate) {
		if (StringUtil.isNull(inDate) || inDate.length() != 10) {
			return false;
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
		simpleDateFormat.applyPattern("yyyy-MM-dd");
		simpleDateFormat.setLenient(false);
		try {
			Date formattedDate = simpleDateFormat.parse(inDate.trim());
			if (formattedDate == null) {
				System.out.println("Invalid Date: " + inDate);
				return false;
			}
		}
		catch (ParseException pe) {
			System.out.println("Invalid Date: " + inDate);
			// Thread.dumpStack(); Thread.currentThread().getStackTrace()
			return false;
		}
		return true;
	}

	public static boolean isValidDateV2(String inDate) {
		try {
			if (inDate.length() >= 10) {
				new DateTime(inDate).format(Text.DATE_TIME_DATE4J);
			}
			else {
				return false;
			}
		}
		catch (Exception pe) {
			return false;
		}
		return true;
	}

	public static boolean isValidDateTime(String inDate) {
		String patterns = "yyyy-MM-dd HH:mm:ss";
		try {
			LocalDateTime.parse(inDate, java.time.format.DateTimeFormatter.ofPattern(patterns));
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	public static DateTime NOW() {
		// return DateTime.now(TimeZone.getTimeZone("Asia/Calcutta"));
		return DateTime.now(TimeZone.getDefault());
	}

	public static org.joda.time.DateTime NowV2() {
		// return DateTime.now(TimeZone.getTimeZone("Asia/Calcutta"));
		return org.joda.time.DateTime.now(DateTimeZone.getDefault());
	}

	public static DateTime getEpochToDatetime(long aNanoseconds) {
		return DateTime.forInstant(aNanoseconds, TimeZone.getDefault());
	}

	public static DateTime addMinituesToDate(DateTime dateinput, int minitues) {
		if (minitues <= 0) {
			return minusMinituesToDate(dateinput, minitues * -1);
		}
		int minituesPart = minitues;
		int hoursPart = 0;
		int DayPart = 0;
		while (minituesPart >= 60) {
			minituesPart = minituesPart - 60;
			hoursPart++;
		}
		while (hoursPart >= 24) {
			hoursPart = hoursPart - 24;
			DayPart++;
		}

		return dateinput.plus(0, 0, DayPart, hoursPart, minituesPart, 0, 0, DateTime.DayOverflow.FirstDay);
	}

	public static DateTime addDaysToDate(DateTime dateinput, int days) {
		return dateinput.plus(0, 0, days, 0, 0, 0, 0, DateTime.DayOverflow.FirstDay);
	}

	public static DateTime plusMinituesToDate(DateTime dateinput, int minitues) {
		return dateinput.plus(0, 0, 0, 0, minitues, 0, 0, DateTime.DayOverflow.FirstDay);

	}

	public static DateTime minusMinituesToDate(DateTime dateinput, int minitues) {
		int minituesPart = minitues;
		int hoursPart = 0;
		int DayPart = 0;
		while (minituesPart >= 60) {
			minituesPart = minituesPart - 60;
			hoursPart++;
		}
		while (hoursPart >= 24) {
			hoursPart = hoursPart - 24;
			DayPart++;
		}
		return dateinput.minus(0, 0, DayPart, hoursPart, minituesPart, 0, 0, DateTime.DayOverflow.FirstDay);
	}

	public static DateTime minusDaysToDate(DateTime dateinput, int days) {
		return dateinput.minus(0, 0, days, 0, 0, 0, 0, DateTime.DayOverflow.FirstDay);
	}

	public static String getCompressDate(DateTime dateTime) {
		return dateTime.format("YYMMDD");
	}

	public static DateTime getUnCompressDate(String date) {
		DateTime dateTime = null;
		Pattern pattern = Pattern.compile("(\\d\\d)(\\d\\d)(\\d\\d)");
		Matcher matcher = pattern.matcher(date);
		if (matcher.find()) {
			dateTime = new DateTime(Integer.parseInt("20" + matcher.group(1)), Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)), 0, 0, 0, 0);
		}
		return dateTime;
	}

	public static int getSecondsDifferent(DateTime fromDate, DateTime toDate) {
		long t1 = fromDate.getMilliseconds(TimeZone.getDefault());
		long t2 = toDate.getMilliseconds(TimeZone.getDefault());
		long diffInMilliSecond = t2 - t1;
		return (int) TimeUnit.MILLISECONDS.toSeconds(diffInMilliSecond);
	}

	public static int getMinutiesDifferent(DateTime fromDate, DateTime toDate) {
		long t1 = fromDate.getMilliseconds(TimeZone.getDefault());
		long t2 = toDate.getMilliseconds(TimeZone.getDefault());
		long diffInMilliSecond = t2 - t1;
		return (int) TimeUnit.MILLISECONDS.toMinutes(diffInMilliSecond);
	}

	public static int getDayDifferent(DateTime fromDate, DateTime toDate) {
		long t1 = fromDate.getMilliseconds(TimeZone.getDefault());
		long t2 = toDate.getMilliseconds(TimeZone.getDefault());
		long diffInMilliSecond = t2 - t1;

		return (int) TimeUnit.MILLISECONDS.toDays(diffInMilliSecond);
	}

	public static String parseDateFormat(String dateOrTime, String existingFormat, String requiredFormat) throws ParseException {
		dateOrTime = dateOrTime.toUpperCase();
		String parsedDate = null;
		SimpleDateFormat returnFormat = new SimpleDateFormat(requiredFormat);

		SimpleDateFormat parseFormat = new SimpleDateFormat(existingFormat);
		Date dateFormat;
		try {
			dateFormat = parseFormat.parse(dateOrTime);
			parsedDate = returnFormat.format(dateFormat);
		}
		catch (ParseException e) {
			throw e;
		}
		return parsedDate;
	}

	public static String currentDateAndTime_dbFormat() {
		String ISTTime = "";
		TimeZone tz = TimeZone.getTimeZone("GMT");
		SimpleDateFormat format3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal3 = Calendar.getInstance(tz);
		Calendar cal4 = Calendar.getInstance();
		cal4.set(cal3.get(1), cal3.get(2), cal3.get(5), (cal3.get(11) + 5), (cal3.get(12) + 30), cal3.get(13));
		ISTTime = format3.format(cal4.getTime());
		return (ISTTime);
	}

	public static String currentDateAndTime() {
		String ISTTime = "";
		TimeZone tz = TimeZone.getTimeZone("GMT");
		SimpleDateFormat format3 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Calendar cal3 = Calendar.getInstance(tz);
		Calendar cal4 = Calendar.getInstance();
		cal4.set(cal3.get(1), cal3.get(2), cal3.get(5), (cal3.get(11) + 5), (cal3.get(12) + 30), cal3.get(13));
		ISTTime = format3.format(cal4.getTime());
		return (ISTTime);
	}

	public static DateTime getWeekStartDate(DateTime dateTime) {
		int todaysWeekday = dateTime.getWeekDay();
		int SUNDAY = 1;
		if (todaysWeekday > SUNDAY) {
			int numDaysFromSunday = todaysWeekday - SUNDAY;
			return dateTime.minusDays(numDaysFromSunday);
		}
		return dateTime;
	}

	public static DateTime getWeekEndDate(DateTime dateTime) {
		int todaysWeekday = dateTime.getWeekDay();
		return dateTime.plusDays(7 - todaysWeekday);
	}

	public static String getMinutesToTime(int minutes) {
		long hours = TimeUnit.MINUTES.toHours(minutes);
		long remainMinute = minutes - TimeUnit.HOURS.toMinutes(hours);
		String result = String.format("%02d", hours) + ":" + String.format("%02d", remainMinute);
		return result;
	}

	public static int getMinutesFromDateTime(DateTime dateTime) {
		return dateTime.getHour() * 60 + dateTime.getMinute();
	}

	public static DateTime getDateFromDateTime(DateTime dateTime) {
		DateTime newdateTime = new DateTime(dateTime.getYear(), dateTime.getMonth(), dateTime.getDay(), 0, 0, 0, 0);
		return newdateTime;
	}

	public static String getParseDateTimeFormat(DateTime dateTime, String format) {
		return dateTime.format(format, Locale.getDefault());
	}

	public static String getTimestamp(Timestamp timestamp) {
		String dateTime = Text.EMPTY;
		if (StringUtil.isNotNull(timestamp)) {
			dateTime = new SimpleDateFormat(Text.YEAR_MONTH_DATE_HIFUN_HOUR_MIN_SEC).format(timestamp);
		}
		return dateTime;
	}

	public static boolean isFallonAlternateDays(DateTime startDate, DateTime endDate) {
		return DateUtil.getDayDifferent(startDate, endDate) % 2 == 0 ? true : false;
	}

	public static boolean isDateTimeWithinRange(DateTime fromDate, DateTime toDate, DateTime currentDateTime) {
		return currentDateTime.gteq(fromDate.getStartOfDay()) && currentDateTime.lteq(toDate.getEndOfDay()) ? true : false;
	}

	public static boolean isHourWithinRange(int fromHour, int toHour, DateTime currentDateTime) {
		return currentDateTime.getHour() > fromHour && currentDateTime.getHour() < toHour ? true : false;
	}

	public static List<String> getDateList(DateTime fromDate, int days) {
		List<String> totalDates = new ArrayList<>();
		for (int day = 0; day < days; day++) {
			totalDates.add(fromDate.plusDays(day).format(Text.DATE_DATE4J));
		}
		return totalDates;
	}

	public static List<DateTime> getDateListV2(DateTime fromDate, int days) {
		List<DateTime> totalDates = new ArrayList<>();
		for (int day = 0; day < days; day++) {
			totalDates.add(fromDate.plusDays(day));
		}
		return totalDates;
	}

	public static List<DateTime> getDateListV3(DateTime fromDate, DateTime toDate, String dayOfWeek) {
		List<DateTime> totalDates = new ArrayList<>();
		if (StringUtil.isNull(dayOfWeek) || dayOfWeek.length() != 7) {
			return totalDates;
		}

		while (fromDate.getStartOfDay().compareTo(toDate.getStartOfDay()) <= 0) {
			if (dayOfWeek.substring(fromDate.getWeekDay() - 1, fromDate.getWeekDay()).equals("1")) {
				totalDates.add(fromDate);
			}
			fromDate = fromDate.plusDays(Numeric.ONE_INT);
		}
		return totalDates;
	}

	public static List<DateTime> getDateList(DateTime fromDate, DateTime toDate) {
		List<DateTime> totalDates = new ArrayList<>();

		while (fromDate.getStartOfDay().compareTo(toDate.getStartOfDay()) <= 0) {
			totalDates.add(fromDate);
			fromDate = fromDate.plusDays(Numeric.ONE_INT);
		}
		return totalDates;
	}

	public static List<String> getDateListToString(DateTime fromDate, DateTime toDate, String dayOfWeek) {
		List<String> totalDates = new ArrayList<>();
		if (StringUtil.isNull(dayOfWeek) || dayOfWeek.length() != 7) {
			return totalDates;
		}

		while (fromDate.getStartOfDay().compareTo(toDate.getStartOfDay()) <= 0) {
			if (dayOfWeek.substring(fromDate.getWeekDay() - 1, fromDate.getWeekDay()).equals("1")) {
				totalDates.add(DateUtil.convertDateTime(fromDate));
			}
			fromDate = fromDate.plusDays(Numeric.ONE_INT);
		}
		return totalDates;
	}

	public static DateTime getDateTime(String datetime) {
		return StringUtil.isNotNull(datetime) ? new DateTime(datetime) : null;
	}

	public static org.joda.time.DateTime getJodaDateTime(String datetime) {
		return StringUtil.isNotNull(datetime) ? new org.joda.time.DateTime(datetime) : null;
	}

	public static String convertDate(DateTime datetime) {
		return StringUtil.isNotNull(datetime) ? datetime.format(Text.DATE_DATE4J) : null;
	}

	public static String convertDateTime(DateTime datetime) {
		return StringUtil.isNotNull(datetime) ? datetime.format(Text.DATE_TIME_DATE4J) : Text.EMPTY;
	}

	public static String getDateTimeByInstant(long epochSecond) {
		Instant instant = Instant.ofEpochSecond(epochSecond);
		Date date = Date.from(instant);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dateFormat.format(date);
	}

	public static boolean isValidBeforeDate(DateTime fromDate, int reportingDays) {
		boolean isValidDate = true;
		DateTime reportFromDate = DateUtil.minusDaysToDate(DateUtil.NOW(), reportingDays);
		if (fromDate.lt(reportFromDate.getStartOfDay())) {
			isValidDate = false;
		}
		return isValidDate;
	}

	public static boolean isValidFutureDate(DateTime occurrenceDate, int futureDays) {
		boolean isValidDate = false;
		DateTime reportFromDate = DateUtil.addDaysToDate(DateUtil.NOW(), futureDays);
		if (occurrenceDate.lt(reportFromDate.getStartOfDay())) {
			isValidDate = true;
		}
		return isValidDate;
	}

	public static void main(String[] args) throws ParseException {
		// DateTime startDate = new DateTime("2018-06-26");
		// DateTime endDate = new DateTime("2018-07-11");
		//
		// System.out.println(DateUtil.getDayDifferent(startDate,
		// startDate.getEndOfMonth()));
		// System.out.println(isFallonAlternateDays(startDate, endDate));
		// System.out.println(isValidDateTime("2020-09-24 00:49:"));
		// System.out.println(DateUtil.getMinutiesDifferent(new
		// DateTime("2020-09-24 00:53:00"),new DateTime("2020-09-24
		// 00:49:00")));

		System.out.println(DateTime.forInstant(Long.parseLong("1480876200000"), TimeZone.getDefault()));
	}

	public static boolean isDateExist(List<DateTime> dateTimes, DateTime dateTime) {
		boolean isDateExist = false;
		if (dateTimes != null && !dateTimes.isEmpty()) {
			for (DateTime datetime : dateTimes) {
				if (datetime.getStartOfDay().compareTo(dateTime.getStartOfDay()) == 0) {
					isDateExist = true;
					break;
				}
			}
		}
		return isDateExist;
	}
	}
