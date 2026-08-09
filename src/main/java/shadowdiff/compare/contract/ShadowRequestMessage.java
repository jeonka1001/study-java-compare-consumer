package shadowdiff.compare.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

/**
 * Kafka {@code shadowdiff.request} 토픽으로 발행되는 메시지.
 *
 * <p>프록시 서버가 레거시 API 요청을 중계할 때 발급한 {@code uuid}와 원본 요청 정보를 담는다.
 * Compare Server는 이 {@code uuid}로 Redis에 적재된 레거시 응답을 조회하여 비교 대상을 완성한다.
 *
 * <p>메시지 예시는 README "3-1. Kafka Message" 참고.
 *
 * @param uuid    요청 구분 키. Redis 조회 키와 동일한 값
 * @param request 원본 요청 정보
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ShadowRequestMessage(
        String uuid,
        RequestPayload request
) {

    public ShadowRequestMessage {
        // 역직렬화 직후 최소 불변식만 검증한다. 상세 검증은 소비 측 Validator 책임.
        Objects.requireNonNull(uuid, "uuid must not be null");
        Objects.requireNonNull(request, "request must not be null");
    }
}
