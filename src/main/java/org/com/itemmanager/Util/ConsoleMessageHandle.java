package org.com.itemmanager.Util;

import org.com.itemmanager.json.JSONArray;
import org.com.itemmanager.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ConsoleMessageHandle {
    public enum ErrorType
    {
        MethodNotFound,
        SyntaxError,
        ItemNotFound,
        ServerError,
        UnknownError
    }

    private String[] args;
    private String Result;

    public ConsoleMessageHandle(String consoleMethod)
    {
        args = consoleMethod.split(" ");
    }

    public String getResult()
    {
        HandleConsole();
        return Result;
    }

    private String PushSpace(int num)
    {
        String result = "";
        for(int i=0;i<num;i++)
        {
            result += " ";
        }

        return result;
    }

    private String SendConsoleMessage(Message m)
    {
        Tools.HttpResponseContainer container = Tools.HttpRequest(ItemManager.GetInstance().getServerConsoleLocation(),m.getFull_message());
        //System.err.println("Respones:" + container.responseCode +" Body:"+container.responseBody);
        if(container.responseCode == 200)
        {
            return container.responseBody;
        }
        else
        {
            return null;
        }
    }

    public void HandleConsole()
    {
        args[0] = args[0].toLowerCase();
        if(args[0].equals("help"))
        {
            if (args.length == 1)
            {
                Result = GenerateHelpMessage(-1);
            }
            else if(args[1].equals("?"))
            {
                Result = GenerateHelpMessage(0);
            }
            else
            {
                for(int i = 0, len = ConsoleMessageHandle.ConsoleMethodList.length; i<len; i++)
                {
                    if(ConsoleMessageHandle.ConsoleMethodList[i].equals(args[1]))
                    {
                        Result = GenerateHelpMessage(i);
                    }
                }
                Result = GenerateErrorMessage(ConsoleMessageHandle.ErrorType.MethodNotFound, args[1]);
            }
        }
        else if(args[0].equals("list"))
        {
            List<Item> items = null;
            if(args.length == 1)
            {
                items = ItemManager.GetInstance().GetItemList();
            }
            else
            {
                String JSData = SendConsoleMessage(Message.GenerateGetItemListMessage(args[1]));
                if(JSData == null)
                {
                    Result = GenerateErrorMessage(ErrorType.ServerError,"");
                }
                else {
                    JSONObject obj = new JSONObject(JSData).getJSONObject("data");
                    if (obj.has("itemList")) {
                        items = new ArrayList<>();
                        JSONArray itemArr = obj.getJSONArray("itemList");
                        for (Object o : itemArr) {
                            JSONObject jo = (JSONObject) o;
                            Item item = new Item();
                            item.name = jo.getString("deviceName");
                            item.mac = jo.getString("deviceID");
                            item.location = null;
                            items.add(item);
                        }
                    } else {
                        Result = GenerateErrorMessage(ErrorType.ItemNotFound, "itemManager: " + args[1]);
                    }
                }
            }
            if(items != null)
            {
                int namelen = 0;
                int idlen = 0;
                for(Item item:items)
                {
                    if(item.name.length() > namelen) namelen = item.name.length();
                    if(item.mac.length() > idlen) idlen = item.mac.length();
                }
                Result = "Name"+PushSpace(namelen-2)+"ID"+PushSpace(idlen);
                for(Item item:items)
                {
                    String temp ="\n" + item.name+PushSpace(namelen-item.name.length()+2)+
                            item.mac+PushSpace(idlen-item.mac.length()+2);
                    Result += temp;
                }
            }
        }
        else if(args[0].equals("remove"))
        {
            if(args.length == 1)
            {
                Result = GenerateErrorMessage(ErrorType.SyntaxError, args[0]);
            }
            else if(args.length == 2)
            {
                Result = ItemManager.GetInstance().RemoveItem(args[1], null);
            }
            else
            {
                Result = ItemManager.GetInstance().RemoveItem(args[1], args[2]);
            }
        }
        else if(args[0].equals("find"))
        {
            if(args.length <= 2)
            {
                Result = GenerateErrorMessage(ErrorType.SyntaxError, args[0]);
            }
            else
            {
                if(args[1].equals("-m"))
                {
                    String JSData = SendConsoleMessage(Message.GenerateFindManagerMessage(args[2]));
                    if(JSData == null)
                    {
                        Result = GenerateErrorMessage(ErrorType.ServerError,"");
                    }
                    else {
                        JSONObject obj = new JSONObject(JSData);
                        boolean findresult = obj.getJSONObject("data").getBoolean("success");
                        if (findresult) {
                            Result = "Manager: " + args[2] + " found.";
                        } else {
                            Result = "Manager: " + args[2] + "not found.";
                        }
                    }
                }
                else if(args[1].equals("-i")) {
                    String JSData = SendConsoleMessage(Message.GenerateFindItemMessage(args[2]));
                    if (JSData == null) {
                        Result = GenerateErrorMessage(ErrorType.ServerError, "");
                    }
                    else {
                        JSONObject obj = new JSONObject(JSData).getJSONObject("data");
                        if (obj.has("items")) {
                            List<Item> items = new ArrayList<>();
                            JSONArray itemArr = obj.getJSONArray("items");
                            for (Object o : itemArr) {
                                JSONObject jo = (JSONObject) o;
                                Item item = new Item();
                                item.name = jo.getString("itemName");
                                item.mac = jo.getString("itemID");
                                item.location = jo.getString("manager");
                                items.add(item);
                            }


                            int namelen = 0;
                            int idlen = 0;
                            int managerlen = 0;
                            for (Item item : items) {
                                if (item.name.length() > namelen) namelen = item.name.length();
                                if (item.mac.length() > idlen) idlen = item.mac.length();
                                if (item.location.length() > managerlen) managerlen = item.location.length();
                            }
                            Result = "Name" + PushSpace(namelen - 2) + "ID" + PushSpace(idlen) + "Manager" + PushSpace(managerlen - 5);
                            for (Item item : items) {
                                String temp = "\n" + item.name + PushSpace(namelen - item.name.length() + 2) +
                                        item.mac + PushSpace(idlen - item.mac.length() + 2) +
                                        item.location + PushSpace(managerlen - item.location.length() + 2);
                                Result += temp;
                            }
                        } else {
                            Result = GenerateErrorMessage(ErrorType.ItemNotFound, "ItemName: " + args[2]);
                        }
                    }
                }
                else
                {
                    Result = GenerateErrorMessage(ErrorType.SyntaxError, args[0]+" "+args[1]);
                }

            }
        }
        else
        {
            Result = GenerateErrorMessage(ConsoleMessageHandle.ErrorType.MethodNotFound, args[0]);
        }
    }

    public static final String[] ConsoleMethodList = new String[]
            {
              "help", "list", "remove", "find"
            };
    private static final String[] HelpList = new String[]
            {
                    "help: show help for itemManager.",
                    "list: list all items registered on this manager.",
                    "remove: remove item.",
                    "find: find item or itemManager."
            };

    private String GenerateErrorMessage(ErrorType errorType, String ErrorSource)
    {
        switch (errorType)
        {
            case MethodNotFound:
                return "No such method "+ ErrorSource +".Use 'help' for more information.";
            case SyntaxError:
                return "Syntax error.Use 'help "+ErrorSource+"' for help.";
            case ItemNotFound:
                return "Cannot find " + ErrorSource + ".";
            case ServerError:
                return "Server Error.";
            default:
                return "Unknown Error";
        }
    }

    private String GenerateHelpMessage(int helpTarget)
    {
        if(helpTarget <0)
        {
            String res = "";
            for (String s: HelpList) {
                res += s+"\n";
            }
            return res;
        }
        else if(helpTarget < HelpList.length)
        {
            return  HelpList[helpTarget];
        }
        else
        {
            return  "Unknown Error";
        }
    }
}
