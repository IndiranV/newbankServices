package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.beanutils.BeanComparator;
import org.in.com.constants.Text;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.GroupIO;
import org.in.com.controller.web.io.PaymentGatewayPartnerIO;
import org.in.com.controller.web.io.PaymentGatewayProviderIO;
import org.in.com.controller.web.io.PaymentMerchantGatewayCredentialsIO;
import org.in.com.controller.web.io.PaymentMerchantGatewayScheduleIO;
import org.in.com.controller.web.io.PaymentModeIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.PaymentGatewayCredentialsDTO;
import org.in.com.dto.PaymentGatewayPartnerDTO;
import org.in.com.dto.PaymentGatewayProviderDTO;
import org.in.com.dto.PaymentGatewayScheduleDTO;
import org.in.com.dto.PaymentModeDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.OrderTypeEM;
import org.in.com.service.PaymentGatewayPartnerService;
import org.in.com.service.PaymentGatewayProviderService;
import org.in.com.service.PaymentMerchantGatewayCredentialsService;
import org.in.com.service.PaymentMerchantGatewayScheduleService;
import org.in.com.service.PaymentModeService;
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
@RequestMapping("/{authtoken}/payment")
public class PaymentGatewayController extends BaseController {
	@Autowired
	PaymentGatewayPartnerService gatewayPartnerService;
	@Autowired
	PaymentGatewayProviderService pgProviderService;
	@Autowired
	PaymentMerchantGatewayCredentialsService pgCredenatialsService;
	@Autowired
	PaymentMerchantGatewayScheduleService ScheduleService;
	@Autowired
	PaymentModeService modeService;

