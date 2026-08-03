package com.resilientcache.port;

import com.resilientcache.warmup.HotKeyRecord;
import java.util.List;

public interface HotKeyPort {

    /**
     * Persists the current top-scored hot keys into permanent storage.
     */
    void saveTopHotKeys(List<HotKeyRecord> records);

    /**
     * Retrieves the top hot keys sorted by popularity score descending.
     */
    List<HotKeyRecord> getTopHotKeys(int limit);
}
