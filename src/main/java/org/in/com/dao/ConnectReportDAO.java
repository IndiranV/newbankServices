package org.in.com.dao;

import java.sql.Connection;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.in.com.aggregator.slack.SlackService;
import org.in.com.aggregator.slack.SlackServiceImpl;
import org.in.com.config.ApplicationConfig;
import org.in.com.constants.Constants;

public class ConnectReportDAO {

	private static DataSource reportInstance = null;

	protected ConnectReportDAO() {
		// Exists only to defeat instantiation.
	}

	public static void setReportInstance(DataSource instance) {
		ConnectReportDAO.reportInstance = instance;
	}

	public static synchronized DataSource getReportInstance() throws NamingException {

		if (reportInstance == null) {
			InitialContext context = new InitialContext();
			reportInstance = (DataSource) context.lookup(Constants.MYSQL_REPORT_JDBC);
			System.out.println(Constants.MYSQL_REPORT_JDBC + " New  DataSource Instance");
		}
		return reportInstance;
	}

	public static Connection getReportConnection() {
		Connection con = null;
		DataSource dataSource = null;
		try {
			dataSource = ConnectReportDAO.getReportInstance();
			con = dataSource.getConnection();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		if (con == null) {
			System.out.println("Report DB - ERRJDBC01 Connection pool has been empty");
			SlackService slack = new SlackServiceImpl();
			slack.sendAlert(ApplicationConfig.getServerZoneCode() + Constants.MYSQL_REPORT_JDBC + " - ERRJDBC01 Connection pool is empty");
		}
		return con;
	}

}
