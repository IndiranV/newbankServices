package org.in.com.service;

import org.in.com.dto.AddonsDiscountOfflineDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;

public interface AddonsDiscountOfflineService extends BaseService<AddonsDiscountOfflineDTO> {
	public AddonsDiscountOfflineDTO getOfflineDiscount(AuthDTO authDTO, String offlineDiscountCode);

	public AddonsDiscountOfflineDTO getAvailableDiscountOffline(AuthDTO authDTO, BookingDTO bookingDTO);

}
