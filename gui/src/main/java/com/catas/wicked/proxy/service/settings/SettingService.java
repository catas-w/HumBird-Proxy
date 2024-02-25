package com.catas.wicked.proxy.service.settings;

import com.catas.wicked.common.config.ApplicationConfig;

public interface SettingService {

    /**
     * init setting page
     */
    void init();

    /**
     * init setting page values on display
     */
    void initValues(ApplicationConfig appConfig);

    /**
     * perform update setting values
     */
    void update(ApplicationConfig appConfig);
}
