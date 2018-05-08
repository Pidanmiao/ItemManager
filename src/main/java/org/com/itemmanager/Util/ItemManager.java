package org.com.itemmanager.Util;

import java.io.*;
import java.util.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.*;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.com.itemmanager.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.bson.Document;


public class ItemManager {
    private static final ItemManager Instance = new ItemManager();
	private Dictionary<String, Item> itemList;
	private String name = "testName";
	private String serverLocation = "location";
	private String MongoHost = "localhost";
	private int MongoPort = 27017;
	private String MongoDataBase = "itemManager";
	private String MongoCollection = "itemList";

	@Value("ItemList.db")
    private String fileName;
	private MongoClient mongoClient;
	private MongoDatabase mongoDatabase;
	private MongoCollection<Document> collection;
	
	private ItemManager()
	{
		System.err.println("ItemManager Initializing...");
        try{
            String JsData = Tools.readToString("./Conf/ItemManagerConfig.json");
            if(JsData != null)
            {
                JSONObject jsonObject = new JSONObject(JsData);
                name = jsonObject.getJSONObject("Core").getString("name");
                serverLocation = jsonObject.getJSONObject("Core").getString("serverLocation");
                MongoHost = jsonObject.getJSONObject("MongoDB").getString("HOST");
                MongoPort = jsonObject.getJSONObject("MongoDB").getInt("PORT");
                MongoDataBase = jsonObject.getJSONObject("MongoDB").getString("DataBase");
                MongoCollection = jsonObject.getJSONObject("MongoDB").getString("Collection");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
		itemList = new Hashtable<String, Item>();
		mongoClient = new MongoClient( MongoHost , MongoPort );
		mongoDatabase = mongoClient.getDatabase(MongoDataBase);
		collection = mongoDatabase.getCollection(MongoCollection);
		FindIterable<Document> findIterable = collection.find();
		MongoCursor<Document> mongoCursor = findIterable.iterator();
		while(mongoCursor.hasNext()){
			Document doc = mongoCursor.next();
			Item item = new Item();
			item.name= doc.get("name").toString();
			item.mac = doc.get("mac").toString();
			item.location = doc.get("location").toString();
			itemList.put(item.name, item);
		}
		System.err.println(itemList.toString());
		System.err.println("ItemManager Initialized.");
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
					if(itemList.get(ItemName).mac == ItemMac)
					{
						itemList.remove(ItemName);
					}
				}
				catch(Exception e)
				{
					
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
					    UploadMessage(Message.GenerateErrorMessasge(m,"No Such Item"));
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
		String manager = dataObject.getString("manager");
        System.err.println(item.toString()+" , "+"manager="+manager);
        try
		{
            Document doc = new Document("name",item.name);
            doc.append("mac",item.mac);
            doc.append("location",item.location);
            if(itemList.get(item.name) == null)
            {
                collection.insertOne(doc);
            }
            else
            {
                Document olddoc = collection.find(new Document("name",item.name)).first();
                System.err.println(olddoc.toString());
                collection.replaceOne(olddoc, doc);
            }
            itemList.put(item.name, item);
            if(manager.equals("null")) manager = null;

			Message m = Message.GenerateRegistResultMessage(item, manager, true);
			SendMessage(m,item);
		}
		catch(Exception e)
		{
		    System.err.println(e.getMessage());
			Message m = Message.GenerateRegistResultMessage(item, e.getMessage(), true);
			SendMessage(m,item);
			return;
		}
		Message m = Message.GenerateItemMessage(item, manager, this.name);
		UploadMessage(m);
	}
	public void RecevieMessage(String fullMessage)
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
}

