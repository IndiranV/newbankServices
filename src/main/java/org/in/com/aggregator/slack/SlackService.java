package org.in.com.aggregator.slack;

import org.in.com.dto.AuthDTO;

public interface SlackService {
	public void sendAlert(String message);

	public void sendAlert(AuthDTO auth, String message);
}
