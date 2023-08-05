package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.NamespaceBannerDTO;
import org.in.com.dto.NamespaceBannerDetailsDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.MediaTypeEM;
import org.in.com.exception.ServiceException;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;

import lombok.Cleanup;

public class NamespaceBannerDAO {

	public NamespaceBannerDTO getBanner(AuthDTO authDTO, NamespaceBannerDTO bannerDTO) {
		NamespaceBannerDTO banner = new NamespaceBannerDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (bannerDTO.getId() != 0) {
				selectPS = connection.prepareStatement("SELECT id, code, name, group_id, display_model, device_medium_id, from_date, to_date, day_of_week, color, updated_at, active_flag FROM namespace_banner WHERE namespace_id = ? AND id = ? AND active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, bannerDTO.getId());
			}
			else if (StringUtil.isNotNull(bannerDTO.getCode())) {
				selectPS = connection.prepareStatement("SELECT id, code, name, group_id, display_model, device_medium_id, from_date, to_date, day_of_week, color, updated_at, active_flag FROM namespace_banner WHERE namespace_id = ? AND code = ? AND active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, bannerDTO.getCode());
			}
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				banner.setId(selectRS.getInt("id"));
				banner.setCode(selectRS.getString("code"));
				banner.setName(selectRS.getString("name"));
				banner.setGroup(convertGroup(selectRS.getString("group_id")));
				banner.setDisplayModel(selectRS.getString("display_model"));
				banner.setDeviceMedium(convertDeviceMedium(selectRS.getString("device_medium_id")));
				banner.setFromDate(DateUtil.getDateTime(selectRS.getString("from_date")));
				banner.setToDate(DateUtil.getDateTime(selectRS.getString("to_date")));
				banner.setDayOfWeek(selectRS.getString("day_of_week"));
				banner.setColor(selectRS.getString("color"));
				banner.setUpdatedAt(DateUtil.getDateTime(selectRS.getString("updated_at")));
				banner.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return banner;
	}

	public NamespaceBannerDTO updateBanner(AuthDTO authDTO, NamespaceBannerDTO bannerDTO) {
		NamespaceBannerDTO banner = new NamespaceBannerDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_NAMESPACE_BANNER_IUD(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?)}");
			callableStatement.setString(++pindex, bannerDTO.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, bannerDTO.getName());
			callableStatement.setString(++pindex, getGroups(bannerDTO.getGroup()));
			callableStatement.setString(++pindex, bannerDTO.getDisplayModel());
			callableStatement.setString(++pindex, getDeviceMediums(bannerDTO.getDeviceMedium()));
			callableStatement.setString(++pindex, DateUtil.convertDate(bannerDTO.getFromDate()));
			callableStatement.setString(++pindex, DateUtil.convertDate(bannerDTO.getToDate()));
			callableStatement.setString(++pindex, bannerDTO.getDayOfWeek());
			callableStatement.setString(++pindex, bannerDTO.getColor());
			callableStatement.setInt(++pindex, bannerDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				banner.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}

		return banner;
	}

	public List<NamespaceBannerDTO> getAllBanner(AuthDTO authDTO) {
		List<NamespaceBannerDTO> bannerList = new ArrayList<NamespaceBannerDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, code, name, group_id, display_model, device_medium_id, from_date, to_date, day_of_week, color, updated_at, active_flag FROM namespace_banner WHERE namespace_id = ? AND active_flag < 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				NamespaceBannerDTO banner = new NamespaceBannerDTO();
				banner.setId(selectRS.getInt("id"));
				banner.setCode(selectRS.getString("code"));
				banner.setName(selectRS.getString("name"));
				banner.setGroup(convertGroup(selectRS.getString("group_id")));
				banner.setDisplayModel(selectRS.getString("display_model"));
				banner.setDeviceMedium(convertDeviceMedium(selectRS.getString("device_medium_id")));
				banner.setFromDate(DateUtil.getDateTime(selectRS.getString("from_date")));
				banner.setToDate(DateUtil.getDateTime(selectRS.getString("to_date")));
				banner.setDayOfWeek(selectRS.getString("day_of_week"));
				banner.setColor(selectRS.getString("color"));
				banner.setUpdatedAt(DateUtil.getDateTime(selectRS.getString("updated_at")));
				banner.setActiveFlag(selectRS.getInt("active_flag"));
				bannerList.add(banner);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return bannerList;
	}

	public List<NamespaceBannerDTO> getActiveBanner(AuthDTO authDTO) {
		List<NamespaceBannerDTO> bannerList = new ArrayList<NamespaceBannerDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			selectPS = connection.prepareStatement("SELECT id, code, name, group_id, display_model, device_medium_id, from_date, to_date, day_of_week, color, updated_at, active_flag FROM namespace_banner WHERE namespace_id = ? AND display_model != 'A' AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				NamespaceBannerDTO banner = new NamespaceBannerDTO();
				banner.setId(selectRS.getInt("id"));
				banner.setCode(selectRS.getString("code"));
				banner.setName(selectRS.getString("name"));
				banner.setGroup(convertGroup(selectRS.getString("group_id")));
				banner.setDisplayModel(selectRS.getString("display_model"));
				banner.setDeviceMedium(convertDeviceMedium(selectRS.getString("device_medium_id")));
				banner.setFromDate(DateUtil.getDateTime(selectRS.getString("from_date")));
				banner.setToDate(DateUtil.getDateTime(selectRS.getString("to_date")));
				banner.setDayOfWeek(selectRS.getString("day_of_week"));
				banner.setColor(selectRS.getString("color"));
				banner.setUpdatedAt(DateUtil.getDateTime(selectRS.getString("updated_at")));
				banner.setActiveFlag(selectRS.getInt("active_flag"));

				List<NamespaceBannerDetailsDTO> bannerDetailsList = new ArrayList<NamespaceBannerDetailsDTO>();
				@Cleanup
				PreparedStatement selectDetailPS = connection.prepareStatement("SELECT code,  media_type_id, url, redirect_url, alt_text, sequence, active_flag FROM namespace_banner_details WHERE namespace_id = ? AND banner_id = ? AND active_flag = 1");
				selectDetailPS.setInt(1, authDTO.getNamespace().getId());
				selectDetailPS.setInt(2, banner.getId());
				@Cleanup
				ResultSet selectDetailRS = selectDetailPS.executeQuery();
				while (selectDetailRS.next()) {
					NamespaceBannerDetailsDTO bannerDetails = new NamespaceBannerDetailsDTO();
					bannerDetails.setCode(selectDetailRS.getString("code"));
					bannerDetails.setMediaType(MediaTypeEM.getMediaTypeEM(selectDetailRS.getInt("media_type_id")));
					bannerDetails.setUrl(selectDetailRS.getString("url"));
					bannerDetails.setRedirectUrl(selectDetailRS.getString("redirect_url"));
					bannerDetails.setAlternateText(selectDetailRS.getString("alt_text"));
					bannerDetails.setSequence(selectDetailRS.getInt("sequence"));
					bannerDetails.setActiveFlag(selectDetailRS.getInt("active_flag"));
					bannerDetailsList.add(bannerDetails);
				}
				banner.setBannerDetails(bannerDetailsList);
				bannerList.add(banner);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return bannerList;
	}
	
	public List<NamespaceBannerDTO> getActiveAdminBanner(String zoneCode) {
		List<NamespaceBannerDTO> bannerList = new ArrayList<NamespaceBannerDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			selectPS = connection.prepareStatement("SELECT nsb.id, nsb.code,nsb. name, nsb.group_id, nsb.display_model, nsb.device_medium_id, nsb.from_date, nsb.to_date, nsb.day_of_week, nsb.color, nsb.updated_at, nsb.active_flag FROM namespace_banner nsb, namespace ns WHERE nsb.namespace_id = ns.id AND ns.code = ? AND nsb.display_model = 'A' AND nsb.active_flag = 1 AND ns.active_flag = 1");
			selectPS.setString(1, zoneCode);
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				NamespaceBannerDTO banner = new NamespaceBannerDTO();
				banner.setId(selectRS.getInt("nsb.id"));
				banner.setCode(selectRS.getString("nsb.code"));
				banner.setName(selectRS.getString("nsb.name"));
				banner.setGroup(convertGroup(selectRS.getString("nsb.group_id")));
				banner.setDisplayModel(selectRS.getString("nsb.display_model"));
				banner.setDeviceMedium(convertDeviceMedium(selectRS.getString("nsb.device_medium_id")));
				banner.setFromDate(DateUtil.getDateTime(selectRS.getString("nsb.from_date")));
				banner.setToDate(DateUtil.getDateTime(selectRS.getString("nsb.to_date")));
				banner.setDayOfWeek(selectRS.getString("nsb.day_of_week"));
				banner.setColor(selectRS.getString("nsb.color"));
				banner.setUpdatedAt(DateUtil.getDateTime(selectRS.getString("nsb.updated_at")));
				banner.setActiveFlag(selectRS.getInt("nsb.active_flag"));

				List<NamespaceBannerDetailsDTO> bannerDetailsList = new ArrayList<NamespaceBannerDetailsDTO>();
				@Cleanup
				PreparedStatement selectDetailPS = connection.prepareStatement("SELECT nsbd.code, nsbd.media_type_id, nsbd.url, nsbd.redirect_url, nsbd.alt_text, nsbd.sequence, nsbd.active_flag FROM namespace_banner_details nsbd, namespace ns WHERE nsbd.namespace_id = ns.id AND ns.code = ? AND nsbd.banner_id = ? AND nsbd.active_flag = 1 AND ns.active_flag = 1");
				selectDetailPS.setString(1, zoneCode);
				selectDetailPS.setInt(2, banner.getId());
				@Cleanup
				ResultSet selectDetailRS = selectDetailPS.executeQuery();
				while (selectDetailRS.next()) {
					NamespaceBannerDetailsDTO bannerDetails = new NamespaceBannerDetailsDTO();
					bannerDetails.setCode(selectDetailRS.getString("nsbd.code"));
					bannerDetails.setMediaType(MediaTypeEM.getMediaTypeEM(selectDetailRS.getInt("nsbd.media_type_id")));
					bannerDetails.setUrl(selectDetailRS.getString("nsbd.url"));
					bannerDetails.setRedirectUrl(selectDetailRS.getString("nsbd.redirect_url"));
					bannerDetails.setAlternateText(selectDetailRS.getString("nsbd.alt_text"));
					bannerDetails.setSequence(selectDetailRS.getInt("nsbd.sequence"));
					bannerDetails.setActiveFlag(selectDetailRS.getInt("nsbd.active_flag"));
					bannerDetailsList.add(bannerDetails);
				}
				banner.setBannerDetails(bannerDetailsList);
				bannerList.add(banner);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return bannerList;
	}

	public List<NamespaceBannerDetailsDTO> getBannerDetails(AuthDTO authDTO, NamespaceBannerDTO bannerDTO) {
		List<NamespaceBannerDetailsDTO> bannerDetailsList = new ArrayList<NamespaceBannerDetailsDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, media_type_id, url, redirect_url, alt_text, sequence, active_flag FROM namespace_banner_details WHERE namespace_id = ? AND banner_id = ? AND active_flag < 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, bannerDTO.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				NamespaceBannerDetailsDTO bannerDetails = new NamespaceBannerDetailsDTO();
				bannerDetails.setCode(selectRS.getString("code"));
				bannerDetails.setMediaType(MediaTypeEM.getMediaTypeEM(selectRS.getInt("media_type_id")));
				bannerDetails.setUrl(selectRS.getString("url"));
				bannerDetails.setRedirectUrl(selectRS.getString("redirect_url"));
				bannerDetails.setAlternateText(selectRS.getString("alt_text"));
				bannerDetails.setSequence(selectRS.getInt("sequence"));
				bannerDetails.setActiveFlag(selectRS.getInt("active_flag"));
				bannerDetailsList.add(bannerDetails);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return bannerDetailsList;
	}

	public NamespaceBannerDTO updateBannerDetails(AuthDTO authDTO, NamespaceBannerDTO namespaceBanner) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_NAMESPACE_BANNER_DETAILS_IUD(?,?,?,?,?, ?,?,?,?,?, ?)}");
			for (NamespaceBannerDetailsDTO detailsDTO : namespaceBanner.getBannerDetails()) {
				pindex = 0;
				callableStatement.setString(++pindex, detailsDTO.getCode());
				callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
				callableStatement.setString(++pindex, namespaceBanner.getCode());
				callableStatement.setInt(++pindex, detailsDTO.getMediaType() != null ? detailsDTO.getMediaType().getId() : 0);
				callableStatement.setString(++pindex, detailsDTO.getUrl());
				callableStatement.setString(++pindex, detailsDTO.getRedirectUrl());
				callableStatement.setString(++pindex, detailsDTO.getAlternateText());
				callableStatement.setInt(++pindex, detailsDTO.getSequence());
				callableStatement.setInt(++pindex, detailsDTO.getActiveFlag());
				callableStatement.setInt(++pindex, authDTO.getUser().getId());
				callableStatement.setInt(++pindex, 0);
				callableStatement.addBatch();
			}
			callableStatement.executeBatch();
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}

