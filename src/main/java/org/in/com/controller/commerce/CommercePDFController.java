package org.in.com.controller.commerce;

import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.in.com.controller.web.BaseController;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.TermDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.enumeration.TransactionTypeEM;
import org.in.com.pdf.PDFBuilder;
import org.in.com.service.AuthService;
import org.in.com.service.BusService;
import org.in.com.service.CancellationTermsService;
import org.in.com.service.TermsService;
import org.in.com.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/{authtoken}/print")
public class CommercePDFController extends BaseController {

	@Autowired
	AuthService authService;
	@Autowired
	TicketService ticketService;
	@Autowired
	TermsService termsService;
	@Autowired
	BusService busService;
	@Autowired
	CancellationTermsService cancellationService;

	@RequestMapping(value = "/pdf/A4/{ticketCode}", method = RequestMethod.GET)
	public void printPdf(HttpServletResponse httpResponse, HttpServletRequest request, @PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ServletOutputStream outStream = null;
		if (authDTO != null) {
			try {
				outStream = httpResponse.getOutputStream();
				TicketDTO ticketDTO = new TicketDTO();
				ticketDTO.setCode(ticketCode);
				ticketService.showTicket(authDTO, ticketDTO);
				ticketDTO.getTripDTO().getBus().setName(busService.getBusCategoryByCode(ticketDTO.getTripDTO().getBus().getCategoryCode()));
				PDFBuilder pdfBuilder = new PDFBuilder();
				TermDTO termDTO = new TermDTO();
				termDTO.setTransactionType(TransactionTypeEM.TICKETS_BOOKING);
				List<TermDTO> terms = termsService.get(authDTO, termDTO);
				ByteArrayOutputStream ticketPDFByteArray = pdfBuilder.buildPdfA4Document(authDTO, ticketDTO, terms);
				httpResponse.setContentType("application/pdf");
				httpResponse.setContentLength(ticketPDFByteArray.size());
				outStream.write(ticketPDFByteArray.toByteArray());
				outStream.close();
			}
			catch (Exception e) {
				System.out.println("PDF Download Error: " + ticketCode);
			}
		}
	}

	@RequestMapping(value = "/pdf/A6/{ticketCode}", method = RequestMethod.GET)
	public void printPdfMini(HttpServletResponse httpResponse, HttpServletRequest request, @PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ServletOutputStream outStream = null;
		if (authDTO != null) {
			try {
				outStream = httpResponse.getOutputStream();
				TicketDTO ticketDTO = new TicketDTO();
				ticketDTO.setCode(ticketCode);
				ticketService.showTicket(authDTO, ticketDTO);

				PDFBuilder pdfBuilder = new PDFBuilder();
				ByteArrayOutputStream ticketPDFByteArray = pdfBuilder.buildPdfA6Document(authDTO, ticketDTO);
				httpResponse.setContentType("application/pdf");
				httpResponse.setContentLength(ticketPDFByteArray.size());
				outStream.write(ticketPDFByteArray.toByteArray());
				outStream.close();
			}
			catch (Exception e) {
				System.out.println("PDF Download Error: " + ticketCode);
			}
		}
	}
}
