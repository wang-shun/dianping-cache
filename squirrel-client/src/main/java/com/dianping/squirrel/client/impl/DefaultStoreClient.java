package com.dianping.squirrel.client.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.squirrel.client.StoreClient;
import com.dianping.squirrel.client.StoreClientFactory;
import com.dianping.squirrel.client.StoreKey;
import com.dianping.squirrel.client.config.StoreCategoryConfig;
import com.dianping.squirrel.client.config.StoreCategoryConfigManager;
import com.dianping.squirrel.client.core.StoreCallback;
import com.dianping.squirrel.client.monitor.SizeMonitor;
import com.dianping.squirrel.client.monitor.StatusHolder;
import com.dianping.squirrel.client.monitor.TimeMonitor;
import com.dianping.squirrel.common.config.ConfigChangeListener;
import com.dianping.squirrel.common.config.ConfigManagerLoader;
import com.dianping.squirrel.common.exception.StoreException;
import com.dianping.squirrel.common.util.PathUtils;

public class DefaultStoreClient implements StoreClient {
    
	private static final Logger logger = LoggerFactory.getLogger(DefaultStoreClient.class);

	private static final String KEY_STORE_ENABLE = "squirrel.enable";
	private static final boolean DEFAULT_STORE_ENABLE = true;
	
	private volatile boolean enable = DEFAULT_STORE_ENABLE;
	
	public DefaultStoreClient() {
        this.enable = ConfigManagerLoader.getConfigManager().getBooleanValue(KEY_STORE_ENABLE, DEFAULT_STORE_ENABLE);
        try {
            ConfigManagerLoader.getConfigManager().registerConfigChangeListener(new ConfigChangeListener() {

                @Override
                public void onChange(String key, String value) {
                    if(KEY_STORE_ENABLE.equals(key)) {
                        enable = Boolean.parseBoolean(value);
                    }
                }
                
            });
        } catch (Exception e) {
            logger.error("failed to register config change listener", e);
        }
        try {
            TimeMonitor.getInstance();
        } catch (Throwable t) {
            logger.warn("error while initializing time monitor", t);
        }
        try {
            SizeMonitor.getInstance();
        } catch (Throwable t) {
            logger.warn("error while initializing size monitor", t);
        }
        try {
            StatusHolder.init();
        } catch (Throwable t) {
            logger.warn("error while initializing status holder", t);
        }
	}
	
	@Override
	public <T> T get(StoreKey key) throws StoreException {
	    checkNotNull(key, "store key is null");
	    StoreClient storeClient = StoreClientFactory.getStoreClientByCategory(key.getCategory());
	    checkNotNull(storeClient, "no store client for category %s", key.getCategory());
		if(enable) {
			return storeClient.get(key);
		} else {
		    return null;
		}
	}

    @Override
	public Boolean set(StoreKey key, Object value) throws StoreException {
        checkNotNull(key, "store key is null");
        checkNotNull(value, "value is null");
        StoreClient storeClient = StoreClientFactory.getStoreClientByCategory(key.getCategory());
        checkNotNull(storeClient, "no store client for category %s", key.getCategory());
        if(enable) {
            return storeClient.set(key, value);
        } else {
            return false;
        }
	}

	@Override
	public Boolean add(StoreKey key, Object value) throws StoreException {
	    checkNotNull(key, "store key is null");
        checkNotNull(value, "value is null");
        StoreClient storeClient = StoreClientFactory.getStoreClientByCategory(key.getCategory());
        checkNotNull(storeClient, "no store client for category %s", key.getCategory());
        if(enable) {
            return storeClient.add(key, value);
        } else {
            return false;
        }
	}

	@Override
	public Boolean delete(StoreKey key) throws StoreException {
	    checkNotNull(key, "store key is null");
        StoreClient storeClient = StoreClientFactory.getStoreClientByCategory(key.getCategory());
        checkNotNull(storeClient, "no store client for category %s", key.getCategory());
        if(enable) {
            return storeClient.delete(key);
        } else {
            return false;
        }
	}

