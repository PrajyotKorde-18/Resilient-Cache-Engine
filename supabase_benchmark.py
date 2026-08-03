import urllib.request
import json
import time
import random
import os
from concurrent.futures import ThreadPoolExecutor, as_completed

total_requests = 50000
concurrency_level = 40
valid_indices = list(range(2, 176))      # Seeded indices in Supabase (2 to 175, since 1 is column header)
invalid_indices = list(range(176, 186))  # Out-of-bounds indices for cold misses

# Construct random selection pool (heavy hits on valid IDs, occasional cold misses)
id_pool = [str(x) for x in valid_indices * 5 + invalid_indices]

latencies = []
hits_count = 0
misses_count = 0

print("====================================================")
print("     SUPABASE POSTGRESQL CONCURRENT LOAD BENCHMARK   ")
print("====================================================")
print(f"Total Requests: {total_requests}")
print(f"Concurrency level: {concurrency_level} threads")
print(f"Index Pool: 2 to 185 (Simulating existing & non-existing customers)")
print("Sending concurrent traffic... Please wait.")

def send_request(request_num):
    customer_index = random.choice(id_pool)
    url = f"http://localhost:8080/customers/{customer_index}"
    start = time.perf_counter()
    try:
        req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
        with urllib.request.urlopen(req) as response:
            data = json.loads(response.read().decode())
            latency = (time.perf_counter() - start) * 1000.0  # ms
            is_cached = data.get("cached", False)
            return {
                "success": True, 
                "latency": latency, 
                "cached": is_cached, 
                "index": customer_index
            }
    except Exception as e:
        return {"success": False, "error": str(e), "index": customer_index}

# Execute concurrently
start_time = time.perf_counter()
with ThreadPoolExecutor(max_workers=concurrency_level) as executor:
    futures = [executor.submit(send_request, i) for i in range(total_requests)]
    
    completed = 0
    for future in as_completed(futures):
        res = future.result()
        completed += 1
        if res.get("success"):
            latencies.append(res["latency"])
            if res["cached"]:
                hits_count += 1
            else:
                misses_count += 1
        
        # Print progress bar
        if completed % 50 == 0 or completed == total_requests:
            hit_ratio = (hits_count / max(hits_count + misses_count, 1)) * 100
            print(f"  Progress: {completed}/{total_requests} requests | Hits: {hits_count} | Misses: {misses_count} | Cache Hit Ratio: {hit_ratio:.1f}%")

total_duration = time.perf_counter() - start_time

if not latencies:
    print("[ERROR] No successful requests could be completed. Ensure your demo app is running and connected to Supabase.")
    exit(1)

# Stats calculation
latencies.sort()
min_lat = latencies[0]
max_lat = latencies[-1]
avg_lat = sum(latencies) / len(latencies)
p50 = latencies[int(len(latencies) * 0.50)]
p95 = latencies[int(len(latencies) * 0.95)]
p99 = latencies[int(len(latencies) * 0.99)]

throughput = total_requests / total_duration

report = f"""# Supabase PostgreSQL Customers Caching Benchmark Report

This report documents the performance of the **Resilient Cache Engine** querying your remote **Supabase PostgreSQL** `customers` table under concurrent load.

---

## Load Test Profile
* **Total Requests:** {total_requests}
* **Concurrent Threads:** {concurrency_level} parallel HTTP workers
* **Table size:** 174 customer rows
* **Total Time Taken:** {total_duration:.2f} seconds
* **Overall Throughput:** {throughput:.2f} requests/second

---

## Statistical Performance Metrics

| Metric | Latency (ms) | Description |
| :--- | :--- | :--- |
| **Minimum Latency** | {min_lat:.2f} ms | Local L1 Cache Hit |
| **Maximum Latency** | {max_lat:.2f} ms | Database query cold miss (hitting remote Supabase DB) |
| **Average Latency** | {avg_lat:.2f} ms | Combined arithmetic average |
| **P50 (Median)** | {p50:.2f} ms | 50% of requests resolved faster than this |
| **P95 Latency** | {p95:.2f} ms | 95% of requests resolved faster than this |
| **P99 Latency** | {p99:.2f} ms | 99% of requests resolved faster than this |

---

## Caching Telemetry
* **Cache Hits:** {hits_count}
* **Cache Misses (DB lookups):** {misses_count}
* **Combined Cache Hit Ratio:** {(hits_count / (hits_count + misses_count)) * 100:.1f}%

---

## Database Protection Analysis
1. **Database Shielding:** Out of {total_requests} requests, only {misses_count} requests hit your Supabase database. Caching **prevented {hits_count} database queries**, reducing database workload by **{(hits_count / total_requests) * 100:.1f}%**.
"""

output_path = r"c:\Users\hp\OneDrive\Desktop\Resiliant Cache\supabase_benchmark_results.md"
with open(output_path, 'w', encoding='utf-8') as f:
    f.write(report)

print(f"\n>>> Supabase PostgreSQL Customers benchmark complete!")
print(f">>> Latency report written to: {output_path}")
print("====================================================")
