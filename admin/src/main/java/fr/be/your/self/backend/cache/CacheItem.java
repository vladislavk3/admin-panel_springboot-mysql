package fr.be.your.self.backend.cache;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class CacheItem implements Serializable {
	/**
	 * 
	 */
    private static final long serialVersionUID = 2029788077576131588L;
    
	private Set<String> uuids;
    private String key;
    private Object value;
    private int type;
    private int action;
    
    public CacheItem() {
	    super();
    }

	public CacheItem(String key, Object value, int type, int action) {
	    super();
	    
	    this.key = key;
	    this.value = value;
	    this.type = type;
	    this.action = action;
    }

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public int getType() {
		return type;
	}
	
	public void setType(int type) {
		this.type = type;
	}

	public int getAction() {
		return action;
	}

	public void setAction(int action) {
		this.action = action;
	}

	public Set<String> getUuids() {
		return uuids;
	}

	public void setUuids(Set<String> uuids) {
		this.uuids = uuids;
	}
	
	public synchronized void addUuid(String uuid) {
		if (this.uuids == null)
		{
			this.uuids = new HashSet<String>();
		}
		
		this.uuids.add(uuid);
	}
	
	public synchronized boolean containUuid(String uuid)
	{
		if (this.uuids == null) {
			return false;
		}
		
		return this.uuids.contains(uuid);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getValue(Class<T> type) {
		if (this.value != null && type != null && !type.isInstance(this.value)) {
			throw new IllegalStateException("Cached value is not of required type [" + type.getName() + "]: " + value);
		}
		
		return (T) value;
	}
}
