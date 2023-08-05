package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.in.com.controller.web.io.GroupIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.UserIO;
import org.in.com.controller.web.io.UserProfileNotificationIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.UserProfileNotificationDTO;
import org.in.com.service.UserProfileNotificationService;
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
@RequestMapping("/{authtoken}/notification")
public class UserProfileNotificationController extends BaseController {
	@Autowired
	UserProfileNotificationService userProfileNotificationService;

	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<UserProfileNotificationIO>> getAllUserProfileNotifications(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "-1") int activeFlag) throws Exception {
		List<UserProfileNotificationIO> upnList = new ArrayList<UserProfileNotificationIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<UserProfileNotificationDTO> list = (List<UserProfileNotificationDTO>) userProfileNotificationService.getAll(authDTO);
			for (UserProfileNotificationDTO upnDTO : list) {
				if (activeFlag != -1 && activeFlag != upnDTO.getActiveFlag()) {
					continue;
				}
				UserProfileNotificationIO upnIO = new UserProfileNotificationIO();
				upnIO.setCode(upnDTO.getCode());
				upnIO.setName(upnDTO.getName());
				upnIO.setMessage(upnDTO.getMessage());
				upnIO.setActiveFrom(upnDTO.getActiveFrom());
				upnIO.setActiveTo(upnDTO.getActiveTo());
				upnIO.setCommentFlag(upnDTO.getCommentFlag());
				if (upnDTO.getGroup() != null && upnDTO.getGroup().getId() != 0) {
					GroupIO groupIO = new GroupIO();
					groupIO.setName(upnDTO.getGroup().getName());
					groupIO.setCode(upnDTO.getGroup().getCode());
					upnIO.setGroup(groupIO);
				}
				if (upnDTO.getUser() != null && upnDTO.getUser().getId() != 0) {
					UserIO userIO = new UserIO();
					userIO.setName(upnDTO.getUser().getName());
					userIO.setCode(upnDTO.getUser().getCode());
					upnIO.setUser(userIO);
				}
				upnIO.setActiveFlag(upnDTO.getActiveFlag());
				upnList.add(upnIO);
			}

		}
		return ResponseIO.success(upnList);
	}

	@RequestMapping(value = "update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<UserProfileNotificationIO> updateNotification(@PathVariable("authtoken") String authtoken, @RequestBody UserProfileNotificationIO userProfileNotification) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		UserProfileNotificationIO io = new UserProfileNotificationIO();
		if (authDTO != null) {
			UserProfileNotificationDTO upnDTO = new UserProfileNotificationDTO();
			upnDTO.setCode(userProfileNotification.getCode());
			upnDTO.setName(userProfileNotification.getName());
			upnDTO.setMessage(userProfileNotification.getMessage());
			upnDTO.setActiveFrom(userProfileNotification.getActiveFrom());
			upnDTO.setCommentFlag(userProfileNotification.getCommentFlag());
			upnDTO.setActiveTo(userProfileNotification.getActiveTo());
			if (userProfileNotification.getGroup() != null && userProfileNotification.getGroup().getCode() != null) {
				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setCode(userProfileNotification.getGroup().getCode());
				upnDTO.setGroup(groupDTO);
			}
			if (userProfileNotification.getUser() != null && userProfileNotification.getUser().getCode() != null) {
				UserDTO userDTO = new UserDTO();
				userDTO.setCode(userProfileNotification.getUser().getCode());
				upnDTO.setUser(userDTO);
			}
			upnDTO.setActiveFlag(userProfileNotification.getActiveFlag());

			userProfileNotificationService.Update(authDTO, upnDTO);

			io.setCode(upnDTO.getCode());
			io.setActiveFlag(upnDTO.getActiveFlag());
		}
		return ResponseIO.success(io);
	}

	@RequestMapping(value = "/like/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<UserProfileNotificationIO> updateNotificationLike(@PathVariable("authtoken") String authtoken, @RequestBody UserProfileNotificationIO userProfileNotification) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		UserProfileNotificationIO io = new UserProfileNotificationIO();
		if (authDTO != null) {
			UserProfileNotificationDTO upnlDTO = new UserProfileNotificationDTO();
			upnlDTO.setCode(userProfileNotification.getCode());
			upnlDTO.setComments(userProfileNotification.getComments());
			upnlDTO.setActiveFlag(userProfileNotification.getActiveFlag());
			userProfileNotificationService.updateLike(authDTO, upnlDTO);

			io.setCode(upnlDTO.getCode());
			io.setActiveFlag(upnlDTO.getActiveFlag());
		}
		return ResponseIO.success(io);
	}

	@RequestMapping(value = "/like", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<UserProfileNotificationIO>> getAvailableNotifications(@PathVariable("authtoken") String authtoken) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<UserProfileNotificationIO> upnList = new ArrayList<UserProfileNotificationIO>();
		if (authDTO != null) {
			UserProfileNotificationDTO notificationDTO = new UserProfileNotificationDTO();
			List<UserProfileNotificationDTO> list = (List<UserProfileNotificationDTO>) userProfileNotificationService.get(authDTO, notificationDTO);
			for (UserProfileNotificationDTO upnDTO : list) {
				UserProfileNotificationIO upnIO = new UserProfileNotificationIO();
				upnIO.setName(upnDTO.getName());
				upnIO.setCode(upnDTO.getCode());
				upnIO.setMessage(upnDTO.getMessage());
				upnIO.setActiveFrom(upnDTO.getActiveFrom());
				upnIO.setCommentFlag(upnDTO.getCommentFlag());
				upnList.add(upnIO);
			}

		}
		return ResponseIO.success(upnList);
	}

}
