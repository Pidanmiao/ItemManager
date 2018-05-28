package org.com.itemmanager.Util;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;
import java.net.HttpURLConnection;
import java.net.URL;

import org.com.itemmanager.json.JSONArray;
import org.com.itemmanager.json.JSONObject;


public class ItemManager {
    private static final ItemManager Instance = new ItemManager();
	private Dictionary<String, Item> itemList;
	private String name = "testName";
	private String serverLocation = "location";
	private JSONArray itemArray;

	
	private ItemManager()
	{
		System.err.println("ItemManager Initializing...");
		itemList = new Hashtable<>();
        try{
            String JsData = Tools.readToString("./Conf/ItemManagerConfig.json");
            if(JsData != null)
            {
                JSONObject jsonObject = new JSONObject(JsData);
                name = jsonObject.getJSONObject("Core").getString("name");
                serverLocation = jsonObject.getJSONObject("Core").getString("serverLocation");
            }
			String ItemData = Tools.readToString("./DB/ItemList.json");
            if (ItemData != null)
			{
				JSONObject itemObject = new JSONObject(ItemData);
				itemArray = itemObject.getJSONArray("items");
				for(int i=0,l=itemArray.length();i<l;i++)
				{
					Item item = new Item();
					JSONObject obj = itemArray.getJSONObject(i);
					item.name = obj.getString("name");
					item.mac = obj.getString("mac");
					item.location = obj.getString("location");
					itemList.put(item.name,item);
				}
			}
			else
            {
                itemArray = new JSONArray();
            }
			System.err.println(itemList.toString());
			System.err.println("ItemManager Initialized.");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

	}
	public static ItemManager GetInstance()
	{
		return Instance;
	}

	public String getName()
    {
        return this.name;
    }

    public List<Item> GetItemList()
    {
        List<Item> items = new ArrayList<>();
        Enumeration<String> keys = itemList.keys();
        while (keys.hasMoreElements())
        {
            String key = keys.nextElement();
            Item item = itemList.get(key);
            items.add(item);
        }

       return items;
    }

    public String HandleConsole(String MethodData)
    {
    	String[] args = MethodData.split(" ");
    	args[0] = args[0].toLowerCase();
        if(args[0].equals("help"))
        {
            if (args.length == 1)
            {
                return ConsoleMessageGenerator.GenerateHelpMessage(-1);
            }
            else if(args[1].equals("?"))
            {
                return ConsoleMessageGenerator.GenerateHelpMessage(0);
            }
            else
            {
                for(int i=0, len=ConsoleMessageGenerator.ConsoleMethodList.length;i<len;i++)
                {
                    if(ConsoleMessageGenerator.ConsoleMethodList[i].equals(args[1]))
                    {
                        return ConsoleMessageGenerator.GenerateHelpMessage(i);
                    }
                }
                return ConsoleMessageGenerator.GenerateErrorMessage(ConsoleMessageGenerator.ErrorType.NotFound, args[1]);
            }
        }
        else
        {
            return ConsoleMessageGenerator.GenerateErrorMessage(ConsoleMessageGenerator.ErrorType.NotFound, args[0]);
        }
    }
	
	private void HandleMessage(Message m)
	{
		String contype = m.getContent_type();
		JSONObject dataObject = (JSONObject)(new JSONObject(m.getFull_message()).get("data"));
		if(contype.equals(Message.ContentType.RegistType))
		{
			RegistItem(dataObject);
		}
		else if(contype.equals(Message.ContentType.RemoveDeviceType))
		{
			String ItemName = dataObject.getString("deviceName");
			String ItemMac = dataObject.getString("deviceID");
			String ManagerName = dataObject.getString("manager");
			if(ManagerName == this.name)
			{
				try
				{
					if(itemList.get(ItemName).mac.equals(ItemMac))
					{
						for(int i=0,l=itemArray.length();i<l;i++)
						{
							JSONObject obj = itemArray.getJSONObject(i);
							if(obj.getString("name").equals(ItemName))
							{
								itemArray.remove(i);
								JSONObject ItemObj = new JSONObject();
								ItemObj.put("items", itemArray);
								Tools.writeString("./DB/ItemList.json", ItemObj.toString());
								break;
							}
						}
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		else if(contype.equals(Message.ContentType.ManagerRequestType) || contype.equals(Message.ContentType.ManagerResponseType))
		{
			if(m.isSendfalg())
			{
				if(this.name.equals(dataObject.getString("targetManager")))
				{
					String ItemName = dataObject.getString("targetName");
					Item item = itemList.get(ItemName);
					if(item != null){
						SendMessage(m,item);
					}
					else {
					    item = itemList.get(dataObject.getString("resourceName"));
					    if(item != null)
						{
							SendMessage(Message.GenerateErrorMessasge(m,"No Such Item"), item);
						}
                    }
				}
				else
				{
					UploadMessage(m);
				}
			}
			else
			{
				if(this.name.equals(dataObject.getString("targetManager")))
				{
					String ItemName = dataObject.getString("targetName");
					Item item = itemList.get(ItemName);
					if(item != null){
						SendMessage(m,item);
					}
                    else {
                        UploadMessage(Message.GenerateErrorMessasge(m,"No Such Item"));
                    }
				}
			}
		}
	}
	
	private void RegistItem(JSONObject dataObject)
	{
		Item item = new Item();
		item.name = dataObject.getString("deviceName");
		item.mac = dataObject.getString("deviceID");
		item.location = dataObject.getString("location");
		String manager = dataObject.get("manager").toString();
        System.err.println(item.toString()+" , "+"manager="+manager);

        try
		{
            if(itemList.get(item.name) == null)
            {
                System.err.println("New item");
				JSONObject obj = new JSONObject();
				obj.put("name",item.name);
				obj.put("mac",item.mac);
				obj.put("location",item.location);
				itemArray.put(obj);
				JSONObject ItemObj = new JSONObject();
				ItemObj.put("items", itemArray);
				Tools.writeString("./DB/ItemList.json", ItemObj.toString());
            }
            else
            {
                System.err.println("Update item");
                JSONObject obj = new JSONObject();
				obj.put("name",item.name);
				obj.put("mac",item.mac);
				obj.put("location",item.location);
				for(int i=0,l=itemArray.length();i<l;i++)
				{
					JSONObject oldobj = itemArray.getJSONObject(i);
					if(obj.getString("name").equals(item.name))
					{
						itemArray.remove(i);
						itemArray.put(obj);
						JSONObject ItemObj = new JSONObject();
						ItemObj.put("items", itemArray);
						Tools.writeString("./DB/ItemList.json", ItemObj.toString());
						break;
					}
				}
            }
            itemList.put(item.name, item);
            if(manager.equals("null")) manager = null;

			Message m = Message.GenerateRegistResultMessage(item, this.name, true);
			SendMessage(m,item);
		}
		catch(Exception e)
		{
		    e.printStackTrace();
		    Message m = Message.GenerateRegistResultMessage(item, e.getMessage(), false);
			SendMessage(m,item);
			return;
		}
		if((manager == null) || !(manager.equals(this.name)))
		{
			Message m = Message.GenerateItemMessage(item, manager, this.name);
			UploadMessage(m);
		}
	}
	public void ReceiveMessage(String fullMessage)
	{
		Message m = new Message(fullMessage, true);
		HandleMessage(m);
	}
	
	private void UploadMessage(Message m)
	{
		HttpURLConnection connection = null;
        InputStream is = null;
        OutputStream os = null;
        BufferedReader br = null;
        String result = null;
		try
		{
			URL url = new URL(this.serverLocation);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setConnectTimeout(15000);
			connection.setReadTimeout(60000);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestProperty("accept", "*/*");  
			connection.setRequestProperty("connection", "Keep-Alive");  
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");  
			os = connection.getOutputStream();
			os.write((m.getFull_message()).getBytes());
            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
            	System.err.println("Message Send:" + m.getFull_message());
                is = connection.getInputStream();
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                StringBuffer sbf = new StringBuffer();
                String temp = null;
                while ((temp = br.readLine()) != null) {
                    sbf.append(temp);
                    sbf.append("\r\n");
                }
                result = sbf.toString();
                System.err.println(result);
            }
			else
			{
				System.err.println("Response: "+responseCode);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void DownloadMessage(String fullMessage)
	{
		Message m = new Message(fullMessage, false);
		HandleMessage(m);
	}
	
	private void SendMessage(Message m,Item item)
	{
		HttpURLConnection connection = null;
        InputStream is = null;
        OutputStream os = null;
        BufferedReader br = null;
        String result = null;
		try
		{
			URL url = new URL(item.location);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setConnectTimeout(15000);
			connection.setReadTimeout(60000);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestProperty("accept", "*/*");  
			connection.setRequestProperty("connection", "Keep-Alive");  
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");  
			os = connection.getOutputStream();
			os.write((m.getFull_message()).getBytes());
			int responseCode;
			responseCode = connection.getResponseCode();
			if ( responseCode == 200) {
                System.err.println("Message Send:" + m.getFull_message());
                is = connection.getInputStream();
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                StringBuffer sbf = new StringBuffer();
                String temp = null;
                while ((temp = br.readLine()) != null) {
                    sbf.append(temp);
                    sbf.append("\r\n");
                }
                result = sbf.toString();
				System.err.println(result);
            }
            else
			{
				System.err.println("Response: "+responseCode+" Message:"+m.getFull_message());
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}