	@Override
	public <T> Future<T> asyncGet(StoreKey key) throws StoreException {
	    checkNotNull(key, "store key is null");
        StoreClient storeClient = StoreClientFactory.getStoreClientByCategory(key.getCategory());
        checkNotNull(storeClient, "no store client for category %s", key.getCategory());
        if(enable) {
            return storeClient.asyncGet(key);
        } else {
            return null;
        }
	}

	@Override
	public Future<Boolean> asyncSet(StoreKey key, Object value) throws StoreException {
	    checkNotNull(key, "store key is null");
        checkNotNull(value, "value is null");
        StoreClient storeClient = StoreClientFactory.getStoreClientByCategory(key.getCategory());
        checkNotNull(storeClient, "no store client for category %s", key.getCategory());
        if(enable) {
            return storeClient.asyncSet(key, value);
        } else {
            return null;
        }
	}

	@Override
	public Future<Boolean> asyncAdd(StoreKey key, Object value) throws StoreException {
	    checkNotNull(key, "store key is null");
        checkNotNull(value, "value is null");
        StoreClient storeClient = StoreClientFactory.getStoreClientByCategory(key.getCategory());
        checkNotNull(storeClient, "no store client for category %s", key.getCategory());
        if(enable) {
            return storeClient.asyncAdd(key, value);
        } else {
            return null;
        }
	}

	@Override
	public Future<Boolean> asyncDelete(StoreKey key) throws StoreException {
	    checkNotNull(key, "store key is null");
        StoreClient storeClient = StoreClientFactory.getStoreClientByCategory(key.getCategory());
        checkNotNull(storeClient, "no store client for category %s", key.getCategory());
        if(enable) {
            return storeClient.asyncDelete(key);
        } else {
            return null;
        }
	}

	@Override
	public <T> Void asyncGet(StoreKey key, StoreCallback<T> callback) {
	    checkNotNull(key, "store key is null");
	    checkNotNull(callback, "callback is null");
        StoreClient storeClient = StoreClientFactory.getStoreClientByCategory(key.getCategory());
        checkNotNull(storeClient, "no store client for category %s", key.getCategory());
        if(enable) {
            return storeClient.asyncGet(key, callback);
        } else {
            return null;
        }
	}

	@Override
	public Void asyncSet(StoreKey key, Object value, StoreCallback<Boolean> callback) {
	    checkNotNull(key, "store key is null");
	    checkNotNull(value, "value is null");
        checkNotNull(callback, "callback is null");
        StoreClient storeClient = StoreClientFactory.getStoreClientByCategory(key.getCategory());
        checkNotNull(storeClient, "no store client for category %s", key.getCategory());
        if(enable) {
            return storeClient.asyncSet(key, value, callback);
        } else {
            return null;
        }
	}

	@Override
	public Void asyncAdd(StoreKey key, Object value, StoreCallback<Boolean> callback) {
	    checkNotNull(key, "store key is null");
        checkNotNull(value, "value is null");
        checkNotNull(callback, "callback is null");
        StoreClient storeClient = StoreClientFactory.getStoreClientByCategory(key.getCategory());
        checkNotNull(storeClient, "no store client for category %s", key.getCategory());
        if(enable) {
            return storeClient.asyncAdd(key, value, callback);
        } else {
            return null;
        }
	}

	@Override
	public Void asyncDelete(StoreKey key, StoreCallback<Boolean> callback) {
	    checkNotNull(key, "store key is null");
        checkNotNull(callback, "callback is null");
        StoreClient storeClient = StoreClientFactory.getStoreClientByCategory(key.getCategory());
        checkNotNull(storeClient, "no store client for category %s", key.getCategory());
        if(enable) {
            return storeClient.asyncDelete(key, callback);
        } else {
            return null;
        }
	}

	@Override
	public Long increase(StoreKey key, int amount) throws StoreException {
	    checkNotNull(key, "store key is null");
        StoreClient storeClient = StoreClientFactory.getStoreClientByCategory(key.getCategory());
        checkNotNull(storeClient, "no store client for category %s", key.getCategory());
        if(enable) {
            return storeClient.increase(key, amount);
        } else {
            return -1L;
        }
	}

	@Override
	public Long decrease(StoreKey key, int amount) throws StoreException {
	    checkNotNull(key, "store key is null");
        StoreClient storeClient = StoreClientFactory.getStoreClientByCategory(key.getCategory());
        checkNotNull(storeClient, "no store client for category %s", key.getCategory());
        if(enable) {
            return storeClient.decrease(key, amount);
        } else {
            return -1L;
        }
	}

