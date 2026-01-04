package guru.springframework.spring6restmvc.listeners;

import guru.springframework.spring6restmvcapi.events.OrderPlacedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class OrderPlacedListener {

    @EventListener
    @Async
    public void handleOrderPlacedEvent(OrderPlacedEvent orderPlacedEvent) {
        // todo add send to kafka
        System.out.println("OrderPlacedEvent: " + orderPlacedEvent);
    }
}
