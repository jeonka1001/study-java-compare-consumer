package shadowdiff.compare.application;

import shadowdiff.compare.contract.LegacyResponseRecord;
import shadowdiff.compare.contract.ShadowRequestMessage;
import shadowdiff.compare.domain.VerificationOutcome;
import shadowdiff.compare.port.LegacyResponseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 검증 유스케이스의 오케스트레이션 담당.
 *
 * <p>절차는 다음 세 단계로 고정한다.
 * <ol>
 *     <li>Redis에서 레거시 응답 조회 (없으면 SKIPPED)</li>
 *     <li>신규 API 재현 호출 (1:N 병렬 호출 후 병합)</li>
 *     <li>Diff 엔진으로 비교 후 결과 기록</li>
 * </ol>
 *
 * <p>각 단계의 실제 구현은 Port 인터페이스 뒤에 두어, 이 클래스는 흐름만 서술한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationService {

    private final LegacyResponseRepository legacyResponseRepository;

    /**
     * 단일 섀도잉 이벤트를 검증한다.
     *
     * @param message Kafka로 수신한 원본 요청 정보
     */
    public void verify(ShadowRequestMessage message) {
        String uuid = message.uuid();

        Optional<LegacyResponseRecord> legacyResponse = legacyResponseRepository.findByUuid(uuid);
        if (legacyResponse.isEmpty()) {
            // TTL 만료 또는 프록시의 Redis 적재 지연. 재시도는 Repository 구현체가 이미 수행했다.
            log.warn("[ShadowDiff] 레거시 응답 없음. 검증을 건너뛴다. uuid={}", uuid);
            record(VerificationOutcome.skipped(uuid, "legacy response not found in redis"));
            return;
        }

        // TODO: 2단계 — 신규 API 재현 호출
        //  - message.request()를 신규 API 호출 스펙으로 매핑 (1:N 가능)
        //  - Virtual Thread + StructuredTaskScope 로 병렬 호출, 부분 실패 시 처리 정책 결정
        //  - 호출 결과를 ResponsePayload로 병합

        // TODO: 3단계 — Diff 엔진 비교
        //  - YAML 룰셋(EXACT / TYPE_CAST / IGNORE_FIELD / STRUCT_MAPPING) 적용
        //  - statusCode 불일치는 바디 비교 이전에 즉시 MISMATCH 판정

        // TODO: 4단계 — 결과 기록
        //  - Micrometer Counter 증가, Mismatch 상세 덤프 적재

        log.debug("[ShadowDiff] 검증 파이프라인 미구현 구간. uuid={}", uuid);
    }

    /**
     * 검증 결과 기록 지점.
     *
     * <p>TODO: VerificationResultRecorder(Port)로 분리하여 메트릭/덤프 저장소에 위임.
     */
    private void record(VerificationOutcome outcome) {
        log.info("[ShadowDiff] 검증 결과. {}", outcome);
    }
}
