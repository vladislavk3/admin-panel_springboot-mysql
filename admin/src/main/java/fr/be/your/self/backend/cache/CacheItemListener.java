package fr.be.your.self.backend.cache;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.Member;
import com.hazelcast.map.impl.MapListenerAdapter;

public class CacheItemListener extends MapListenerAdapter<String, CacheItem>
{
	private CacheManager cacheManager;
	private CacheEngine cacheEngine;
	
	public CacheItemListener(CacheManager cacheManager, CacheEngine cacheEngine)
	{
		this.cacheManager = cacheManager;
		this.cacheEngine = cacheEngine;
	}
	
	@Override
    public void entryAdded(EntryEvent<String, CacheItem> event) {
	    super.entryAdded(event);
	    
	    Member member = event.getMember(); 
	    if (!member.localMember())
	    {
	    	CacheItem item = event.getValue();
	    	if (item != null && !item.containUuid(cacheManager.getUuid()))
	    	{
	    		this.cacheEngine.processUpdate(item);
	    	}
	    }
    }

	@Override
    public void entryRemoved(EntryEvent<String, CacheItem> event) {
	    super.entryRemoved(event);
	    
	    Member member = event.getMember(); 
	    if (!member.localMember())
	    {
	    	CacheItem item = event.getValue();
	    	if (item != null && !item.containUuid(cacheManager.getUuid()))
	    	{
	    		this.cacheEngine.processUpdate(item);
	    	}
	    }
    }

	@Override
    public void entryUpdated(EntryEvent<String, CacheItem> event) {
	    super.entryUpdated(event);
	    
	    Member member = event.getMember(); 
	    if (!member.localMember())
	    {
	    	CacheItem item = event.getValue();
	    	if (item != null && !item.containUuid(cacheManager.getUuid()))
	    	{
	    		this.cacheEngine.processUpdate(item);
	    	}
	    }
    }
}