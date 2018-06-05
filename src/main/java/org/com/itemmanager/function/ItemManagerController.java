package org.com.itemmanager.function;

import org.com.itemmanager.Util.Item;
import org.com.itemmanager.Util.ItemManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.LinkedList;
import java.util.List;

@Controller
public class ItemManagerController {
    @RequestMapping(value = "/",method = RequestMethod.GET)
    public String Index(Model model)
    {
        ItemManager manager = ItemManager.GetInstance();
        model.addAttribute("ManagerName", manager.getName());
        return "Home";
    }

    @RequestMapping(value = "/ItemList",method = RequestMethod.GET)
    public String ItemList(Model model)
    {
        ItemManager manager = ItemManager.GetInstance();
        List<Item> items = manager.GetItemList();
        model.addAttribute("ItemList", items);
        String serverHost = "../Controller/DeleteItem";
        model.addAttribute("serverHost", serverHost);
        return "ItemList";
    }

    @RequestMapping(value = "/Console", method = RequestMethod.GET)
    public String Console(Model model)
    {
        String serverHost = "../Controller/ConsoleInput";
        model.addAttribute("serverHost", serverHost);

        return "Console";
    }

    @RequestMapping(value = "/Map", method = RequestMethod.GET)
    public String Map(Model model)
    {
        String serverHost = ItemManager.GetInstance().getServerMapLocation();
        model.addAttribute("serverHost", serverHost);

        return "Map";
    }
}
