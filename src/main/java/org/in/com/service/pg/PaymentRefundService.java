package org.in.com.service.pg;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.RefundDTO;

public interface PaymentRefundService {

	public void doRefund(AuthDTO authDTO, RefundDTO refund);
}
