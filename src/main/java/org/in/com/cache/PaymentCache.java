package org.in.com.cache;

import net.sf.ehcache.Element;

import org.in.com.constants.Text;
import org.in.com.dto.OrderDTO;

public class PaymentCache {

	public OrderDTO getTransactionDetails(String transactionCode) {
		Element element = EhcacheManager.getPGTransactionCache().get(transactionCode);
		if (element != null) {
			return (OrderDTO) element.getObjectValue();
		}
		else {
			return null;
		}
	}

	public void putTransactionDetailsInCache(OrderDTO order) {
		/**
		 * Adding order details in cache such that it can be reused during
		 * payment response validation
		 */
		Element orderElement = new Element(order.getTransactionCode(), order);
		EhcacheManager.getPGTransactionCache().put(orderElement);
	}

	public void putPayPalAuthorizationToken(String Key, String value) {
		Element orderElement = new Element(Key, value);
		// 6 Hrs
		orderElement.setTimeToLive(6 * 60 * 60);
		EhcacheManager.getPGTransactionCache().put(orderElement);
	}

	public String getPayPalAuthorizationToken(String Key) {
		Element element = EhcacheManager.getPGTransactionCache().get(Key);
		if (element != null) {
			return (String) element.getObjectValue();
		}
		else {
			return Text.NA;
		}
	}

}
