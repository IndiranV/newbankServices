package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserFeedbackIO extends BaseIO {
	private String ticketCode;
	private String email;
	private String mobile;
	private String feedbackDate;
	private String comments;
	private String replyContent;
}
