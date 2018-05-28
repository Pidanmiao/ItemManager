package org.com.itemmanager.function;

import org.com.itemmanager.Util.ItemManager;
import org.springframework.boot.autoconfigure.*;
import org.springframework.web.bind.annotation.*;
@RestController
@EnableAutoConfiguration
public class ReceiveMessage {
    @RequestMapping(value = "/ReceiveMessage", produces="application/json;charset=UTF-8")
    public String ReceiveItemMessage(@RequestBody String JsData)
    {
        try{
            ItemManager manager = ItemManager.GetInstance();
            System.err.println(JsData);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    manager.ReceiveMessage(JsData);
                }
            };
            new Thread(runnable).start();
            return "Message Accepted";
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
