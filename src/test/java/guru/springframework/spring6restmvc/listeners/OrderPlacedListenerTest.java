package guru.springframework.spring6restmvc.listeners;

import guru.springframework.spring6restmvc.configs.KafkaConfig;
import guru.springframework.spring6restmvcapi.events.OrderPlacedEvent;
import guru.springframework.spring6restmvcapi.models.BeerDTO;
import guru.springframework.spring6restmvcapi.models.BeerOrderDTO;
import guru.springframework.spring6restmvcapi.models.BeerOrderLineDTO;
import guru.springframework.spring6restmvcapi.models.BeerStyle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@EmbeddedKafka(controlledShutdown = true, topics = {KafkaConfig.ORDER_PLACED_TOPIC}, partitions = 1, kraft = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OrderPlacedListenerTest {

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired
    private OrderPlacedListener orderPlacedListener;

    @Autowired
    private OrderPlacedKafkaListener orderPlacedKafkaListener;

    @Autowired
    private DrinkSplitterRouter drinkSplitterRouter;

    @Autowired
    private DrinkListenerKafkaConsumer drinkListenerKafkaConsumer;

    @BeforeEach
    void setUp() {
        kafkaListenerEndpointRegistry.getListenerContainers().forEach(container -> {
            ContainerTestUtils.waitForAssignment(container, 1);
        });
    }

    @Test
    void orderPlacedListenerTest() {
        OrderPlacedEvent orderPlacedEvent = OrderPlacedEvent.builder()
                .beerOrderDTO(BeerOrderDTO.builder().id(UUID.randomUUID()).build())
                .build();

        orderPlacedListener.handleOrderPlacedEvent(orderPlacedEvent);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertEquals(1, orderPlacedKafkaListener.messageCounter.get()));
    }

    @Test
    void listenSplitter() {
        drinkSplitterRouter.receive(OrderPlacedEvent.builder()
                .beerOrderDTO(buildOrder())
                .build());

        await().atMost(15, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS)
                .until(drinkListenerKafkaConsumer.iceColdMessageCounter::get, greaterThan(0));

        await().atMost(15, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS)
                .until(drinkListenerKafkaConsumer.coldMessageCounter::get, greaterThan(0));

        await().atMost(15, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS)
                .until(drinkListenerKafkaConsumer.coolMessageCounter::get, greaterThan(0));
    }


    BeerOrderDTO buildOrder() {

        Set<BeerOrderLineDTO> beerOrderLines = new HashSet<>();

        beerOrderLines.add(BeerOrderLineDTO.builder()
                .beer(BeerDTO.builder()
                        .id(UUID.randomUUID())
                        .beerStyle(BeerStyle.IPA)
                        .beerName("Test Beer")
                        .build())
                .build());

        //add lager
        beerOrderLines.add(BeerOrderLineDTO.builder()
                .beer(BeerDTO.builder()
                        .id(UUID.randomUUID())
                        .beerStyle(BeerStyle.LAGER)
                        .beerName("Test Beer")
                        .build())
                .build());

        //add gose
        beerOrderLines.add(BeerOrderLineDTO.builder()
                .beer(BeerDTO.builder()
                        .id(UUID.randomUUID())
                        .beerStyle(BeerStyle.GOSE)
                        .beerName("Test Beer")
                        .build())
                .build());

        return BeerOrderDTO.builder()
                .id(UUID.randomUUID())
                .beerOrderLines(beerOrderLines)
                .build();
    }
}