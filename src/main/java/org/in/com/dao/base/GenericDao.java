package org.in.com.dao.base;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import lombok.Cleanup;

import org.in.com.dao.ConnectDAO;
import org.in.com.dto.AuthDTO;

public class GenericDao {
	public String tableName = null;

	protected int getActiveId(AuthDTO authDTO, String code) {
		int id = 0;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement PS = connection.prepareStatement("SELECT id FROM " + tableName + " WHERE code = ? AND active_flag = 1");
			PS.setString(1, code);
			@Cleanup
			ResultSet RS = PS.executeQuery();
			if (RS.next()) {
				id = RS.getInt("id");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return id;
	}

	protected int getId(AuthDTO authDTO, String code) {
		int id = 0;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement PS = connection.prepareStatement("SELECT id FROM " + tableName + " WHERE code = ?");
			PS.setString(1, code);
			@Cleanup
			ResultSet RS = PS.executeQuery();
			if (RS.next()) {
				id = RS.getInt("id");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return id;
	}

}
