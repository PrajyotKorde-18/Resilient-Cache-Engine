# Resilient Cache Engine 🚀

A highly resilient, multi-level caching system built for Spring Boot applications to shield database performance and guarantee high availability.

🔗 **Live Demo URL:** [https://resilient-cache-engine.onrender.com](https://resilient-cache-engine.onrender.com)

---

## 📖 What is this? (In Simple Terms)

When thousands of users visit a website at the same time, the database gets overloaded and can crash (similar to a traffic jam). 

This project is a **custom library (Spring Boot Starter)** that sits between your website and the database. It saves data in two fast-access layers:
1. **L1 Cache (Caffeine):** A super-fast memory cache located directly inside the application's RAM.
2. **L2 Cache (Redis):** A shared, persistent cache container.

### 🛡️ Why is it "Resilient"?
If your Redis database goes offline, **most applications crash.** 
With this engine, the system automatically detects the crash, marks Redis as **`DOWN`**, and keeps serving your users from the L1 (RAM) cache. Once Redis is turned back on, the engine automatically reconnects without you having to restart the website!

---

## 🛠️ How to Add This Dependency to Your Project

You can easily import this custom starter library into your own Spring Boot project using **JitPack** directly from this GitHub repository.

### Step 1: Add the JitPack Repository to your `pom.xml`
Open your project's `pom.xml` file and add the following repository configuration:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

### Step 2: Add the Dependency
Add the starter dependency under the `<dependencies>` tag:

```xml
<dependency>
    <groupId>com.github.PrajyotKorde-18</groupId>
    <artifactId>Resilient-Cache-Engine</artifactId>
    <version>main-SNAPSHOT</version>
</dependency>
```

---

## ⚙️ How to Configure It in `application.yml`

Simply enable the cache engine and choose your provider:

```yaml
resilient-cache:
  enabled: true
  provider: multi-level        # Options: caffeine, redis, multi-level
  default-ttl: 30m             # How long items stay in cache
  stampede-protection: true    # Prevents multiple duplicate database queries on cache miss
  caffeine:
    maximum-size: 10000
    expire-after-write: 15m
```

---

## 💻 How to Run It Locally

### Prerequisites
* Java 21 or higher
* Maven 3.6+
* Docker (for running local Redis)

### Step 1: Start Redis
Run the following command to start a local Redis container:
```bash
docker run -d --name resilient-redis -p 6379:6379 redis:alpine
```

### Step 2: Build the Project
Compile and package the parent project:
```bash
./mvnw clean install -DskipTests
```

### Step 3: Run the Demo App
Start the spring boot demo application:
```bash
java -jar resilient-cache-demo/target/resilient-cache-demo-1.0.0.jar
```

Once running, you can visit the Cache Dashboard at:
👉 `http://localhost:8080/cache-dashboard.html`
