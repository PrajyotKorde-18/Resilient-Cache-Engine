# MySQL Customers Caching Benchmark Report

This report documents the performance of the **Resilient Cache Engine** querying your local MySQL `customers` table under concurrent load.

---

## Load Test Profile
* **Total Requests:** 50000
* **Concurrent Threads:** 40 parallel HTTP workers
* **Table size:** 175 customer rows
* **Total Time Taken:** 184.40 seconds
* **Overall Throughput:** 271.14 requests/second

---

## Statistical Performance Metrics

| Metric | Latency (ms) | Description |
| :--- | :--- | :--- |
| **Minimum Latency** | 4.64 ms | Local L1 Cache Hit |
| **Maximum Latency** | 7119.57 ms | Database query cold miss (hitting local MySQL) |
| **Average Latency** | 130.01 ms | Combined arithmetic average |
| **P50 (Median)** | 66.26 ms | 50% of requests resolved faster than this |
| **P95 Latency** | 266.11 ms | 95% of requests resolved faster than this |
| **P99 Latency** | 1771.88 ms | 99% of requests resolved faster than this |

---

## Caching Telemetry
* **Cache Hits:** 39105
* **Cache Misses (DB lookups):** 10629
* **Combined Cache Hit Ratio:** 78.6%

---

## Database Protection Analysis
1. **Database Shielding:** Out of 50000 requests, only 10629 requests hit your local MySQL database. Caching **prevented 39105 database queries**, reducing database workload by **78.2%**.
