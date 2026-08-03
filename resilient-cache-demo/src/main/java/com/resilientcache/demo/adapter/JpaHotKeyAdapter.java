package com.resilientcache.demo.adapter;

import com.resilientcache.demo.model.HotKeyEntity;
import com.resilientcache.demo.repository.HotKeyJpaRepository;
import com.resilientcache.port.HotKeyPort;
import com.resilientcache.warmup.HotKeyRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class JpaHotKeyAdapter implements HotKeyPort {

    private final HotKeyJpaRepository repository;

    public JpaHotKeyAdapter(HotKeyJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void saveTopHotKeys(List<HotKeyRecord> records) {
        // Truncate previous top list and write the fresh statistics
        repository.deleteAllInBatch();
        List<HotKeyEntity> entities = records.stream()
                .map(r -> new HotKeyEntity(r.getKey(), r.getScore(), r.getLastAccessTime()))
                .collect(Collectors.toList());
        repository.saveAll(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotKeyRecord> getTopHotKeys(int limit) {
        return repository.findTopKeysOrdered().stream()
                .limit(limit)
                .map(e -> new HotKeyRecord(e.getCacheKey(), e.getScore(), e.getLastAccessTime()))
                .collect(Collectors.toList());
    }
}
