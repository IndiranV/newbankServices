package org.in.com.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.in.com.constants.Text;

import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class FileConvertor {
	protected static InputStream getPDF(List<List<String>> report, String[] header, InputStream stream) throws Exception {
		try {
			Document document = new Document();
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			PdfWriter.getInstance(document, outputStream);
			document.setPageSize(PageSize.A2.rotate());
			document.open();
			PdfPTable table = new PdfPTable(report.get(0).size());
			Font font = new Font();
			font.setSize(10);
			font.setFamily("Arial");
			boolean isHeader = (report.get(0).size() == header.length) ? true : false;
			int i = 0;
			for (Iterator<List<String>> itr = report.iterator(); itr.hasNext(); i++) {
				if (isHeader) {
					for (int cnt = 0; cnt < header.length; cnt++) {
						table.addCell(new Paragraph(header[cnt].toUpperCase(), font));
					}
					isHeader = false;
					itr.next();
					continue;
				}
				Iterator<String> row = itr.next().iterator();
				while (row.hasNext()) {
					String next = row.next();
					if (i == 0) {
						table.addCell(new Paragraph(next.toUpperCase(), font));
					}
					else {
						table.addCell(new Paragraph(next, font));
					}
				}
			}
			document.add(table);
			document.close();
			stream = new ByteArrayInputStream(outputStream.toByteArray());
		}
		catch (Exception e) {
			throw e;
		}
		return stream;
	}

	protected static InputStream getCSV(List<List<String>> report, String[] header, InputStream stream) throws Exception {
		try {
			boolean isHeader = (report.get(0).size() == header.length) ? true : false;
			StringBuilder writer = new StringBuilder();
			Iterator<List<String>> itr = report.iterator();
			while (itr.hasNext()) {
				if (isHeader) {
					for (int cnt = 0; cnt < header.length; cnt++) {
						writer.append(header[cnt].toUpperCase());
						writer.append(Text.COMMA);
					}
					isHeader = false;
					itr.next();
					writer.append(Text.NEW_LINE);
					continue;
				}
				Iterator<String> row = itr.next().iterator();
				while (row.hasNext()) {
					String next = row.next();
					writer.append(StringUtil.isNull(next) ? next : next.replaceAll(Text.COMMA, Text.UNDER_SCORE));
					writer.append(Text.COMMA);
				}
				writer.append(Text.NEW_LINE);
			}
			stream = new ByteArrayInputStream(writer.toString().getBytes());
		}
		catch (Exception e) {
			throw e;
		}
		return stream;
	}

	protected static InputStream getTSV(List<List<String>> report, String[] header, InputStream stream) throws Exception {
		try {
			boolean isHeader = (report.get(0).size() == header.length) ? true : false;
			StringBuilder writer = new StringBuilder();
			int[] space = new int[report.get(0).size()];
			int i = 0;
			for (Iterator<List<String>> itrIterator = report.iterator(); itrIterator.hasNext(); i++) {
				int j = 0;
				List<String> list = itrIterator.next();
				for (Iterator<String> itrIterator2 = list.iterator(); itrIterator2.hasNext(); j++) {
					String next = itrIterator2.next();
					space[j] = (!StringUtil.isNull(next) && next.length() > space[j]) ? next.length() : space[j];
				}
			}
			i = 0;
			for (Iterator<List<String>> itrIterator = report.iterator(); itrIterator.hasNext(); i++) {
				if (isHeader) {
					for (int cnt = 0; cnt < header.length; cnt++) {
						writer.append(header[cnt].toUpperCase()).append(FileConvertor.getSpace((space[cnt] - header[cnt].length()))).append(Text.NEW_TAB);
					}
					isHeader = false;
					itrIterator.next();
					writer.append(Text.NEW_LINE);
					continue;
				}
				int j = 0;
				List<String> list = itrIterator.next();
				for (Iterator<String> itrIterator2 = list.iterator(); itrIterator2.hasNext(); j++) {
					String next = itrIterator2.next();
					if (i == 0) {
						writer.append(next.toUpperCase()).append(FileConvertor.getSpace((space[j] - next.length()))).append(Text.NEW_TAB);
					}
					else {
						writer.append(next).append(FileConvertor.getSpace((space[j] - (StringUtil.isNull(next) ? 0 : next.length())))).append(Text.NEW_TAB);
					}
				}
				writer.append(Text.NEW_LINE);
			}
			stream = new ByteArrayInputStream(writer.toString().getBytes());
		}
		catch (Exception e) {
			throw e;
		}
		return stream;
	}

	public static String getSpace(int cnt) {
		StringBuilder writer = new StringBuilder();
		for (int count = 0; count < cnt; count++) {
			writer.append(Text.SINGLE_SPACE);
		}
		return writer.toString();
	}

	protected static InputStream getHTML(List<List<String>> report, String[] header, InputStream stream) throws Exception {
		try {
			boolean isHeader = (report.get(0).size() == header.length) ? true : false;
			StringBuilder writer = new StringBuilder();
			writer.append("<table align='center' cellspacing='0' cellpadding='3' border='1'>");
			int i = 0;
			for (Iterator<List<String>> itr = report.iterator(); itr.hasNext(); i++) {
				if (isHeader) {
					writer.append("<tr>");
					for (int cnt = 0; cnt < header.length; cnt++) {
						writer.append("<th>").append(header[cnt].toUpperCase()).append("</th>");
					}
					writer.append("</tr>");
					isHeader = false;
					itr.next();
					continue;
				}
				Iterator<String> row = itr.next().iterator();
				writer.append("<tr>");
				while (row.hasNext()) {
					String next = row.next();
					if (i == 0) {
						writer.append("<th>").append(next).append("</th>");
					}
					else {
						writer.append("<td>").append(next).append("</td>");
					}
				}
				writer.append("</tr>");
			}
			writer.append("</table>");
			stream = new ByteArrayInputStream(writer.toString().getBytes());
		}
		catch (Exception e) {
			throw e;
		}
		return stream;
	}

	protected static InputStream getXLSWorkBook(List<List<String>> report, String[] header, InputStream stream) throws Exception {
		try {
			HSSFWorkbook workBook = new HSSFWorkbook();
			HSSFSheet sheet = workBook.createSheet();
			HSSFRow hssRow = null;
			int countR = 0, countC = 0;

			HSSFCellStyle cellStyle = workBook.createCellStyle();
			HSSFFont font = workBook.createFont();
			font.setBold(true);
			cellStyle.setFont(font);

			boolean isHeader = (report.get(0).size() == header.length) ? true : false;
			int i = 0;
			for (Iterator<List<String>> itr = report.iterator(); itr.hasNext(); i++) {
				hssRow = sheet.createRow((short) countR++);
				if (isHeader) {
					for (int cnt = 0; cnt < header.length; cnt++) {
						HSSFCell cell = hssRow.createCell(countC++);
						cell.setCellValue(header[cnt].toUpperCase());
						cell.setCellStyle(cellStyle);
					}
					isHeader = false;
					itr.next();
					countC = 0;
					continue;
				}
				Iterator<String> row = itr.next().iterator();
				while (row.hasNext()) {
					String next = row.next();
					HSSFCell cell = hssRow.createCell(countC++);
					if (i == 0) {
						cell.setCellValue(next.toUpperCase());
						cell.setCellStyle(cellStyle);
					}
					else {
						cell.setCellValue(next);
					}
				}
				countC = 0;
			}

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			workBook.write(outputStream);
			stream = new ByteArrayInputStream(outputStream.toByteArray());
		}
		catch (Exception e) {
			throw e;
		}
		return stream;
	}

	protected static InputStream getXLSXWorkBook(List<List<String>> report, String[] header, InputStream stream) throws Exception {
		try {
			XSSFWorkbook workBook = new XSSFWorkbook();
			XSSFSheet sheet = workBook.createSheet();
			XSSFRow hssRow = null;
			int countR = 0, countC = 0;

			XSSFCellStyle cellStyle = workBook.createCellStyle();
			XSSFFont font = workBook.createFont();
			font.setBold(true);
			cellStyle.setFont(font);

			boolean isHeader = (report.get(0).size() == header.length) ? true : false;
			int i = 0;
			for (Iterator<List<String>> itr = report.iterator(); itr.hasNext(); i++) {
				hssRow = sheet.createRow((short) countR++);
				if (isHeader) {
					for (int cnt = 0; cnt < header.length; cnt++) {
						XSSFCell cell = hssRow.createCell(countC++);
						cell.setCellValue(header[cnt].toUpperCase());
						cell.setCellStyle(cellStyle);
					}
					isHeader = false;
					itr.next();
					countC = 0;
					continue;
				}
				Iterator<String> row = itr.next().iterator();
				while (row.hasNext()) {
					String next = row.next();
					XSSFCell cell = hssRow.createCell(countC++);
					if (i == 0) {
						cell.setCellValue(next.toUpperCase());
						cell.setCellStyle(cellStyle);
					}
					else {
						cell.setCellValue(next);
					}
				}
				countC = 0;
			}

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			workBook.write(outputStream);
			stream = new ByteArrayInputStream(outputStream.toByteArray());
		}
		catch (Exception e) {
			throw e;
		}
		return stream;
	}

	protected static InputStream getDOC(List<List<String>> report, String[] header, InputStream stream) throws Exception {
		try {
			XWPFDocument document = new XWPFDocument();
			XWPFTable table = document.createTable(report.size(), report.get(0).size());
			table.setWidth(10000);
			boolean isHeader = (report.get(0).size() == header.length) ? true : false;
			int i = 0;
			for (Iterator<List<String>> itr = report.iterator(); itr.hasNext(); i++) {
				if (isHeader) {
					for (int cnt = 0; cnt < header.length; cnt++) {
						XWPFTableRow xwrow = table.getRow(i);
						xwrow.getCell(cnt).setText(header[cnt].toUpperCase());
					}
					isHeader = false;
					itr.next();
					continue;
				}
				int j = 0;
				List<String> list = itr.next();
				for (Iterator<String> next = list.iterator(); next.hasNext(); j++) {
					if (i == 0) {
						XWPFTableRow xwrow = table.getRow(i);
						xwrow.getCell(j).setText(next.next().toUpperCase());
					}
					else {
						XWPFTableRow xwrow = table.getRow(i);
						xwrow.getCell(j).setText(next.next());
					}
				}
			}
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			document.getXWPFDocument().write(outputStream);
			stream = new ByteArrayInputStream(outputStream.toByteArray());
		}
		catch (Exception e) {
			throw e;
		}
		return stream;
	}
}
