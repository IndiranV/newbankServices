package org.in.com.aggregator.fcm;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class FCMServiceImpl implements FCMService {

	@Async
	public void pushNotification(AuthDTO authDTO, String serverCode, String userDeviceIdKey, String title, String body, String image, JSONObject data) {
		FCMServerKeyEM fcmServerKeyEM = FCMServerKeyEM.getFCMServerKeyEM(serverCode);
		if (fcmServerKeyEM == null) {
			throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
		}
		FCMCommunicator fcmCommunicator = new FCMCommunicator();
		fcmCommunicator.pushFCMNotification(fcmServerKeyEM, userDeviceIdKey, title, body, image, data);
	}

	public void pushFlutterNotification(AuthDTO authDTO, String serverCode, List<String> userDeviceIdKey, String title, String body, String image, JSONObject data) {
		FCMServerKeyEM fcmServerKeyEM = FCMServerKeyEM.getFCMServerKeyEM(serverCode);
		if (fcmServerKeyEM == null) {
			throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
		}
		FCMCommunicator fcmCommunicator = new FCMCommunicator();
		fcmCommunicator.pushFlutterNotification(fcmServerKeyEM, userDeviceIdKey, title, body, image, data);
	}

	@Override
	public void alertzService(AuthDTO authDTO, String channelCode, JSONArray data) {
		String url = authDTO.getNamespaceCode() + "/ezeebus/" + channelCode + "/send";
		FCMCommunicator fcmCommunicator = new FCMCommunicator();
		fcmCommunicator.alertzService(url, data);
	}
}
