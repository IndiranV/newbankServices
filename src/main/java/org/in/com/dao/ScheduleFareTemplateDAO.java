package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.in.com.constants.Text;
import org.in.com.dto.AuditDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleFareTemplateDTO;
import org.in.com.dto.StageFareDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.BusSeatTypeEM;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

import lombok.Cleanup;

public class ScheduleFareTemplateDAO {

	private static Pattern pattern = Pattern.compile("[0-9:0-9][\\[[A-Z-]*\\]]", Pattern.CASE_INSENSITIVE);

	public ScheduleFareTemplateDTO updateScheduleFareTemplate(AuthDTO authDTO, ScheduleFareTemplateDTO scheduleFareTemplateDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_SCHEDULE_FARE_TEMPLATE_IUD(?,?,?,?,?, ?,?,?,?)}");
			callableStatement.setString(++pindex, scheduleFareTemplateDTO.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, scheduleFareTemplateDTO.getName());
			callableStatement.setInt(++pindex, scheduleFareTemplateDTO.getBus().getId());
			callableStatement.setString(++pindex, getStageFare(scheduleFareTemplateDTO));
			callableStatement.setInt(++pindex, scheduleFareTemplateDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				scheduleFareTemplateDTO.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return scheduleFareTemplateDTO;
	}

	public List<ScheduleFareTemplateDTO> getAllScheduleFareTemplate(AuthDTO authDTO) {
		List<ScheduleFareTemplateDTO> scheduleFareTemplateList = new ArrayList<ScheduleFareTemplateDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, name, bus_id, stage_fare, active_flag, updated_by, DATE_FORMAT(updated_at,'%Y-%m-%d %H:%i:%s') as updated_datetime  FROM schedule_fare_template WHERE namespace_id = ? AND active_flag <= 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleFareTemplateDTO scheduleFareTemplateDTO = new ScheduleFareTemplateDTO();
				scheduleFareTemplateDTO.setCode(selectRS.getString("code"));
				scheduleFareTemplateDTO.setName(selectRS.getString("name"));

				BusDTO bus = new BusDTO();
				bus.setId(selectRS.getInt("bus_id"));
				scheduleFareTemplateDTO.setBus(bus);

				List<RouteDTO> stageList = convertStageFare(selectRS.getString("stage_fare"));
				scheduleFareTemplateDTO.setStageFare(stageList);
				scheduleFareTemplateDTO.setActiveFlag(selectRS.getInt("active_flag"));

				UserDTO user = new UserDTO();
				user.setId(selectRS.getInt("updated_by"));
				AuditDTO auditDTO = new AuditDTO();
				auditDTO.setUpdatedAt(selectRS.getString("updated_datetime"));
				auditDTO.setUser(user);
				scheduleFareTemplateDTO.setAudit(auditDTO);

				scheduleFareTemplateList.add(scheduleFareTemplateDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return scheduleFareTemplateList;
	}

	public ScheduleFareTemplateDTO getScheduleFareTemplate(AuthDTO authDTO, ScheduleFareTemplateDTO scheduleFareTemplate) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, name, bus_id, stage_fare, active_flag, updated_by, DATE_FORMAT(updated_at,'%Y-%m-%d %H:%i:%s') as updated_datetime  FROM schedule_fare_template WHERE namespace_id = ? AND code = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, scheduleFareTemplate.getCode());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				scheduleFareTemplate.setCode(selectRS.getString("code"));
				scheduleFareTemplate.setName(selectRS.getString("name"));

				BusDTO bus = new BusDTO();
				bus.setId(selectRS.getInt("bus_id"));
				scheduleFareTemplate.setBus(bus);

				List<RouteDTO> stageList = convertStageFare(selectRS.getString("stage_fare"));
				scheduleFareTemplate.setStageFare(stageList);
				scheduleFareTemplate.setActiveFlag(selectRS.getInt("active_flag"));

				UserDTO user = new UserDTO();
				user.setId(selectRS.getInt("updated_by"));
				AuditDTO auditDTO = new AuditDTO();
				auditDTO.setUpdatedAt(selectRS.getString("updated_datetime"));
				auditDTO.setUser(user);
				scheduleFareTemplate.setAudit(auditDTO);

			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return scheduleFareTemplate;
	}

	private String getStageFare(ScheduleFareTemplateDTO scheduleFareTemplateDTO) {
		StringBuilder stageFare = new StringBuilder();
		if (scheduleFareTemplateDTO.getStageFare() != null && !scheduleFareTemplateDTO.getStageFare().isEmpty()) {
			for (RouteDTO routeDTO : scheduleFareTemplateDTO.getStageFare()) {
				if (routeDTO.getFromStation() == null || routeDTO.getFromStation().getId() == 0 || routeDTO.getToStation() == null || routeDTO.getToStation().getId() == 0) {
					continue;
				}
				if (stageFare.length() > 0) {
					stageFare.append(Text.COMMA);
				}

				stageFare.append(routeDTO.getFromStation().getId() + Text.HYPHEN + routeDTO.getToStation().getId());
				stageFare.append("[");

				StringBuilder busType = new StringBuilder();
				if (routeDTO.getStageFare() != null && !routeDTO.getStageFare().isEmpty()) {
					for (StageFareDTO stageFareDTO : routeDTO.getStageFare()) {
						if (busType.length() > 0) {
							busType.append(Text.VERTICAL_BAR);
						}
						busType.append(stageFareDTO.getBusSeatType().getCode() + Text.COLON + stageFareDTO.getFare());
					}
				}
				stageFare.append(busType + "]");
			}
		}
		return stageFare.toString();
	}

	private List<RouteDTO> convertStageFare(String stageFares) {
		List<RouteDTO> stageList = new ArrayList<RouteDTO>();
		if (StringUtil.isNotNull(stageFares)) {
			List<String> stageFareList = Arrays.asList(stageFares.split(Text.COMMA));
			for (String stageFare : stageFareList) {

				RouteDTO routeDTO = new RouteDTO();
				int fromStationId = StringUtil.getIntegerValue(stageFare.split(Text.HYPHEN)[0]);
				int toStationId = StringUtil.getIntegerValue(stageFare.split(Text.HYPHEN)[1].split("\\[")[0]);

				StationDTO fromStation = new StationDTO();
				fromStation.setId(fromStationId);
				routeDTO.setFromStation(fromStation);

				StationDTO toStation = new StationDTO();
				toStation.setId(toStationId);
				routeDTO.setToStation(toStation);

				List<StageFareDTO> stagefareList = new ArrayList<StageFareDTO>();
				String busTypeFares = stageFare.split(Text.HYPHEN)[1].split("\\[")[1].replace("]", "");
				List<String> busTypeFareList = Arrays.asList(busTypeFares.split("\\|"));
				for (String busTypeFare : busTypeFareList) {
					StageFareDTO stageFareDTO = new StageFareDTO();
					stageFareDTO.setBusSeatType(BusSeatTypeEM.getBusSeatTypeEM(busTypeFare.split(Text.COLON)[0]));
					stageFareDTO.setFare(StringUtil.getBigDecimalValue(busTypeFare.split(Text.COLON)[1]));
					stagefareList.add(stageFareDTO);
				}
				routeDTO.setStageFare(stagefareList);
				stageList.add(routeDTO);
			}
		}
		return stageList;
	}

}
