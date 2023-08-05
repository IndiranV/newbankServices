package org.in.com.listener;

import java.util.Map;

import org.in.com.aggregator.sms.SMSService;
import org.in.com.cache.TicketCache;
import org.in.com.constants.Constants;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.enumeration.JourneyTypeEM;
import org.in.com.dto.enumeration.PaymentOrderEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.service.NotificationPushService;
import org.in.com.service.NotificationService;
import org.in.com.service.PaymentOrderStatusService;
import org.in.com.service.TicketService;
import org.in.com.service.UserService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

@Component
final class TicketCacheEventListener implements CacheEventListener {
	@Autowired
	SMSService smsService;
	@Autowired
	NotificationService notificationService;
	@Autowired
	NotificationPushService notificationPushService;
	@Autowired
	TicketService ticketService;
	@Autowired
	UserService userService;
	@Autowired
	PaymentOrderStatusService paymentOrderStatusService;

	@Override
	public void notifyElementRemoved(final Ehcache cache, final Element element) throws CacheException {
		if (element.getObjectKey() != null && element.getObjectValue() != null) {
			String bookingCode = (String) element.getObjectKey();
			String namespaceCode = (String) element.getObjectValue();
			sendNotification(namespaceCode, bookingCode);
		}
	}

	@Override
	public void notifyElementPut(final Ehcache cache, final Element element) throws CacheException {

	}

	@Override
	public void notifyElementUpdated(final Ehcache cache, final Element element) throws CacheException {

	}

	@Override
	public void notifyElementExpired(final Ehcache cache, final Element element) {
		if (element.getObjectKey() != null && element.getObjectValue() != null) {
			String bookingCode = (String) element.getObjectKey();
			String namespaceCode = (String) element.getObjectValue();
			sendNotification(namespaceCode, bookingCode);
		}
	}

	@Override
	public void notifyElementEvicted(final Ehcache cache, final Element element) {
		if (element.getObjectKey() != null && element.getObjectValue() != null) {
			String bookingCode = (String) element.getObjectKey();
			String namespaceCode = (String) element.getObjectValue();
			sendNotification(namespaceCode, bookingCode);
		}
	}

	@Override
	public void notifyRemoveAll(final Ehcache cache) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException("Singleton instance");
	}

	private void sendNotification(String namespaceCode, String bookingCode) {
		AuthDTO authDTO = new AuthDTO();
		authDTO.setNamespaceCode(namespaceCode);

		TicketCache ticketCache = new TicketCache();
		BookingDTO bookingDTO = ticketCache.getBookingDTO(authDTO, bookingCode);
		if (bookingDTO != null) {
			for (TicketDTO ticketDTO : bookingDTO.getTicketList()) {
				// ALL Device Transaction
				if (ticketDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId() && !StringUtil.isMaskedMobileNumber(ticketDTO.getPassengerMobile())) {
					ticketDTO.setTicketUser(userService.getUser(authDTO, ticketDTO.getTicketUser()));
					if (StringUtil.isContains(Constants.TICKET_EVENT_FAILURE_PASSENGER, authDTO.getNamespace().getCode())) {
						smsService.sendFaliureTicketSMS(authDTO, ticketDTO);
					}
					if (StringUtil.isContains(Constants.TICKET_EVENT_FAILURE_OPERATOR, authDTO.getNamespace().getCode())) {
						notificationService.sendFaliureBookSMS(authDTO, ticketDTO);
					}
					// Push Notification
					notificationPushService.pushTicketFailureNotification(authDTO, ticketDTO);

					// Push Ticket Event to API Callback
					authDTO.getAdditionalAttribute().put("activity_type", "block-release");
					ticketService.pushInventoryChangesEvent(authDTO, ticketDTO);

				}
				else if (ticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() && ticketDTO.getTripDTO().getTripDate().getStartOfDay().compareTo(DateUtil.NOW().getStartOfDay()) == 0) {
					notificationService.sendBusBuddyAlert(authDTO, ticketDTO);
				}
			}

			if (StringUtil.isNotNull(bookingDTO.getPaymentGatewayPartnerCode()) && bookingDTO.getTicketDTO(JourneyTypeEM.ONWARD_TRIP).getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId()) {
				Map<String, String> orderStatusMap = paymentOrderStatusService.getOrderStatus(authDTO, bookingDTO.getCode(), namespaceCode);
				if (orderStatusMap != null && StringUtil.isNotNull(orderStatusMap.get("paymentOrderStatus")) && PaymentOrderEM.SUCCESS.getCode().equals(orderStatusMap.get("paymentOrderStatus"))) {
					notificationService.sendDenialTicketEmail(authDTO, bookingDTO.getTicketDTO(JourneyTypeEM.ONWARD_TRIP));
				}
			}
		}
	}
}
