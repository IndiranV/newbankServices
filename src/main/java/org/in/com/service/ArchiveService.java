package org.in.com.service;

import hirondelle.date4j.DateTime;

import java.util.List;
import java.util.Map;

public interface ArchiveService {
	public Map<String, List<Map<String, ?>>> getArchiveReport(String tableName, String fromDate, String toDate);

	public Map<String, List<Map<String, ?>>> getMasterForDrill(String tableName, String fromDate, String toDate);

	public Map<String, List<Map<String, ?>>> getBitsTicketTransaction(DateTime fromDate, DateTime toDate);
}
