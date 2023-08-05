package org.in.com.utils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import javax.mail.URLName;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;
//https://github.com/sdcuike/aspirin-demo/tree/master/src/main/java/com/doctor/javamail/util
public class EmailUtil {

	private static final String SMTP_PROTOCOL_PREFIX = "smtp://";

	private static Set<URLName> getMXRecordsForHost(String hostName) throws TextParseException {

		Set<URLName> recordsColl = new HashSet<>();

		boolean foundOriginalMX = true;
		Record[] records = new Lookup(hostName, Type.MX).run();

		/*
		 * Sometimes we should send an email to a subdomain which does not
		 * have own MX record and MX server. At this point we should find an
		 * upper level domain and server where we can deliver our email.
		 * Example: subA.subB.domain.name has not own MX record and
		 * subB.domain.name is the mail exchange master of the subA domain
		 * too.
		 */
		if (records == null || records.length == 0) {
			foundOriginalMX = false;
			String upperLevelHostName = hostName;
			while (records == null && upperLevelHostName.indexOf(".") != upperLevelHostName.lastIndexOf(".") && upperLevelHostName.lastIndexOf(".") != -1) {
				upperLevelHostName = upperLevelHostName.substring(upperLevelHostName.indexOf(".") + 1);
				records = new Lookup(upperLevelHostName, Type.MX).run();
			}
		}

		if (records != null) {
			// Sort in MX priority (higher number is lower priority)
			Arrays.sort(records, new Comparator<Record>() {
				@Override
				public int compare(Record arg0, Record arg1) {
					return ((MXRecord) arg0).getPriority() - ((MXRecord) arg1).getPriority();
				}
			});
			// Create records collection
			for (int i = 0; i < records.length; i++) {
				MXRecord mx = (MXRecord) records[i];
				String targetString = mx.getTarget().toString();
				URLName uName = new URLName(SMTP_PROTOCOL_PREFIX + targetString.substring(0, targetString.length() - 1));
				recordsColl.add(uName);
			}
		}
		else {
			foundOriginalMX = false;
		}

		/*
		 * If we found no MX record for the original hostname (the upper
		 * level domains does not matter), then we add the original domain
		 * name (identified with an A record) to the record collection,
		 * because the mail exchange server could be the main server too.
		 * We append the A record to the first place of the record
		 * collection, because the standard says if no MX record found then
		 * we should to try send email to the server identified by the A
		 * record.
		 */
		if (!foundOriginalMX) {
			Record[] recordsTypeA = new Lookup(hostName, Type.A).run();
			if (recordsTypeA != null && recordsTypeA.length > 0) {
				recordsColl.add(new URLName(SMTP_PROTOCOL_PREFIX + hostName));
			}
		}

		return recordsColl;
	}

	public static boolean isValid(String email) {
		try {
			String hostName = email.substring(email.lastIndexOf("@") + 1);
			Set<URLName> mxRecordsForHost = getMXRecordsForHost(hostName);
			if (!mxRecordsForHost.isEmpty()) {
				return true;
			}
		}
		catch (Exception e) {
			return false;
		}
		return false;
	}
}
