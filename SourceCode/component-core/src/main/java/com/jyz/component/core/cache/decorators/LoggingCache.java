package com.jyz.component.core.cache.decorators;

import java.util.concurrent.locks.ReadWriteLock;

import com.jyz.component.core.cache.Cache;
import com.jyz.component.core.logging.Log;
import com.jyz.component.core.logging.LogFactory;

/**
 * 
 *	@author JoyoungZhang@gmail.com
 *
 */
public class LoggingCache implements Cache {

	private static final Log log = LogFactory.getLog(LoggingCache.class);

	private Cache delegate;
	protected int requests = 0;
	protected int hits = 0;

	public LoggingCache(Cache delegate) {
		this.delegate = delegate;
	}

	public String getId() {
		return delegate.getId();
	}

	public int getSize() {
		return delegate.getSize();
	}

	public void putObject(Object key, Object object) {
		delegate.putObject(key, object);
	}

	public Object getObject(Object key) {
		requests++;
		final Object value = delegate.getObject(key);
		if (value != null) {
			hits++;
		}
		if (log.isDebugEnabled()) {
			log.debug("Cache Hit Ratio [" + getId() + "]: " + getHitRatio());
		}
		return value;
	}

	public Object removeObject(Object key) {
		return delegate.removeObject(key);
	}

	public void clear() {
		delegate.clear();
	}

	public ReadWriteLock getReadWriteLock() {
		return delegate.getReadWriteLock();
	}

	public int hashCode() {
		return delegate.hashCode();
	}

	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

	private double getHitRatio() {
		return (double) hits / (double) requests;
	}

}
