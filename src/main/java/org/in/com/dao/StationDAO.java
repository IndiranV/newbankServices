package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Cleanup;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.StateDTO;
import org.in.com.dto.StationAreaDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationOtaPartnerDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.enumeration.OTAPartnerEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StationDAO {
	Logger logger = LoggerFactory.getLogger(StationDAO.class);

	public List<StationDTO> getAllStations(StateDTO stateDTO) {
		List<StationDTO> list = new ArrayList<StationDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT stat.id,stat.code,stat.name,stat.latitude,stat.longitude,stat.radius,stat.api_flag,stte.code,stte.name,stat.active_flag FROM station stat,state stte where stte.id = stat.state_id and stte.active_flag  = 1 and stat.active_flag < 2 AND stte.code  = ?");
			selectPS.setString(1, stateDTO.getCode());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				StationDTO dto = new StationDTO();
				stateDTO.setCode(selectRS.getString("stte.code"));
				stateDTO.setName(selectRS.getString("stte.name"));
				dto.setState(stateDTO);
				dto.setId(selectRS.getInt("stat.id"));
				dto.setCode(selectRS.getString("stat.code"));
				dto.setName(selectRS.getString("stat.name"));
				dto.setLatitude(selectRS.getString("stat.latitude"));
				dto.setLongitude(selectRS.getString("stat.longitude"));
				dto.setRadius(selectRS.getInt("stat.radius"));
				dto.setApiFlag(selectRS.getInt("stat.api_flag"));
				dto.setActiveFlag(selectRS.getInt("stat.active_flag"));
				list.add(dto);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public StationDTO StationUID(AuthDTO authDTO, StationDTO stationDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_STATION_IUD(?,?,?,?,?, ?,?,?,?,? ,?)}");
			callableStatement.setString(++pindex, stationDTO.getCode());
			callableStatement.setString(++pindex, stationDTO.getName().trim());
			callableStatement.setInt(++pindex, stationDTO.getState().getId());
			callableStatement.setString(++pindex, stationDTO.getLatitude());
			callableStatement.setString(++pindex, stationDTO.getLongitude());
			callableStatement.setInt(++pindex, stationDTO.getRadius());
			callableStatement.setInt(++pindex, stationDTO.getApiFlag());
			callableStatement.setInt(++pindex, stationDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				stationDTO.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return null;
	}

	public List<StationDTO> getNamespaceStations(AuthDTO authDTO) {
		List<StationDTO> list = new ArrayList<StationDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT stat.code,stat.name,tion.code,tion.name,tion.latitude,tion.longitude,tion.radius,tion.api_flag,nsst.related_station,tion.active_flag FROM station tion,namespace_station nsst,state stat WHERE stat.id= tion.state_id AND nsst.station_id = tion.id AND stat.active_flag  = 1 AND nsst.active_flag = 1 AND tion.active_flag = 1 AND nsst.namespace_id = ?");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				StationDTO dto = new StationDTO();
				dto.setCode(selectRS.getString("tion.code"));
				dto.setName(selectRS.getString("tion.name"));
				dto.setLatitude(selectRS.getString("tion.latitude"));
				dto.setLongitude(selectRS.getString("tion.longitude"));
				dto.setRadius(selectRS.getInt("tion.radius"));
				dto.setApiFlag(selectRS.getInt("tion.api_flag"));
				dto.setList(getRelatedStationIds(selectRS.getString("nsst.related_station")));
				dto.setActiveFlag(selectRS.getInt("active_flag"));
				StateDTO stateDTO = new StateDTO();
				stateDTO.setCode(selectRS.getString("stat.code"));
				stateDTO.setName(selectRS.getString("stat.name"));
				dto.setState(stateDTO);
				list.add(dto);
			}
		}
		catch (Exception e) {
			logger.error("Exception while getting the namespace Stations: " + e.getMessage());
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public List<RouteDTO> getNamespaceRoutes(AuthDTO authDTO) {
		List<RouteDTO> list = new ArrayList<RouteDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, from_station_id, to_station_id, min_fare, max_fare, top_route, booking_count, active_flag FROM  route WHERE namespace_id = ? AND route.active_flag < 2 ");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				RouteDTO routeDTO = new RouteDTO();
				StationDTO fromDto = new StationDTO();
				StationDTO toDto = new StationDTO();
				fromDto.setId(selectRS.getInt("from_station_id"));
				toDto.setId(selectRS.getInt("to_station_id"));
				routeDTO.setCode(selectRS.getString("code"));
				routeDTO.setFromStation(fromDto);
				routeDTO.setToStation(toDto);
				routeDTO.setMinFare(selectRS.getInt("min_fare"));
				routeDTO.setMaxFare(selectRS.getInt("max_fare"));
				routeDTO.setTopRouteFlag(selectRS.getInt("top_route"));
				routeDTO.setBookingCount(selectRS.getInt("booking_count"));
				routeDTO.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(routeDTO);
			}
		}
		catch (Exception e) {
			logger.error("Exception while retreiving the route details");
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public StationDTO updateNamespaceStation(AuthDTO authDTO, StationDTO stationDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_NAMESPACE_STATION_IUD(?,?,?,?,?, ?,?)}");
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setInt(++pindex, stationDTO.getId());
			callableStatement.setString(++pindex, stationDTO.getRelatedStationIds(stationDTO.getList()));
			callableStatement.setInt(++pindex, stationDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				stationDTO.setActiveFlag(callableStatement.getInt("pitRowCount"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return null;
	}

	public RouteDTO NamespaceRouteUID(AuthDTO authDTO, RouteDTO routeDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_NAMESPACE_ROUTE_IUD(?,?,?,?,?, ?,?,?,?,?)}");
			callableStatement.setString(++pindex, routeDTO.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setInt(++pindex, routeDTO.getFromStation().getId());
			callableStatement.setInt(++pindex, routeDTO.getToStation().getId());
			callableStatement.setInt(++pindex, routeDTO.getMinFare());
			callableStatement.setInt(++pindex, routeDTO.getMaxFare());
			callableStatement.setInt(++pindex, routeDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				routeDTO.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return null;
	}

	public void updateRouteStatus(AuthDTO authDTO, List<RouteDTO> routes, int enableFlag) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("UPDATE route SET enable_flag = ? WHERE namespace_id = ? AND code = ?");
			for (RouteDTO routeDTO : routes) {
				selectPS.setInt(1, enableFlag);
				selectPS.setInt(2, authDTO.getNamespace().getId());
				selectPS.setString(3, routeDTO.getCode());
				selectPS.addBatch();
			}
			selectPS.executeBatch();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public void getStationDTO(StationDTO dto) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (dto.getId() != 0) {
				selectPS = connection.prepareStatement("SELECT stat.id,stat.code,stat.name,stat.latitude,stat.longitude,stat.radius,stte.code,stte.name,stat.active_flag FROM station stat,state stte where stte.id = stat.state_id and stte.active_flag = 1 and stat.active_flag = 1 AND stat.id  = ?");
				selectPS.setInt(1, dto.getId());
			}
			else {
				selectPS = connection.prepareStatement("SELECT stat.id,stat.code,stat.name,stat.latitude,stat.longitude,stat.radius,stte.code,stte.name,stat.active_flag FROM station stat,state stte where stte.id = stat.state_id and stte.active_flag = 1 and stat.active_flag = 1 AND stat.code = ?");
				selectPS.setString(1, dto.getCode());
			}
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				StateDTO stateDTO = new StateDTO();
				stateDTO.setCode(selectRS.getString("stte.code"));
				stateDTO.setName(selectRS.getString("stte.name").trim());
				dto.setState(stateDTO);
				dto.setId(selectRS.getInt("stat.id"));
				dto.setCode(selectRS.getString("stat.code"));
				dto.setName(selectRS.getString("stat.name").trim());
				dto.setLatitude(selectRS.getString("stat.latitude"));
				dto.setLongitude(selectRS.getString("stat.longitude"));
				dto.setRadius(selectRS.getInt("stat.radius"));
				dto.setActiveFlag(selectRS.getInt("stat.active_flag"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public List<StationPointDTO> getAllStationsAndPoints(AuthDTO authDTO) {
		List<StationPointDTO> list = new ArrayList<StationPointDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT stp.station_id, stp.code, stp.name, stp.address,stp.landmark,stp.contact_number,stp.latitude,stp.longitude FROM namespace_station nst,station_point stp WHERE stp.namespace_id = ? AND stp.station_id = nst.station_id AND nst.active_flag = stp.active_flag AND nst.namespace_id = ? AND nst.active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				StationPointDTO pointDTO = new StationPointDTO();
				StationDTO stationDTO = new StationDTO();
				stationDTO.setId(selectRS.getInt("stp.station_id"));
				pointDTO.setCode(selectRS.getString("stp.code"));
				pointDTO.setName(selectRS.getString("stp.name").trim());
				pointDTO.setAddress(selectRS.getString("stp.address").trim());
				pointDTO.setLandmark(selectRS.getString("stp.landmark").trim());
				pointDTO.setNumber(selectRS.getString("stp.contact_number").trim());
				pointDTO.setLatitude(selectRS.getString("stp.latitude"));
				pointDTO.setLongitude(selectRS.getString("stp.longitude"));
				pointDTO.setStation(stationDTO);
				list.add(pointDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public RouteDTO getRouteDTO(AuthDTO authDTO, StationDTO fromStation, StationDTO toStation) {
		RouteDTO routeDTO = null;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, code, from_station_id, to_station_id, min_fare, max_fare,  active_flag FROM  route WHERE namespace_id = ? AND from_station_id = ? AND to_station_id = ? AND active_flag = 1 ");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, fromStation.getId());
			selectPS.setInt(3, toStation.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				routeDTO = new RouteDTO();
				StationDTO fromDto = new StationDTO();
				StationDTO toDto = new StationDTO();
				fromDto.setId(selectRS.getInt("from_station_id"));
				toDto.setId(selectRS.getInt("to_station_id"));
				routeDTO.setId(selectRS.getInt("id"));
				routeDTO.setCode(selectRS.getString("code"));
				routeDTO.setFromStation(fromDto);
				routeDTO.setToStation(toDto);
				routeDTO.setMinFare(selectRS.getInt("min_fare"));
				routeDTO.setMaxFare(selectRS.getInt("max_fare"));
				routeDTO.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return routeDTO;
	}

	public List<StationDTO> getAllStationsApi(AuthDTO authDTO) {
		List<StationDTO> list = new ArrayList<StationDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT stat.code,stat.name,tion.code,tion.name,tion.active_flag FROM station tion,namespace_station nsst,state stat WHERE stat.id= tion.state_id AND nsst.station_id = tion.id AND stat.active_flag  = 1 AND nsst.active_flag = 1 AND tion.active_flag = 1 AND nsst.namespace_id = ?");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				StationDTO dto = new StationDTO();
				dto.setCode(selectRS.getString("tion.code"));
				dto.setName(selectRS.getString("tion.name"));
				dto.setActiveFlag(selectRS.getInt("active_flag"));
				StateDTO stateDTO = new StateDTO();
				stateDTO.setCode(selectRS.getString("stat.code"));
				stateDTO.setName(selectRS.getString("stat.name"));
				dto.setState(stateDTO);
				list.add(dto);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public List<StationDTO> getAllStations() {
		List<StationDTO> list = new ArrayList<StationDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT  stat.code,stat.name,stat.latitude,stat.longitude,stat.radius,stat.api_flag, stte.code,stte.name FROM station stat,state stte where stte.id = stat.state_id and stte.active_flag  = 1 and stat.active_flag = 1");
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				StationDTO station = new StationDTO();
				StateDTO state = new StateDTO();
				station.setCode(selectRS.getString("stat.code"));
				station.setName(selectRS.getString("stat.name"));
				station.setLatitude(selectRS.getString("stat.latitude"));
				station.setLongitude(selectRS.getString("stat.longitude"));
				station.setRadius(selectRS.getInt("stat.radius"));
				station.setApiFlag(selectRS.getInt("stat.api_flag"));

				state.setCode(selectRS.getString("stte.code"));
				state.setName(selectRS.getString("stte.name"));
				station.setState(state);
				list.add(station);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public List<StationDTO> getAllforZoneSync(String syncDate) {
		List<StationDTO> list = new ArrayList<StationDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT stat.id,stat.code,stat.name,stat.latitude,stat.longitude,stat.radius,stat.api_flag,stte.code,stte.name,stat.active_flag FROM station stat,state stte WHERE stte.id = stat.state_id AND DATE(stat.updated_at) >= ?");
			selectPS.setString(1, syncDate);
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				StationDTO dto = new StationDTO();
				StateDTO stateDTO = new StateDTO();
				stateDTO.setCode(selectRS.getString("stte.code"));
				stateDTO.setName(selectRS.getString("stte.name").trim());
				dto.setState(stateDTO);
				dto.setId(selectRS.getInt("stat.id"));
				dto.setCode(selectRS.getString("stat.code"));
				dto.setName(selectRS.getString("stat.name").trim());
				dto.setLatitude(selectRS.getString("stat.latitude"));
				dto.setLongitude(selectRS.getString("stat.longitude"));
				dto.setRadius(selectRS.getInt("stat.radius"));
				dto.setApiFlag(selectRS.getInt("stat.api_flag"));
				dto.setActiveFlag(selectRS.getInt("stat.active_flag"));
				list.add(dto);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public String getZoneSyncDate(AuthDTO authDTO) {
		String zoneSyncDate = null;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT DATE(MAX(updated_at)) as zoneSyncDate FROM station");
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				zoneSyncDate = selectRS.getString("zoneSyncDate");
			}
			if (StringUtil.isNull(zoneSyncDate)) {
				zoneSyncDate = "2014-02-12";
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return zoneSyncDate;
	}

	public List<StationDTO> updateZoneSync(AuthDTO authDTO, List<StationDTO> list) {
		try {
			int batchCount = 0;
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_STATION_ZONESYNC(?,?,?,?,?, ?,?,?,?)}");
			for (StationDTO stationDTO : list) {
				try {
					int pindex = 0;
					callableStatement.setString(++pindex, stationDTO.getCode());
					callableStatement.setString(++pindex, stationDTO.getName().trim());
					callableStatement.setString(++pindex, stationDTO.getState().getCode());
					callableStatement.setString(++pindex, stationDTO.getLatitude());
					callableStatement.setString(++pindex, stationDTO.getLongitude());
					callableStatement.setInt(++pindex, stationDTO.getRadius());
					callableStatement.setInt(++pindex, stationDTO.getApiFlag());
					callableStatement.setInt(++pindex, stationDTO.getActiveFlag());
					callableStatement.setInt(++pindex, authDTO.getUser().getId());
					callableStatement.addBatch();
					batchCount++;

					if (batchCount > 100) {
						callableStatement.executeBatch();
						callableStatement.clearBatch();
						callableStatement.clearParameters();
						batchCount = 0;
					}

				}
				catch (Exception e) {
					e.printStackTrace();
					System.out.println(stationDTO.getCode() + "-" + stationDTO.getName());
				}
			}
			if (batchCount > 0) {
				callableStatement.executeBatch();
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public List<StationDTO> getStationAndStationPoint(AuthDTO authDTO) {
		List<StationDTO> list = new ArrayList<StationDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT stat.id,stat.code,stat.name,stat.api_flag,stte.code,stte.name,stat.active_flag FROM station stat, namespace_station nsst, state stte WHERE nsst.namespace_id = ? AND stte.id = stat.state_id AND nsst.station_id = stat.id AND stte.active_flag  = 1 AND nsst.active_flag  = 1 AND stat.active_flag < 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				StationDTO stationDTO = new StationDTO();
				stationDTO.setId(selectRS.getInt("stat.id"));
				stationDTO.setCode(selectRS.getString("stat.code"));
				stationDTO.setName(selectRS.getString("stat.name"));
				stationDTO.setApiFlag(selectRS.getInt("stat.api_flag"));

				StateDTO stateDTO = new StateDTO();
				stateDTO.setCode(selectRS.getString("stte.code"));
				stateDTO.setName(selectRS.getString("stte.name"));
				stationDTO.setState(stateDTO);

				stationDTO.setActiveFlag(selectRS.getInt("stat.active_flag"));

				List<StationPointDTO> stationPointList = new ArrayList<StationPointDTO>();
				@Cleanup
				PreparedStatement selectPointPS = connection.prepareStatement("SELECT code, name, address, landmark, contact_number, latitude, longitude, map_url, active_flag FROM station_point WHERE namespace_id = ? AND station_id = ? AND active_flag < 2");
				selectPointPS.setInt(1, authDTO.getNamespace().getId());
				selectPointPS.setInt(2, stationDTO.getId());
				@Cleanup
				ResultSet selectPointRS = selectPointPS.executeQuery();
				while (selectPointRS.next()) {
					StationPointDTO pointDTO = new StationPointDTO();
					pointDTO.setCode(selectPointRS.getString("code"));
					pointDTO.setName(selectPointRS.getString("name"));
					pointDTO.setAddress(selectPointRS.getString("address"));
					pointDTO.setLandmark(selectPointRS.getString("landmark"));
					pointDTO.setNumber(selectPointRS.getString("contact_number"));
					pointDTO.setLatitude(selectPointRS.getString("latitude"));
					pointDTO.setLongitude(selectPointRS.getString("longitude"));
					pointDTO.setMapUrl(selectPointRS.getString("map_url"));
					pointDTO.setActiveFlag(selectPointRS.getInt("active_flag"));
					stationPointList.add(pointDTO);
				}
				stationDTO.setStationPoints(stationPointList);

				list.add(stationDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public void updateStationOtaPartner(AuthDTO authDTO, StationOtaPartnerDTO stationOtaPartnerDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int psCount = 0;
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE station_ota_partner SET station_code = ?, updated_at = NOW() WHERE code = ? AND active_flag = 1");
			ps.setString(++psCount, stationOtaPartnerDTO.getStationCode());
			ps.setString(++psCount, stationOtaPartnerDTO.getCode());
			ps.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addStationOtaPartner(AuthDTO authDTO, StationOtaPartnerDTO stationOtaPartnerDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int psCount = 0;
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("INSERT INTO station_ota_partner (code, ota_station_code, ota_station_name, state_id, station_code, ota_partner_id, active_flag, updated_at) VALUES (?,?,?,?,'NA', ?,1,NOW())");
			ps.setString(++psCount, stationOtaPartnerDTO.getCode());
			ps.setString(++psCount, stationOtaPartnerDTO.getOtaStationCode());
			ps.setString(++psCount, stationOtaPartnerDTO.getOtaStationName());
			ps.setInt(++psCount, stationOtaPartnerDTO.getState().getId());
			ps.setInt(++psCount, stationOtaPartnerDTO.getOtaPartner().getId());
			ps.executeUpdate();
		}
		catch (SQLIntegrityConstraintViolationException e) {
			throw new ServiceException(ErrorCode.UNABLE_TO_UPDATE, e.getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateStationOtaPartnerV2(AuthDTO authDTO, List<StationOtaPartnerDTO> stationOtaList) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE station_ota_partner SET station_code = ?, updated_at = NOW() WHERE code = ? AND active_flag = 1");
			for (StationOtaPartnerDTO stationOtaPartnerDTO : stationOtaList) {
				ps.setString(1, stationOtaPartnerDTO.getStationCode());
				ps.setString(2, stationOtaPartnerDTO.getCode());
				ps.executeUpdate();
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<StationOtaPartnerDTO> getStationOtaPartners(AuthDTO authDTO, StationOtaPartnerDTO stationOtaPartnerDTO) {
		List<StationOtaPartnerDTO> stationOtaPartners = new ArrayList<>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (stationOtaPartnerDTO.getState().getId() != 0) {
				selectPS = connection.prepareStatement("SELECT code, ota_station_code, ota_station_name, state_id, station_code, ota_partner_id, active_flag FROM station_ota_partner WHERE ota_partner_id = ? AND state_id = ? AND active_flag = 1");
				selectPS.setInt(1, stationOtaPartnerDTO.getOtaPartner().getId());
				selectPS.setInt(2, stationOtaPartnerDTO.getState().getId());
			}
			else {
				selectPS = connection.prepareStatement("SELECT code, ota_station_code, ota_station_name, state_id, station_code, ota_partner_id, active_flag FROM station_ota_partner WHERE ota_partner_id = ? AND active_flag = 1");
				selectPS.setInt(1, stationOtaPartnerDTO.getOtaPartner().getId());
			}

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				StationOtaPartnerDTO stationOtaPartner = new StationOtaPartnerDTO();
				stationOtaPartner.setCode(selectRS.getString("code"));
				stationOtaPartner.setOtaStationCode(selectRS.getString("ota_station_code"));
				stationOtaPartner.setOtaStationName(selectRS.getString("ota_station_name"));
				StateDTO state = new StateDTO();
				state.setId(selectRS.getInt("state_id"));
				stationOtaPartner.setState(state);
				List<StationDTO> stations = convertStationList(selectRS.getString("station_code"));
				stationOtaPartner.setStations(stations);
				stationOtaPartner.setOtaPartner(OTAPartnerEM.getOtaPartnerEM(selectRS.getInt("ota_partner_id")));
				stationOtaPartner.setActiveFlag(selectRS.getInt("active_flag"));
				stationOtaPartners.add(stationOtaPartner);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return stationOtaPartners;
	}

	public List<StationOtaPartnerDTO> getOtaStation(AuthDTO authDTO, StationOtaPartnerDTO stationOtaPartnerDTO) {
		List<StationOtaPartnerDTO> stationOtaPartners = new ArrayList<>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, ota_station_code, ota_station_name, state_id, ota_partner_id, active_flag FROM station_ota_partner WHERE ota_partner_id = ? AND FIND_IN_SET(?, station_code) AND active_flag = 1");
			for (StationDTO stationDTO : stationOtaPartnerDTO.getStations()) {
				selectPS.setInt(1, stationOtaPartnerDTO.getOtaPartner().getId());
				selectPS.setString(2, stationDTO.getCode());

				@Cleanup
				ResultSet selectRS = selectPS.executeQuery();
				while (selectRS.next()) {
					StationOtaPartnerDTO stationOtaPartner = new StationOtaPartnerDTO();
					stationOtaPartner.setCode(selectRS.getString("code"));
					stationOtaPartner.setOtaStationCode(selectRS.getString("ota_station_code"));
					stationOtaPartner.setOtaStationName(selectRS.getString("ota_station_name"));
					StateDTO state = new StateDTO();
					state.setId(selectRS.getInt("state_id"));
					stationOtaPartner.setState(state);
					stationOtaPartner.setStations(Arrays.asList(stationDTO));
					stationOtaPartner.setOtaPartner(OTAPartnerEM.getOtaPartnerEM(selectRS.getInt("ota_partner_id")));
					stationOtaPartner.setActiveFlag(selectRS.getInt("active_flag"));
					stationOtaPartners.add(stationOtaPartner);
				}
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return stationOtaPartners;
	}

	public StationOtaPartnerDTO getOtaStationByCode(AuthDTO authDTO, StationOtaPartnerDTO stationOtaPartnerDTO) {
		StationOtaPartnerDTO stationOtaPartner = null;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, ota_station_code, ota_station_name, state_id, station_code, ota_partner_id, active_flag FROM station_ota_partner WHERE code = ? AND active_flag = 1");
			selectPS.setString(1, stationOtaPartnerDTO.getCode());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				stationOtaPartner = new StationOtaPartnerDTO();
				stationOtaPartner.setCode(selectRS.getString("code"));
				stationOtaPartner.setOtaStationCode(selectRS.getString("ota_station_code"));
				stationOtaPartner.setOtaStationName(selectRS.getString("ota_station_name"));
				StateDTO state = new StateDTO();
				state.setId(selectRS.getInt("state_id"));
				stationOtaPartner.setState(state);

				List<StationDTO> stations = convertStationList(selectRS.getString("station_code"));
				stationOtaPartner.setStations(stations);

				stationOtaPartner.setOtaPartner(OTAPartnerEM.getOtaPartnerEM(selectRS.getInt("ota_partner_id")));
				stationOtaPartner.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return stationOtaPartner;
	}

	public StationOtaPartnerDTO getOtaPartnerStationByCode(AuthDTO authDTO, StationOtaPartnerDTO stationOtaPartnerDTO) {
		StationOtaPartnerDTO stationOtaPartner = new StationOtaPartnerDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, ota_station_code, ota_station_name, state_id, station_code FROM station_ota_partner WHERE ota_station_code = ? AND ota_partner_id = ? AND active_flag = 1");
			selectPS.setString(1, stationOtaPartnerDTO.getOtaStationCode());
			selectPS.setInt(2, stationOtaPartnerDTO.getOtaPartner().getId());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				stationOtaPartner.setCode(selectRS.getString("code"));
				stationOtaPartner.setOtaStationCode(selectRS.getString("ota_station_code"));
				stationOtaPartner.setOtaStationName(selectRS.getString("ota_station_name"));
				StateDTO state = new StateDTO();
				state.setId(selectRS.getInt("state_id"));
				stationOtaPartner.setState(state);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return stationOtaPartner;
	}

	public List<StationOtaPartnerDTO> getStationOtaforZoneSync(String syncDate) {
		List<StationOtaPartnerDTO> stationOtaPartners = new ArrayList<>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			selectPS = connection.prepareStatement("SELECT code, ota_station_code, ota_station_name, state_id, station_code, ota_partner_id, active_flag FROM station_ota_partner WHERE station_code != 'NA' AND DATE(updated_at) >= ?");
			selectPS.setString(1, syncDate);
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				StationOtaPartnerDTO stationOtaPartner = new StationOtaPartnerDTO();
				stationOtaPartner.setCode(selectRS.getString("code"));
				stationOtaPartner.setOtaStationCode(selectRS.getString("ota_station_code"));
				stationOtaPartner.setOtaStationName(selectRS.getString("ota_station_name"));

				StateDTO state = new StateDTO();
				state.setId(selectRS.getInt("state_id"));
				stationOtaPartner.setState(state);

				List<StationDTO> stations = convertStationList(selectRS.getString("station_code"));
				stationOtaPartner.setStations(stations);
				stationOtaPartner.setOtaPartner(OTAPartnerEM.getOtaPartnerEM(selectRS.getInt("ota_partner_id")));
				stationOtaPartner.setActiveFlag(selectRS.getInt("active_flag"));
				stationOtaPartners.add(stationOtaPartner);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return stationOtaPartners;
	}

	public List<StationOtaPartnerDTO> updateStationOtaZoneSync(AuthDTO authDTO, List<StationOtaPartnerDTO> list) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_STATION_OTA_PARTNER_ZONESYNC(?,?,?,?,? ,?,?)}");
			for (StationOtaPartnerDTO stationOtaPartner : list) {
				int pindex = 0;
				callableStatement.setString(++pindex, stationOtaPartner.getCode());
				callableStatement.setString(++pindex, stationOtaPartner.getOtaStationCode());
				callableStatement.setString(++pindex, stationOtaPartner.getOtaStationName());
				callableStatement.setString(++pindex, stationOtaPartner.getState().getCode());
				callableStatement.setString(++pindex, stationOtaPartner.getStationCode());
				callableStatement.setInt(++pindex, stationOtaPartner.getOtaPartner().getId());
				callableStatement.setInt(++pindex, stationOtaPartner.getActiveFlag());
				callableStatement.execute();
				callableStatement.clearParameters();
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public String getStationOtaZoneSyncDate(AuthDTO authDTO) {
		String zoneSyncDate = null;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT DATE(MAX(updated_at)) as zoneSyncDate FROM station_ota_partner");
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				zoneSyncDate = selectRS.getString("zoneSyncDate");
			}
			if (StringUtil.isNull(zoneSyncDate)) {
				zoneSyncDate = "2020-11-20";
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return zoneSyncDate;
	}

	private List<StationDTO> convertStationList(String existingIds) {
		List<StationDTO> stationList = new ArrayList<>();
		if (StringUtil.isNotNull(existingIds)) {
			List<String> stationCode = Arrays.asList(existingIds.split(Text.COMMA));
			for (String code : stationCode) {
				if (StringUtil.isNull(code) || Numeric.ZERO.equals(code)) {
					continue;
				}

				StationDTO stationDTO = new StationDTO();
				stationDTO.setCode(code);
				stationList.add(stationDTO);
			}
		}
		return stationList;
	}

	public void updateStationArea(AuthDTO authDTO, StationAreaDTO stationAreaDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_STATION_AREA_IUD(?,?,?,?,?, ?,?,?,?,?)}");
			callableStatement.setString(++pindex, stationAreaDTO.getCode());
			callableStatement.setString(++pindex, stationAreaDTO.getName().trim());
			callableStatement.setString(++pindex, stationAreaDTO.getStation().getCode());
			callableStatement.setString(++pindex, stationAreaDTO.getLatitude());
			callableStatement.setString(++pindex, stationAreaDTO.getLongitude());
			callableStatement.setInt(++pindex, stationAreaDTO.getRadius());
			callableStatement.setInt(++pindex, stationAreaDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				stationAreaDTO.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public List<StationAreaDTO> getStationAreas(StationDTO stationDTO) {
		List<StationAreaDTO> stationAreas = new ArrayList<StationAreaDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT stat.id, stat.code, stat.name, stat.latitude, stat.longitude, stat.radius, stte.code, stte.name, stat.active_flag FROM station_area stat, station stte WHERE stte.id = stat.station_id and stte.active_flag  = 1 and stat.active_flag < 2 AND stte.code  = ?");
			selectPS.setString(1, stationDTO.getCode());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				StationAreaDTO stationAreaDTO = new StationAreaDTO();

				stationDTO.setCode(selectRS.getString("stte.code"));
				stationDTO.setName(selectRS.getString("stte.name"));
				stationAreaDTO.setStation(stationDTO);

				stationAreaDTO.setId(selectRS.getInt("stat.id"));
				stationAreaDTO.setCode(selectRS.getString("stat.code"));
				stationAreaDTO.setName(selectRS.getString("stat.name"));
				stationAreaDTO.setLatitude(selectRS.getString("stat.latitude"));
				stationAreaDTO.setLongitude(selectRS.getString("stat.longitude"));
				stationAreaDTO.setRadius(selectRS.getInt("stat.radius"));
				stationAreaDTO.setActiveFlag(selectRS.getInt("stat.active_flag"));
				stationAreas.add(stationAreaDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return stationAreas;
	}

	public void getStationArea(StationAreaDTO stationAreaDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (stationAreaDTO.getId() != 0) {
				selectPS = connection.prepareStatement("SELECT stat.id, stat.code, stat.name, stat.latitude, stat.longitude, stat.radius, stte.code, stte.name, stat.active_flag FROM station_area stat, station stte WHERE stte.id = stat.station_id and stte.active_flag  = 1 and stat.active_flag < 2 AND stat.id  = ?");
				selectPS.setInt(1, stationAreaDTO.getId());
			}
			else {
				selectPS = connection.prepareStatement("SELECT stat.id, stat.code, stat.name, stat.latitude, stat.longitude, stat.radius, stte.code, stte.name, stat.active_flag FROM station_area stat, station stte WHERE stte.id = stat.station_id and stte.active_flag  = 1 and stat.active_flag < 2 AND stat.code  = ?");
				selectPS.setString(1, stationAreaDTO.getCode());
			}
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				StationDTO stationDTO = new StationDTO();
				stationDTO.setCode(selectRS.getString("stte.code"));
				stationDTO.setName(selectRS.getString("stte.name"));
				stationAreaDTO.setStation(stationDTO);

				stationAreaDTO.setId(selectRS.getInt("stat.id"));
				stationAreaDTO.setCode(selectRS.getString("stat.code"));
				stationAreaDTO.setName(selectRS.getString("stat.name"));
				stationAreaDTO.setLatitude(selectRS.getString("stat.latitude"));
				stationAreaDTO.setLongitude(selectRS.getString("stat.longitude"));
				stationAreaDTO.setRadius(selectRS.getInt("stat.radius"));
				stationAreaDTO.setActiveFlag(selectRS.getInt("stat.active_flag"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public String getStationAreaZoneSyncDate(AuthDTO authDTO) {
		String zoneSyncDate = null;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT DATE(MAX(updated_at)) as zoneSyncDate FROM station_area");
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				zoneSyncDate = selectRS.getString("zoneSyncDate");
			}
			if (StringUtil.isNull(zoneSyncDate)) {
				zoneSyncDate = "2021-02-10";
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return zoneSyncDate;
	}

	public List<StationAreaDTO> getStationAreaZoneSync(String syncDate) {
		List<StationAreaDTO> stationAreaList = new ArrayList<StationAreaDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT stat.id, stat.code, stat.name, stat.latitude, stat.longitude, stat.radius, stte.code, stte.name, stat.active_flag FROM station_area stat, station stte WHERE stte.id = stat.station_id AND DATE(stat.updated_at) >= ?");
			selectPS.setString(1, syncDate);
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				StationAreaDTO stationAreaDTO = new StationAreaDTO();
				StationDTO stationDTO = new StationDTO();
				stationDTO.setCode(selectRS.getString("stte.code"));
				stationDTO.setName(selectRS.getString("stte.name"));
				stationAreaDTO.setStation(stationDTO);

				stationAreaDTO.setId(selectRS.getInt("stat.id"));
				stationAreaDTO.setCode(selectRS.getString("stat.code"));
				stationAreaDTO.setName(selectRS.getString("stat.name"));
				stationAreaDTO.setLatitude(selectRS.getString("stat.latitude"));
				stationAreaDTO.setLongitude(selectRS.getString("stat.longitude"));
				stationAreaDTO.setRadius(selectRS.getInt("stat.radius"));
				stationAreaDTO.setActiveFlag(selectRS.getInt("stat.active_flag"));
				stationAreaList.add(stationAreaDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return stationAreaList;
	}

	public void updateStationAreaZoneSync(AuthDTO authDTO, List<StationAreaDTO> list) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_STATION_AREA_ZONESYNC_IUD(?,?,?,?,?, ?,?,?)}");
			for (StationAreaDTO stationAreaDTO : list) {
				pindex = 0;
				callableStatement.setString(++pindex, stationAreaDTO.getCode());
				callableStatement.setString(++pindex, stationAreaDTO.getName().trim());
				callableStatement.setString(++pindex, stationAreaDTO.getStation().getCode());
				callableStatement.setString(++pindex, stationAreaDTO.getLatitude());
				callableStatement.setString(++pindex, stationAreaDTO.getLongitude());
				callableStatement.setInt(++pindex, stationAreaDTO.getRadius());
				callableStatement.setInt(++pindex, stationAreaDTO.getActiveFlag());
				callableStatement.setInt(++pindex, authDTO.getUser().getId());
				callableStatement.execute();
				callableStatement.clearParameters();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public void removeTopRouteFlag(AuthDTO authDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("UPDATE route SET top_route = 0, booking_count = 0 WHERE namespace_id = ?");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public void updateTopRouteFlag(AuthDTO authDTO, List<RouteDTO> routes) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("UPDATE route SET top_route = ?, booking_count = ? WHERE namespace_id = ? AND from_station_id = ? AND to_station_id = ?");
			for (RouteDTO routeDTO : routes) {
				selectPS.setInt(1, routeDTO.getTopRouteFlag());
				selectPS.setInt(2, routeDTO.getBookingCount());
				selectPS.setInt(3, authDTO.getNamespace().getId());
				selectPS.setInt(4, routeDTO.getFromStation().getId());
				selectPS.setInt(5, routeDTO.getToStation().getId());
				selectPS.addBatch();
			}
			selectPS.executeBatch();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public List<StationDTO> getRelatedStationIds(String relatedStationIds) {
		List<StationDTO> relatedStations = new ArrayList<>();
		List<String> relatedStationids = Arrays.asList(relatedStationIds.split(Text.COMMA));
		for (String relatedStationId : relatedStationids) {
			StationDTO stationDTO = new StationDTO();
			stationDTO.setId(StringUtil.getIntegerValue(relatedStationId));
			if (stationDTO.getId() != 0) {
				relatedStations.add(stationDTO);
			}
		}
		return relatedStations;
	}
}
