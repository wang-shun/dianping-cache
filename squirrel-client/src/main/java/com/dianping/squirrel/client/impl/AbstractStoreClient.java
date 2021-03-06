package com.dianping.squirrel.client.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.squirrel.client.StoreClient;
import com.dianping.squirrel.client.StoreKey;
import com.dianping.squirrel.client.config.StoreCategoryConfig;
import com.dianping.squirrel.client.config.StoreCategoryConfigManager;
import com.dianping.squirrel.client.config.StoreClientConfigListener;
import com.dianping.squirrel.client.core.StoreCallback;
import com.dianping.squirrel.client.core.StoreTypeAware;
import com.dianping.squirrel.client.log.LoggerLoader;
import com.dianping.squirrel.client.monitor.HitRateMonitor;
import com.dianping.squirrel.client.monitor.KeyCountMonitor;
import com.dianping.squirrel.client.monitor.StatusHolder;
import com.dianping.squirrel.client.util.KeyHolder;
import com.dianping.squirrel.common.exception.StoreException;
import com.dianping.squirrel.common.exception.StoreTimeoutException;
import com.dianping.squirrel.common.lifecycle.Configurable;
import com.dianping.squirrel.common.lifecycle.Lifecycle;
import com.dianping.squirrel.common.util.PathUtils;