	@Override
	public <T> Map<StoreKey, T> multiGet(List<StoreKey> keys)
			throws StoreException {
	    checkNotNull(keys, "store key list is null");
        if(keys.size() == 0) {
            return Collections.emptyMap();
        }
        
        StoreClient storeClient = StoreClientFactory.getStoreClientByCategory(keys.get(0).getCategory());
        checkNotNull(storeClient, "no store client for category %s", keys.get(0).getCategory());
        if(enable) {
            return storeClient.multiGet(keys);
        } else {
            return null;
        }
	}

	@Override
	public <T> Void asyncMultiGet(List<StoreKey> keys,
			StoreCallback<Map<StoreKey, T>> callback) {
	    checkNotNull(keys, "store key list is null");
        checkNotNull(callback, "callback is null");
        if(keys.size() == 0) {
            callback.onSuccess(Collections.<StoreKey, T> emptyMap());
            return null;
        }
        
        String category = keys.get(0).getCategory();
        StoreClient storeClient = StoreClientFactory.getStoreClientByCategory(category);
        checkNotNull(storeClient, "no store client for category %s", category);
        if(enable) {
            return storeClient.asyncMultiGet(keys, callback);
        } else {
            return null;
        }
	}

	@Override
	public <T> Boolean multiSet(List<StoreKey> keys, List<T> values)
			throws StoreException {
	    checkNotNull(keys, "store key list is null");
        checkNotNull(values, "value list is null");
        checkArgument(keys.size() == values.size(), "key size is not equal to value size");
        if(keys.size() == 0) {
            return false;
        }
        
        String category = keys.get(0).getCategory();
        StoreClient storeClient = StoreClientFactory.getStoreClientByCategory(category);
        checkNotNull(storeClient, "no store client for category %s", category);
        if(enable) {
            return storeClient.multiSet(keys, values);
        } else {
            return null;
        }
	}

	@Override
	public <T> Void asyncMultiSet(List<StoreKey> keys, List<T> values,
			StoreCallback<Boolean> callback) {
	    checkNotNull(keys, "store key list is null");
        checkNotNull(values, "value list is null");
        checkNotNull(callback, "callback is null");
        checkArgument(keys.size() == values.size(), "key size is not equal to value size");
        if(keys.size() == 0) {
            callback.onSuccess(true);
            return null;
        }
        
        String category = keys.get(0).getCategory();
        StoreClient storeClient = StoreClientFactory.getStoreClientByCategory(category);
        checkNotNull(storeClient, "no store client for category %s", category);
        if(enable) {
            return storeClient.asyncMultiSet(keys, values, callback);
        } else {
            return null;
        }
	}
	
	@Override
    public <T> T get(String finalKey) throws StoreException {
        checkNotNull(finalKey, "final key is null");
        String category = PathUtils.getCategoryFromKey(finalKey);
        StoreClient storeClient = StoreClientFactory.getStoreClientByCategory(category);
        checkNotNull(storeClient, "no store client for category %s", category);
        if(enable) {
            return storeClient.get(finalKey);
        } else {
            return null;
        }
    }

    @Override
    public Boolean delete(String finalKey) throws StoreException {
        checkNotNull(finalKey, "final key is null");
        String category = PathUtils.getCategoryFromKey(finalKey);
        StoreClient storeClient = StoreClientFactory.getStoreClientByCategory(category);
        checkNotNull(storeClient, "no store client for category %s", category);
        if(enable) {
            return storeClient.delete(finalKey);
        } else {
            return false;
        }
    }
    
    @Override
    public String getFinalKey(StoreKey storeKey) {
        checkNotNull(storeKey, "store key is null");
        StoreCategoryConfig categoryConfig = StoreCategoryConfigManager.getInstance().findCacheKeyType(storeKey.getCategory());
        checkNotNull(categoryConfig, "%s's category config is null", storeKey.getCategory());
        return categoryConfig.getKey(storeKey.getParams());
    }

    @Override
    public boolean isDistributed() {
        return false;
    }

    @Override
    public String getScheme() {
        return "default";
    }

}
