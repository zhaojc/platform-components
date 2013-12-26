package com.jyz.component.core.cache.decorators;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

import com.jyz.component.core.cache.Cache;

/**
 *  @author JoyoungZhang@gmail.com
 */
public class TransactionalCache implements Cache {

	private Cache delegate;
	private boolean clearOnCommit;
	private Map<Object, AddEntry> entriesToAddOnCommit;
	private Map<Object, RemoveEntry> entriesToRemoveOnCommit;

	public TransactionalCache(Cache delegate) {
		this.delegate = delegate;
		this.clearOnCommit = false;
		this.entriesToAddOnCommit = new HashMap<Object, AddEntry>();
		this.entriesToRemoveOnCommit = new HashMap<Object, RemoveEntry>();
	}

	public String getId() {
		return delegate.getId();
	}

	public int getSize() {
		return delegate.getSize();
	}

	public Object getObject(Object key) {
		return delegate.getObject(key);
	}

	public ReadWriteLock getReadWriteLock() {
		return delegate.getReadWriteLock();
	}

	public void putObject(Object key, Object object) {
		entriesToRemoveOnCommit.remove(key);
		entriesToAddOnCommit.put(key, new AddEntry(delegate, key, object));
	}

	public Object removeObject(Object key) {
		entriesToAddOnCommit.remove(key);
		entriesToRemoveOnCommit.put(key, new RemoveEntry(delegate, key));
		return delegate.getObject(key);
	}

	public void clear() {
		reset();
		clearOnCommit = true;
	}

	public void commit() {
		delegate.getReadWriteLock().writeLock().lock();
		try {
			if (clearOnCommit) {
				delegate.clear();
			} else {
				for (RemoveEntry entry : entriesToRemoveOnCommit.values()) {
					entry.commit();
				}
			}
			for (AddEntry entry : entriesToAddOnCommit.values()) {
				entry.commit();
			}
			reset();
		} finally {
			delegate.getReadWriteLock().writeLock().unlock();
		}
	}

	public void rollback() {
		reset();
	}

	private void reset() {
		clearOnCommit = false;
		entriesToRemoveOnCommit.clear();
		entriesToAddOnCommit.clear();
	}

	private static class AddEntry {
		private Cache cache;
		private Object key;
		private Object value;

		public AddEntry(Cache cache, Object key, Object value) {
			this.cache = cache;
			this.key = key;
			this.value = value;
		}

		public void commit() {
			cache.putObject(key, value);
		}
	}

	private static class RemoveEntry {
		private Cache cache;
		private Object key;

		public RemoveEntry(Cache cache, Object key) {
			this.cache = cache;
			this.key = key;
		}

		public void commit() {
			cache.removeObject(key);
		}
	}

}
