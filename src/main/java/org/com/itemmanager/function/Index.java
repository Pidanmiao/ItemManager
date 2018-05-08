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
public class Index {
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
        return "ItemList";
    }
}
