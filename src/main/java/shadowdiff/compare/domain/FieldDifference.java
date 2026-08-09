package shadowdiff.compare.domain;

/**
 * 단일 필드 불일치 정보.
 *
 * @param path        JSON Path. 예: {@code $.data.user_name}
 * @param legacyValue 레거시(Redis) 측 값
 * @param newValue    신규 API 측 값
 * @param reason      불일치 사유. 예: {@code VALUE_NOT_EQUAL}, {@code FIELD_MISSING}
 */
public record FieldDifference(
        String path,
        Object legacyValue,
        Object newValue,
        String reason
) {
}
