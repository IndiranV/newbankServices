package org.in.com.aggregator.event;

import java.util.HashMap;
import java.util.Map;

import org.in.com.aggregator.mail.EmailService;
import org.in.com.aggregator.sms.SMSService;
import org.in.com.dto.AuditEventDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.DiscountCriteriaDTO;
import org.in.com.dto.PaymentTransactionDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleFareAutoOverrideDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.AuditEventTypeEM;
import org.in.com.service.AuditEventService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AuditEventAlertImpl implements AuditEventAlertService {
	@Autowired
	EmailService emailService;
	@Autowired
	AuditEventService namespaceEventService;

	@Async
	public void getNamespaceEventAlertImpl(AuthDTO authDTO, Object object, AuditEventTypeEM namespaceEvent) {
		AuditEventDTO auditEvent = new AuditEventDTO();
		auditEvent.setNamespaceEventType(namespaceEvent);
		namespaceEventService.getNamespaceEvent(authDTO, auditEvent);

		Map<String, String> dataModel = new HashMap<String, String>();
		try {
			if (namespaceEvent.getId() == AuditEventTypeEM.USER_EVENT.getId()) {
				UserDTO userDTO = (UserDTO) object;
				dataModel.put("name", userDTO.getName());
			}
			else if (namespaceEvent.getId() == AuditEventTypeEM.BUS_EVENT.getId()) {
				BusDTO busDTO = (BusDTO) object;
				dataModel.put("name", busDTO.getName());
			}
			else if (namespaceEvent.getId() == AuditEventTypeEM.DISCOUNT_EVENT.getId()) {
				DiscountCriteriaDTO discountCriteria = new DiscountCriteriaDTO();
				dataModel.put("forUserGroup", discountCriteria.getGroupList().toString());
				dataModel.put("deviceMedium", discountCriteria.getDeviceMediumCodes().toString());
				dataModel.put("dateRange", discountCriteria.getActiveFrom() + "-" + discountCriteria.getActiveTo());
				dataModel.put("discount", String.valueOf(discountCriteria.getValue()));
				dataModel.put("maxDiscountAmount", String.valueOf(discountCriteria.getMaxDiscountAmount()));
				dataModel.put("minTicketFare", String.valueOf(discountCriteria.getMinTicketFare()));
				dataModel.put("maxUsageLimitPerUser", String.valueOf(discountCriteria.getMaxUsageLimitPerUser()));
			}
			else if (namespaceEvent.getId() == AuditEventTypeEM.SCHEDULE_EVENT.getId()) {
				ScheduleDTO schedule = (ScheduleDTO) object;
				dataModel.put("serviceNumber", schedule.getServiceNumber());
			}
			else if (namespaceEvent.getId() == AuditEventTypeEM.SCHEDULE_FARE.getId()) {
				ScheduleFareAutoOverrideDTO scheduleFareAutoOverride = (ScheduleFareAutoOverrideDTO) object;
				dataModel.put("serviceNumber", scheduleFareAutoOverride.getSchedule().getServiceNumber());
				dataModel.put("dateRange", scheduleFareAutoOverride.getActiveFrom() + "-" + scheduleFareAutoOverride.getActiveTo());
				dataModel.put("seatFare", String.valueOf(scheduleFareAutoOverride.getFare()));
//				dataModel.put("fareOverrideType", scheduleFareAutoOverride.getFareOverrideType().getCode());
			}
			else if (namespaceEvent.getId() == AuditEventTypeEM.TICKET_BOOK_EVENT.getId()) {
				TicketDTO ticket = (TicketDTO) object;
				dataModel.put("ticketCode", ticket.getBookingCode());
				dataModel.put("serviceNo", ticket.getServiceNo());
				dataModel.put("travelDate", ticket.getTripDate().format("YYYY-MM-DD"));
				dataModel.put("deviceMedium", ticket.getDeviceMedium().getCode());
			}
			else if (namespaceEvent.getId() == AuditEventTypeEM.TICKET_CANCEL_EVENT.getId()) {
				TicketDTO ticket = (TicketDTO) object;
				dataModel.put("ticketCode", ticket.getBookingCode());
				dataModel.put("serviceNo", ticket.getServiceNo());
				dataModel.put("travelDate", ticket.getTripDate().format("YYYY-MM-DD"));
				dataModel.put("deviceMedium", ticket.getDeviceMedium().getCode());
			}
			else if (namespaceEvent.getId() == AuditEventTypeEM.TRIP_EVENT.getId()) {
				TripDTO trip = (TripDTO) object;
				dataModel.put("code", trip.getCode());
			}
			else if (namespaceEvent.getId() == AuditEventTypeEM.USER_PAYMENT_EVENT.getId()) {
				PaymentTransactionDTO paymentTransaction = (PaymentTransactionDTO) object;
				dataModel.put("name", paymentTransaction.getUser().getName());
				dataModel.put("amount", String.valueOf(paymentTransaction.getTransactionAmount()));
			}
			dataModel.put("eventType", namespaceEvent.getCode());
			dataModel.put("handledBy", authDTO.getUser().getName());
			dataModel.put("dateTime", DateUtil.NOW().format("YYYY-MM-DD hh:mm:ss"));
			dataModel.put("domainUrl", authDTO.getNamespace().getProfile().getDomainURL());

			if (StringUtil.isNotNull(auditEvent.getMobileNumber())) {
				String[] mobiles = auditEvent.getMobileNumber().split(",");
				for (String mobile : mobiles) {
					dataModel.put("mobileNumber", mobile);
//					smsService.eventAlertUtil(authDTO, dataModel, namespaceEvent);
				}
			}
			else {
				dataModel.put("emailId", auditEvent.getEmailId());
				emailService.sendAuditEventAlertEmail(authDTO, dataModel, auditEvent);
			}
		}
		catch (IllegalArgumentException | SecurityException e) {
			e.printStackTrace();
		}
	}
}
