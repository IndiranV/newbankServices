package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.SequenceDTO;

import lombok.Cleanup;

public class UtilDAO {

	public SequenceDTO generateSequenceNumber(AuthDTO authDTO) {
		SequenceDTO sequence = new SequenceDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_SEQUENCE_GENERATOR_V2(?,?,?,?)}");
			callableStatement.setString(1, authDTO.getNamespaceCode());
			callableStatement.setInt(2, 0);
			callableStatement.registerOutParameter(3, Types.INTEGER);
			callableStatement.registerOutParameter(4, Types.VARCHAR);
			callableStatement.execute();
			int nextSequence = callableStatement.getInt("pitSequenceNumber");
			String prefixCode = callableStatement.getString("pcrPrefixCode");
			if (nextSequence != 0) {
				sequence.setNextSequence(nextSequence);
				sequence.setPrefixcode(prefixCode);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return sequence;
	}

}
