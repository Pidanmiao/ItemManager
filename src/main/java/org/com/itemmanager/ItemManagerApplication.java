package org.com.itemmanager;

import org.com.itemmanager.Util.MQTTClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ItemManagerApplication {
    public static void main(String[] args) {
        MQTTClient client = new MQTTClient();
        new Thread(client).start();
        SpringApplication.run(ItemManagerApplication.class, args);
    }
}
