package com.copywriting.assistant.cache;

import com.copywriting.assistant.model.dto.CopywritingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class CopywritingCache {

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    @Value("${cache.copywriting.expire-hours:24}")
    private int expireHours;

    public Optional<CopywritingResponse> get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry != null && !entry.isExpired()) {
            entry.response.setFromCache(true);
            return Optional.of(entry.response);
        }
        if (entry != null) {
            cache.remove(key);
        }
        return Optional.empty();
    }

    public void put(String key, CopywritingResponse response) {
        cache.put(key, new CacheEntry(response, System.currentTimeMillis() + expireHours * 3600 * 1000L));
        log.debug("写入缓存成功, key: {}", key);
    }

    public void evict(String key) {
        cache.remove(key);
    }

    private static class CacheEntry {
        final CopywritingResponse response;
        final long expireAt;

        CacheEntry(CopywritingResponse response, long expireAt) {
            this.response = response;
            this.expireAt = expireAt;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireAt;
        }
    }
}
