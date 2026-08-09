package shadowdiff.compare.port;

import shadowdiff.compare.contract.LegacyResponseRecord;

import java.util.Optional;

/**
 * 레거시 응답 스냅샷 조회 Port.
 *
 * <p>저장소 기술(Redis)을 애플리케이션 계층에서 감추기 위한 경계.
 * 재시도 정책은 구현체가 책임진다.
 */
public interface LegacyResponseRepository {

    /**
     * 구분 키로 레거시 응답을 조회한다.
     *
     * @param uuid Kafka 메시지의 구분 키
     * @return 조회 결과. TTL 만료 또는 미적재 시 {@link Optional#empty()}
     */
    Optional<LegacyResponseRecord> findByUuid(String uuid);
}
