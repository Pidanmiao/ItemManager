package org.com.itemmanager.Util;

import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;  
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;  
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;  
import org.eclipse.paho.client.mqttv3.MqttException;  
import org.eclipse.paho.client.mqttv3.MqttSecurityException;  
import org.eclipse.paho.client.mqttv3.MqttTopic;  
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import org.com.itemmanager.json.JSONObject;

public class MQTTClient implements Runnable {
	public  String HOST = "tcp://192.168.1.109:61613";
    public  String TOPIC = "abcd";
    private String clientid = "client124";
    private MqttClient client;  

    private String userName = "test1";
    private String passWord = "test1";

    public MQTTClient()
    {
        try{
            String JsData = Tools.readToString("./Conf/MQTTConfig.json");
            if(JsData != null)
            {
                JSONObject jsonObject = new JSONObject(JsData);
                HOST = jsonObject.getString("HOST");
                TOPIC = jsonObject.getString("TOPIC");
                clientid = jsonObject.getString("clientID");
                userName = jsonObject.getString("userName");
                passWord = jsonObject.getString("passWord");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private MqttConnectOptions getOptions()
    {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(userName);
        options.setPassword(passWord.toCharArray());
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(120);

        return options;
    }
    @Override
    public void run() {
        try {
            if(client == null) {
                client = new MqttClient(HOST, clientid, new MemoryPersistence());
                client.setCallback(new PushCallback(MQTTClient.this));
            }

            connect(10);
        } catch (Exception e) {  
            e.printStackTrace();
        }  
    }

    public void stop()
    {
        try
        {
            if (client.isConnected()) {
                client.disconnect();
                System.err.println("client disconnected");
                return;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();

        }
    }
    private void connect(int nums) throws Exception
    {
        MqttConnectOptions options = getOptions();

        for(int i=0;i<nums;i++)
        {
            try
            {
                if (!client.isConnected()) {
                    client.connect(options);
                    int[] Qos  = {1};
                    String[] topic1 = {TOPIC};
                    client.subscribe(topic1, Qos);
                    System.err.println("connect succeed");
                    return;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.err.println("connect failed retrying "+i+"times");
                Thread.sleep(2000);
                continue;
            }
        }
        System.err.println("Cannot connect to server.");
        while(true)
        {
            try
            {
                if (!client.isConnected()) {
                    client.connect(options);
                    int[] Qos  = {1};
                    String[] topic1 = {TOPIC};
                    client.subscribe(topic1, Qos);
                    System.err.println("connect succeed");
                    return;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.err.println("connect failed.");
                Thread.sleep(600000);
                continue;
            }
        }
    }
}


