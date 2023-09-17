package com.catas.wicked.common.config;

import com.catas.wicked.common.bean.message.RequestMessage;
import com.catas.wicked.common.util.WebUtils;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.PersistentCacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;

import java.io.File;
import java.io.IOException;

@Factory
public class CacheConfiguration implements AutoCloseable {

    @Bean(preDestroy = "close")
    @Singleton
    public CacheManager cacheManager() throws IOException {
        CacheManager persistentCacheManager = CacheManagerBuilder.newCacheManagerBuilder()
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

    @Bean(preDestroy = "clear")
    @Singleton
    public Cache<String, RequestMessage> requestCache(CacheManager cacheManager) {
        return cacheManager.getCache("requestCache", String.class, RequestMessage.class);
    }

    @PreDestroy
    @Override
    public void close() throws Exception {

    }

    // @Override
    // public void destroy() throws CachePersistenceException {
    //     PersistentCacheManager cacheManager = (PersistentCacheManager) AppContextUtil.getBean("cacheManager");
    //     cacheManager.close();
    //     cacheManager.destroy();
    // }
}
