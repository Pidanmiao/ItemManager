package org.com.itemmanager.function;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ItemManagerControllerHandle {
    @RequestMapping(value = "/MessageTest", produces="application/json;charset=UTF-8")
    public String MessageTest(@RequestBody String JSData)
    {
        System.out.println(JSData);
        return "Message received";
    }

}
