package fr.be.your.self.backend.cache;

import org.springframework.stereotype.Component;

@Component
public class CacheEngine {
	private CacheManager cacheManager;

	public CacheEngine() {
		super();
	}
	
	/*
	public CacheEngine(CacheManager cacheManager) {
	    this();
	    this.cacheManager = cacheManager;
    }
	*/
	
	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	public void processUpdate(CacheItem item) {
		cacheManager.updateItem(item);
	}
}
