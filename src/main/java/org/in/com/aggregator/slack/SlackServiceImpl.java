package org.in.com.aggregator.slack;

import org.in.com.config.ApplicationConfig;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@Service
@EnableAsync
public class SlackServiceImpl implements SlackService {

	@Async
	public void sendAlert(AuthDTO auth, String message) {
		StringBuilder content = new StringBuilder();
		content.append(ApplicationConfig.getServerZoneCode());
		content.append(Text.HYPHEN);
		content.append(auth.getNamespaceCode());
		content.append(Text.HYPHEN);
		content.append(message);
		SlackCommunicator slackCommunicator = new SlackCommunicator();
		slackCommunicator.sendAlert(message);
	}

	public void sendAlert(String message) {
		StringBuilder content = new StringBuilder();
		content.append(ApplicationConfig.getServerZoneCode());
		content.append(Text.HYPHEN);
 		content.append(message);
		SlackCommunicator slackCommunicator = new SlackCommunicator();
		slackCommunicator.sendAlert(message);
	}

}
