package org.com.itemmanager;

import org.com.itemmanager.Util.MQTTClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ItemManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ItemManagerApplication.class, args);
        MQTTClient client = new MQTTClient();
        client.run();
    }
}
