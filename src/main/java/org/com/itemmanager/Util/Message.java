package org.com.itemmanager.Util;
import org.com.itemmanager.json.JSONObject;

public class Message {
	private String content_type;
	private boolean sendfalg;
	private String full_message;
	
	public static class ContentType
	{
		static final String RegistType = "manager-register";
		static final String RegistResultType = "register-result";
		static final String NewDeviceType = "device-new";
		static final String ChangeDeviceType = "device-change";
		static final String RemoveDeviceType = "device-remove";
		static final String ManagerRequestType = "device-manager-request";
		static final String ManagerResponseType = "device-manager-response";
	}
	
	public Message(String fullMessage, boolean sendflag)
	{
		this.sendfalg = sendflag;
		this.full_message = fullMessage;
		JSONObject jObject = new JSONObject(fullMessage);
		content_type = jObject.getString("contentType");
	}

	public String getContent_type() {
		return content_type;
	}

	public void setContent_type(String content_type) {
		this.content_type = content_type;
	}

	public boolean isSendfalg() {
		return sendfalg;
	}

	public void setSendfalg(boolean sendfalg) {
		this.sendfalg = sendfalg;
	}

	public String getFull_message() {
		return full_message;
	}

	public void setFull_message(String full_message) {
		this.full_message = full_message;
	}
	
	public static Message GenerateRegistResultMessage(Item item, String infoStr, boolean successFlag)
	{
		JSONObject jObject = new JSONObject();
		JSONObject dataObject = new JSONObject();
		dataObject.put("success", successFlag);
		if(successFlag)
		{
			dataObject.put("manager", infoStr);
		}
		else
		{
			dataObject.put("error-message", infoStr);	
		}
		dataObject.put("deviceName", item.name);
		dataObject.put("deviceID", item.mac);
		jObject.put("contentType", ContentType.RegistResultType);
		jObject.put("data", dataObject);
		return new Message(jObject.toString(), true);
	}
	
	public static Message GenerateItemMessage(Item item, String oldManager, String newManager)
	{
		JSONObject jObject = new JSONObject();
		JSONObject dataObject = new JSONObject();
		if(oldManager == null)
		{
			jObject.put("contentType", ContentType.NewDeviceType);
		}
		else
		{
			jObject.put("contentType", ContentType.ChangeDeviceType);
			dataObject.put("old-manager", oldManager);
		}
		dataObject.put("new-manager", newManager);
		dataObject.put("deviceName", item.name);
		dataObject.put("deviceID", item.mac);
		jObject.put("data", dataObject);
		return new Message(jObject.toString(), true);
	}

	public static Message GenerateErrorMessasge(Message m, String ErrorMessage)
	{
		JSONObject jObject = new JSONObject();
		JSONObject dataObject = new JSONObject();
		jObject.put("contentType", "device-manager-response");
		JSONObject targetDataObject = (JSONObject)(new JSONObject(m).get("data"));
		dataObject.put("resourceManager",targetDataObject.getString("targetManager"));
		dataObject.put("resourceName",targetDataObject.getString("targetName"));
		dataObject.put("targetManager",targetDataObject.getString("resourceManager"));
		dataObject.put("targetName",targetDataObject.getString("resourceName"));
		dataObject.put("success", false);
		dataObject.put("error", ErrorMessage);
		jObject.put("data", dataObject);

		return new Message(jObject.toString(), true);
	}
}
