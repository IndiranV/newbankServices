package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.BusIO;
import org.in.com.controller.web.io.BusSeatLayoutIO;
import org.in.com.controller.web.io.GroupIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.ScheduleSeatPreferenceIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.ScheduleSeatPreferenceDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.enumeration.SeatGendarEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.ScheduleTripSeatPreferenceService;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/{authtoken}/trip")
public class ScheduleTripSeatPreferenceController extends BaseController {

	@Autowired
	ScheduleTripSeatPreferenceService tripSeatPreferenceService;

	@RequestMapping(value = "/{tripCode}/seat/preference/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<ScheduleSeatPreferenceIO> addTripSeatPereference(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode, @RequestBody ScheduleSeatPreferenceIO seatPreference) throws Exception {
		ScheduleSeatPreferenceIO preferenceIO = new ScheduleSeatPreferenceIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ScheduleSeatPreferenceDTO preferenceDTO = new ScheduleSeatPreferenceDTO();

			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(tripCode);

			List<BusSeatLayoutDTO> layoutList = new ArrayList<>();
			if (seatPreference.getBusSeatLayout() != null) {
				for (BusSeatLayoutIO layoutIO : seatPreference.getBusSeatLayout()) {
					if (StringUtil.isNull(layoutIO.getCode())) {
						continue;
					}
					BusSeatLayoutDTO layoutDTO = new BusSeatLayoutDTO();
					layoutDTO.setCode(layoutIO.getCode());
					layoutList.add(layoutDTO);
				}
			}
			BusDTO busDTO = new BusDTO();
			BusSeatLayoutDTO busSeatLayoutDTO = new BusSeatLayoutDTO();
			busSeatLayoutDTO.setList(layoutList);
			busDTO.setBusSeatLayoutDTO(busSeatLayoutDTO);
			preferenceDTO.setBus(busDTO);

			List<GroupDTO> groupList = new ArrayList<>();
			if (seatPreference.getGroupList() != null) {
				for (GroupIO group : seatPreference.getGroupList()) {
					if (StringUtil.isNull(group.getCode())) {
						continue;
					}
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setCode(group.getCode());
					groupList.add(groupDTO);
				}
			}
			preferenceDTO.setGroupList(groupList);
			preferenceDTO.setGendar(StringUtil.isNotNull(seatPreference.getPreferenceGendar()) ? SeatGendarEM.getSeatGendarEM(seatPreference.getPreferenceGendar()) : null);
			preferenceDTO.setActiveFlag(seatPreference.getActiveFlag());
			preferenceDTO = tripSeatPreferenceService.updateTripSeatPereference(authDTO, tripDTO, preferenceDTO);

			preferenceIO.setCode(preferenceDTO.getCode());
			preferenceIO.setActiveFlag(preferenceDTO.getActiveFlag());
		}
		return ResponseIO.success(preferenceIO);
	}

	@RequestMapping(value = "/seat/preference/{tripCode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<ScheduleSeatPreferenceIO>> getTripSeatPereference(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode) throws Exception {
		List<ScheduleSeatPreferenceIO> seatPreferenceIOList = new ArrayList<ScheduleSeatPreferenceIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(tripCode);

			List<ScheduleSeatPreferenceDTO> seatPreferenceList = tripSeatPreferenceService.getScheduleTripSeatPreference(authDTO, tripDTO);
			for (ScheduleSeatPreferenceDTO seatPreferenceDTO : seatPreferenceList) {
				ScheduleSeatPreferenceIO seatPreferenceIO = new ScheduleSeatPreferenceIO();
				seatPreferenceIO.setCode(seatPreferenceDTO.getCode());
				seatPreferenceIO.setActiveFrom(seatPreferenceDTO.getActiveFrom());
				seatPreferenceIO.setActiveTo(seatPreferenceDTO.getActiveTo());
				seatPreferenceIO.setDayOfWeek(seatPreferenceDTO.getDayOfWeek());
				seatPreferenceIO.setPreferenceGendar(seatPreferenceDTO.getGendar().getCode());

				BusIO busIO = new BusIO();
				busIO.setCode(seatPreferenceDTO.getBus().getCode());
				busIO.setName(seatPreferenceDTO.getBus().getName());
				busIO.setCategoryCode(seatPreferenceDTO.getBus().getCategoryCode() == null ? "" : seatPreferenceDTO.getBus().getCategoryCode());
				busIO.setDisplayName(seatPreferenceDTO.getBus().getDisplayName() == null ? "" : seatPreferenceDTO.getBus().getDisplayName());

				List<BusSeatLayoutIO> busLayoutList = new ArrayList<BusSeatLayoutIO>();
				for (BusSeatLayoutDTO layoutDTO : seatPreferenceDTO.getBus().getBusSeatLayoutDTO().getList()) {
					BusSeatLayoutIO busSeatLayout = new BusSeatLayoutIO();
					busSeatLayout.setCode(layoutDTO.getCode());
					busSeatLayout.setCode(layoutDTO.getCode());
					busSeatLayout.setSeatName(layoutDTO.getName());
					busSeatLayout.setColPos(layoutDTO.getColPos());
					busSeatLayout.setRowPos(layoutDTO.getRowPos());
					busSeatLayout.setLayer(layoutDTO.getLayer());
					busSeatLayout.setActiveFlag(layoutDTO.getActiveFlag());
					busLayoutList.add(busSeatLayout);
				}
				busIO.setSeatLayoutList(busLayoutList);
				seatPreferenceIO.setBus(busIO);

				List<GroupIO> groupList = new ArrayList<GroupIO>();
				for (GroupDTO groupDTO : seatPreferenceDTO.getGroupList()) {
					GroupIO groupIO = new GroupIO();
					groupIO.setCode(groupDTO.getCode());
					groupIO.setName(groupDTO.getName());
					groupList.add(groupIO);
				}
				seatPreferenceIO.setGroupList(groupList);

				List<ScheduleSeatPreferenceIO> seatPreferenceOverrideList = new ArrayList<ScheduleSeatPreferenceIO>();
				for (ScheduleSeatPreferenceDTO seatOverrideDTO : seatPreferenceDTO.getOverrideList()) {
					ScheduleSeatPreferenceIO seatPreferenceOverrideIO = new ScheduleSeatPreferenceIO();
					seatPreferenceOverrideIO.setCode(seatOverrideDTO.getCode());
					seatPreferenceOverrideIO.setActiveFrom(seatOverrideDTO.getActiveFrom());
					seatPreferenceOverrideIO.setActiveTo(seatOverrideDTO.getActiveTo());
					seatPreferenceOverrideIO.setDayOfWeek(seatOverrideDTO.getDayOfWeek());
					seatPreferenceOverrideIO.setPreferenceGendar(seatOverrideDTO.getGendar() != null ? seatOverrideDTO.getGendar().getCode() : Text.NA);

					BusIO busOverrideIO = new BusIO();
					List<BusSeatLayoutIO> busLayoutOverrideList = new ArrayList<BusSeatLayoutIO>();
					for (BusSeatLayoutDTO layoutDTO : seatOverrideDTO.getBus().getBusSeatLayoutDTO().getList()) {
						BusSeatLayoutIO busSeatLayout = new BusSeatLayoutIO();
						busSeatLayout.setCode(layoutDTO.getCode());
						busSeatLayout.setSeatName(layoutDTO.getName());
						busSeatLayout.setColPos(layoutDTO.getColPos());
						busSeatLayout.setRowPos(layoutDTO.getRowPos());
						busSeatLayout.setLayer(layoutDTO.getLayer());
						busSeatLayout.setActiveFlag(layoutDTO.getActiveFlag());
						busLayoutOverrideList.add(busSeatLayout);
					}
					busOverrideIO.setSeatLayoutList(busLayoutList);
					seatPreferenceOverrideIO.setBus(busOverrideIO);

					List<GroupIO> GroupOverrideList = new ArrayList<GroupIO>();
					for (GroupDTO groupDTO : seatOverrideDTO.getGroupList()) {
						GroupIO groupIO = new GroupIO();
						groupIO.setCode(groupDTO.getCode());
						groupIO.setName(groupDTO.getName());
						GroupOverrideList.add(groupIO);
					}
					seatPreferenceOverrideIO.setGroupList(groupList);

					seatPreferenceOverrideIO.setActiveFlag(seatOverrideDTO.getActiveFlag());
					seatPreferenceOverrideList.add(seatPreferenceOverrideIO);
				}
				seatPreferenceIO.setActiveFlag(seatPreferenceDTO.getActiveFlag());
				seatPreferenceIO.setOverrideList(seatPreferenceOverrideList);

				seatPreferenceIOList.add(seatPreferenceIO);
			}
		}
		return ResponseIO.success(seatPreferenceIOList);
	}

	@RequestMapping(value = "/{tripCode}/seat/preference/remove", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> removeTripSeatPereference(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode, @RequestBody ScheduleSeatPreferenceIO seatPreference) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			if (StringUtil.isNull(seatPreference.getCode()) || seatPreference.getActiveFlag() == Numeric.ONE_INT) {
				throw new ServiceException(ErrorCode.UPDATE_FAIL);
			}

			ScheduleSeatPreferenceDTO seatPreferenceDTO = new ScheduleSeatPreferenceDTO();
			seatPreferenceDTO.setCode(seatPreference.getCode());
			seatPreferenceDTO.setBus(new BusDTO());
			seatPreferenceDTO.setGendar(SeatGendarEM.getSeatGendarEM(seatPreference.getPreferenceGendar()));
			seatPreferenceDTO.setActiveFlag(seatPreference.getActiveFlag());

			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(tripCode);

			tripSeatPreferenceService.removeTripSeatPereference(authDTO, tripDTO, seatPreferenceDTO);
		}
		return ResponseIO.success();

	}
}
