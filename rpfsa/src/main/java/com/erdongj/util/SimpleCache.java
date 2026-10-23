package com.erdongj.util;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 极简进程内 TTL 缓存。
 * <p>
 * 用于首页/榜单这类读多写少、对实时性要求不高的接口（默认 60 秒过期），
 * 不引入 Redis 等外部依赖，单机部署够用。
 */
@Component
public class SimpleCache {

    private static final long DEFAULT_TTL_MILLIS = 60_000L;

    private static class Entry {
        final Object value;
        final long expireAt;

        Entry(Object value, long ttlMillis) {
            this.value = value;
            this.expireAt = System.currentTimeMillis() + ttlMillis;
        }

        boolean expired() {
            return System.currentTimeMillis() > expireAt;
        }
    }

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    /** 读取缓存；未命中或已过期则调用 loader 并回填 */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Supplier<T> loader) {
        return get(key, DEFAULT_TTL_MILLIS, loader);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, long ttlMillis, Supplier<T> loader) {
        Entry entry = store.get(key);
        if (entry != null && !entry.expired()) {
            return (T) entry.value;
        }
        T value = loader.get();
        if (value != null) {
            store.put(key, new Entry(value, ttlMillis));
        }
        return value;
    }

    /** 主动失效某个 key（数据变更后调用） */
    public void evict(String key) {
        store.remove(key);
    }
}
