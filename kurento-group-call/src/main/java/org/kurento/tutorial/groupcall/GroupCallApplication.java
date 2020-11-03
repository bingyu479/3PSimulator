package org.kurento.tutorial.groupcall;

import org.kurento.tutorial.groupcall.config.GroupCallWebSocketConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GroupCallApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(GroupCallApplication.class, args);
    }

}
