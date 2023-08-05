package org.in.com.service;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.CancellationTermDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.UserDTO;

public interface CancellationTermsService extends BaseService<CancellationTermDTO> {

	public CancellationTermDTO getCancellationTermsByTripDTO(AuthDTO authDTO, UserDTO userDTO, TripDTO tripDTO);

	public CancellationTermDTO getCancellationTermsById(AuthDTO authDTO, CancellationTermDTO cancellationTermDTO);

	public void getCancellationTermGroupIdByGroupKey(AuthDTO authDTO, CancellationTermDTO cancellationTermDTO);
}
