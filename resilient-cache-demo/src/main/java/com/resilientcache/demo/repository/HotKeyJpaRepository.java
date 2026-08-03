package com.resilientcache.demo.repository;

import com.resilientcache.demo.model.HotKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotKeyJpaRepository extends JpaRepository<HotKeyEntity, String> {

    /**
     * Finds the top hot keys sorted by popularity score descending.
     */
    @Query(value = "SELECT h FROM HotKeyEntity h ORDER BY h.score DESC")
    List<HotKeyEntity> findTopKeysOrdered();
}