	@RequestMapping(value = "gatewaypartner", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<PaymentGatewayPartnerIO>> getAllPaymentGatewayPartner(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "-1") int activeFlag) throws Exception {
		List<PaymentGatewayPartnerIO> pgPartner = new ArrayList<PaymentGatewayPartnerIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<PaymentGatewayPartnerDTO> list = (List<PaymentGatewayPartnerDTO>) gatewayPartnerService.getAll(authDTO);
			for (PaymentGatewayPartnerDTO pgPartnerDTO : list) {
				if (activeFlag != -1 && activeFlag != pgPartnerDTO.getActiveFlag()) {
					continue;
				}
				PaymentGatewayPartnerIO pgPartio = new PaymentGatewayPartnerIO();
				pgPartio.setCode(pgPartnerDTO.getCode());
				pgPartio.setName(pgPartnerDTO.getName());
				pgPartio.setOfferNotes(pgPartnerDTO.getOfferNotes());
				pgPartio.setOfferTerms(pgPartnerDTO.getOfferTerms());
				PaymentGatewayProviderIO gatewayProviderIO = new PaymentGatewayProviderIO();
				gatewayProviderIO.setCode(pgPartnerDTO.getGatewayProvider().getCode());
				gatewayProviderIO.setName(pgPartnerDTO.getGatewayProvider().getName());
				pgPartio.setGatewayProvider(gatewayProviderIO);

				PaymentModeIO paymentModeIO = new PaymentModeIO();
				paymentModeIO.setCode(pgPartnerDTO.getPaymentMode().getCode());
				paymentModeIO.setName(pgPartnerDTO.getPaymentMode().getName());
				pgPartio.setPaymentMode(paymentModeIO);

				pgPartio.setApiProviderCode(pgPartnerDTO.getApiProviderCode());
				pgPartio.setActiveFlag(pgPartnerDTO.getActiveFlag());
				pgPartio.setCode(pgPartnerDTO.getCode());
				pgPartner.add(pgPartio);
			}

		}
		return ResponseIO.success(pgPartner);
	}

	@RequestMapping(value = "gatewaypartner/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<PaymentGatewayPartnerIO> updatePaymentPartner(@PathVariable("authtoken") String authtoken, @RequestBody PaymentGatewayPartnerIO pgPartnerCtrl) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		PaymentGatewayPartnerIO pgPartnerio = new PaymentGatewayPartnerIO();
		if (authDTO != null) {
			PaymentGatewayPartnerDTO pgPartnerDTO = new PaymentGatewayPartnerDTO();
			pgPartnerDTO.setCode(pgPartnerCtrl.getCode());
			pgPartnerDTO.setName(pgPartnerCtrl.getName());
			pgPartnerDTO.setOfferNotes(StringUtil.isNull(pgPartnerCtrl.getOfferNotes(), Text.NA));
			pgPartnerDTO.setOfferTerms(pgPartnerCtrl.getOfferTerms() != null ? pgPartnerCtrl.getOfferTerms() : new ArrayList<String>());

			PaymentGatewayProviderDTO gatewayProviderDTO = new PaymentGatewayProviderDTO();
			gatewayProviderDTO.setCode(pgPartnerCtrl.getGatewayProvider() != null ? pgPartnerCtrl.getGatewayProvider().getCode() : null);
			pgPartnerDTO.setGatewayProvider(gatewayProviderDTO);

			PaymentModeDTO modeDTO = new PaymentModeDTO();
			modeDTO.setCode(pgPartnerCtrl.getPaymentMode().getCode());
			pgPartnerDTO.setPaymentMode(modeDTO);
			pgPartnerDTO.setApiProviderCode(pgPartnerCtrl.getApiProviderCode());
			pgPartnerDTO.setActiveFlag(pgPartnerCtrl.getActiveFlag());
			gatewayPartnerService.Update(authDTO, pgPartnerDTO);
			pgPartnerio.setCode(pgPartnerDTO.getCode());
			pgPartnerio.setActiveFlag(pgPartnerDTO.getActiveFlag());
		}
		return ResponseIO.success(pgPartnerio);
	}

	@RequestMapping(value = "provider", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<PaymentGatewayProviderIO>> getAllPgProvider(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "-1") int activeFlag) throws Exception {

		List<PaymentGatewayProviderIO> pgProvider = new ArrayList<PaymentGatewayProviderIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<PaymentGatewayProviderDTO> list = (List<PaymentGatewayProviderDTO>) pgProviderService.getAll(authDTO);
			// Sorting
			Comparator<PaymentGatewayProviderDTO> comp = new BeanComparator("name");
			Collections.sort(list, comp);

			for (PaymentGatewayProviderDTO pgProviderDTO : list) {
				if (activeFlag != -1 && activeFlag != pgProviderDTO.getActiveFlag()) {
					continue;
				}
				PaymentGatewayProviderIO pgProviderio = new PaymentGatewayProviderIO();
				pgProviderio.setCode(pgProviderDTO.getCode());
				pgProviderio.setName(pgProviderDTO.getName());
				pgProviderio.setServiceName(pgProviderDTO.getServiceName());
				pgProviderio.setActiveFlag(pgProviderDTO.getActiveFlag());
				pgProviderio.setCode(pgProviderDTO.getCode());
				pgProvider.add(pgProviderio);
			}
		}
		return ResponseIO.success(pgProvider);
	}

	@RequestMapping(value = "/provider/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<PaymentGatewayProviderIO> updatePaymentGatewayProvider(@PathVariable("authtoken") String authtoken, @RequestBody PaymentGatewayProviderIO pgProvider) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		PaymentGatewayProviderIO pgProviderio = new PaymentGatewayProviderIO();
		if (authDTO != null) {
			PaymentGatewayProviderDTO pgProviderDTO = new PaymentGatewayProviderDTO();
			pgProviderDTO.setCode(pgProvider.getCode());
			pgProviderDTO.setName(pgProvider.getName());
			pgProviderDTO.setServiceName(pgProvider.getServiceName());
			pgProviderDTO.setActiveFlag(pgProvider.getActiveFlag());
			pgProviderService.Update(authDTO, pgProviderDTO);
			pgProviderio.setName(pgProviderDTO.getName());
			pgProviderio.setCode(pgProviderDTO.getCode());
			pgProviderio.setActiveFlag(pgProviderDTO.getActiveFlag());
			pgProviderio.setServiceName(pgProviderDTO.getServiceName());
		}
		return ResponseIO.success(pgProviderio);
	}

	@RequestMapping(value = "credential", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<PaymentMerchantGatewayCredentialsIO>> getAllpgCredentials(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "-1") int activeFlag) throws Exception {

		List<PaymentMerchantGatewayCredentialsIO> pgCredentials = new ArrayList<PaymentMerchantGatewayCredentialsIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<PaymentGatewayCredentialsDTO> list = (List<PaymentGatewayCredentialsDTO>) pgCredenatialsService.getAll(authDTO);
			for (PaymentGatewayCredentialsDTO pgCredentialsDTO : list) {
				if (activeFlag != -1 && activeFlag != pgCredentialsDTO.getActiveFlag()) {
					continue;
				}
				PaymentMerchantGatewayCredentialsIO pgCredentialsio = new PaymentMerchantGatewayCredentialsIO();
				pgCredentialsio.setCode(pgCredentialsDTO.getCode());

				PaymentGatewayProviderIO gatewayProviderIO = new PaymentGatewayProviderIO();
				gatewayProviderIO.setCode(pgCredentialsDTO.getGatewayProvider().getCode());
				gatewayProviderIO.setName(pgCredentialsDTO.getGatewayProvider().getName());
				pgCredentialsio.setGatewayProvider(gatewayProviderIO);

				pgCredentialsio.setReturnUrl(pgCredentialsDTO.getPgReturnUrl());
				// pgCredentialsio.setAppReturnUrl(pgCredentialsDTO.getAppReturnUrl());

				pgCredentialsio.setAccessCode(pgCredentialsDTO.getAccessCode());
				pgCredentialsio.setAccessKey(pgCredentialsDTO.getAccessKey());
				pgCredentialsio.setAttr1(pgCredentialsDTO.getAttr1());
				pgCredentialsio.setPropertiesFileName(pgCredentialsDTO.getPropertiesFileName());
				pgCredentialsio.setAccountOwner(pgCredentialsDTO.getAccountOwner());
				pgCredentialsio.setActiveFlag(pgCredentialsDTO.getActiveFlag());
				pgCredentials.add(pgCredentialsio);
			}
		}
		return ResponseIO.success(pgCredentials);
	}

	@RequestMapping(value = "/credential/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<PaymentMerchantGatewayCredentialsIO> updatePaymentGatewayProvider(@PathVariable("authtoken") String authtoken, @RequestBody PaymentMerchantGatewayCredentialsIO pgCredentials) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		PaymentMerchantGatewayCredentialsIO pgCredentialsio = new PaymentMerchantGatewayCredentialsIO();
		if (authDTO != null) {
			PaymentGatewayCredentialsDTO pgCredentialsDTO = new PaymentGatewayCredentialsDTO();
			pgCredentialsDTO.setCode(pgCredentials.getCode());

			PaymentGatewayProviderDTO gatewayProviderDTO = new PaymentGatewayProviderDTO();
			gatewayProviderDTO.setCode(pgCredentials.getGatewayProvider().getCode());
			pgCredentialsDTO.setGatewayProvider(gatewayProviderDTO);

			pgCredentialsDTO.setPgReturnUrl(pgCredentials.getReturnUrl());
			pgCredentialsDTO.setAccessCode(pgCredentials.getAccessCode());
			pgCredentialsDTO.setAccessKey(pgCredentials.getAccessKey());
			pgCredentialsDTO.setAttr1(pgCredentials.getAttr1());
			pgCredentialsDTO.setPropertiesFileName(pgCredentials.getPropertiesFileName());
			pgCredentialsDTO.setActiveFlag(pgCredentials.getActiveFlag());
			pgCredenatialsService.Update(authDTO, pgCredentialsDTO);
			pgCredentialsio.setCode(pgCredentialsDTO.getCode());
			pgCredentialsio.setActiveFlag(pgCredentialsDTO.getActiveFlag());
		}
		return ResponseIO.success(pgCredentialsio);
	}

	@RequestMapping(value = "schedule", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<PaymentMerchantGatewayScheduleIO>> getAllMerchantSchedule(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "-1") int activeFlag) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<PaymentMerchantGatewayScheduleIO> pgMerSchIO = new ArrayList<PaymentMerchantGatewayScheduleIO>();
		List<PaymentGatewayScheduleDTO> paymentMerchantScheduleList = (List<PaymentGatewayScheduleDTO>) ScheduleService.getAll(authDTO);
		if (paymentMerchantScheduleList != null) {
			for (PaymentGatewayScheduleDTO dto : paymentMerchantScheduleList) {
				if (activeFlag != -1 && activeFlag != dto.getActiveFlag()) {
					continue;
				}
				PaymentMerchantGatewayScheduleIO io = new PaymentMerchantGatewayScheduleIO();
				io.setCode(dto.getCode());
				io.setName(dto.getName());

				GroupIO groupIO = new GroupIO();
				groupIO.setCode(dto.getGroup().getCode());
				groupIO.setName(dto.getGroup().getName());
				io.setGroup(groupIO);

				PaymentGatewayPartnerIO gatewayPartnerIO = new PaymentGatewayPartnerIO();
				gatewayPartnerIO.setCode(dto.getGatewayPartner().getCode());
				gatewayPartnerIO.setName(dto.getGatewayPartner().getName());

				PaymentGatewayProviderIO provider = new PaymentGatewayProviderIO();
				provider.setCode(dto.getGatewayPartner().getGatewayProvider().getCode());
				provider.setName(dto.getGatewayPartner().getGatewayProvider().getName());
				gatewayPartnerIO.setGatewayProvider(provider);

				io.setGatewayPartner(gatewayPartnerIO);
				io.setServiceCharge(dto.getServiceCharge());
				io.setPrecedence(dto.getPrecedence());

				BaseIO transactionType = new BaseIO();
				transactionType.setCode(dto.getOrderType().getCode());
				transactionType.setName(dto.getOrderType().getName());
				io.setTransactionType(transactionType);

				List<String> deviceMedoumList = new ArrayList<String>();
				for (DeviceMediumEM devicemMedium : dto.getDeviceMedium()) {
					deviceMedoumList.add(devicemMedium.getCode());
				}
				io.setDeviceMedium(deviceMedoumList);

				io.setFromDate(dto.getFromDate());
				io.setToDate(dto.getToDate());
				io.setActiveFlag(dto.getActiveFlag());
				pgMerSchIO.add(io);
			}
		}
		return ResponseIO.success(pgMerSchIO);
	}

	@RequestMapping(value = "/schedule/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<PaymentMerchantGatewayScheduleIO> updatePaymentGatewaySchedule(@PathVariable("authtoken") String authtoken, @RequestBody PaymentMerchantGatewayScheduleIO pgMerSchedule) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		PaymentMerchantGatewayScheduleIO pgMerScheduleio = new PaymentMerchantGatewayScheduleIO();
		if (authDTO != null) {
			PaymentGatewayScheduleDTO pgMerScheduleDTO = new PaymentGatewayScheduleDTO();

			GroupDTO groupDTO = new GroupDTO();
			groupDTO.setCode(pgMerSchedule.getGroup().getCode());
			pgMerScheduleDTO.setGroup(groupDTO);
			pgMerScheduleDTO.setCode(pgMerSchedule.getCode());
			pgMerScheduleDTO.setName(pgMerSchedule.getName());
			PaymentGatewayPartnerDTO gatewayPartnerDTO = new PaymentGatewayPartnerDTO();
			gatewayPartnerDTO.setCode(pgMerSchedule.getGatewayPartner().getCode());
			pgMerScheduleDTO.setGatewayPartner(gatewayPartnerDTO);

			List<DeviceMediumEM> deviceMediumList = new ArrayList<>();
			for (String code : pgMerSchedule.getDeviceMedium()) {
				deviceMediumList.add(DeviceMediumEM.getDeviceMediumEM(code));
			}
			pgMerScheduleDTO.setDeviceMedium(deviceMediumList);

			pgMerScheduleDTO.setFromDate(pgMerSchedule.getFromDate());
			pgMerScheduleDTO.setToDate(pgMerSchedule.getToDate());
			pgMerScheduleDTO.setActiveFlag(pgMerSchedule.getActiveFlag());
			pgMerScheduleDTO.setServiceCharge(pgMerSchedule.getServiceCharge());
			pgMerScheduleDTO.setPrecedence(pgMerSchedule.getPrecedence());
			pgMerScheduleDTO.setOrderType(pgMerSchedule.getTransactionType() != null ? OrderTypeEM.getOrderTypeEM(pgMerSchedule.getTransactionType().getCode()) : OrderTypeEM.TICKET);
			ScheduleService.Update(authDTO, pgMerScheduleDTO);
			pgMerScheduleio.setCode(pgMerScheduleDTO.getCode());
			pgMerScheduleio.setActiveFlag(pgMerScheduleDTO.getActiveFlag());
		}
		return ResponseIO.success(pgMerScheduleio);
	}

	@RequestMapping(value = "paymentmode", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<PaymentModeIO>> getAllPaymentMode(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "-1") int activeFlag) throws Exception {
		List<PaymentModeIO> pgMode = new ArrayList<PaymentModeIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<PaymentModeDTO> list = (List<PaymentModeDTO>) modeService.getAll(authDTO);
			for (PaymentModeDTO pgModeDTO : list) {
				if (activeFlag != -1 && activeFlag != pgModeDTO.getActiveFlag()) {
					continue;
				}
				PaymentModeIO pgModeio = new PaymentModeIO();
				pgModeio.setCode(pgModeDTO.getCode());
				pgModeio.setName(pgModeDTO.getName());
				pgModeio.setActiveFlag(pgModeDTO.getActiveFlag());
				pgMode.add(pgModeio);
			}

		}
		return ResponseIO.success(pgMode);
	}

	@RequestMapping(value = "/paymentmode/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<PaymentModeIO> updatePaymentMode(@PathVariable("authtoken") String authtoken, @RequestBody PaymentModeIO pgMode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		PaymentModeIO pgModeio = new PaymentModeIO();
		if (authDTO != null) {
			PaymentModeDTO pgModeDTO = new PaymentModeDTO();
			pgModeDTO.setCode(pgMode.getCode());
			pgModeDTO.setName(pgMode.getName());
			pgModeDTO.setActiveFlag(pgMode.getActiveFlag());
			modeService.Update(authDTO, pgModeDTO);
			pgModeio.setName(pgModeDTO.getName());
			pgModeio.setCode(pgModeDTO.getCode());
			pgModeio.setActiveFlag(pgModeDTO.getActiveFlag());
		}
		return ResponseIO.success(pgModeio);
	}
}
