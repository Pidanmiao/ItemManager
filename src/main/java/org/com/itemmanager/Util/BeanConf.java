package org.com.itemmanager.Util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConf {
    @Bean("ItemManager")
    public ItemManager Manager()
    {
        return ItemManager.GetInstance();
    }
}
