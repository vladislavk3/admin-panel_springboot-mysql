package fr.be.your.self.backend.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class CacheManager implements InitializingBean, DisposableBean {
	
	private static final Logger logger = LoggerFactory.getLogger(CacheManager.class);
	
	private static final String DEFAULT_MAP_NAME = "CacheItems";
	
	@Autowired
	private ApplicationContext context;
	
	private CacheEngine cacheEngine;
	private HazelcastInstance hazelcastInstance;
	
	private String uuid;
	private IMap<String, CacheItem> mapItems;
	
	public CacheManager(HazelcastInstance hazelcastInstance) {
		this.hazelcastInstance = hazelcastInstance;
	}

	public String getUuid() {
		return uuid;
	}
	
	public void updateItem(CacheItem item)
	{
		if (this.mapItems != null && !item.containUuid(uuid))
		{
			item.addUuid(uuid);
			
			if (item.getValue() == null) {
				if (this.mapItems.containsKey(item.getKey())) {
					this.mapItems.remove(item.getKey());
				}
			} else {
				this.mapItems.put(item.getKey(), item);
			}
		}
	}
	
	public void updateItem(String key, Object value) {
		CacheItem item = new CacheItem(key, value, 0, 0);
		this.updateItem(item);
	}
	
	public CacheItem getItem(String key)
	{
		if (this.mapItems != null && this.mapItems.containsKey(key))
		{
			return this.mapItems.get(key);
		}
		
		return null;
	}
	
	public <T> T getItemValue(String key, Class<T> type)
	{
		if (this.mapItems != null && this.mapItems.containsKey(key))
		{
			CacheItem item = this.mapItems.get(key);
			if (item != null) {
				return item.getValue(type);
			}
		}
		
		return null;
	}
	
	@Override
    public void afterPropertiesSet() throws Exception {
		if (hazelcastInstance != null)
		{
			try 
			{
				this.cacheEngine = this.context.getBean(CacheEngine.class);
				this.cacheEngine.setCacheManager(this);
				
				this.uuid = hazelcastInstance.getCluster().getLocalMember().getUuid();
				
				this.mapItems = hazelcastInstance.getMap(DEFAULT_MAP_NAME);
				this.mapItems.addEntryListener(new CacheItemListener(this, cacheEngine), true);
			}
			catch (Exception ex) {
				logger.error("Initialize cache is error!", ex);
			}
		}
    }
	
	@Override
    public void destroy() throws Exception {
		if (hazelcastInstance != null)
		{
			try {
				hazelcastInstance.shutdown();
			} catch (Exception ex) {
				logger.error("Destroy cache is error!", ex);
			}
		}
    }
}