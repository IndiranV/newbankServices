package org.in.com.dto;

import java.math.BigDecimal;
import java.util.List;

import org.in.com.constants.Text;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.OrderTypeEM;
import org.in.com.dto.enumeration.UserRoleEM;
import org.in.com.utils.StringUtil;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentGatewayScheduleDTO extends BaseDTO<PaymentGatewayScheduleDTO> {
	private PaymentGatewayPartnerDTO gatewayPartner;
	private GroupDTO group;
	private UserRoleEM role;
	private String fromDate;
	private String toDate;
	private List<DeviceMediumEM> deviceMedium;
	private BigDecimal serviceCharge;
	private OrderTypeEM orderType;
	private int precedence;

	public String getDeviceMediums() {
		StringBuilder medium = new StringBuilder();
		if (deviceMedium != null) {
			for (DeviceMediumEM deviceMediumEM : deviceMedium) {
				medium.append(deviceMediumEM.getId());
				medium.append(Text.COMMA);
			}
		}
		return StringUtil.isNotNull(medium.toString()) ? medium.toString() : Text.NA;
	}
}
