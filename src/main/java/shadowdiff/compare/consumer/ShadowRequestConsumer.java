package shadowdiff.compare.consumer;

import shadowdiff.compare.contract.ShadowRequestMessage;
import shadowdiff.compare.application.VerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * {@code shadowdiff.request} 토픽 컨슈머.
 *
 * <p>컨슈머는 "수신 → 위임"만 담당한다. 검증 절차는 전적으로
 * {@link VerificationService}가 소유하여, 메시징 인프라와 도메인 로직을 분리한다.
 *
 * <p>오프셋 커밋과 재시도는 컨테이너와 {@code DefaultErrorHandler}에 위임하므로
 * 이 클래스에서 예외를 삼키지 않는다. 예외를 그대로 던져야 재시도 정책이 동작한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShadowRequestConsumer {

    private final VerificationService verificationService;

    @KafkaListener(
            topics = "${shadowdiff.topic}",
            containerFactory = "shadowRequestListenerContainerFactory"
    )
    public void consume(
            @Payload ShadowRequestMessage message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.debug("[ShadowDiff] 이벤트 수신. uuid={}, partition={}, offset={}",
                message.uuid(), partition, offset);

        verificationService.verify(message);
    }
}
