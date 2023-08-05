package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.CommissionDTO;
import org.in.com.dto.ExtraCommissionDTO;
import org.in.com.dto.ExtraCommissionSlabDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.CommissionTypeEM;

public interface CommissionService {
	public List<CommissionDTO> getAllCommission(AuthDTO authDTO, UserDTO user);

	public CommissionDTO updateCommission(AuthDTO authDTO, UserDTO user, CommissionDTO commission);

	public CommissionDTO getCommission(AuthDTO authDTO, UserDTO userDTO, CommissionTypeEM commissionType);

	public List<CommissionDTO> getCommissionV2(AuthDTO authDTO, UserDTO userDTO);

	public CommissionDTO getUserTaxDetails(AuthDTO authDTO, UserDTO userDTO);

	public List<ExtraCommissionDTO> getAllExtraCommission(AuthDTO authDTO);

	public CommissionDTO getBookingExtraCommission(AuthDTO authDTO, UserDTO userDTO, CommissionDTO commissionDTO, TicketDTO ticketDTO);

	public ExtraCommissionDTO getExtraCommission(AuthDTO authDTO, ExtraCommissionDTO commissionDTO);

	public void UpdateExtraCommission(AuthDTO authDTO, ExtraCommissionDTO commissionDTO);

	public void updateExtraCommissionSlabDetails(AuthDTO authDTO, ExtraCommissionSlabDTO commissionSlabDTO);

	public List<ExtraCommissionSlabDTO> getAllExtraCommissionSlab(AuthDTO authDTO);

	public List<CommissionDTO> getCommerceCommission(AuthDTO authDTO, UserDTO userDTO);

}
