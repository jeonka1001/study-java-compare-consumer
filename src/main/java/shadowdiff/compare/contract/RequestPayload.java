package shadowdiff.compare.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 클라이언트가 보낸 원본 요청 정보.
 *
 * <p>Compare Server는 이 정보를 신규 API 호출 스펙으로 변환(매핑)하여 재현 호출한다.
 *
 * @param method HTTP Method. 예: {@code GET}, {@code POST}
 * @param url    Query String을 제외한 요청 경로. 예: {@code /api/v1/user/profile}
 * @param query  Query Parameter Map. 값이 없으면 빈 Map으로 정규화된다.
 * @param body   요청 바디(JSON). 바디가 없는 요청이면 {@code null}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RequestPayload(
        String method,
        String url,
        Map<String, String> query,
        Map<String, Object> body
) {

    public RequestPayload {
        // null 대신 빈 Map으로 정규화하여 소비 측의 null 체크를 제거한다.
        query = (query == null)
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(query));
    }

    /**
     * 바디는 없을 수 있으므로 Optional로 노출한다.
     */
    public Optional<Map<String, Object>> bodyAsOptional() {
        return Optional.ofNullable(body);
    }
}
