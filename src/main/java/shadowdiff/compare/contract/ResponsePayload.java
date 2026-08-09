package shadowdiff.compare.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * API 응답 스냅샷. 레거시 응답(Redis)과 신규 응답(실시간 호출) 양쪽에 동일하게 사용한다.
 *
 * @param statusCode HTTP 상태 코드
 * @param body       응답 바디(JSON). Diff 엔진의 실제 비교 대상
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ResponsePayload(
        int statusCode,
        Map<String, Object> body
) {

    public ResponsePayload {
        // JSON 응답에는 null 값 필드가 존재할 수 있으므로 Map.copyOf(NPE 발생) 대신 LinkedHashMap 복사를 사용한다.
        body = (body == null)
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(body));
    }

    /**
     * 2xx 여부. 상태 코드 자체가 다르면 바디 비교 이전에 Mismatch로 판정하기 위해 사용한다.
     */
    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }
}
