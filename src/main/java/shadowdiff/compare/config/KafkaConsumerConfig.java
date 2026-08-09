package shadowdiff.compare.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import shadowdiff.compare.contract.ShadowRequestMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Consumer 설정.
 *
 * <p>역직렬화 실패 메시지가 파티션을 막지 않도록 {@link ErrorHandlingDeserializer}로 감싸고,
 * 처리 실패는 제한된 횟수만 재시도한 뒤 DLT로 넘긴다.
 */
@Slf4j
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, ShadowRequestMessage> shadowRequestConsumerFactory(
            KafkaProperties kafkaProperties,
            ObjectMapper objectMapper
    ) {
        // Deserializer 인스턴스를 직접 주입하므로 props의 *_DESERIALIZER_CLASS_CONFIG는 설정하지 않는다.
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties(null));

        // 프록시가 보낸 타입 헤더에 의존하지 않도록 대상 타입을 고정한다.
        JsonDeserializer<ShadowRequestMessage> valueDeserializer =
                new JsonDeserializer<>(ShadowRequestMessage.class, objectMapper, false);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(valueDeserializer)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ShadowRequestMessage> shadowRequestListenerContainerFactory(
            ConsumerFactory<String, ShadowRequestMessage> consumerFactory
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, ShadowRequestMessage>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(shadowRequestErrorHandler());
        return factory;
    }

    /**
     * 처리 실패 시 1초 간격 2회 재시도 후 로그만 남기고 다음 메시지로 진행한다.
     *
     * <p>검증 파이프라인은 원본 트래픽에 영향을 주지 않는 부가 경로이므로,
     * 개별 메시지 실패로 컨슈머가 멈추지 않는 것을 우선한다.
     * TODO: DeadLetterPublishingRecoverer를 붙여 DLT(shadowdiff.request.DLT)로 적재.
     */
    private DefaultErrorHandler shadowRequestErrorHandler() {
        var errorHandler = new DefaultErrorHandler(
                (record, exception) -> log.error(
                        "[ShadowDiff] 메시지 처리 최종 실패. topic={}, partition={}, offset={}",
                        record.topic(), record.partition(), record.offset(), exception
                ),
                new FixedBackOff(1_000L, 2L)
        );
        // 역직렬화 실패는 재시도해도 동일하게 실패하므로 즉시 포기한다.
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);
        return errorHandler;
    }
}
