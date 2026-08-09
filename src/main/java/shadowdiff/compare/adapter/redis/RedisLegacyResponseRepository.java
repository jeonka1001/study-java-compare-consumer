package shadowdiff.compare.adapter.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import shadowdiff.compare.contract.LegacyResponseRecord;
import shadowdiff.compare.config.ShadowDiffProperties;
import shadowdiff.compare.port.LegacyResponseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Redis 기반 {@link LegacyResponseRepository} 구현체.
 *
 * <p>프록시의 Redis 적재는 클라이언트 응답 이후 비동기로 이뤄지므로,
 * Kafka 이벤트가 먼저 도착해 조회에 실패할 수 있다.
 * 이 경우를 흡수하기 위해 짧은 간격으로 제한된 횟수만 재시도한다.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisLegacyResponseRepository implements LegacyResponseRepository {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final ShadowDiffProperties properties;

    @Override
    public Optional<LegacyResponseRecord> findByUuid(String uuid) {
        ShadowDiffProperties.Redis config = properties.redis();
        String key = config.keyOf(uuid);

        for (int attempt = 0; attempt <= config.maxRetry(); attempt++) {
            Optional<LegacyResponseRecord> found = readOnce(key);
            if (found.isPresent()) {
                return found;
            }
            if (attempt < config.maxRetry() && !sleepQuietly(config.retryDelay().toMillis())) {
                break;
            }
        }

        return Optional.empty();
    }

    private Optional<LegacyResponseRecord> readOnce(String key) {
        String raw = redisTemplate.opsForValue().get(key);
        if (raw == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(objectMapper.readValue(raw, LegacyResponseRecord.class));
        } catch (Exception e) {
            // 규격이 깨진 값은 재시도해도 동일하므로 빈 값으로 처리하고 상위에서 SKIPPED 판정한다.
            log.error("[ShadowDiff] 레거시 응답 역직렬화 실패. key={}", key, e);
            return Optional.empty();
        }
    }

    /**
     * @return 인터럽트 없이 정상 대기했으면 true
     */
    private boolean sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
