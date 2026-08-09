package shadowdiff.compare.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Redis {@code shadowdiff:response:{uuid}} 키에 저장되는 레거시 API 응답 스냅샷.
 *
 * <p>프록시가 레거시 응답을 클라이언트에 반환하기 직전 비동기로 적재하며,
 * Compare Server는 신규 API 응답과 이 값을 비교한다.
 *
 * <p>값 예시는 README "3-2. Redis Value" 참고.
 *
 * @param uuid     Kafka 메시지의 uuid와 매칭되는 구분 키
 * @param response 레거시 응답 본문 및 상태 코드
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LegacyResponseRecord(
        String uuid,
        ResponsePayload response
) {
}
