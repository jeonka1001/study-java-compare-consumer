package shadowdiff.compare;

import shadowdiff.compare.config.ShadowDiffProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Compare Server 진입점.
 *
 * <p>Kafka {@code shadowdiff.request} 토픽을 소비하여 신규 API를 재현 호출하고,
 * Redis에 적재된 레거시 응답과 비교하는 검증 전용 애플리케이션이다.
 */
@SpringBootApplication
@EnableConfigurationProperties(ShadowDiffProperties.class)
public class CompareServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompareServerApplication.class, args);
    }
}
