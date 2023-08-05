package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.in.com.constants.Text;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.IntegrationIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.IntegrationDTO;
import org.in.com.dto.enumeration.IntegrationTypeEM;
import org.in.com.service.AuthService;
import org.in.com.service.IntegrationService;
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
@RequestMapping("/{authtoken}/namespace/integration")
public class IntegrationController extends BaseController {
	@Autowired
	AuthService authService;
	@Autowired
	IntegrationService integrationService;

	@RequestMapping(value = "/type", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<BaseIO>> getIntegrationType(@PathVariable("authtoken") String authtoken) throws Exception {
		List<BaseIO> integrationTypeList = new ArrayList<BaseIO>();
		authService.getAuthDTO(authtoken);
		for (IntegrationTypeEM interationType : IntegrationTypeEM.values()) {
			BaseIO integrationType = new BaseIO();
			integrationType.setCode(interationType.getCode());
			integrationType.setName(interationType.getName());
			integrationTypeList.add(integrationType);
		}
		return ResponseIO.success(integrationTypeList);
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<IntegrationIO> updateIntegration(@PathVariable("authtoken") String authtoken, @RequestBody IntegrationIO integration) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		IntegrationIO integrationIO = new IntegrationIO();
		if (StringUtil.isNotNull(integration.getAccount()) && StringUtil.isNotNull(integration.getAccessToken()) && StringUtil.isNotNull(integration.getAccessUrl()) && integration.getIntegrationType() != null && StringUtil.isNotNull(integration.getIntegrationType().getCode())) {
			IntegrationDTO integrationDTO = new IntegrationDTO();
			integrationDTO.setIntegrationtype(IntegrationTypeEM.getIntegrationTypeEM(integration.getIntegrationType().getCode()));
			integrationDTO.setAccount(integration.getAccount());
			integrationDTO.setAccessToken(integration.getAccessToken());
			integrationDTO.setAccessUrl(integration.getAccessUrl());
			integrationDTO.setProvider(StringUtil.isNull(integration.getProvider(), "NA"));
			integrationDTO.setActiveFlag(integration.getActiveFlag());
			integrationService.Update(authDTO, integrationDTO);
			integrationIO.setActiveFlag(integrationDTO.getActiveFlag());
		}
		return ResponseIO.success(integrationIO);
	}

	@RequestMapping(value = "/type/{integrationTypeCode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<IntegrationIO>> getIntegrationByIntegrationType(@PathVariable("authtoken") String authtoken, @PathVariable("integrationTypeCode") String integrationTypeCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		List<IntegrationIO> integrationIOList = new ArrayList<IntegrationIO>();
		IntegrationTypeEM integrationTypeEM = IntegrationTypeEM.getIntegrationTypeEM(integrationTypeCode);
		List<IntegrationDTO> integrationList = integrationService.getIntegration(authDTO, integrationTypeEM);
		for (IntegrationDTO integration : integrationList) {
			IntegrationIO integrationIO = new IntegrationIO();
			integrationIO.setAccessToken(integration.getAccessToken());
			integrationIO.setAccessUrl(integration.getAccessUrl());
			integrationIO.setAccount(integration.getAccount());
			integrationIO.setProvider(integration.getProvider());

			BaseIO intergationType = new BaseIO();
			intergationType.setCode(integration.getIntegrationtype().getCode());
			intergationType.setName(integration.getIntegrationtype().getName());
			integrationIO.setIntegrationType(intergationType);

			integrationIO.setActiveFlag(integration.getActiveFlag());
			integrationIOList.add(integrationIO);
		}
		return ResponseIO.success(integrationIOList);
	}

	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<IntegrationIO>> getAllIntegration(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "-1") int activeFlag) throws Exception {
		List<IntegrationIO> integrationIOList = new ArrayList<IntegrationIO>();

		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<IntegrationDTO> integrationList = integrationService.getAll(authDTO);
		for (IntegrationDTO integrationDTO : integrationList) {
			if (activeFlag != -1 && activeFlag != integrationDTO.getActiveFlag()) {
				continue;
			}
			IntegrationIO integrationIO = new IntegrationIO();
			integrationIO.setAccessToken(integrationDTO.getAccessToken());
			integrationIO.setAccessUrl(integrationDTO.getAccessUrl());
			integrationIO.setAccount(integrationDTO.getAccount());
			integrationIO.setProvider(integrationDTO.getProvider());

			BaseIO intergationType = new BaseIO();
			intergationType.setCode(integrationDTO.getIntegrationtype().getCode());
			intergationType.setName(integrationDTO.getIntegrationtype().getName());
			integrationIO.setIntegrationType(intergationType);

			integrationIO.setActiveFlag(integrationDTO.getActiveFlag());
			integrationIOList.add(integrationIO);
		}
		return ResponseIO.success(integrationIOList);
	}
}
