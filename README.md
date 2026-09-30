# Resilient Cache Engine

**A caching library that keeps working when Redis doesn't.**

I built this after asking a simple question: what actually happens to a Spring Boot app when its cache server dies? With most setups, the answer is "everything falls over at once." I wanted a cache that notices the failure, quietly covers for it, and heals itself when Redis comes back.

**Try it live:** [Dashboard](https://resilient-cache-engine.onrender.com) · [Sample API call](https://resilient-cache-engine.onrender.com/products/B07JW9H4J1)
*(The demo runs on a free Render instance, so the first load may take about 30 seconds to wake up.)*

<!-- Add a dashboard screenshot or short GIF here: docs/dashboard.png -->

---

## The problem

When thousands of users hit a site at once, every request that misses the cache lands on the database, like a traffic jam building up on a single road. Add a Redis outage on top and the jam turns into a pile-up.

## How it works

Every read goes through two layers:

| Layer | What it is | Why it's there |
|---|---|---|
| **L1: Caffeine** | In-memory cache inside each app instance | Fastest possible reads, and it keeps working if Redis is gone |
| **L2: Redis** | Shared cache across all instances | Consistency between instances |

The interesting part is what happens when things break:

1. **Redis goes down.** The engine detects it, marks Redis as `DOWN`, and serves everything from L1. Users notice nothing.
2. **Redis comes back.** The engine reconnects and resyncs on its own. No restart, no redeploy.
3. **Data changes on one instance.** A Redis pub/sub message tells the other instances to drop their stale copy, so nobody serves outdated data.

## Numbers

I load-tested it under concurrent traffic:

- **99%+** cache hit rate
- Average API latency dropped from **847 ms to 180 ms**

## Use it in your project

You only need to add one annotation:

```java
@ResilientCache
public Product getProduct(String id) {
    return productRepository.findById(id);
}
```

No hand-written cache logic and no manual invalidation code.

### 1. Add JitPack to your `pom.xml`

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

### 2. Add the dependency

```xml
<dependency>
    <groupId>com.github.PrajyotKorde-18</groupId>
    <artifactId>Resilient-Cache-Engine</artifactId>
    <version>main-SNAPSHOT</version>
</dependency>
```

### 3. Configure it in `application.yml`

```yaml
resilient-cache:
  enabled: true
  provider: multi-level        # caffeine | redis | multi-level
  default-ttl: 30m             # how long entries live
  stampede-protection: true    # one DB query on a miss, not hundreds
  caffeine:
    maximum-size: 10000
    expire-after-write: 15m
```

## Run the demo locally

You'll need Java 21+, Maven 3.6+ and Docker.

```bash
# 1. Start Redis
docker run -d --name resilient-redis -p 6379:6379 redis:alpine

# 2. Build
./mvnw clean install -DskipTests

# 3. Run the demo app
java -jar resilient-cache-demo/target/resilient-cache-demo-1.0.0.jar
```

Then open http://localhost:8080/cache-dashboard.html.

**To see the self-healing yourself:** with the app running, stop Redis (`docker stop resilient-redis`), refresh the dashboard, and watch it switch to L1 only. Start it again (`docker start resilient-redis`) and it reconnects.

## What's inside

- **Language and framework:** Java 21, Spring Boot, Spring AOP
- **Caching:** Caffeine (L1), Redis (L2), pub/sub for invalidation
- **Persistence:** Supabase (migrated from local MySQL)
- **Testing:** JUnit tests for cache hits and misses, failover and recovery, plus Postman for the REST endpoints
- **Delivery:** Docker, published through JitPack, demo hosted on Render

## What I learned

- Failure handling is the real work. The happy path took a day, and failover and recovery took the rest.
- Distributed caches go stale in quiet ways, which is why pub/sub invalidation ended up being essential.
- Moving from local MySQL to Supabase taught me a lot about connection and data-type mismatches.

## Roadmap

- [ ] GitHub Actions CI
- [ ] More test coverage for edge cases
- [ ] Publish a stable release instead of `main-SNAPSHOT`

## Author

**Prajyot Korde**, IT undergrad at Ramdeobaba University
[LinkedIn](https://www.linkedin.com/in/prajyot-korde-912621281) · [GitHub](https://github.com/PrajyotKorde-18)
