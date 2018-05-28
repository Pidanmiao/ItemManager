package org.com.itemmanager.function;

import org.com.itemmanager.Util.ItemManager;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableAutoConfiguration
public class ConsoleHandle {
    @RequestMapping(value = "/ConsoleMethod", method = RequestMethod.POST)
    public String ReceiveConsoleMethod(@RequestBody String FuncData)
    {
        try
        {
            return ItemManager.GetInstance().HandleConsole(FuncData);
        }
        catch (Exception e)
        {
            return e.getMessage();
        }

    }
}
