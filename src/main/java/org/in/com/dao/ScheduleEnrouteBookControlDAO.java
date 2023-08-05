package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Cleanup;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleEnrouteBookControlDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StageStationDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.enumeration.EnRouteTypeEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

public class ScheduleEnrouteBookControlDAO {

	public ScheduleEnrouteBookControlDTO updateScheduleEnrouteBookControl(AuthDTO authDTO, ScheduleEnrouteBookControlDTO scheduleEnrouteBookControl) {
		try {
			int pindex = Numeric.ZERO_INT;
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("CALL EZEE_SP_SCHEDULE_ENROUTE_BOOK_CONTROL_IUD(?,?,?,?,?, ?,?,?,?,?, ?)");
			callableStatement.setString(++pindex, scheduleEnrouteBookControl.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setInt(++pindex, scheduleEnrouteBookControl.getSchedule().getId());
			callableStatement.setString(++pindex, scheduleEnrouteBookControl.getStages());
			callableStatement.setInt(++pindex, scheduleEnrouteBookControl.getReleaseMinutes());
			callableStatement.setString(++pindex, scheduleEnrouteBookControl.getDayOfWeek());
			callableStatement.setInt(++pindex, scheduleEnrouteBookControl.getEnRouteType().getId());
			callableStatement.setInt(++pindex, scheduleEnrouteBookControl.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				scheduleEnrouteBookControl.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(ErrorCode.UPDATE_FAIL);
		}
		return scheduleEnrouteBookControl;
	}

	public List<ScheduleEnrouteBookControlDTO> getAllScheduleEnrouteBookControl(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		List<ScheduleEnrouteBookControlDTO> list = new ArrayList<ScheduleEnrouteBookControlDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, stage_list, release_minutes, day_of_week, enroute_type_id, active_flag FROM schedule_enroute_book_control WHERE namespace_id = ? AND schedule_id = ? AND active_flag < 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, scheduleDTO.getId());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleEnrouteBookControlDTO scheduleEnrouteBookControl = new ScheduleEnrouteBookControlDTO();
				scheduleEnrouteBookControl.setCode(selectRS.getString("code"));

				String stageCodes = selectRS.getString("stage_list");
				List<StageDTO> stageList = new ArrayList<StageDTO>();
				if (StringUtil.isNotNull(stageCodes)) {
					List<String> stages = Arrays.asList(stageCodes.split(Text.COMMA));
					for (String stage : stages) {
						StageDTO stageDTO = new StageDTO();

						StageStationDTO fromStageStationDTO = new StageStationDTO();
						StationDTO fromStation = new StationDTO();
						fromStation.setId(Integer.valueOf(stage.split(Text.HYPHEN)[Numeric.ZERO_INT]));
						fromStageStationDTO.setStation(fromStation);
						stageDTO.setFromStation(fromStageStationDTO);

						StageStationDTO toStageStationDTO = new StageStationDTO();
						StationDTO toStation = new StationDTO();
						toStation.setId(Integer.valueOf(stage.split(Text.HYPHEN)[Numeric.ONE_INT]));
						toStageStationDTO.setStation(toStation);
						stageDTO.setToStation(toStageStationDTO);

						stageList.add(stageDTO);
					}
				}
				scheduleEnrouteBookControl.setStageList(stageList);

				scheduleEnrouteBookControl.setReleaseMinutes(selectRS.getInt("release_minutes"));
				scheduleEnrouteBookControl.setDayOfWeek(selectRS.getString("day_of_week"));
				scheduleEnrouteBookControl.setEnRouteType(EnRouteTypeEM.getEnRouteTypeEM(selectRS.getInt("enroute_type_id")));
				scheduleEnrouteBookControl.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(scheduleEnrouteBookControl);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public List<ScheduleEnrouteBookControlDTO> getBySchedule(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		List<ScheduleEnrouteBookControlDTO> list = new ArrayList<ScheduleEnrouteBookControlDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, stage_list, release_minutes, day_of_week, enroute_type_id, active_flag FROM schedule_enroute_book_control WHERE namespace_id = ? AND schedule_id = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, scheduleDTO.getId());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleEnrouteBookControlDTO scheduleEnrouteBookControl = new ScheduleEnrouteBookControlDTO();
				scheduleEnrouteBookControl.setCode(selectRS.getString("code"));

				String stageCodes = selectRS.getString("stage_list");
				List<StageDTO> stageList = new ArrayList<StageDTO>();
				if (StringUtil.isNotNull(stageCodes)) {
					List<String> stages = Arrays.asList(stageCodes.split(Text.COMMA));
					for (String stage : stages) {
						StageDTO stageDTO = new StageDTO();

						StageStationDTO fromStageStationDTO = new StageStationDTO();
						StationDTO fromStation = new StationDTO();
						fromStation.setId(Integer.valueOf(stage.split(Text.HYPHEN)[Numeric.ZERO_INT]));
						fromStageStationDTO.setStation(fromStation);
						stageDTO.setFromStation(fromStageStationDTO);

						StageStationDTO toStageStationDTO = new StageStationDTO();
						StationDTO toStation = new StationDTO();
						toStation.setId(Integer.valueOf(stage.split(Text.HYPHEN)[Numeric.ONE_INT]));
						toStageStationDTO.setStation(toStation);
						stageDTO.setToStation(toStageStationDTO);

						stageList.add(stageDTO);
					}
				}
				scheduleEnrouteBookControl.setStageList(stageList);

				scheduleEnrouteBookControl.setReleaseMinutes(selectRS.getInt("release_minutes"));
				scheduleEnrouteBookControl.setDayOfWeek(selectRS.getString("day_of_week"));
				scheduleEnrouteBookControl.setEnRouteType(EnRouteTypeEM.getEnRouteTypeEM(selectRS.getInt("enroute_type_id")));
				scheduleEnrouteBookControl.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(scheduleEnrouteBookControl);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public ScheduleEnrouteBookControlDTO getScheduleEnrouteBookControl(AuthDTO authDTO, ScheduleEnrouteBookControlDTO scheduleEnrouteBookControl) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, schedule_id, stage_list, release_minutes, day_of_week, enroute_type_id, active_flag FROM schedule_enroute_book_control WHERE namespace_id = ? AND code = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, scheduleEnrouteBookControl.getCode());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				scheduleEnrouteBookControl.setCode(selectRS.getString("code"));

				ScheduleDTO schedule = new ScheduleDTO();
				schedule.setId(selectRS.getInt("schedule_id"));
				scheduleEnrouteBookControl.setSchedule(schedule);

				String stageCodes = selectRS.getString("stage_list");
				List<StageDTO> stageList = new ArrayList<StageDTO>();
				if (StringUtil.isNotNull(stageCodes)) {
					List<String> stages = Arrays.asList(stageCodes.split(Text.COMMA));
					for (String stage : stages) {
						StageDTO stageDTO = new StageDTO();

						StageStationDTO fromStageStationDTO = new StageStationDTO();
						StationDTO fromStation = new StationDTO();
						fromStation.setId(Integer.valueOf(stage.split(Text.HYPHEN)[Numeric.ZERO_INT]));
						fromStageStationDTO.setStation(fromStation);
						stageDTO.setFromStation(fromStageStationDTO);

						StageStationDTO toStageStationDTO = new StageStationDTO();
						StationDTO toStation = new StationDTO();
						toStation.setId(Integer.valueOf(stage.split(Text.HYPHEN)[Numeric.ONE_INT]));
						toStageStationDTO.setStation(toStation);
						stageDTO.setToStation(toStageStationDTO);

						stageList.add(stageDTO);
					}
				}
				scheduleEnrouteBookControl.setStageList(stageList);

				scheduleEnrouteBookControl.setReleaseMinutes(selectRS.getInt("release_minutes"));
				scheduleEnrouteBookControl.setDayOfWeek(selectRS.getString("day_of_week"));
				scheduleEnrouteBookControl.setEnRouteType(EnRouteTypeEM.getEnRouteTypeEM(selectRS.getInt("enroute_type_id")));
				scheduleEnrouteBookControl.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return scheduleEnrouteBookControl;
	}
}
