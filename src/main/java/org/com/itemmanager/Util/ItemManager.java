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
	private String serverMethod = "method";
	private String consoleMethod = "console";
	private String mapMethod = "map";
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
                serverMethod = jsonObject.getJSONObject("Core").getString("serverMethod");
                consoleMethod = jsonObject.getJSONObject("Core").getString("serverConsole");
                mapMethod = jsonObject.getJSONObject("Core").getString("serverMap");
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
			if(itemArray == null)
            {
                itemArray = new JSONArray();
            }
			System.err.println(itemList.toString());
			System.err.println("ItemManager Initialized.");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            if(itemArray == null)
            {
                itemArray = new JSONArray();
            }
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

    public String getServerMessageLocation()
    {
        return serverLocation+serverMethod;
    }

    public String getServerConsoleLocation()
    {
        return serverLocation+consoleMethod;
    }

    public String getServerMapLocation()
    {
        return serverLocation+mapMethod;
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
			    if(itemList.get(ItemName).mac.equals(ItemMac))
			    {
			        RemoveItem(ItemName, ItemMac);
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

	public String RemoveItem(String itemName, String itemID)
    {
        String iName = null;
        String iID = null;
        try {
            if (itemName == null) {
                if (itemID == null) {
                    return "Cannot find item with neither itemName nor itemID.\n";
                } else {
                    while (itemList.elements().hasMoreElements()) {
                        Item item = itemList.elements().nextElement();
                        if (item.mac.equals(itemID)) {
                            iName = item.name;
                            iID = item.mac;
                            break;
                        }
                    }
                }
            } else {
                Item item = itemList.get(itemName);
                if ((itemID == null) || (item.mac.equals(itemID))) {
                    iName = item.name;
                    iID = item.mac;
                } else {
                    return "Cannot find item with both name: " + itemName + " and id: " + itemID + ".\n";
                }
            }

            for (int i = 0, l = itemArray.length(); i < l; i++) {
                JSONObject obj = itemArray.getJSONObject(i);
                if (obj.getString("name").equals(iName)) {
                    itemArray.remove(i);
                    JSONObject ItemObj = new JSONObject();
                    ItemObj.put("items", itemArray);
                    Tools.writeString("./DB/ItemList.json", ItemObj.toString());
                    break;
                }
            }
            itemList.remove(iName);

            return "Item with name: " + iName + " id: " + iID + " removed.\n";
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return e.getMessage();
        }
    }
	public void ReceiveMessage(String fullMessage)
	{
		Message m = new Message(fullMessage, true);
		HandleMessage(m);
	}
	
	private void UploadMessage(Message m)
	{
		Tools.HttpResponseContainer container = Tools.HttpRequest(this.serverLocation+this.serverMethod, m.getFull_message());
		if(container.responseCode == 200)
		{
			System.err.println("Message Send:" + m.getFull_message());
			System.err.println(container.responseBody);
		}
		else
		{
			System.err.println("Response: "+container.responseCode);
		}
	}
	
	public void DownloadMessage(String fullMessage)
	{
		Message m = new Message(fullMessage, false);
		HandleMessage(m);
	}
	
	private void SendMessage(Message m,Item item)
	{
        Tools.HttpResponseContainer container = Tools.HttpRequest(item.location, m.getFull_message());
		System.err.println("Respones:" + container.responseCode +" Body:"+container.responseBody);
        if(container.responseCode == 200)
        {
            System.err.println("Message Send:" + m.getFull_message());
            System.err.println(container.responseBody);
        }
        else
        {
            System.err.println("Response: "+container.responseCode);
        }
	}
}

