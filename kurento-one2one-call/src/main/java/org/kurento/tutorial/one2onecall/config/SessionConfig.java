package org.kurento.tutorial.one2onecall.config;

import org.kurento.tutorial.one2onecall.room.RoomManager;
import org.kurento.tutorial.one2onecall.users.UserRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SessionConfig {

	@Bean
	public RoomManager roomManager() {
		return new RoomManager();
	}

}
