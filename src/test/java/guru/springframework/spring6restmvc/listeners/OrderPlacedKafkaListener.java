package guru.springframework.spring6restmvc.listeners;

import guru.springframework.spring6restmvc.configs.KafkaConfig;
import guru.springframework.spring6restmvcapi.events.OrderPlacedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class OrderPlacedKafkaListener {

    AtomicInteger messageCounter = new AtomicInteger(0);

    @KafkaListener(groupId = "KafkaIntegrationTest", topics = KafkaConfig.ORDER_PLACED_TOPIC)
    public void receive(OrderPlacedEvent orderPlacedEvent) {
        System.out.println("OrderPlacedEvent received: " + orderPlacedEvent);
        messageCounter.incrementAndGet();
    }
}
