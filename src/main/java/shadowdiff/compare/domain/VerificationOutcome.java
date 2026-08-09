package shadowdiff.compare.domain;

import java.util.List;

/**
 * 검증 결과. sealed interface로 가능한 상태를 컴파일 타임에 한정하여
 * switch 패턴 매칭 시 누락된 분기를 컴파일러가 잡도록 한다.
 */
public sealed interface VerificationOutcome {

    String uuid();

    /**
     * 모든 규칙을 통과한 정상 상태.
     */
    record Match(String uuid) implements VerificationOutcome {
    }

    /**
     * 불일치. 원인 필드 목록을 함께 보관한다.
     *
     * @param differences 불일치 상세. 필드 경로와 양측 값을 담는다.
     */
    record Mismatch(String uuid, List<FieldDifference> differences) implements VerificationOutcome {

        public Mismatch {
            differences = List.copyOf(differences);
        }
    }

    /**
     * 비교 자체가 불가능하여 건너뛴 상태 (예: Redis 미스, 신규 API 매핑 규칙 없음).
     */
    record Skipped(String uuid, String reason) implements VerificationOutcome {
    }

    /**
     * 검증 도중 예외로 실패한 상태.
     */
    record Failed(String uuid, String reason) implements VerificationOutcome {
    }

    static VerificationOutcome match(String uuid) {
        return new Match(uuid);
    }

    static VerificationOutcome mismatch(String uuid, List<FieldDifference> differences) {
        return new Mismatch(uuid, differences);
    }

    static VerificationOutcome skipped(String uuid, String reason) {
        return new Skipped(uuid, reason);
    }

    static VerificationOutcome failed(String uuid, String reason) {
        return new Failed(uuid, reason);
    }
}
