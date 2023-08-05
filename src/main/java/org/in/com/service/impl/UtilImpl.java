package org.in.com.service.impl;

import org.in.com.dao.UtilDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.SequenceDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.UtilService;
import org.in.com.utils.TokenGenerator;
import org.springframework.stereotype.Service;

@Service
public class UtilImpl implements UtilService {

	public String getGenerateSequenceNumber(AuthDTO authDTO) {
		UtilDAO utilDAO = new UtilDAO();
		SequenceDTO sequence = utilDAO.generateSequenceNumber(authDTO);
		if (sequence.getNextSequence() == 0) {
			throw new ServiceException(ErrorCode.UNABLE_TO_GENERATE_SEQUENCE);
		}
		String nextAlphabeticSequence = TokenGenerator.generateSequenceAlphabetic(sequence.getNextSequence());
		return sequence.getPrefixcode() + nextAlphabeticSequence;
	}

}