		return namespaceBanner;
	}

	private String getGroups(List<GroupDTO> groupList) {
		StringBuilder groupIds = new StringBuilder();
		for (GroupDTO groupDTO : groupList) {
			if (groupDTO.getId() == Numeric.ZERO_INT) {
				continue;
			}
			groupIds.append(groupDTO.getId());
			groupIds.append(Text.COMMA);
		}

		return groupIds.toString();
	}

	private String getDeviceMediums(List<DeviceMediumEM> deivceMediumList) {
		StringBuilder deviceMediumIds = new StringBuilder();
		for (DeviceMediumEM deviceMedium : deivceMediumList) {
			deviceMediumIds.append(deviceMedium.getId());
			deviceMediumIds.append(Text.COMMA);
		}
		return deviceMediumIds.toString();
	}

	private List<GroupDTO> convertGroup(String groupIds) {
		List<GroupDTO> groupList = new ArrayList<GroupDTO>();
		if (StringUtil.isNotNull(groupIds)) {
			String[] groupId = groupIds.split(Text.COMMA);
			for (String id : groupId) {
				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setId(StringUtil.getIntegerValue(id));
				groupList.add(groupDTO);
			}
		}
		return groupList;
	}

	private List<DeviceMediumEM> convertDeviceMedium(String deviceMediumIds) {
		List<DeviceMediumEM> deviceMediumList = new ArrayList<DeviceMediumEM>();
		if (StringUtil.isNotNull(deviceMediumIds)) {
			String[] deviceMediumId = deviceMediumIds.split(Text.COMMA);
			for (String id : deviceMediumId) {
				deviceMediumList.add(DeviceMediumEM.getDeviceMediumEM(StringUtil.getIntegerValue(id)));
			}
		}
		return deviceMediumList;
	}
}
