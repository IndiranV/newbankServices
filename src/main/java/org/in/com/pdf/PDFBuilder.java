package org.in.com.pdf;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import org.in.com.cache.CacheCentral;
import org.in.com.config.ApplicationConfig;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.CancellationPolicyDTO;
import org.in.com.dto.TermDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class PDFBuilder extends CacheCentral {

	public ByteArrayOutputStream buildPdfA4Document(AuthDTO authDTO, TicketDTO ticketDTO, List<TermDTO> terms) {
		// get data model which is passed by the Spring container
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			BaseFont arial = BaseFont.createFont("http://localhost:8080/busservices/fonts/arial.ttf", BaseFont.CP1252, BaseFont.NOT_EMBEDDED);

			BaseColor violet = new BaseColor(105, 118, 203);
			BaseColor darkGrey = new BaseColor(96, 96, 96);
			Font headerFont1 = new Font(arial, 18, Font.BOLD, violet);
			Font lineFont = new Font(arial, 8, Font.NORMAL, darkGrey);
			Font tableDataFont = new Font(arial, 9, Font.NORMAL, BaseColor.BLACK);
			Font tableDataStrike = new Font(arial, 9, Font.STRIKETHRU, BaseColor.BLACK);
			Font tablePnrFont = new Font(arial, 9, Font.NORMAL, BaseColor.BLACK);
			Font tableNameFont = new Font(arial, 9, Font.BOLD, BaseColor.DARK_GRAY);
			Font tableNameFont1 = new Font(arial, 9, Font.BOLD, BaseColor.DARK_GRAY);
			Font importantNotesFont = new Font(arial, 9, Font.BOLD, BaseColor.DARK_GRAY);
			Font importantNotes = new Font(arial, 9, Font.NORMAL, BaseColor.DARK_GRAY);
			Document document = new Document(PageSize.A4);
			document.setMargins(20, 20, 20, 20);
			PdfWriter.getInstance(document, baos);
			document.open();

			Image gif;
			try {
				gif = Image.getInstance("http://" + ApplicationConfig.getServerZoneUrl() + "/public/" + authDTO.getNamespace().getCode() + "/trip_chart_logo.jpg");
				gif.setAlignment(Image.ALIGN_TOP | Image.ALIGN_CENTER);
				gif.scaleToFit(750, 75);
				document.add(gif);
			}
			catch (FileNotFoundException exception) {
				System.out.println("SocketTimeoutException: " + "http://" + ApplicationConfig.getServerZoneUrl() + "/public/" + authDTO.getNamespace().getCode() + "/trip_chart_logo.jpg");
			}
			catch (Exception e) {
				System.out.println("SocketTimeout Exception: " + "http://" + ApplicationConfig.getServerZoneUrl() + "/public/" + authDTO.getNamespace().getCode() + "/trip_chart_logo.jpg");
			}

			Phrase titilePhrase = new Phrase("", headerFont1);

			PdfPCell titleCell = new PdfPCell(titilePhrase);
			titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			titleCell.setVerticalAlignment(Element.ALIGN_TOP);
			titleCell.setMinimumHeight(5f);

			PdfPTable titleTable = new PdfPTable(1);
			titleTable.setWidthPercentage(100);
			titleCell.setBorderWidth(1);
			titleCell.setBorder(0);
			titleTable.addCell(titleCell);
			document.add(titleTable);
			// -------------------------------------------------------------------------------------------

			PdfPTable tablePnr = new PdfPTable(1);
			tablePnr.setWidthPercentage(95);
			Phrase contactPhrase = new Phrase("", lineFont);
			PdfPCell cellCont = new PdfPCell(contactPhrase);
			cellCont.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
			cellCont.setVerticalAlignment(Element.ALIGN_TOP);
			cellCont.setBorderColor(darkGrey);
			cellCont.setBorder(1);
			tablePnr.addCell(cellCont);
			document.add(tablePnr);

			// -------------------------------------------------------------------------------------------
			PdfPTable tableHead = new PdfPTable(4);
			tableHead.setWidths(new float[] { 15f, 35f, 17f, 33f });
			tableHead.setWidthPercentage(95);

			Phrase pnrPhrase = new Phrase("\nPNR No\n", tableNameFont);
			PdfPCell cellPnr = new PdfPCell(pnrPhrase);
			cellPnr.setHorizontalAlignment(Element.ALIGN_LEFT);
			cellPnr.setVerticalAlignment(Element.ALIGN_TOP);
			cellPnr.setBorderColor(darkGrey);
			titleCell.setBorderWidth(1);
			cellPnr.setBorder(0);
			tableHead.addCell(cellPnr);

			Phrase pnrPhrase3 = new Phrase("\n:   " + ticketDTO.getCode() + "\n", tablePnrFont);
			PdfPCell cellPnr3 = new PdfPCell(pnrPhrase3);
			cellPnr3.setHorizontalAlignment(Element.ALIGN_LEFT);
			cellPnr3.setVerticalAlignment(Element.ALIGN_TOP);
			cellPnr3.setBorderColor(darkGrey);
			titleCell.setBorderWidth(1);
			cellPnr3.setBorder(0);
			tableHead.addCell(cellPnr3);

			Phrase pointPhrase1 = new Phrase("\nTravel Date/Time\n", tableNameFont);
			PdfPCell cellDate = new PdfPCell(pointPhrase1);
			cellDate.setHorizontalAlignment(Element.ALIGN_LEFT);
			cellDate.setVerticalAlignment(Element.ALIGN_TOP);
			cellDate.setBorderColor(darkGrey);
			titleCell.setBorderWidth(1);
			cellDate.setBorder(0);
			tableHead.addCell(cellDate);

			Phrase pointPhrase4 = new Phrase("\n:   " + ticketDTO.getTripDate().format("DD-MMM-YYYY", Locale.ENGLISH) + " , " + ticketDTO.getTripTime() + "\n", tableDataFont);
			PdfPCell cellDate1 = new PdfPCell(pointPhrase4);
			cellDate1.setHorizontalAlignment(Element.ALIGN_LEFT);
			cellDate1.setVerticalAlignment(Element.ALIGN_TOP);
			cellDate1.setBorderColor(darkGrey);
			cellDate1.setBorderWidth(1);
			cellDate1.setBorder(0);
			tableHead.addCell(cellDate1);

			document.add(tableHead);
			// ----------------------------------------------------------------------------------------------------------------------------------------
			PdfPTable tableHead1 = new PdfPTable(4);
			tableHead1.setWidths(new float[] { 15f, 35f, 17f, 33f });
			tableHead1.setWidthPercentage(95);

			Phrase routePhrase = new Phrase("\nRoute\n", tableNameFont);
			PdfPCell cellRoute1 = new PdfPCell(routePhrase);
			cellRoute1.setHorizontalAlignment(Element.ALIGN_LEFT);
			cellRoute1.setVerticalAlignment(Element.ALIGN_TOP);
			cellRoute1.setBorderColor(darkGrey);
			cellRoute1.setBorderWidth(0);
			cellRoute1.setBorder(0);
			tableHead1.addCell(cellRoute1);

			Phrase routePhrase1 = new Phrase("\n:   " + ticketDTO.getFromStation().getName() + " - " + ticketDTO.getDroppingPoint().getName() + "\n", tableDataFont);
			PdfPCell cellRoute2 = new PdfPCell(routePhrase1);
			cellRoute2.setHorizontalAlignment(Element.ALIGN_LEFT);
			cellRoute2.setVerticalAlignment(Element.ALIGN_TOP);
			cellRoute2.setBorderColor(darkGrey);
			cellRoute2.setBorderWidth(0);
			cellRoute2.setBorder(0);
			tableHead1.addCell(cellRoute2);

			Phrase busPhrase1 = new Phrase("\nBus type\n", tableNameFont);
			PdfPCell busCell = new PdfPCell(busPhrase1);
			busCell.setHorizontalAlignment(Element.ALIGN_LEFT);
			busCell.setVerticalAlignment(Element.ALIGN_TOP);
			busCell.setBorderColor(darkGrey);
			busCell.setBorderWidth(0);
			busCell.setBorder(0);
			tableHead1.addCell(busCell);

			Phrase busPhrase2 = new Phrase("\n:   " + ticketDTO.getTripDTO().getBus().getName() + "\n", tableDataFont);
			PdfPCell busCell1 = new PdfPCell(busPhrase2);
			busCell1.setHorizontalAlignment(Element.ALIGN_LEFT);
			busCell1.setVerticalAlignment(Element.ALIGN_TOP);
			busCell1.setBorderColor(darkGrey);
			busCell1.setBorderWidth(1);
			busCell1.setBorder(0);
			tableHead1.addCell(busCell1);

			document.add(tableHead1);

			// --------------------------------------------------------------------------------------------
			PdfPTable tablePnr1 = new PdfPTable(4);
			tablePnr1.setWidths(new float[] { 15f, 35f, 17f, 33f });
			tablePnr1.setWidthPercentage(95);

			Phrase pnrPhrase2a = new Phrase("\nBoarding Info\n", tableNameFont);
			PdfPCell cellPnr2a = new PdfPCell(pnrPhrase2a);
			cellPnr2a.setHorizontalAlignment(Element.ALIGN_LEFT);
			cellPnr2a.setVerticalAlignment(Element.ALIGN_TOP);
			cellPnr2a.setBorder(0);
			tablePnr1.addCell(cellPnr2a);

			Phrase pnrPhrase2 = new Phrase("\n:   " + ticketDTO.getBoardingPoint().getLandmark() + ",\n", tableDataFont);
			PdfPCell cellPnr2 = new PdfPCell(pnrPhrase2);
			cellPnr2.setHorizontalAlignment(Element.ALIGN_LEFT);
			cellPnr2.setVerticalAlignment(Element.ALIGN_TOP);
			cellPnr2.setBorder(0);
			tablePnr1.addCell(cellPnr2);

			Phrase contactPhrase1a = new Phrase("\nAlightening Info\n", tableNameFont);
			PdfPCell cellCont1a = new PdfPCell(contactPhrase1a);
			cellCont1a.setHorizontalAlignment(Element.ALIGN_LEFT);
			cellCont1a.setVerticalAlignment(Element.ALIGN_TOP);
			cellCont1a.setBorder(0);
			tablePnr1.addCell(cellCont1a);

			Phrase contactPhrase1 = new Phrase("\n:   " + ticketDTO.getDroppingPoint().getLandmark() + ",\n", tableDataFont);
			PdfPCell cellCont1 = new PdfPCell(contactPhrase1);
			cellCont1.setHorizontalAlignment(Element.ALIGN_LEFT);
			cellCont1.setVerticalAlignment(Element.ALIGN_TOP);
			cellCont1.setBorder(0);
			tablePnr1.addCell(cellCont1);

			document.add(tablePnr1);
			// -------------------------------------------------------------------------------------------------------------

			PdfPTable tableAddress = new PdfPTable(4);
			tableAddress.setWidths(new float[] { 15f, 35f, 17f, 33f });
			tableAddress.setWidthPercentage(95);

			Phrase addressPhrase1 = new Phrase("", tableNameFont);
			PdfPCell addressCell1 = new PdfPCell(addressPhrase1);
			addressCell1.setHorizontalAlignment(Element.ALIGN_LEFT);
			addressCell1.setVerticalAlignment(Element.ALIGN_TOP);
			addressCell1.setBorder(0);
			tableAddress.addCell(addressCell1);

			Phrase addressPhrase2 = new Phrase("    " + ticketDTO.getBoardingPoint().getName() + "\n", tableDataFont);
			PdfPCell addressCell2 = new PdfPCell(addressPhrase2);
			addressCell2.setHorizontalAlignment(Element.ALIGN_LEFT);
			addressCell2.setVerticalAlignment(Element.ALIGN_TOP);
			addressCell2.setBorder(0);
			tableAddress.addCell(addressCell2);

			Phrase addressPhrase3 = new Phrase("", tableNameFont);
			PdfPCell addressCell3 = new PdfPCell(addressPhrase3);
			addressCell3.setHorizontalAlignment(Element.ALIGN_LEFT);
			addressCell3.setVerticalAlignment(Element.ALIGN_TOP);
			addressCell3.setBorder(0);
			tableAddress.addCell(addressCell3);

			Phrase addressPhrase4 = new Phrase("    " + ticketDTO.getDroppingPoint().getName() + "\n", tableDataFont);
			PdfPCell addressCell4 = new PdfPCell(addressPhrase4);
			addressCell4.setHorizontalAlignment(Element.ALIGN_LEFT);
			addressCell4.setVerticalAlignment(Element.ALIGN_TOP);
			addressCell4.setBorder(0);
			tableAddress.addCell(addressCell4);

			document.add(tableAddress);

			// ------------------------------------------------------------------------------------------------------------

			PdfPTable tableMain3 = new PdfPTable(4);
			tableMain3.setWidths(new float[] { 15f, 35f, 17f, 33f });
			tableMain3.setWidthPercentage(95);

			Phrase timePhrase7 = new Phrase("\nPassenger Mobile\n\n", tableNameFont);
			PdfPCell cellTime7 = new PdfPCell(timePhrase7);
			cellTime7.setHorizontalAlignment(Element.ALIGN_LEFT);
			cellTime7.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cellTime7.setBorder(0);
			tableMain3.addCell(cellTime7);

			Phrase timePhrase8 = new Phrase("\n:   " + ticketDTO.getPassengerMobile() + "\n\n", tableDataFont);
			PdfPCell cellTime8 = new PdfPCell(timePhrase8);
			cellTime8.setHorizontalAlignment(Element.ALIGN_LEFT);
			cellTime8.setVerticalAlignment(Element.ALIGN_TOP);
			cellTime8.setBorder(0);
			tableMain3.addCell(cellTime8);

			Phrase timePhrase9 = new Phrase("\nBooked At\n\n", tableNameFont);
			PdfPCell cellTime9 = new PdfPCell(timePhrase9);
			cellTime9.setHorizontalAlignment(Element.ALIGN_LEFT);
			cellTime9.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cellTime9.setBorder(0);
			tableMain3.addCell(cellTime9);

			Phrase alightPhrase4 = new Phrase("\n:   " + ticketDTO.getTicketAt().format("DD-MMM-YYYY , hh:mm", Locale.ENGLISH) + "\n\n", tableDataFont);
			PdfPCell cellAlight4 = new PdfPCell(alightPhrase4);
			cellAlight4.setHorizontalAlignment(Element.ALIGN_LEFT);
			cellAlight4.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cellAlight4.setBorder(0);
			tableMain3.addCell(cellAlight4);
			document.add(tableMain3);

			// ---------------------------------------------------------------------------------------------------------------

			PdfPTable table = new PdfPTable(6);
			table.setWidthPercentage(95);

			titilePhrase = new Phrase("Passenger Name\n", tableNameFont1);
			PdfPCell cell = new PdfPCell(titilePhrase);
			cell.setBorder(1);
			// cell.setBackgroundColor(lightgrey);
			cell.setBorderColor(darkGrey);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cell.addElement(titilePhrase);
			table.addCell(cell);

			titilePhrase = new Phrase("Seat No\n", tableNameFont1);
			cell = new PdfPCell(titilePhrase);
			cell.setBorder(1);
			// cell.setBackgroundColor(lightgrey);
			cell.setBorderColor(darkGrey);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cell.addElement(titilePhrase);
			table.addCell(cell);

			titilePhrase = new Phrase("Gender\n", tableNameFont1);
			cell = new PdfPCell(titilePhrase);
			cell.setBorder(1);
			// cell.setBackgroundColor(lightgrey);
			cell.setBorderColor(darkGrey);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cell.addElement(titilePhrase);
			table.addCell(cell);

			titilePhrase = new Phrase("Age\n", tableNameFont1);
			cell = new PdfPCell(titilePhrase);
			cell.setBorder(1);
			// cell.setBackgroundColor(lightgrey);
			cell.setBorderColor(darkGrey);
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.addElement(titilePhrase);
			table.addCell(cell);

			titilePhrase = new Phrase("Status\n", tableNameFont1);
			cell = new PdfPCell(titilePhrase);
			cell.setBorder(1);
			// cell.setBackgroundColor(lightgrey);
			cell.setBorderColor(darkGrey);
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.addElement(titilePhrase);
			table.addCell(cell);

			titilePhrase = new Phrase("Fare\n", tableNameFont1);
			cell = new PdfPCell(titilePhrase);
			cell.setBorder(1);
			// cell.setBackgroundColor(lightgrey);
			cell.setBorderColor(darkGrey);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cell.addElement(titilePhrase);
			table.addCell(cell);

			BigDecimal total = new BigDecimal(0);
			if (ticketDTO.getTicketDetails() != null) {
				for (TicketDetailsDTO dto : ticketDTO.getTicketDetails()) {
					if (dto.getTicketStatus().getCode() == "CA" || dto.getTicketStatus().getCode() == "PCA") {
						titilePhrase = new Phrase(dto.getPassengerName(), tableDataStrike);
						cell = new PdfPCell(titilePhrase);
						cell.setBorder(1);
						cell.setBorderColor(darkGrey);
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.addElement(titilePhrase);
						table.addCell(cell);
					}
					else {
						titilePhrase = new Phrase(dto.getPassengerName(), tableDataFont);
						cell = new PdfPCell(titilePhrase);
						cell.setBorder(1);
						cell.setBorderColor(darkGrey);
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.addElement(titilePhrase);
						table.addCell(cell);
					}
					if (dto.getTicketStatus().getCode() == "CA" || dto.getTicketStatus().getCode() == "PCA") {
						titilePhrase = new Phrase(dto.getSeatName(), tableDataStrike);
						cell = new PdfPCell(titilePhrase);
						cell.setBorderColor(darkGrey);
						cell.setBorder(1);
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.addElement(titilePhrase);
						table.addCell(cell);
					}
					else {
						titilePhrase = new Phrase(dto.getSeatName(), tableDataFont);
						cell = new PdfPCell(titilePhrase);
						cell.setBorderColor(darkGrey);
						cell.setBorder(1);
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.addElement(titilePhrase);
						table.addCell(cell);
					}
					if (dto.getTicketStatus().getCode() == "CA" || dto.getTicketStatus().getCode() == "PCA") {
						titilePhrase = new Phrase(dto.getSeatGendar().getCode(), tableDataStrike);
						cell = new PdfPCell(titilePhrase);
						cell.setBorderColor(darkGrey);
						cell.setBorder(1);
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.addElement(titilePhrase);
						table.addCell(cell);
					}
					else {
						titilePhrase = new Phrase(dto.getSeatGendar().getCode(), tableDataFont);
						cell = new PdfPCell(titilePhrase);
						cell.setBorderColor(darkGrey);
						cell.setBorder(1);
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.addElement(titilePhrase);
						table.addCell(cell);
					}
					if (dto.getTicketStatus().getCode() == "CA" || dto.getTicketStatus().getCode() == "PCA") {
						titilePhrase = new Phrase("" + dto.getPassengerAge(), tableDataStrike);
						PdfPCell cell1 = new PdfPCell(titilePhrase);
						cell1.setBorderColor(darkGrey);
						cell1.setBorder(1);
						cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
						cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell1.addElement(titilePhrase);
						table.addCell(cell1);
					}
					else {
						titilePhrase = new Phrase("" + dto.getPassengerAge(), tableDataFont);
						PdfPCell cell1 = new PdfPCell(titilePhrase);
						cell1.setBorderColor(darkGrey);
						cell1.setBorder(1);
						cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
						cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell1.addElement(titilePhrase);
						table.addCell(cell1);
					}
					if (dto.getTicketStatus().getCode() == "CA" || dto.getTicketStatus().getCode() == "PCA") {
						titilePhrase = new Phrase("" + dto.getTicketStatus().getCode(), tableDataStrike);
						PdfPCell cell2 = new PdfPCell(titilePhrase);
						cell2.setBorderColor(darkGrey);
						cell2.setBorder(1);
						cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
						cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell2.addElement(titilePhrase);
						table.addCell(cell2);
					}
					else {
						titilePhrase = new Phrase("" + dto.getTicketStatus().getCode(), tableDataFont);
						PdfPCell cell2 = new PdfPCell(titilePhrase);
						cell2.setBorderColor(darkGrey);
						cell2.setBorder(1);
						cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
						cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell2.addElement(titilePhrase);
						table.addCell(cell2);
					}
					if (dto.getTicketStatus().getCode() == "CA" || dto.getTicketStatus().getCode() == "PCA") {
						titilePhrase = new Phrase("" + dto.getSeatFare(), tableDataStrike);

						// titilePhrase = new Phrase("" + dto.getAcBusTax(),
						// tableHeaderFont);

						PdfPCell cell3 = new PdfPCell(titilePhrase);
						cell3.setBorderColor(darkGrey);
						cell3.setBorder(1);
						cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
						cell3.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell3.addElement(titilePhrase);
						table.addCell(cell3);
					}
					else {
						titilePhrase = new Phrase("" + dto.getSeatFare(), tableDataFont);

						// titilePhrase = new Phrase("" + dto.getAcBusTax(),
						// tableHeaderFont);

						PdfPCell cell3 = new PdfPCell(titilePhrase);
						cell3.setBorderColor(darkGrey);
						cell3.setBorder(1);
						cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
						cell3.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell3.addElement(titilePhrase);
						table.addCell(cell3);
					}

					total = total.add(dto.getSeatFare());
					// total =
					// total.add(dto.getSeatFare().add(dto.getServiceTax()));
					// total =
					// total.add(dto.getSeatFare().add(dto.getAcBusTax()));

					// System.out.println("" + total);
				}
			}
			document.add(table);

			PdfPTable tableSpace2 = new PdfPTable(1);
			tableSpace2.setWidthPercentage(95);

			Phrase spacePhrase2 = new Phrase();
			PdfPCell cellSpace2 = new PdfPCell(spacePhrase2);
			cellSpace2.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
			cellSpace2.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cellSpace2.setBorder(0);
			tableSpace2.addCell(cellSpace2);
			document.add(tableSpace2);

			PdfPTable sumTable = new PdfPTable(1);
			sumTable.setWidthPercentage(95);

			Phrase titile2Phrase = new Phrase("\nTotal Fare : Rs." + total, tableDataFont);
			PdfPCell sumTitle2 = new PdfPCell(titile2Phrase);
			sumTitle2.setBorderColor(darkGrey);
			sumTitle2.setHorizontalAlignment(Element.ALIGN_RIGHT);
			sumTitle2.setVerticalAlignment(Element.ALIGN_MIDDLE);
			sumTitle2.setBorder(1);
			sumTable.addCell(sumTitle2);
			document.add(sumTable);

			titleTable = new PdfPTable(1);
			titleTable.setWidthPercentage(95);
			document.add(new Phrase("\n\n    Cancellation Policy:", importantNotesFont));

			titleCell.setBorder(1);
			document.add(titleTable);

			PdfPTable cancelTable = new PdfPTable(4);
			cancelTable.setWidthPercentage(95);
			cancelTable.setWidths(new float[] { 44f, 19f, 19f, 19f });
			Phrase cancelPhrase = new Phrase("\n\n Timing", importantNotesFont);
			PdfPCell cancelCell = new PdfPCell(cancelPhrase);
			cancelCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cancelCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cancelCell.setMinimumHeight(15f);
			cancelCell.setBorderWidth(0);
			cancelCell.setBorder(0);
			cancelTable.addCell(cancelCell);

			Phrase cancelPhrase1 = new Phrase("\n\n Deduction", importantNotesFont);
			PdfPCell cancelCell1 = new PdfPCell(cancelPhrase1);
			cancelCell1.setHorizontalAlignment(Element.ALIGN_CENTER);
			cancelCell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cancelCell1.setMinimumHeight(15f);
			cancelCell1.setBorderWidth(0);
			cancelCell1.setBorder(0);
			cancelTable.addCell(cancelCell1);

			Phrase cancelPhrase2 = new Phrase("\n\n Refund", importantNotesFont);
			PdfPCell cancelCell2 = new PdfPCell(cancelPhrase2);
			cancelCell2.setHorizontalAlignment(Element.ALIGN_CENTER);
			cancelCell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cancelCell2.setMinimumHeight(15f);
			cancelCell2.setBorderWidth(0);
			cancelCell2.setBorder(0);
			cancelTable.addCell(cancelCell2);

			Phrase cancelPhrase3 = new Phrase("\n\n Charges", importantNotesFont);
			PdfPCell cancelCell3 = new PdfPCell(cancelPhrase3);
			cancelCell3.setHorizontalAlignment(Element.ALIGN_CENTER);
			cancelCell3.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cancelCell3.setMinimumHeight(15f);
			cancelCell3.setBorderWidth(0);
			cancelCell3.setBorder(0);
			cancelTable.addCell(cancelCell3);

			List<CancellationPolicyDTO> cancellationTerms = ticketDTO.getCancellationTerm().getPolicyList();
			if (cancellationTerms != null && !cancellationTerms.isEmpty()) {
				for (CancellationPolicyDTO cancellationDesc : cancellationTerms) {
					titilePhrase = new Phrase(cancellationDesc.getTerm(), importantNotes);
					titleCell = new PdfPCell(titilePhrase);
					titleCell.setHorizontalAlignment(Element.ALIGN_LEFT);
					titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					titleCell.setMinimumHeight(15f);
					titleCell.setBorderWidth(0);
					titleCell.setBorder(0);
					cancelTable.addCell(titleCell);

					titilePhrase = new Phrase(cancellationDesc.getDeductionAmountTxt(), importantNotes);
					titleCell = new PdfPCell(titilePhrase);
					titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
					titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					titleCell.setMinimumHeight(15f);
					titleCell.setBorderWidth(0);
					titleCell.setBorder(0);
					cancelTable.addCell(titleCell);

					titilePhrase = new Phrase(cancellationDesc.getRefundAmountTxt(), importantNotes);
					titleCell = new PdfPCell(titilePhrase);
					titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
					titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					titleCell.setMinimumHeight(15f);
					titleCell.setBorderWidth(0);
					titleCell.setBorder(0);
					cancelTable.addCell(titleCell);

					titilePhrase = new Phrase(cancellationDesc.getChargesTxt(), importantNotes);
					titleCell = new PdfPCell(titilePhrase);
					titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
					titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					titleCell.setMinimumHeight(15f);
					titleCell.setBorderWidth(0);
					titleCell.setBorder(0);
					cancelTable.addCell(titleCell);
				}
				document.add(cancelTable);
			}
			if (terms != null && !terms.isEmpty()) {
				titleTable = new PdfPTable(1);
				titleTable.setWidthPercentage(95);
				document.add(new Phrase("\n"));
				document.add(new Phrase("    Terms:", importantNotesFont));
				titleCell.setBorder(0);
				for (TermDTO termDesc : terms) {
					titilePhrase = new Phrase(termDesc.getName(), importantNotes);
					titleCell = new PdfPCell(titilePhrase);
					titleCell.setHorizontalAlignment(Element.ALIGN_LEFT);
					titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					titleCell.setMinimumHeight(15f);
					titleCell.setBorderWidth(0);
					titleTable.addCell(titleCell);
				}
				document.add(titleTable);
			}
			document.close();
		}
		catch (Exception e) {
			System.out.println("PDF ERPDF01 - " + ticketDTO.getCode());
			e.printStackTrace();
		}
		return baos;

	}

	public ByteArrayOutputStream buildPdfA6Document(AuthDTO authDTO, TicketDTO ticketDTO) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			String myServerURLActual = "http://localhost:8080/";
			BaseFont verdana;
			verdana = BaseFont.createFont(myServerURLActual + "busservices/fonts/verdana.ttf", BaseFont.CP1252, BaseFont.NOT_EMBEDDED);

			BaseColor color = new BaseColor(255, 255, 255);
			BaseColor black = new BaseColor(0, 0, 0);
			Font tableHeaderFont = new Font(verdana, 11, Font.NORMAL, color);
			Font tableDataFont = new Font(verdana, 11, Font.NORMAL, black);

			Rectangle pageSize = new Rectangle(0, 0, 648, 288); // - 9

			Document document = new Document(pageSize);
			document.setMargins(60, 20, 90, 20);
			PdfWriter.getInstance(document, baos);
			document.open();

			PdfPTable table = new PdfPTable(3);
			table.setWidthPercentage(100);
			float[] widths1 = { 0.23f, 0.02f, 0.75f };
			table.setWidths(widths1);

			PdfPCell cell = new PdfPCell(new Paragraph("PNR No", tableDataFont));
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setFixedHeight(15);
			cell.setBorder(0);
			table.addCell(cell);

			cell = new PdfPCell(new Paragraph(":", tableDataFont));
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setBorder(0);
			table.addCell(cell);

			cell = new PdfPCell(new Paragraph(ticketDTO.getCode(), tableHeaderFont));
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setBorder(0);
			table.addCell(cell);

			cell = new PdfPCell(new Paragraph("Trip", tableDataFont));
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setFixedHeight(15);
			cell.setBorder(0);
			table.addCell(cell);

			cell = new PdfPCell(new Paragraph(":", tableDataFont));
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setBorder(0);
			table.addCell(cell);

			cell = new PdfPCell(new Paragraph(ticketDTO.getFromStation().getName() + " - " + ticketDTO.getToStation().getName() + "-" + ticketDTO.getBoardingPointDateTime(), tableHeaderFont));
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setBorder(0);
			table.addCell(cell);

			cell = new PdfPCell(new Paragraph("Bus Type", tableDataFont));
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setFixedHeight(15);
			cell.setBorder(0);
			table.addCell(cell);

			cell = new PdfPCell(new Paragraph(":", tableDataFont));
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setBorder(0);
			table.addCell(cell);

			cell = new PdfPCell(new Paragraph(ticketDTO.getTripDTO().getBus().getDisplayName(), tableHeaderFont));
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setBorder(0);
			table.addCell(cell);

			cell = new PdfPCell(new Paragraph("Booked On", tableDataFont));
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setFixedHeight(15);
			cell.setBorder(0);
			table.addCell(cell);

			cell = new PdfPCell(new Paragraph(":", tableDataFont));
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setBorder(0);
			table.addCell(cell);

			cell = new PdfPCell(new Paragraph(ticketDTO.getTicketAt().format("DD/MM/YYYY hh12:mm a", Locale.forLanguageTag("en_IN")), tableHeaderFont));
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setBorder(0);
			table.addCell(cell);

			// Booked by is added in mini ticket pdf : Jero (31.01.2013)
			/**
			 * @author JERO
			 * @description new row Booked by is added in the mini ticket
			 */

			cell = new PdfPCell(new Paragraph("Booked by", tableDataFont));
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setFixedHeight(15);
			cell.setBorder(0);
			table.addCell(cell);

			cell = new PdfPCell(new Paragraph(":", tableDataFont));
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setBorder(0);
			table.addCell(cell);

			// String bookedby = (ticketDTO.getUserTransactionDTO().getName());
			String bookedby = "test";
			cell = new PdfPCell(new Paragraph(bookedby, tableHeaderFont));
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setBorder(0);
			table.addCell(cell);

			cell = new PdfPCell(new Paragraph("Boarding", tableDataFont));
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setFixedHeight(15);
			cell.setBorder(0);
			table.addCell(cell);

			cell = new PdfPCell(new Paragraph(":", tableDataFont));
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setBorder(0);
			table.addCell(cell);

			cell = new PdfPCell(new Paragraph(ticketDTO.getBoardingPoint().getName(), tableHeaderFont));
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setBorder(0);
			table.addCell(cell);
			String seat_no = null;
			BigDecimal fare = new BigDecimal(0);
			String name = null;
			for (TicketDetailsDTO dto : ticketDTO.getTicketDetails()) {

				if (seat_no == null) {
					seat_no = dto.getSeatName();
				}
				else {
					seat_no += "," + dto.getSeatName();
				}

				fare = fare.add(dto.getSeatFare());
				if (name == null) {
					name = dto.getPassengerName();
				}
			}

			cell = new PdfPCell(new Paragraph("Seat No", tableDataFont));
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setFixedHeight(15);
			cell.setBorder(0);
			table.addCell(cell);

			cell = new PdfPCell(new Paragraph(":", tableDataFont));
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setBorder(0);
			table.addCell(cell);

			cell = new PdfPCell(new Paragraph(seat_no, tableHeaderFont));
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setBorder(0);
			table.addCell(cell);

			cell = new PdfPCell(new Paragraph("Name", tableDataFont));
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setFixedHeight(15);
			cell.setBorder(0);
			table.addCell(cell);

			cell = new PdfPCell(new Paragraph(":", tableDataFont));
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setBorder(0);
			table.addCell(cell);

			cell = new PdfPCell(new Paragraph(name, tableHeaderFont));
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setBorder(0);
			table.addCell(cell);

			cell = new PdfPCell(new Paragraph("Fare", tableDataFont));
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setFixedHeight(15);
			cell.setBorder(0);
			table.addCell(cell);

			cell = new PdfPCell(new Paragraph(":", tableDataFont));
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setBorder(0);
			table.addCell(cell);

			cell = new PdfPCell(new Paragraph(fare + "0", tableHeaderFont));
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setBorder(0);
			table.addCell(cell);

			document.add(table);

			document.close();

		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return baos;
	}

}