package com.resilientcache.provider.multi;

import com.resilientcache.provider.caffeine.CaffeineAdapter;
import com.resilientcache.provider.redis.RedisAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MultiLevelCacheAdapterTest {

    private CaffeineAdapter caffeineAdapter;
    private RedisAdapter redisAdapter;
    private RedisTemplate<String, Object> redisTemplate;
    private MultiLevelCacheAdapter adapter;

    @BeforeEach
    void setUp() {
        caffeineAdapter = mock(CaffeineAdapter.class);
        redisAdapter = mock(RedisAdapter.class);
        redisTemplate = mock(RedisTemplate.class);
        adapter = new MultiLevelCacheAdapter(caffeineAdapter, redisAdapter, redisTemplate, "test-instance");
    }

    @Test
    void testGetL1Hit() {
        String key = "testKey";
        String value = "l1Value";
        when(caffeineAdapter.get(key)).thenReturn(Optional.of(value));

        Optional<Object> result = adapter.get(key);

        assertTrue(result.isPresent());
        assertEquals(value, result.get());
        verify(caffeineAdapter, times(1)).get(key);
        verifyNoInteractions(redisAdapter);
        verifyNoInteractions(redisTemplate);
    }

    @Test
    void testGetL1MissL2HitWithTtl() {
        String key = "testKey";
        String value = "l2Value";
        when(caffeineAdapter.get(key)).thenReturn(Optional.empty());
        when(redisAdapter.get(key)).thenReturn(Optional.of(value));
        when(redisTemplate.getExpire(key, TimeUnit.SECONDS)).thenReturn(120L);

        Optional<Object> result = adapter.get(key);

        assertTrue(result.isPresent());
        assertEquals(value, result.get());
        verify(caffeineAdapter, times(1)).get(key);
        verify(redisAdapter, times(1)).get(key);
        verify(redisTemplate, times(1)).getExpire(eq(expireKey(key)), eq(TimeUnit.SECONDS));
        verify(caffeineAdapter, times(1)).put(key, value, Duration.ofSeconds(120));


    }

    @Test
    void testGetL1MissL2HitWithNoTtl() {
        String key = "testKey";
        String value = "l2Value";
        when(caffeineAdapter.get(key)).thenReturn(Optional.empty());
        when(redisAdapter.get(key)).thenReturn(Optional.of(value));
        when(redisTemplate.getExpire(key, TimeUnit.SECONDS)).thenReturn(-1L); // -1 means no TTL in Redis

        Optional<Object> result = adapter.get(key);

        assertTrue(result.isPresent());
        assertEquals(value, result.get());
        verify(caffeineAdapter, times(1)).get(key);
        verify(redisAdapter, times(1)).get(key);
        verify(caffeineAdapter, times(1)).put(key, value, null); // should fall back to default L1 TTL
    }

    @Test
    void testGetL1MissL2Miss() {
        String key = "testKey";
        when(caffeineAdapter.get(key)).thenReturn(Optional.empty());
        when(redisAdapter.get(key)).thenReturn(Optional.empty());

        Optional<Object> result = adapter.get(key);

        assertFalse(result.isPresent());
        verify(caffeineAdapter, times(1)).get(key);
        verify(redisAdapter, times(1)).get(key);
    }

    @Test
    void testPutWritesThroughBoth() {
        String key = "testKey";
        String value = "newValue";
        Duration ttl = Duration.ofMinutes(10);

        adapter.put(key, value, ttl);

        verify(caffeineAdapter, times(1)).put(key, value, ttl);
        verify(redisAdapter, times(1)).put(key, value, ttl);
    }

    @Test
    void testEvictDeletesFromBoth() {
        String key = "testKey";

        adapter.evict(key);

        verify(caffeineAdapter, times(1)).evict(key);
        verify(redisAdapter, times(1)).evict(key);
    }

    @Test
    void testClearClearsBoth() {
        adapter.clear();

        verify(caffeineAdapter, times(1)).clear();
        verify(redisAdapter, times(1)).clear();
    }

    private String expireKey(String key) {
        return key;
    }
}
