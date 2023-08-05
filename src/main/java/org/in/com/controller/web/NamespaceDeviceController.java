package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.in.com.controller.web.io.GroupIO;
import org.in.com.controller.web.io.NamespaceDeviceAuthIO;
import org.in.com.controller.web.io.NamespaceDeviceIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.UserIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.NamespaceDeviceAuthDTO;
import org.in.com.dto.NamespaceDeviceDTO;
import org.in.com.dto.UserDTO;
import org.in.com.service.AuthService;
import org.in.com.service.NamespaceDeviceAuthService;
import org.in.com.service.NamespaceDeviceService;
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
@RequestMapping("/{authtoken}/namespace/device")
public class NamespaceDeviceController extends BaseController {

	@Autowired
	NamespaceDeviceService deviceService;
	@Autowired
	NamespaceDeviceAuthService deviceAuthService;
	@Autowired
	AuthService authService;

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<NamespaceDeviceIO>> getAllNamespaceDevices(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "-1") int activeFlag) throws Exception {
		List<NamespaceDeviceIO> devices = new ArrayList<NamespaceDeviceIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<NamespaceDeviceDTO> list = deviceService.getAll(authDTO);
			for (NamespaceDeviceDTO dto : list) {
				if (activeFlag != -1 && activeFlag != dto.getActiveFlag()) {
					continue;
				}
				NamespaceDeviceIO namespaceDeviceIO = new NamespaceDeviceIO();
				namespaceDeviceIO.setCode(dto.getCode());
				namespaceDeviceIO.setName(dto.getName());
				namespaceDeviceIO.setToken(dto.getToken());
				namespaceDeviceIO.setRemarks(dto.getRemarks());
				devices.add(namespaceDeviceIO);
			}
		}
		return ResponseIO.success(devices);
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<NamespaceDeviceIO> updateNamespaceDevice(@PathVariable("authtoken") String authtoken, @RequestBody NamespaceDeviceIO namespaceDevice) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		NamespaceDeviceIO namespaceDeviceIO = new NamespaceDeviceIO();
		if (authDTO != null) {
			NamespaceDeviceDTO dto = new NamespaceDeviceDTO();
			dto.setCode(namespaceDevice.getCode());
			dto.setName(namespaceDevice.getName());
			dto.setRemarks(namespaceDevice.getRemarks());
			dto.setToken(namespaceDevice.getToken());
			dto.setActiveFlag(namespaceDevice.getActiveFlag());
			deviceService.Update(authDTO, dto);
			namespaceDeviceIO.setCode(dto.getCode());
			namespaceDeviceIO.setActiveFlag(dto.getActiveFlag());
		}
		return ResponseIO.success(namespaceDeviceIO);
	}

	@RequestMapping(value = "/register/{namespaceCode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<NamespaceDeviceIO> registerNamespaceDevice(@PathVariable("authtoken") String authtoken, @PathVariable("namespaceCode") String namespaceCode, @RequestBody NamespaceDeviceIO namespaceDevice) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		NamespaceDeviceIO namespaceDeviceIO = new NamespaceDeviceIO();
		if (authDTO != null) {
			NamespaceDeviceDTO deviceDTO = new NamespaceDeviceDTO();
			deviceDTO.setCode(namespaceDevice.getCode());
			deviceDTO.setToken(namespaceDevice.getToken());
			deviceDTO.setActiveFlag(namespaceDevice.getActiveFlag());
			deviceDTO = deviceService.registerDevice(authDTO, getNamespaceDTO(namespaceCode), deviceDTO);

			namespaceDeviceIO.setToken(deviceDTO.getToken());
			namespaceDeviceIO.setCode(deviceDTO.getCode());
			namespaceDeviceIO.setActiveFlag(deviceDTO.getActiveFlag());
		}
		return ResponseIO.success(namespaceDeviceIO);
	}

	@RequestMapping(value = "/auth/{namespaceDeviceCode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<NamespaceDeviceAuthIO>> getAllNamespaceDeviceAuth(@PathVariable("authtoken") String authtoken, @PathVariable("namespaceDeviceCode") String namespaceDeviceCode) throws Exception {
		List<NamespaceDeviceAuthIO> list = new ArrayList<NamespaceDeviceAuthIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			NamespaceDeviceDTO namespaceDevice = new NamespaceDeviceDTO();
			namespaceDevice.setCode(namespaceDeviceCode);
			List<NamespaceDeviceAuthDTO> list2 = deviceAuthService.getAll(authDTO, namespaceDevice);
			for (NamespaceDeviceAuthDTO dto : list2) {
				NamespaceDeviceAuthIO deviceAuthIO = new NamespaceDeviceAuthIO();
				deviceAuthIO.setCode(dto.getCode());
				deviceAuthIO.setRefferenceType(dto.getRefferenceType());

				if (dto.getUser() != null) {
					UserIO userIO = new UserIO();
					userIO.setCode(dto.getUser().getCode());
					userIO.setName(dto.getUser().getName());
					deviceAuthIO.setUser(userIO);
				}

				else if (dto.getGroup() != null) {
					GroupIO groupIO = new GroupIO();
					groupIO.setCode(dto.getGroup().getCode());
					groupIO.setName(dto.getGroup().getName());
					deviceAuthIO.setGroup(groupIO);
				}
				list.add(deviceAuthIO);
			}
		}
		return ResponseIO.success(list);
	}

	@RequestMapping(value = "/auth/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<NamespaceDeviceAuthIO> update(@PathVariable("authtoken") String authtoken, @RequestBody NamespaceDeviceAuthIO namespaceDeviceAuthIO) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		NamespaceDeviceAuthIO deviceAuthIO = new NamespaceDeviceAuthIO();
		if (authDTO != null) {
			NamespaceDeviceAuthDTO dto = new NamespaceDeviceAuthDTO();
			dto.setCode(namespaceDeviceAuthIO.getCode());
			dto.setActiveFlag(namespaceDeviceAuthIO.getActiveFlag());
			dto.setRefferenceType(namespaceDeviceAuthIO.getRefferenceType());

			if (namespaceDeviceAuthIO.getRefferenceType().equals("UR") && namespaceDeviceAuthIO.getUser() != null) {
				UserDTO userDTO = new UserDTO();
				userDTO.setCode(namespaceDeviceAuthIO.getUser().getCode());
				dto.setUser(userDTO);
			}

			if (namespaceDeviceAuthIO.getRefferenceType().equals("GR") && namespaceDeviceAuthIO.getGroup() != null) {
				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setCode(namespaceDeviceAuthIO.getGroup().getCode());
				dto.setGroup(groupDTO);
			}
			NamespaceDeviceDTO namespaceDeviceDTO = new NamespaceDeviceDTO();
			namespaceDeviceDTO.setCode(namespaceDeviceAuthIO.getNamespaceDevice().getCode());
			dto.setNamespaceDevice(namespaceDeviceDTO);

			deviceAuthService.Update(authDTO, dto);
			deviceAuthIO.setCode(dto.getCode());
			deviceAuthIO.setActiveFlag(dto.getActiveFlag());
		}
		return ResponseIO.success(deviceAuthIO);
	}
}
