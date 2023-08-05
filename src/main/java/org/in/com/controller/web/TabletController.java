package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.Collection;

import net.sf.ehcache.Element;

import org.in.com.cache.EhcacheManager;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.controller.web.io.AuthIO;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.BusVehicleIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.TabletIO;
import org.in.com.controller.web.io.UserIO;
import org.in.com.dto.AppStoreDetailsDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusVehicleDTO;
import org.in.com.dto.BusVehicleDriverDTO;
import org.in.com.dto.TabletDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.TabletService;
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
@RequestMapping("/{authtoken}/tablet")
public class TabletController extends BaseController {
	@Autowired
	TabletService tabletService;

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<Collection<TabletIO>> registerTablet(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "-1") int activeFlag) throws Exception {
		Collection<TabletIO> tabletList = new ArrayList<TabletIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			Collection<TabletDTO> list = tabletService.getAllTablets(authDTO);
			for (TabletDTO tabletDTO : list) {
				if (activeFlag != -1 && activeFlag != tabletDTO.getActiveFlag()) {
					continue;
				}
				TabletIO tabletIO = new TabletIO();
				tabletIO.setCode(tabletDTO.getCode());
				tabletIO.setName(tabletDTO.getName());
				tabletIO.setMobileNumber(tabletDTO.getMobileNumber());
				tabletIO.setMobileVerifyFlag(tabletDTO.getMobileVerifyFlag());
				tabletIO.setSyncTime(getSyncTime(authDTO.getNamespaceCode(), tabletDTO.getCode()));
				tabletIO.setModel(tabletDTO.getModel());
				tabletIO.setVersion(tabletDTO.getVersion());
				tabletIO.setRemarks(tabletDTO.getRemarks());
				tabletIO.setActiveFlag(tabletDTO.getActiveFlag());

				BusVehicleIO busVehicle = new BusVehicleIO();
				busVehicle.setCode(tabletDTO.getBusVehicle() != null ? tabletDTO.getBusVehicle().getCode() : null);
				busVehicle.setName(tabletDTO.getBusVehicle() != null ? tabletDTO.getBusVehicle().getName() : null);
				busVehicle.setRegistationNumber(tabletDTO.getBusVehicle() != null ? tabletDTO.getBusVehicle().getRegistationNumber() : null);
				busVehicle.setRegistrationDate(tabletDTO.getBusVehicle() != null ? tabletDTO.getBusVehicle().getRegistrationDate() : null);
				busVehicle.setLicNumber(tabletDTO.getBusVehicle() != null ? tabletDTO.getBusVehicle().getLicNumber() : null);
				busVehicle.setGpsDeviceCode(tabletDTO.getBusVehicle() != null ? tabletDTO.getBusVehicle().getGpsDeviceCode() : null);
				busVehicle.setMobileNumber(tabletDTO.getBusVehicle() != null ? tabletDTO.getBusVehicle().getMobileNumber() : null);

				BaseIO gpsVendor = new BaseIO();
				gpsVendor.setCode(tabletDTO.getBusVehicle().getDeviceVendor() != null ? tabletDTO.getBusVehicle().getDeviceVendor().getCode() : null);
				gpsVendor.setName(tabletDTO.getBusVehicle().getDeviceVendor() != null ? tabletDTO.getBusVehicle().getDeviceVendor().getName() : null);
				busVehicle.setGpsDeviceVendor(gpsVendor);

				tabletIO.setBusVehicle(busVehicle);
				tabletList.add(tabletIO);
			}
		}
		return ResponseIO.success(tabletList);
	}

	private String getSyncTime(String namespaceCode, String deviceCode) {
		String syncTime = Text.NA;
		Element element = EhcacheManager.getBusBuddyEhCache().get(Text.BUS_BUDDY_SYNC + Text.UNDER_SCORE + namespaceCode + Text.UNDER_SCORE + deviceCode);
		if (element != null) {
			syncTime = (String) element.getObjectValue();
		}
		return syncTime;
	}

	@RequestMapping(value = "/register/pending", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<Collection<TabletIO>> registerTabletPending(@PathVariable("authtoken") String authtoken) throws Exception {
		Collection<TabletIO> tabletList = new ArrayList<TabletIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			Collection<TabletDTO> list = tabletService.getRegisterPending(authDTO);
			for (TabletDTO tabletDTO : list) {
				TabletIO tabletIO = new TabletIO();
				tabletIO.setCode(tabletDTO.getCode());
				UserIO user = new UserIO();
				user.setMobile(tabletDTO.getUser().getMobile());
				user.setName(tabletDTO.getUser().getName());
				user.setLastname(tabletDTO.getUser().getLastname());
				tabletIO.setUser(user);
				tabletIO.setName(tabletDTO.getName());
				tabletIO.setMobileNumber(tabletDTO.getMobileNumber());
				tabletIO.setModel(tabletDTO.getModel());
				tabletIO.setVersion(tabletDTO.getVersion());
				tabletIO.setRemarks(tabletDTO.getRemarks());
				tabletIO.setActiveFlag(tabletDTO.getUser().getActiveFlag());
				tabletList.add(tabletIO);
			}
		}
		return ResponseIO.success(tabletList);
	}

	@RequestMapping(value = "/register/add", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> registerNewTablet(@PathVariable("authtoken") String authtoken, @RequestBody UserIO user) throws Exception {
		BaseIO baseIO = new BaseIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			UserDTO userDTO = new UserDTO();
			userDTO.setName(user.getName());
			userDTO.setLastname(user.getName());
			userDTO.setMobile(user.getMobile());
			userDTO.setActiveFlag(user.getActiveFlag());
			TabletDTO tabletDTO = new TabletDTO();
			tabletDTO.setName(user.getName());
			tabletDTO.setMobileNumber(user.getMobile());
			tabletDTO.setUser(userDTO);
			tabletService.registerNewTablet(authDTO, tabletDTO);
			baseIO.setCode(tabletDTO.getCode());
			baseIO.setActiveFlag(userDTO.getActiveFlag());
		}
		return ResponseIO.success(baseIO);
	}

	@RequestMapping(value = "/register/authorize/{authorizeCode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<AuthIO> authorizeRegisterTablet(@PathVariable("authtoken") String authtoken, @PathVariable("authorizeCode") String authorizeCode, @RequestBody TabletIO tablet) throws Exception {
		AuthIO baseIO = new AuthIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			if (tablet.getStoreDetails() == null || StringUtil.isNull(tablet.getStoreDetails().getUdid()) || StringUtil.isNull(tablet.getStoreDetails().getGcmToken())) {
				throw new ServiceException(ErrorCode.REQURIED_FIELD_SHOULD_NOT_NULL);
			}
			TabletDTO tabletDTO = new TabletDTO();
			tabletDTO.setCode(tablet.getCode());
			// tabletDTO.setName(tablet.getName());
			AppStoreDetailsDTO appStoreDetails = new AppStoreDetailsDTO();
			appStoreDetails.setUdid(tablet.getStoreDetails().getUdid());
			appStoreDetails.setGcmToken(tablet.getStoreDetails().getGcmToken());
			appStoreDetails.setModel(tablet.getStoreDetails().getModel());
			appStoreDetails.setOs(tablet.getStoreDetails().getOs());
			appStoreDetails.setActiveFlag(Numeric.ONE_INT);
			tabletDTO.setMobileNumber(StringUtil.isNotNull(tablet.getMobileNumber()) ? tablet.getMobileNumber() : "NA");
			tabletDTO.setModel(StringUtil.isNotNull(tablet.getModel()) ? tablet.getModel() : appStoreDetails.getModel());
			tabletDTO.setVersion(StringUtil.isNotNull(tablet.getVersion()) ? tablet.getVersion() : Text.NA);
			tabletDTO.setRemarks(StringUtil.isNotNull(tablet.getRemarks()) ? tablet.getRemarks() : Text.NA);
			tabletDTO.setActiveFlag(tablet.getActiveFlag());
			UserDTO userDTO = new UserDTO();
			userDTO.setAppStoreDetails(appStoreDetails);
			userDTO.setToken(authorizeCode);
			tabletDTO.setUser(userDTO);
			tabletService.Update(authDTO, tabletDTO);
			baseIO.setDeviceCode(tabletDTO.getCode());
			baseIO.setDeviceToken(tabletDTO.getUser().getToken());
			baseIO.setNamespaceCode(authDTO.getNamespaceCode());
			baseIO.setDeviceMediumCode(DeviceMediumEM.APP_TABLET_POB.getCode());
			baseIO.setUsername(tabletDTO.getUser().getUsername());
		}
		return ResponseIO.success(baseIO);
	}

	@RequestMapping(value = "/register/authorize/generate/driver/{driverCode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> generateDriverAuthorizePIN(@PathVariable("authtoken") String authtoken, @PathVariable("driverCode") String driverCode) throws Exception {
		BaseIO baseIO = new BaseIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		BusVehicleDriverDTO busVehicleDriver = new BusVehicleDriverDTO();
		busVehicleDriver.setCode(driverCode);
		String pin = tabletService.generateDriverAuthorizePIN(authDTO, busVehicleDriver);
		baseIO.setCode(pin);
		return ResponseIO.success(baseIO);
	}

	@RequestMapping(value = "/register/authorize/{authorizeCode}/driver", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<AuthIO> authorizeRegisterDriverTablet(@PathVariable("authtoken") String authtoken, @PathVariable("authorizeCode") String authorizeCode, @RequestBody TabletIO tablet) throws Exception {
		AuthIO baseIO = new AuthIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (tablet.getStoreDetails() == null || StringUtil.isNull(tablet.getStoreDetails().getUdid()) || StringUtil.isNull(tablet.getStoreDetails().getGcmToken())) {
			throw new ServiceException(ErrorCode.REQURIED_FIELD_SHOULD_NOT_NULL);
		}

		AppStoreDetailsDTO appStoreDetails = new AppStoreDetailsDTO();
		appStoreDetails.setUdid(tablet.getStoreDetails().getUdid());
		appStoreDetails.setGcmToken(tablet.getStoreDetails().getGcmToken());
		appStoreDetails.setModel(tablet.getStoreDetails().getModel());
		appStoreDetails.setOs(tablet.getStoreDetails().getOs());
		appStoreDetails.setActiveFlag(Numeric.ONE_INT);

		TabletDTO tabletDTO = new TabletDTO();
		tabletDTO.setModel(StringUtil.isNotNull(tablet.getModel()) ? tablet.getModel() : appStoreDetails.getModel());
		tabletDTO.setVersion(StringUtil.isNotNull(tablet.getVersion()) ? tablet.getVersion() : Text.NA);
		tabletDTO.setRemarks(StringUtil.isNotNull(tablet.getRemarks()) ? tablet.getRemarks() : Text.NA);
		tabletDTO.setActiveFlag(1);

		UserDTO userDTO = new UserDTO();
		userDTO.setAppStoreDetails(appStoreDetails);
		userDTO.setToken(authorizeCode);
		tabletDTO.setUser(userDTO);

		tabletService.saveDriverApp(authDTO, tabletDTO);

		baseIO.setDeviceCode(tabletDTO.getCode());
		baseIO.setDriverName(tabletDTO.getName());
		baseIO.setDeviceToken(tabletDTO.getUser().getToken());
		baseIO.setNamespaceCode(authDTO.getNamespaceCode());
		baseIO.setDeviceMediumCode(DeviceMediumEM.APP_TABLET_POB.getCode());
		baseIO.setUsername(tabletDTO.getUser().getUsername());
		return ResponseIO.success(baseIO);
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> updateTablet(@PathVariable("authtoken") String authtoken, @RequestBody TabletIO tablet) throws Exception {
		BaseIO baseIO = new BaseIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			TabletDTO tabletDTO = new TabletDTO();
			tabletDTO.setCode(tablet.getCode());
			tabletDTO.setName(tablet.getName());
			tabletDTO.setMobileNumber(tablet.getMobileNumber());
			tabletDTO.setModel(StringUtil.isNotNull(tablet.getModel()) ? tablet.getModel() : Text.NA);
			tabletDTO.setVersion(StringUtil.isNotNull(tablet.getVersion()) ? tablet.getVersion() : Text.NA);
			tabletDTO.setRemarks(StringUtil.isNotNull(tablet.getRemarks()) ? tablet.getRemarks() : Text.NA);

			tabletService.updateTablet(authDTO, tabletDTO);

			baseIO.setCode(tabletDTO.getCode());
			baseIO.setActiveFlag(tabletDTO.getActiveFlag());
		}
		return ResponseIO.success(baseIO);
	}

	@RequestMapping(value = "/register/authorize/generate/{tabletCode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> generateAuthorizePIN(@PathVariable("authtoken") String authtoken, @PathVariable("tabletCode") String tabletCode) throws Exception {
		BaseIO baseIO = new BaseIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			String pin = tabletService.generateAuthorizePIN(authDTO, tabletCode);
			baseIO.setCode(pin);
		}
		return ResponseIO.success(baseIO);
	}

	@RequestMapping(value = "/delete", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> deleteRegistedTablet(@PathVariable("authtoken") String authtoken, @RequestBody TabletIO tablet) throws Exception {
		BaseIO baseIO = new BaseIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			TabletDTO tabletDTO = new TabletDTO();
			tabletDTO.setCode(tablet.getCode());
			tabletDTO.setActiveFlag(tablet.getActiveFlag());
			tabletService.deleteTablet(authDTO, tabletDTO);
			baseIO.setCode(tabletDTO.getCode());
			baseIO.setActiveFlag(tabletDTO.getActiveFlag());
		}
		return ResponseIO.success(baseIO);
	}

	@RequestMapping(value = "/vehicle/mapping", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> getVehicleMapping(@PathVariable("authtoken") String authtoken, @RequestBody TabletIO tablet) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			TabletDTO tabletDTO = new TabletDTO();
			tabletDTO.setCode(tablet.getCode());

			BusVehicleDTO busVehicleDTO = new BusVehicleDTO();
			busVehicleDTO.setCode(tablet.getBusVehicle() != null ? tablet.getBusVehicle().getCode() : null);
			tabletDTO.setBusVehicle(busVehicleDTO);

			tabletService.tabletVehicleMapping(authDTO, tabletDTO);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/deregister/{tabletCode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> deRegisterTablet(@PathVariable("authtoken") String authtoken, @PathVariable("tabletCode") String tabletCode) throws Exception {
		BaseIO baseIO = new BaseIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			TabletDTO tabletDTO = new TabletDTO();
			tabletDTO.setCode(tabletCode);
			tabletService.deRegisterTablet(authDTO, tabletDTO);
			baseIO.setCode(tabletDTO.getCode());
		}
		return ResponseIO.success(baseIO);
	}

	@RequestMapping(value = "/{tabletCode}/generate/otp", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> generateOTP(@PathVariable("authtoken") String authtoken, @PathVariable("tabletCode") String tabletCode) throws Exception {
		BaseIO baseIO = new BaseIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		TabletDTO tabletDTO = new TabletDTO();
		tabletDTO.setCode(tabletCode);
		tabletService.generateOTP(authDTO, tabletDTO);
		return ResponseIO.success(baseIO);
	}

	@RequestMapping(value = "/{tabletCode}/verify/mobile/{authorizeCode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> authorizeRegisterTablet(@PathVariable("authtoken") String authtoken, @PathVariable("tabletCode") String tabletCode, @PathVariable("authorizeCode") int authorizeCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			TabletDTO tabletDTO = new TabletDTO();
			tabletDTO.setCode(tabletCode);
			tabletService.verifyDeviceMobile(authDTO, tabletDTO, authorizeCode);
		}
		return ResponseIO.success();
	}

}
