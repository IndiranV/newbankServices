package org.in.com.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.sf.json.JSONObject;

import org.in.com.utils.StringUtil;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserFeedbackDTO extends BaseDTO<UserFeedbackDTO> {

	private String email;
	private String ticketCode;
	private String mobile;
	private String comments;
	private String replyContent;
	private String feedbackDate;

	public String convertCommentReply() {
		String comment = comments;
		if (StringUtil.isNotNull(comments) && comments.startsWith("{") && comments.endsWith("}")) {
			JSONObject jsonObject = JSONObject.fromObject(comments);
			comment = jsonObject.getString("C");
		}
		JSONObject commentJSON = new JSONObject();
		commentJSON.put("C", comment);
		commentJSON.put("R", replyContent);
		return commentJSON.toString();
	}

	public String getComment() {
		String comment = comments;
		if (StringUtil.isNotNull(comment) && comment.startsWith("{") && comment.endsWith("}")) {
			JSONObject jsonObject = JSONObject.fromObject(comment);
			comment = jsonObject.has("C") ? jsonObject.getString("C") : null;
		}
		return comment;
	}

	public String getReply() {
		String comment = null;
		if (StringUtil.isNotNull(comments) && comments.startsWith("{") && comments.endsWith("}")) {
			JSONObject jsonObject = JSONObject.fromObject(comments);
			comment = jsonObject.has("R") ? jsonObject.getString("R") : null;
		}
		return comment;
	}
}
