package shadowdiff.compare.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Compare Server 동작 설정.
 *
 * <p>application.yml의 {@code shadowdiff.*} 프리픽스에 바인딩된다.
 * record 기반 불변 설정으로 런타임 변경 가능성을 차단한다.
 *
 * @param topic      소비 대상 Kafka 토픽
 * @param redis      Redis 조회 관련 설정
 * @param newApi     신규 API 호출 관련 설정
 */
@ConfigurationProperties(prefix = "shadowdiff")
public record ShadowDiffProperties(
        @DefaultValue("shadowdiff.request") String topic,
        @DefaultValue Redis redis,
        @DefaultValue NewApi newApi
) {

    /**
     * @param keyPrefix    레거시 응답 키 프리픽스. 최종 키는 {@code {keyPrefix}{uuid}}
     * @param retryDelay   Redis 미스 시 재시도 대기 시간 (프록시의 비동기 적재 지연 흡수용)
     * @param maxRetry     Redis 미스 재시도 횟수. 초과 시 해당 이벤트는 SKIPPED 처리
     */
    public record Redis(
            @DefaultValue("shadowdiff:response:") String keyPrefix,
            @DefaultValue("200ms") Duration retryDelay,
            @DefaultValue("2") int maxRetry
    ) {
        public String keyOf(String uuid) {
            return keyPrefix + uuid;
        }
    }

    /**
     * @param baseUrl        신규 API 베이스 URL
     * @param timeout        신규 API 개별 호출 타임아웃
     * @param maxConcurrency 1:N 호출 시 동시 실행 상한. 하류 시스템 보호 목적
     */
    public record NewApi(
            @DefaultValue("http://localhost:8081") String baseUrl,
            @DefaultValue("3s") Duration timeout,
            @DefaultValue("50") int maxConcurrency
    ) {
    }
}
