package OfflinePay.bridge;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class IdempotencyService {

    private final Map<String, Instant> seen =
            new ConcurrentHashMap<>();

    @Value("${offlinepay.idempotency-ttl-seconds:86400}")
    private long ttlSeconds;

    public boolean claim(String packetHash) {

        if (packetHash == null || packetHash.isBlank()) {
            throw new IllegalArgumentException(
                    "Packet hash is required"
            );
        }

        Instant now = Instant.now();

        Instant previous =
                seen.putIfAbsent(packetHash, now);

        return previous == null;
    }

    public int size() {
        return seen.size();
    }

    @Scheduled(fixedDelay = 60_000)
    public void evictExpired() {

        Instant cutoff =
                Instant.now().minusSeconds(ttlSeconds);

        seen.entrySet().removeIf(
                entry -> entry.getValue().isBefore(cutoff)
        );
    }

    public void clear() {
        seen.clear();
    }
}
