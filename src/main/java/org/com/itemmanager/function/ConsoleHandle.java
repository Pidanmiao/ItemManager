package org.com.itemmanager.function;

import org.com.itemmanager.Util.ConsoleMessageHandle;
import org.com.itemmanager.Util.ItemManager;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableAutoConfiguration
public class ConsoleHandle {
    @RequestMapping(value = "/Controller/DeleteItem", method = RequestMethod.POST)
    public String DeleteItem(@RequestBody String ItemID)
    {
        return ItemManager.GetInstance().RemoveItem(null, ItemID);
    }
    @RequestMapping(value = "/Controller/ConsoleInput", method = RequestMethod.POST)
    public String ReceiveConsoleMethod(@RequestBody String FuncData)
    {
        try
        {
            ConsoleMessageHandle handle = new ConsoleMessageHandle(FuncData);
            return handle.getResult();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return e.getMessage();
        }

    }
}
