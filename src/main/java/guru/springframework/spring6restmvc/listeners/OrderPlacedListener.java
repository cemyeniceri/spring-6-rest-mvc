package guru.springframework.spring6restmvc.listeners;

import guru.springframework.spring6restmvc.configs.KafkaConfig;
import guru.springframework.spring6restmvcapi.events.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderPlacedListener {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Async
    @EventListener
    public void handleOrderPlacedEvent(OrderPlacedEvent orderPlacedEvent) {
        log.debug("Received OrderPlacedEvent {}", orderPlacedEvent);
        kafkaTemplate.send(KafkaConfig.ORDER_PLACED_TOPIC, orderPlacedEvent);
    }
}
