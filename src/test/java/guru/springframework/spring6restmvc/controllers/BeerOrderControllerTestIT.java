package guru.springframework.spring6restmvc.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6restmvc.repositories.BeerOrderRepository;
import guru.springframework.spring6restmvc.repositories.BeerRepository;
import guru.springframework.spring6restmvc.repositories.CustomerRepository;
import guru.springframework.spring6restmvcapi.models.*;
import jakarta.transaction.Transactional;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashSet;
import java.util.Set;

import static guru.springframework.spring6restmvc.controllers.BeerControllerTest.jwtRequestPostProcessor;
import static guru.springframework.spring6restmvc.controllers.BeerOrderController.BEER_ORDER_PATH;
import static guru.springframework.spring6restmvc.controllers.BeerOrderController.BEER_ORDER_PATH_ID;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
class BeerOrderControllerTestIT {

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    BeerOrderRepository beerOrderRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    BeerRepository beerRepository;

    @Autowired
    ObjectMapper objectMapper;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
    }

    @Test
    void testGetListBeerOrders() throws Exception {
        mockMvc.perform(get(BEER_ORDER_PATH)
                        .with(jwtRequestPostProcessor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", greaterThan(0)));
    }

    @Test
    void testGetBeerOrderById() throws Exception {
        val beerOrder = beerOrderRepository.findAll().getFirst();

        mockMvc.perform(get(BeerOrderController.BEER_ORDER_PATH_ID, beerOrder.getId())
                        .with(jwtRequestPostProcessor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(beerOrder.getId().toString())));
    }

    @Test
    void testCreateBeerOrder() throws Exception {
        val customer = customerRepository.findAll().getFirst();
        val beer = beerRepository.findAll().getFirst();

        val beerOrderCreateDto = BeerOrderCreateDTO.builder()
                .customerId(customer.getId())
                .beerOrderLines(Set.of(BeerOrderLineCreateDTO.builder()
                        .beerId(beer.getId())
                        .orderQuantity(1)
                        .build()))
                .build();

        mockMvc.perform(post(BEER_ORDER_PATH)
                        .with(jwtRequestPostProcessor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(beerOrderCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    @Transactional
    void testUpdateBeerOrder() throws Exception {
        val beerOrder = beerOrderRepository.findAll().getFirst();

        Set<BeerOrderLineUpdateDTO> beerOrderLines = new HashSet<>();

        beerOrder.getBeerOrderLines().forEach(beerOrderLine -> beerOrderLines.add(BeerOrderLineUpdateDTO.builder()
                .id(beerOrderLine.getId())
                .beerId(beerOrderLine.getBeer().getId())
                .quantityAllocated(beerOrderLine.getQuantityAllocated())
                .orderQuantity(beerOrderLine.getOrderQuantity())
                .build()));

        val beerOrderUpdateDto = BeerOrderUpdateDTO.builder()
                .customerId(beerOrder.getCustomer().getId())
                .customerRef("testCustomerRef")
                .beerOrderLines(beerOrderLines)
                .beerOrderShipment(BeerOrderShipmentUpdateDTO.builder().trackingNumber("123456").build())
                .build();

        mockMvc.perform(put(BEER_ORDER_PATH_ID, beerOrder.getId())
                        .with(jwtRequestPostProcessor)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(beerOrderUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerRef", is("testCustomerRef")));
    }

    @Test
    void testDeleteBeerOrder() throws Exception {
        val beerOrder = beerOrderRepository.findAll().getFirst();

        mockMvc.perform(delete(BEER_ORDER_PATH + "/" + beerOrder.getId())
                        .with(jwtRequestPostProcessor))
                .andExpect(status().isNoContent());

        assertFalse(beerOrderRepository.findById(beerOrder.getId()).isPresent());

    }
}