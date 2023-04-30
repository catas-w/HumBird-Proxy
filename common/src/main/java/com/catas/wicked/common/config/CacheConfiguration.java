package com.catas.wicked.common.config;

import com.catas.wicked.common.bean.RequestMessage;
import com.catas.wicked.common.util.AppContextUtil;
import com.catas.wicked.common.util.WebUtils;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.CachePersistenceException;
import org.ehcache.PersistentCacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;

@Configuration
public class CacheConfiguration implements DisposableBean {

    @Bean(name = "cacheManager")
    public CacheManager cacheManager() throws IOException {
        PersistentCacheManager persistentCacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .with(CacheManagerBuilder.persistence(new File(WebUtils.getStoragePath(), "cache")))
                .withCache("requestCache",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, RequestMessage.class,
                                ResourcePoolsBuilder.newResourcePoolsBuilder()
                                        .heap(10, EntryUnit.ENTRIES)
                                        .offheap(1, MemoryUnit.MB)
                                        .disk(100, MemoryUnit.MB, true)
                        )
                ).build(true);

        return persistentCacheManager;
    }

    @Bean(name = "requestCache")
    public Cache<String, RequestMessage> requestCache(@Autowired CacheManager cacheManager) {
        return cacheManager.getCache("requestCache", String.class, RequestMessage.class);
    }

    @Override
    public void destroy() throws CachePersistenceException {
        PersistentCacheManager cacheManager = (PersistentCacheManager) AppContextUtil.getBean("cacheManager");
        cacheManager.close();
        cacheManager.destroy();
    }
}
