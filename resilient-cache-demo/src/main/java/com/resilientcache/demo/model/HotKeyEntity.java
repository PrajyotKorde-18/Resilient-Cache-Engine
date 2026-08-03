package com.resilientcache.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cache_hot_keys")
public class HotKeyEntity {

    @Id
    @Column(name = "cache_key")
    private String cacheKey;

    @Column(name = "score")
    private double score;

    @Column(name = "last_access_time")
    private long lastAccessTime;

    public HotKeyEntity() {
    }

    public HotKeyEntity(String cacheKey, double score, long lastAccessTime) {
        this.cacheKey = cacheKey;
        this.score = score;
        this.lastAccessTime = lastAccessTime;
    }

    public String getCacheKey() {
        return cacheKey;
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }
}