public abstract class AbstractStoreClient
		implements StoreClient, StoreTypeAware, Configurable, Lifecycle, StoreClientConfigListener {

	static {
		LoggerLoader.init();
	}

	protected StoreCategoryConfigManager configManager;

	protected String storeType;

	public AbstractStoreClient() {
		configManager = StoreCategoryConfigManager.getInstance();
	}

	@Override
	public void setStoreType(String storeType) {
		this.storeType = storeType;
	}

	@Override
	public String getStoreType() {
		return storeType;
	}

	@Override
	public <T> T get(final String finalKey) throws StoreException {
		checkNotNull(finalKey, "final key is null");
		String category = PathUtils.getCategoryFromKey(finalKey);
		final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(category);
		checkNotNull(categoryConfig, "%s' category config is null", category);

		return executeWithMonitor(new Command() {

			@Override
			public Object execute() throws Exception {
				return doGet(categoryConfig, finalKey);
			}

		}, categoryConfig, finalKey, "get");
	}

	@Override
	public Boolean delete(final String finalKey) throws StoreException {
		checkNotNull(finalKey, "final key is null");
		String category = PathUtils.getCategoryFromKey(finalKey);
		final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(category);
		checkNotNull(categoryConfig, "%s' category config is null", category);

		return executeWithMonitor(new Command() {

			@Override
			public Object execute() throws Exception {
				return doDelete(categoryConfig, finalKey);
			}

		}, categoryConfig, finalKey, "delete");
	}

	@Override
	public <T> T get(StoreKey key) throws StoreException {
		checkNotNull(key, "store key is null");
		final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
		checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
		final String finalKey = categoryConfig.getKey(key.getParams());

		return executeWithMonitor(new Command() {

			@Override
			public Object execute() throws Exception {
				Object result = doGet(categoryConfig, finalKey);
				if (result == null) {
					Cat.getProducer().logEvent("Squirrel." + categoryConfig.getCacheType(),
							categoryConfig.getCategory() + ":missed");
				}
				return result;
			}

		}, categoryConfig, finalKey, "get");
	}

	protected abstract <T> T doGet(StoreCategoryConfig categoryConfig, String finalKey) throws Exception;

	@Override
	public Boolean set(StoreKey key, final Object value) throws StoreException {
		checkNotNull(key, "store key is null");
		checkNotNull(value, "value is null");
		final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
		checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
		final String finalKey = categoryConfig.getKey(key.getParams());

		return executeWithMonitor(new Command() {

			@Override
			public Object execute() throws Exception {
				return doSet(categoryConfig, finalKey, value);
			}

		}, categoryConfig, finalKey, "set");
	}

	protected abstract Boolean doSet(StoreCategoryConfig categoryConfig, String finalKey, Object value)
			throws Exception;

	@Override
	public Boolean add(StoreKey key, final Object value) throws StoreException {
		checkNotNull(key, "store key is null");
		checkNotNull(value, "value is null");
		final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
		checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
		final String finalKey = categoryConfig.getKey(key.getParams());

		return executeWithMonitor(new Command() {

			@Override
			public Object execute() throws Exception {
				return doAdd(categoryConfig, finalKey, value);
			}

		}, categoryConfig, finalKey, "add");
	}

	protected abstract Boolean doAdd(StoreCategoryConfig categoryConfig, String finalKey, Object value)
			throws Exception;

	@Override
	public Boolean delete(StoreKey key) throws StoreException {
		checkNotNull(key, "store key is null");
		final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
		checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
		final String finalKey = categoryConfig.getKey(key.getParams());

		return executeWithMonitor(new Command() {

			@Override
			public Object execute() throws Exception {
				return doDelete(categoryConfig, finalKey);
			}

		}, categoryConfig, finalKey, "delete");
	}

	protected abstract Boolean doDelete(StoreCategoryConfig categoryConfig, String finalKey) throws Exception;

	@Override
	public <T> Future<T> asyncGet(StoreKey key) throws StoreException {
		checkNotNull(key, "store key is null");
		final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
		checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
		final String finalKey = categoryConfig.getKey(key.getParams());

		return executeWithMonitor(new Command() {

			@Override
			public Object execute() throws Exception {
				return doAsyncGet(categoryConfig, finalKey);
			}

		}, categoryConfig, finalKey, "asyncGet");
	}

	protected abstract <T> Future<T> doAsyncGet(StoreCategoryConfig categoryConfig, String finalKey) throws Exception;

	@Override
	public Future<Boolean> asyncSet(StoreKey key, final Object value) throws StoreException {
		checkNotNull(key, "store key is null");
		checkNotNull(value, "value is null");
		final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
		checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
		final String finalKey = categoryConfig.getKey(key.getParams());

		return executeWithMonitor(new Command() {

			@Override
			public Object execute() throws Exception {
				return doAsyncSet(categoryConfig, finalKey, value);
			}

		}, categoryConfig, finalKey, "asyncSet");
	}

	protected abstract Future<Boolean> doAsyncSet(StoreCategoryConfig categoryConfig, String finalKey, Object value)
			throws Exception;

	@Override
	public Future<Boolean> asyncAdd(StoreKey key, final Object value) throws StoreException {
		checkNotNull(key, "store key is null");
		checkNotNull(value, "value is null");
		final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
		checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
		final String finalKey = categoryConfig.getKey(key.getParams());

		return executeWithMonitor(new Command() {

			@Override
			public Object execute() throws Exception {
				return doAsyncAdd(categoryConfig, finalKey, value);
			}

		}, categoryConfig, finalKey, "asyncAdd");
	}

	protected abstract Future<Boolean> doAsyncAdd(StoreCategoryConfig categoryConfig, String finalKey, Object value)
			throws Exception;

	@Override
	public Future<Boolean> asyncDelete(StoreKey key) throws StoreException {
		checkNotNull(key, "store key is null");
		final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
		checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
		final String finalKey = categoryConfig.getKey(key.getParams());

		return executeWithMonitor(new Command() {

			@Override
			public Object execute() throws Exception {
				return doAsyncDelete(categoryConfig, finalKey);
			}

		}, categoryConfig, finalKey, "asyncDelete");
	}

	protected abstract Future<Boolean> doAsyncDelete(StoreCategoryConfig categoryConfig, String finalKey)
			throws Exception;

	@Override
	public <T> Void asyncGet(StoreKey key, final StoreCallback<T> callback) {
		checkNotNull(key, "store key is null");
		checkNotNull(callback, "callback is null");
		final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
		checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
		final String finalKey = categoryConfig.getKey(key.getParams());

		return executeWithMonitor(new Command() {

			@Override
			public Object execute() throws Exception {
				return doAsyncGet(categoryConfig, finalKey, callback);
			}

		}, categoryConfig, finalKey, "asyncGet");
	}

	protected abstract <T> Void doAsyncGet(StoreCategoryConfig categoryConfig, String finalKey,
			StoreCallback<T> callback) throws Exception;

	@Override
	public Void asyncSet(StoreKey key, final Object value, final StoreCallback<Boolean> callback) {
		checkNotNull(key, "store key is null");
		checkNotNull(value, "value is null");
		checkNotNull(callback, "callback is null");
		final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
		checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
		final String finalKey = categoryConfig.getKey(key.getParams());

		return executeWithMonitor(new Command() {

			@Override
			public Object execute() throws Exception {
				return doAsyncSet(categoryConfig, finalKey, value, callback);
			}

		}, categoryConfig, finalKey, "asyncSet");
	}

	protected abstract Void doAsyncSet(StoreCategoryConfig categoryConfig, String finalKey, Object value,
			StoreCallback<Boolean> callback) throws Exception;

	@Override
	public Void asyncAdd(StoreKey key, final Object value, final StoreCallback<Boolean> callback) {
		checkNotNull(key, "store key is null");
		checkNotNull(value, "value is null");
		checkNotNull(callback, "callback is null");
		final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
		checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
		final String finalKey = categoryConfig.getKey(key.getParams());

		return executeWithMonitor(new Command() {

			@Override
			public Object execute() throws Exception {
				return doAsyncAdd(categoryConfig, finalKey, value, callback);
			}

		}, categoryConfig, finalKey, "asyncAdd");
	}

	protected abstract Void doAsyncAdd(StoreCategoryConfig categoryConfig, String finalKey, Object value,
			StoreCallback<Boolean> callback) throws Exception;

	@Override
	public Void asyncDelete(StoreKey key, final StoreCallback<Boolean> callback) {
		checkNotNull(key, "store key is null");
		checkNotNull(callback, "callback is null");
		final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
		checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
		final String finalKey = categoryConfig.getKey(key.getParams());

		return executeWithMonitor(new Command() {

			@Override
			public Object execute() throws Exception {
				return doAsyncDelete(categoryConfig, finalKey, callback);
			}

		}, categoryConfig, finalKey, "asyncDelete");
	}

	protected abstract Void doAsyncDelete(StoreCategoryConfig categoryConfig, String finalKey,
			StoreCallback<Boolean> callback) throws Exception;

	@Override
	public Long increase(StoreKey key, final int amount) throws StoreException {
		checkNotNull(key, "store key is null");
		final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
		checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
		final String finalKey = categoryConfig.getKey(key.getParams());

		return executeWithMonitor(new Command() {

			@Override
			public Object execute() throws Exception {
				return doIncrease(categoryConfig, finalKey, amount);
			}

		}, categoryConfig, finalKey, "increase");
	}

	protected abstract Long doIncrease(StoreCategoryConfig categoryConfig, String finalKey, int amount)
			throws Exception;

	@Override
	public Long decrease(StoreKey key, final int amount) throws StoreException {
		checkNotNull(key, "store key is null");
		final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(key.getCategory());
		checkNotNull(categoryConfig, "%s's category config is null", key.getCategory());
		final String finalKey = categoryConfig.getKey(key.getParams());

		return executeWithMonitor(new Command() {

			@Override
			public Object execute() throws Exception {
				return doDecrease(categoryConfig, finalKey, amount);
			}

		}, categoryConfig, finalKey, "decrease");
	}

	protected abstract Long doDecrease(StoreCategoryConfig categoryConfig, String finalKey, int amount)
			throws Exception;

	@Override
	public <T> Map<StoreKey, T> multiGet(List<StoreKey> keys) throws StoreException {
		checkNotNull(keys, "store key list is null");
		if (keys.size() == 0) {
			return Collections.emptyMap();
		}

		final String category = keys.get(0).getCategory();
		final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(category);
		checkNotNull(categoryConfig, "%s's category config is null", category);

		final KeyHolder keyHolder = new KeyHolder(categoryConfig, keys);
		final List<String> finalKeyList = keyHolder.getFinalKeys();

		return executeWithMonitor(new Command() {

			@Override
			public Object execute() throws Exception {
				Map<String, T> innerResult = doMultiGet(categoryConfig, finalKeyList);
				Map<StoreKey, T> result = keyHolder.convertKeys(innerResult);
				int hits = result.size();
				HitRateMonitor.getInstance().logHitRate(categoryConfig.getCacheType(), category, "multiGet",
						(int) (100.0F * (hits / finalKeyList.size())), hits);
				return result;
			}

		}, categoryConfig, finalKeyList, "multiGet");
	}

	protected abstract <T> Map<String, T> doMultiGet(StoreCategoryConfig categoryConfig, List<String> finalKeyList)
			throws Exception;

	@Override
	public <T> Void asyncMultiGet(List<StoreKey> keys, final StoreCallback<Map<StoreKey, T>> callback) {
		checkNotNull(keys, "store key list is null");
		checkNotNull(callback, "callback is null");
		if (keys.size() == 0) {
			callback.onSuccess(Collections.<StoreKey, T> emptyMap());
			return null;
		}

		final String category = keys.get(0).getCategory();
		final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(category);
		checkNotNull(categoryConfig, "%s's category config is null", category);

		final KeyHolder keyHolder = new KeyHolder(categoryConfig, keys);
		final List<String> finalKeyList = keyHolder.getFinalKeys();

		return executeWithMonitor(new Command() {

			@Override
			public Object execute() throws Exception {
				StoreCallback<Map<String, T>> innerCallback = new StoreCallback<Map<String, T>>() {

					@Override
					public void onSuccess(Map<String, T> innerResult) {
						Map<StoreKey, T> result = keyHolder.convertKeys(innerResult);
						int hits = result.size();
						HitRateMonitor.getInstance().logHitRate(categoryConfig.getCacheType(), category,
								"asyncMultiGet", (int) (100.0F * (hits / finalKeyList.size())), hits);
						callback.onSuccess(result);
					}

					@Override
					public void onFailure(Throwable e) {
						callback.onFailure(e);
					}

				};
				return doAsyncMultiGet(categoryConfig, finalKeyList, innerCallback);
			}

		}, categoryConfig, finalKeyList, "asyncMultiGet");
	}

	protected abstract <T> Void doAsyncMultiGet(StoreCategoryConfig categoryConfig, List<String> finalKeyList,
			StoreCallback<Map<String, T>> callback) throws Exception;

	@Override
	public <T> Boolean multiSet(List<StoreKey> keys, final List<T> values) throws StoreException {
		checkNotNull(keys, "store key list is null");
		checkNotNull(values, "value list is null");
		checkArgument(keys.size() == values.size(), "key size is not equal to value size");
		if (keys.size() == 0) {
			return false;
		}

		String category = keys.get(0).getCategory();
		final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(category);
		checkNotNull(categoryConfig, "%s's category config is null", category);

		final List<String> finalKeyList = new ArrayList<String>(keys.size());
		for (int i = 0; i < keys.size(); i++) {
			StoreKey sk = keys.get(i);
			checkNotNull(sk, "some store key is null");
			T value = values.get(i);
			checkNotNull(value, "some value is null");
			String fk = categoryConfig.getKey(sk.getParams());
			finalKeyList.add(fk);
		}

		return executeWithMonitor(new Command() {

			@Override
			public Object execute() throws Exception {
				return doMultiSet(categoryConfig, finalKeyList, values);
			}

		}, categoryConfig, finalKeyList, "multiSet");
	}

	protected abstract <T> Boolean doMultiSet(StoreCategoryConfig categoryConfig, List<String> finalKeyList,
			List<T> values) throws Exception;

	@Override
	public <T> Void asyncMultiSet(List<StoreKey> keys, final List<T> values, final StoreCallback<Boolean> callback) {
		checkNotNull(keys, "store key list is null");
		checkNotNull(values, "value list is null");
		checkNotNull(callback, "callback is null");
		checkArgument(keys.size() == values.size(), "key size is not equal to value size");
		if (keys.size() == 0) {
			callback.onSuccess(true);
			return null;
		}

		String category = keys.get(0).getCategory();
		final StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(category);
		checkNotNull(categoryConfig, "%s's category config is null", category);

		final List<String> finalKeyList = new ArrayList<String>(keys.size());
		for (int i = 0; i < keys.size(); i++) {
			StoreKey sk = keys.get(i);
			checkNotNull(sk, "some store key is null");
			T value = values.get(i);
			checkNotNull(value, "some value is null");
			String fk = categoryConfig.getKey(sk.getParams());
			finalKeyList.add(fk);
		}

		return executeWithMonitor(new Command() {
			@Override
			public Object execute() throws Exception {
				return doAsyncMultiSet(categoryConfig, finalKeyList, values, callback);
			}

		}, categoryConfig, finalKeyList, "asyncMultiSet");
	}

	protected abstract <T> Void doAsyncMultiSet(StoreCategoryConfig categoryConfig, List<String> keys, List<T> values,
			StoreCallback<Boolean> callback) throws Exception;

	@Override
	public String getFinalKey(StoreKey storeKey) {
		checkNotNull(storeKey, "store key is null");
		StoreCategoryConfig categoryConfig = configManager.findCacheKeyType(storeKey.getCategory());
		checkNotNull(categoryConfig, "%s's category config is null", storeKey.getCategory());
		return categoryConfig.getKey(storeKey.getParams());
	}

	@Override
	public boolean isDistributed() {
		return true;
	}

	@SuppressWarnings("unchecked")
	protected <T> T executeWithMonitor(Command command, StoreCategoryConfig categoryConfig, String finalKey,
			String action) {
		String storeType = categoryConfig.getCacheType();
		String category = categoryConfig.getCategory();

		Transaction t = null;
		if (needMonitor(storeType)) {
			t = Cat.getProducer().newTransaction("Squirrel." + storeType, category + ":" + action);
			t.addData("finalKey", finalKey);
			t.setStatus(Message.SUCCESS);
		}
		StatusHolder.flowIn(storeType, category, action);
		long begin = System.nanoTime();
		int second = (int) (begin / 1000000000 % 60) + 1;
		try {
			Cat.getProducer().logEvent("Squirrel." + storeType + ".qps", "S" + second);
			return (T) command.execute();
		} catch (TimeoutException e) {
			Cat.getProducer().logEvent("Squirrel." + storeType, category + ":timeout");
			StoreTimeoutException ste = new StoreTimeoutException(e);
			if (t != null) {
				t.setStatus(ste);
			}
			throw ste;
		} catch (StoreException e) {
			Cat.getProducer().logError(e);
			if (t != null) {
				t.setStatus(e);
			}
			throw e;
		} catch (Throwable e) {
			StoreException se = new StoreException(e);
			Cat.getProducer().logError(se);
			if (t != null) {
				t.setStatus(se);
			}
			throw se;
		} finally {
			StatusHolder.flowOut(storeType, category, action);
			if (t != null) {
				t.complete();
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected <T> T executeWithMonitor(Command command, StoreCategoryConfig categoryConfig, List<String> keys,
			String action) {
		String storeType = categoryConfig.getCacheType();
		String category = categoryConfig.getCategory();

		Transaction t = null;
		if (needMonitor(storeType)) {
			t = Cat.getProducer().newTransaction("Squirrel." + storeType, category + ":" + action);
			KeyCountMonitor.getInstance().logKeyCount(storeType, category, action, keys.size());
			t.setStatus(Message.SUCCESS);
		}
		StatusHolder.flowIn(storeType, category, action);
		long begin = System.nanoTime();
		int second = (int) (begin / 1000000000 % 60) + 1;
		try {
			Cat.getProducer().logEvent("Squirrel." + storeType + ".qps", "S" + second);
			return (T) command.execute();
		} catch (TimeoutException e) {
			Cat.getProducer().logEvent("Squirrel." + storeType, category + ":timeout", Message.SUCCESS, "");
			StoreTimeoutException ste = new StoreTimeoutException(e);
			if (t != null) {
				t.setStatus(ste);
			}
			throw ste;
		} catch (StoreException e) {
			Cat.getProducer().logError(e);
			if (t != null) {
				t.setStatus(e);
			}
			throw e;
		} catch (Throwable e) {
			StoreException se = new StoreException(e);
			Cat.getProducer().logError(se);
			if (t != null) {
				t.setStatus(se);
			}
			throw se;
		} finally {
			StatusHolder.flowOut(storeType, category, action);
			if (t != null) {
				t.complete();
			}
		}
	}

	protected boolean needMonitor(String cacheType) {
		return true;
	}

	public static interface Command {
		Object execute() throws Exception;
	}
}
