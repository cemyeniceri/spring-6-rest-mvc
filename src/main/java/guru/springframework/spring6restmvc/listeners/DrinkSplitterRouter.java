package guru.springframework.spring6restmvc.listeners;

import guru.springframework.spring6restmvc.configs.KafkaConfig;
import guru.springframework.spring6restmvcapi.events.DrinkRequestEvent;
import guru.springframework.spring6restmvcapi.events.OrderPlacedEvent;
import guru.springframework.spring6restmvcapi.models.BeerOrderLineDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DrinkSplitterRouter {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(groupId = "DrinkSplitterRouter", topics = KafkaConfig.ORDER_PLACED_TOPIC)
    public void receive(@Payload OrderPlacedEvent orderPlacedEvent) {

        if (orderPlacedEvent.getBeerOrderDTO() == null ||
                orderPlacedEvent.getBeerOrderDTO().getBeerOrderLines() == null ||
                orderPlacedEvent.getBeerOrderDTO().getBeerOrderLines().isEmpty()) {
            log.error("Received OrderPlacedEvent without beer order lines");
            return;
        }

        orderPlacedEvent.getBeerOrderDTO().getBeerOrderLines().forEach(beerOrderLine -> {
            switch (beerOrderLine.getBeer().getBeerStyle()) {
                case LAGER:
                    log.info("Splitting LAGER Order");
                    sendIceColdBeer(beerOrderLine);
                    break;
                case STOUT:
                    log.info("Splitting STOUT Order");
                    sendCoolBeer(beerOrderLine);
                    break;
                case GOSE:
                    log.info("Splitting GOSE Order");
                    sendColdBeer(beerOrderLine);
                    break;
                case PORTER:
                    log.info("Splitting PORTER Order");
                    sendCoolBeer(beerOrderLine);
                    break;
                case ALE:
                    log.info("Splitting ALE Order");
                    sendCoolBeer(beerOrderLine);
                    break;
                case WHEAT:
                    log.info("Splitting WHEAT Order");
                    sendColdBeer(beerOrderLine);
                    break;
                case IPA:
                    log.info("Splitting IPA Order");
                    sendCoolBeer(beerOrderLine);
                    break;
                case PALE_ALE:
                    log.info("Splitting PALE_ALE Order");
                    sendCoolBeer(beerOrderLine);
                    break;
                case SAISON:
                    log.info("Splitting SAISON Order");
                    sendIceColdBeer(beerOrderLine);
                    break;
            }
        });
    }

    private void sendIceColdBeer(BeerOrderLineDTO beerOrderLine) {
        kafkaTemplate.send(KafkaConfig.DRINK_REQUEST_ICE_COLD_TOPIC, DrinkRequestEvent.builder()
                .beerOrderLine(beerOrderLine)
                .build());
    }

    private void sendColdBeer(BeerOrderLineDTO beerOrderLine) {
        kafkaTemplate.send(KafkaConfig.DRINK_REQUEST_COLD_TOPIC, DrinkRequestEvent.builder()
                .beerOrderLine(beerOrderLine)
                .build());
    }

    private void sendCoolBeer(BeerOrderLineDTO beerOrderLine) {
        kafkaTemplate.send(KafkaConfig.DRINK_REQUEST_COOL_TOPIC, DrinkRequestEvent.builder()
                .beerOrderLine(beerOrderLine)
                .build());
    }


}
