package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.beanutils.BeanComparator;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.BusIO;
import org.in.com.controller.web.io.BusSeatLayoutIO;
import org.in.com.controller.web.io.BusSeatTypeIO;
import org.in.com.controller.web.io.BusTypeCategoryDetailsIO;
import org.in.com.controller.web.io.BusTypeCategoryIO;
import org.in.com.controller.web.io.BusVehicleIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.BusTypeCategoryDTO;
import org.in.com.dto.BusTypeCategoryDetailsDTO;
import org.in.com.dto.BusVehicleDTO;
import org.in.com.dto.enumeration.BusSeatTypeEM;
import org.in.com.dto.enumeration.GPSDeviceVendorEM;
import org.in.com.dto.enumeration.VehicleTypeEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.BusService;
import org.in.com.service.BusVehicleService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/{authtoken}/bus")
public class BusController extends BaseController {
	@Autowired
	BusService busService;
	@Autowired
	BusVehicleService busVehicleService;

	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<Collection<BusIO>> getAllBus(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "-1") int activeFlag) throws Exception {
		Collection<BusIO> busIOList = new ArrayList<BusIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			Collection<BusDTO> list = (Collection<BusDTO>) busService.getAll(authDTO);
			for (BusDTO dto : list) {
				if (activeFlag != -1 && activeFlag != dto.getActiveFlag()) {
					continue;
				}
				BusIO busIO = new BusIO();
				busIO.setCode(dto.getCode());
				busIO.setName(dto.getName());
				busIO.setCategoryCode(dto.getCategoryCode() == null ? "" : dto.getCategoryCode());
				busIO.setDisplayName(dto.getDisplayName() == null ? "" : dto.getDisplayName());
				busIO.setActiveFlag(dto.getActiveFlag());
				busIO.setSeatCount(dto.getSeatCount());
				busIOList.add(busIO);
			}
		}
		return ResponseIO.success(busIOList);
	}

	@RequestMapping(value = "/{buscode}/layout", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<Collection<BusSeatLayoutIO>> getBusLayout(@PathVariable("authtoken") String authtoken, @PathVariable("buscode") String buscode) throws Exception {
		Collection<BusSeatLayoutIO> layoutIOList = new ArrayList<BusSeatLayoutIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			BusDTO busDTO = new BusDTO();
			busDTO.setCode(buscode);
			Collection<BusSeatLayoutDTO> list = (Collection<BusSeatLayoutDTO>) busService.getBusLayout(authDTO, busDTO);
			for (BusSeatLayoutDTO layoutDTO : list) {
				BusSeatLayoutIO layoutIO = new BusSeatLayoutIO();
				layoutIO.setCode(layoutDTO.getCode());
				layoutIO.setSeatName(layoutDTO.getName());
				BusSeatTypeIO seatTypeIO = new BusSeatTypeIO();
				if (layoutDTO.getBusSeatType() != null) {
					seatTypeIO.setCode(layoutDTO.getBusSeatType().getCode());
					seatTypeIO.setName(layoutDTO.getBusSeatType().getName());
					layoutIO.setBusSeatType(seatTypeIO);
				}
				layoutIO.setColPos(layoutDTO.getColPos());
				layoutIO.setRowPos(layoutDTO.getRowPos());
				layoutIO.setLayer(layoutDTO.getLayer());
				layoutIO.setSequence(layoutDTO.getSequence());
				layoutIO.setOrientation(layoutDTO.getOrientation());
				layoutIO.setActiveFlag(layoutDTO.getActiveFlag());
				layoutIOList.add(layoutIO);
			}
		}
		return ResponseIO.success(layoutIOList);
	}

	@RequestMapping(value = "/{buscode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<Collection<BusIO>> getBusbyCode(@PathVariable("authtoken") String authtoken, @PathVariable("buscode") String buscode) throws Exception {
		Collection<BusIO> busIOList = new ArrayList<BusIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			BusDTO busDTO = new BusDTO();
			busDTO.setCode(buscode);

			Collection<BusDTO> list = (Collection<BusDTO>) busService.get(authDTO, busDTO);
			for (BusDTO dto : list) {
				BusIO busIO = new BusIO();
				busIO.setCode(dto.getCode());
				busIO.setName(dto.getName());
				busIO.setCategoryCode(dto.getCategoryCode() == null ? "" : dto.getCategoryCode());
				busIO.setDisplayName(dto.getDisplayName() == null ? "" : dto.getDisplayName());
				busIO.setActiveFlag(dto.getActiveFlag());
				busIO.setSeatCount(dto.getSeatCount());
				busIOList.add(busIO);
			}
		}

		return ResponseIO.success(busIOList);

	}

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BusIO> getBusUpdateUID(@PathVariable("authtoken") String authtoken, @RequestBody BusIO busIO) {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			BusDTO busDTO = new BusDTO();
			busDTO.setCode(busIO.getCode());
			busDTO.setDisplayName(busIO.getDisplayName());
			busDTO.setCategoryCode(busIO.getCategoryCode());
			busDTO.setName(busIO.getName());
			busDTO.setActiveFlag(busIO.getActiveFlag());
			busServiceValidation(busDTO);
			busService.Update(authDTO, busDTO);
			busIO.setActiveFlag(busDTO.getActiveFlag());
			busIO.setCode(busDTO.getCode());
		}
		return ResponseIO.success(busIO);

	}

	private void busServiceValidation(BusDTO busDTO) {
		try {
			if (StringUtil.isNull(busDTO.getCode()) || busDTO.getActiveFlag() == 1) {
				if (StringUtil.isNull(busDTO.getName())) {
					throw new ServiceException(ErrorCode.NAME_SHOULD_NOT_NULL);
				}
				if (StringUtil.isNull(busDTO.getCategoryCode())) {
					throw new ServiceException("Bus Type not found");
				}
			}
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ServiceException(500);
		}
	}

	@RequestMapping(value = "/{buscode}/layout/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BusSeatLayoutIO> getBusLayoutUpdateUID(@PathVariable("authtoken") String authtoken, @PathVariable("buscode") String buscode, @RequestBody List<BusSeatLayoutIO> layoutList) {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			BusDTO busDTO = new BusDTO();
			busDTO.setCode(buscode);
			List<BusSeatLayoutDTO> busDTOList = new ArrayList<BusSeatLayoutDTO>();
			for (BusSeatLayoutIO dto : layoutList) {
				BusSeatLayoutDTO seatTypeDTO = new BusSeatLayoutDTO();
				seatTypeDTO.setCode(dto.getCode());
				if (dto.getBusSeatType() != null) {
					seatTypeDTO.setBusSeatType(BusSeatTypeEM.getBusSeatTypeEM(dto.getBusSeatType().getCode()));
				}
				seatTypeDTO.setColPos(dto.getColPos());
				seatTypeDTO.setRowPos(dto.getRowPos());
				seatTypeDTO.setLayer(dto.getLayer());
				seatTypeDTO.setName(dto.getSeatName().trim());
				seatTypeDTO.setSequence(dto.getSequence());
				seatTypeDTO.setOrientation(dto.getOrientation());
				seatTypeDTO.setActiveFlag(dto.getActiveFlag());
				UpdateLayoutValidation(seatTypeDTO);
				busDTOList.add(seatTypeDTO);
			}

			busService.getUpdateLayout(authDTO, busDTO, busDTOList);
			layoutList.get(0).setCode(busDTOList.get(0).getCode());
		}
		return ResponseIO.success(layoutList.get(0));

	}

	@RequestMapping(value = "/{buscode}/layout/seat/sequence/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> getBusLayoutSeatSequenceUpdateUID(@PathVariable("authtoken") String authtoken, @PathVariable("buscode") String buscode, @RequestBody List<BusSeatLayoutIO> layoutList) {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			BusDTO busDTO = new BusDTO();
			busDTO.setCode(buscode);
			List<BusSeatLayoutDTO> busDTOList = new ArrayList<BusSeatLayoutDTO>();
			for (BusSeatLayoutIO dto : layoutList) {
				BusSeatLayoutDTO seatTypeDTO = new BusSeatLayoutDTO();
				seatTypeDTO.setCode(dto.getCode());
				seatTypeDTO.setSequence(dto.getSequence());
				busDTOList.add(seatTypeDTO);
			}
			busService.UpdateSeatSequence(authDTO, busDTO, busDTOList);
		}
		return ResponseIO.success();

	}

	@RequestMapping(value = "/seattype", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<Collection<BusSeatTypeIO>> getBusSeatType(@PathVariable("authtoken") String authtoken) throws Exception {
		Collection<BusSeatTypeIO> busIOList = new ArrayList<BusSeatTypeIO>();
		Collection<BusSeatTypeEM> list = (Collection<BusSeatTypeEM>) busService.getBusSeatType();
		for (BusSeatTypeEM dto : list) {
			if (dto.getId() == 0) {
				continue;
			}
			BusSeatTypeIO seatTypeIO = new BusSeatTypeIO();
			seatTypeIO.setCode(dto.getCode());
			seatTypeIO.setName(dto.getName());
			seatTypeIO.setReservation(dto.isReservation());
			seatTypeIO.setActiveFlag(1);
			busIOList.add(seatTypeIO);
		}

		return ResponseIO.success(busIOList);

	}

	@RequestMapping(value = "/bustypecategory", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<Collection<BusTypeCategoryIO>> getBusTypeCategory(@PathVariable("authtoken") String authtoken) throws Exception {
		Collection<BusTypeCategoryIO> busIOList = new ArrayList<BusTypeCategoryIO>();
		List<BusTypeCategoryDTO> list = busService.getBusTypeCategory();
		Comparator<BusTypeCategoryDTO> comp = new BeanComparator("name");
		Collections.sort(list, comp);

		for (BusTypeCategoryDTO dto : list) {
			BusTypeCategoryIO seatTypeIO = new BusTypeCategoryIO();
			seatTypeIO.setCode(dto.getCode());
			seatTypeIO.setCode(dto.getName());
			seatTypeIO.setActiveFlag(dto.getActiveFlag());
			List<BusTypeCategoryDetailsIO> categoryList = new ArrayList<>();
			for (BusTypeCategoryDetailsDTO detailsDTO : dto.getCategoryList()) {
				BusTypeCategoryDetailsIO detailsIO = new BusTypeCategoryDetailsIO();
				detailsIO.setCode(detailsDTO.getCode());
				detailsIO.setName(detailsDTO.getName());
				categoryList.add(detailsIO);
			}
			seatTypeIO.setCategoryList(categoryList);

			busIOList.add(seatTypeIO);
		}
		return ResponseIO.success(busIOList);
	}

	@RequestMapping(value = "vehicle", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<BusVehicleIO>> getAllBusVehicle(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "1") int activeFlag) throws Exception {
		List<BusVehicleIO> bvList = new ArrayList<BusVehicleIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<BusVehicleDTO> list = (List<BusVehicleDTO>) busVehicleService.getAll(authDTO);
			for (BusVehicleDTO bvDTO : list) {
				if (activeFlag != -1 && activeFlag != bvDTO.getActiveFlag()) {
					continue;
				}
				BusVehicleIO bvIo = new BusVehicleIO();
				bvIo.setCode(bvDTO.getCode());
				bvIo.setName(bvDTO.getName());

				BusIO busIO = new BusIO();
				busIO.setCode(bvDTO.getBus().getCode());
				busIO.setName(bvDTO.getBus().getName());
				busIO.setCategoryCode(bvDTO.getBus().getCategoryCode());
				bvIo.setBus(busIO);

				BaseIO vehicleType = new BaseIO();
				vehicleType.setCode(bvDTO.getVehicleType().getCode());
				vehicleType.setName(bvDTO.getVehicleType().getName());
				bvIo.setVehicleType(vehicleType);

				bvIo.setRegistrationDate(bvDTO.getRegistrationDate());
				bvIo.setRegistationNumber(bvDTO.getRegistationNumber());
				bvIo.setLicNumber(bvDTO.getLicNumber());
				bvIo.setGpsDeviceCode(bvDTO.getGpsDeviceCode());
				BaseIO deviceVendor = new BaseIO();
				deviceVendor.setCode(bvDTO.getDeviceVendor().getCode());
				deviceVendor.setName(bvDTO.getDeviceVendor().getName());
				bvIo.setGpsDeviceVendor(deviceVendor);
				bvIo.setMobileNumber(bvDTO.getMobileNumber());
				bvIo.setLastAssignedDate(bvDTO.getLastAssignedDateToString());
				bvIo.setActiveFlag(bvDTO.getActiveFlag());
				bvList.add(bvIo);
			}

		}
		return ResponseIO.success(bvList);
	}

	@RequestMapping(value = "/vehicle/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BusVehicleIO> updateBusVehicle(@PathVariable("authtoken") String authtoken, @RequestBody BusVehicleIO busVehicle) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		BusVehicleIO io = new BusVehicleIO();
		if (authDTO != null) {
			BusVehicleDTO bvDTO = new BusVehicleDTO();
			bvDTO.setCode(busVehicle.getCode());
			bvDTO.setName(busVehicle.getName());
			BusDTO busDTO = new BusDTO();
			if (busVehicle.getBus() != null && busVehicle.getBus().getCode() != null) {
				busDTO.setCode(busVehicle.getBus().getCode());
			}
			bvDTO.setBus(busDTO);
			bvDTO.setVehicleType(busVehicle.getVehicleType() != null ? VehicleTypeEM.getVehicleTypeEM(busVehicle.getVehicleType().getCode()) : VehicleTypeEM.BUS);
			bvDTO.setRegistrationDate(busVehicle.getRegistrationDate());
			bvDTO.setRegistationNumber(busVehicle.getRegistationNumber());
			bvDTO.setLicNumber(busVehicle.getLicNumber());
			bvDTO.setGpsDeviceCode(StringUtil.isNotNull(busVehicle.getGpsDeviceCode()) ? busVehicle.getGpsDeviceCode().trim() : null);
			bvDTO.setDeviceVendor(GPSDeviceVendorEM.getGPSDeviceVendorEM(busVehicle.getGpsDeviceVendor() != null ? busVehicle.getGpsDeviceVendor().getCode() : null));
			bvDTO.setMobileNumber(busVehicle.getMobileNumber());
			bvDTO.setActiveFlag(busVehicle.getActiveFlag());

			busVehicleService.Update(authDTO, bvDTO);

			io.setCode(bvDTO.getCode());
			io.setActiveFlag(bvDTO.getActiveFlag());
		}
		return ResponseIO.success(io);
	}

	@RequestMapping(value = "/vehicle/gps/device/vendor", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<BaseIO>> getGPSDeviceVendor(@PathVariable("authtoken") String authtoken) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<BaseIO> vendorList = new ArrayList<BaseIO>();
		for (GPSDeviceVendorEM vendor : GPSDeviceVendorEM.getGPSDeviceVendorByNamespace(authDTO.getNamespaceCode())) {
			BaseIO seatTypeIO = new BaseIO();
			seatTypeIO.setCode(vendor.getCode());
			seatTypeIO.setName(vendor.getName());
			vendorList.add(seatTypeIO);
		}
		return ResponseIO.success(vendorList);
	}

	private void UpdateLayoutValidation(BusSeatLayoutDTO seatTypeDTO) {
		try {
			if (StringUtil.isNull(seatTypeDTO.getCode()) && seatTypeDTO.getActiveFlag() == 1) {
				if (StringUtil.isNull(seatTypeDTO.getName()) || seatTypeDTO.getColPos() == 0 || seatTypeDTO.getLayer() == 0 || seatTypeDTO.getBusSeatType() == null) {
					throw new ServiceException(302);
				}
			}
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ServiceException(500);
		}
	}
}
