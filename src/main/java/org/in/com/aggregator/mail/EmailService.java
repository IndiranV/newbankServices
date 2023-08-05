package org.in.com.aggregator.mail;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.in.com.dto.AuditEventDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.NamespaceDeviceDTO;
import org.in.com.dto.OrderDTO;
import org.in.com.dto.RefundDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketTaxDTO;
import org.in.com.dto.TicketTransactionDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.UserFeedbackDTO;
import org.in.com.dto.UserRegistrationDTO;
import org.in.com.dto.UserTransactionDTO;

public interface EmailService {
	void sendBookingEmail(AuthDTO authDTO, TicketDTO ticketDTO);

	void sendForgetPasswordEmail(AuthDTO authDTO, UserDTO userDTO);

	void sendRegisterEmail(AuthDTO authDTO, UserDTO userDTO);

	void sendLowBalanceEmail(AuthDTO authDTO, BigDecimal amount);

	void sendFeedbackEmail(AuthDTO authDTO, UserFeedbackDTO feedbackDTO, String toEmail);

	public String sendReplyToFeedbackEmail(AuthDTO authDTO, UserFeedbackDTO userFeedback);

	void sendRegistrationRequestEmail(AuthDTO authDTO, UserRegistrationDTO registrationDTO);

	void sendDeviceRegistration(AuthDTO authDTO, NamespaceDeviceDTO deviceDTO);

	public void sendAuditEventAlertEmail(AuthDTO authDTO, Map<String, String> dataModel, AuditEventDTO auditEvent);

	void sendFailureOrderBookingEmail(AuthDTO authDTO, TicketDTO ticketDTO, Map<String, String> orderStatusMap);

	public void sendTripCancellEmail(AuthDTO authDTO, TicketDTO ticketDTO);

	void sendScheduleUpdateEmail(AuthDTO authDTO, List<Map<String, String>> finalScheduleChanges, String referenceCode, List<String> toEmailIds, List<String> ccEmailIds);

	public void sendS2SFailureBookingEmail(AuthDTO authDTO, TicketDTO ticketDTO);

	public void sendPaymentOrderVerificationFailureEmail(AuthDTO authDTO, OrderDTO orderDTO, Map<String, String> orderStatusMap);

	public void sendTaxInvoiceEmail(AuthDTO authDTO, TicketDTO ticketDTO, TicketTaxDTO ticketTaxDTO);

	public void sendCancelEmailV2(AuthDTO authDTO, TicketDTO ticketDTO);

	public void sendTransactionEmail(AuthDTO authDTO, List<UserTransactionDTO> userTransactionList);

	public void sendReportEmail(AuthDTO authDTO, String mailIds, String fileName, String url);

	public void sendDenialTicketEmail(AuthDTO authDTO, TicketDTO ticketDTO);

	public void sendPendingOrderCancelMail(AuthDTO authDTO, RefundDTO refundDTO, TicketDTO ticketDTO);

	public void sendMismatchTransactionEmail(AuthDTO authDTO, List<TicketTransactionDTO> ticketTransactionlist);

	public void sendEzeebotFeedbackMail(Map<String, String> dataModel);

	public void sendFailureSMSGatewayEmail(AuthDTO authDTO, Map<String, Object> dataModel);
}
