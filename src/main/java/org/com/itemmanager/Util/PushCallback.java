package org.com.itemmanager.Util;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class PushCallback implements MqttCallback {
	
	private ItemManager manager;
	private MQTTClient client;
	public PushCallback(MQTTClient client)
	{
		this.client = client;
	}

	@Override
	public void connectionLost(Throwable arg0) {
		// TODO Auto-generated method stub
		while (true){
			try {
				System.err.println("MQTTListener connectionLost.");
				Thread.sleep(20000);
				client.run();
				break;
			}catch (Exception e){
				continue;
			}
		}
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		try
		{
			System.err.println("Message get:"+new String(message.getPayload()));
			manager = ItemManager.GetInstance();
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					manager.DownloadMessage(new String(message.getPayload()));
				}
			};
			new Thread(runnable).start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}  
}
