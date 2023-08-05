package org.in.com.controller.api;

import java.net.URL;

import org.in.com.aggregator.aws.S3Service;
import org.in.com.config.ApplicationConfig;
import org.in.com.controller.api.io.ResponseIO;
import org.in.com.controller.web.BaseController;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.NamespaceService;
import org.in.com.utils.BitsEnDecrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.sf.json.JSONObject;

@Controller
@RequestMapping("/utility")
public class UtilityController extends BaseController {
	@Autowired
	NamespaceService namespaceService;
	@Autowired
	S3Service s3Service;

	@RequestMapping(value = "/report/offline/retrieve", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<String> retriveOfflineReport(String fileAccessKey) throws Exception {
		JsonObject json = null;
		try {
			String data = BitsEnDecrypt.getDecoder(fileAccessKey);
			JsonParser parser = new JsonParser();
			json = (JsonObject) parser.parse(data);
		}
		catch (Exception e) {
			throw new ServiceException(ErrorCode.UNABLE_TO_PROVIDE_DATA);
		}
		if (json == null) {
			throw new ServiceException(ErrorCode.INVALID_NAMESPACE);
		}
		NamespaceDTO namespace = namespaceService.getNamespace(json.get("namespaceCode").getAsString());
		if (namespace == null) {
			throw new ServiceException(ErrorCode.INVALID_NAMESPACE);
		}

		if (!json.get("zoneCode").getAsString().equals(ApplicationConfig.getServerZoneCode())) {
			throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
		}
		AuthDTO authDTO = new AuthDTO();
		authDTO.setNamespace(namespace);
		URL url = s3Service.generatePresignedURL(authDTO, json);
		return ResponseIO.success(url.toString());
	}

	@RequestMapping(value = "/decrypt", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<JSONObject> decrypt(@RequestParam String data) throws Exception {
		String details = BitsEnDecrypt.getDecoder(data);
		JSONObject linkpayJSON = JSONObject.fromObject(details);
		return ResponseIO.success(linkpayJSON);
	}
}
