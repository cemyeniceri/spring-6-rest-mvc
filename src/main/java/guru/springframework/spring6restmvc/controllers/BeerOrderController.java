package guru.springframework.spring6restmvc.controllers;

import guru.springframework.spring6restmvc.entities.BeerOrder;
import guru.springframework.spring6restmvc.services.BeerOrderService;
import guru.springframework.spring6restmvcapi.models.BeerOrderCreateDTO;
import guru.springframework.spring6restmvcapi.models.BeerOrderDTO;
import guru.springframework.spring6restmvcapi.models.BeerOrderUpdateDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class BeerOrderController {

    public static final String BEER_ORDER_PATH = "/api/v1/beerorder";
    public static final String BEER_ORDER_PATH_ID = BEER_ORDER_PATH + "/{beerOrderId}";

    private final BeerOrderService beerOrderService;

    @GetMapping(BEER_ORDER_PATH)
    public Page<BeerOrderDTO> listBeerOrders(@RequestParam(required = false) Integer pageNumber,
                                             @RequestParam(required = false) Integer pageSize) {
        log.debug("List beer orders - in controller");
        return beerOrderService.getAllBeerOrders(pageNumber, pageSize);
    }

    @GetMapping(BEER_ORDER_PATH_ID)
    public BeerOrderDTO getBeerOrderById(@PathVariable UUID beerOrderId) {
        log.debug("Get beer order by id - in controller");
        return beerOrderService.getBeerOrderById(beerOrderId).orElseThrow(NotFoundException::new);
    }

    @PostMapping(BEER_ORDER_PATH)
    public ResponseEntity<Void> createBeerOrder(@Validated @RequestBody BeerOrderCreateDTO beerOrderCreateDTO) {
        log.debug("Create beer order - in controller");
        BeerOrder savedBeerOrder = beerOrderService.createBeerOrder(beerOrderCreateDTO);
        return ResponseEntity.created(URI.create(BEER_ORDER_PATH + "/" + savedBeerOrder.getId().toString())).build();
    }

    @PutMapping(BEER_ORDER_PATH_ID)
    public ResponseEntity<BeerOrderDTO> updateBeerOrder(@PathVariable UUID beerOrderId, @Validated @RequestBody BeerOrderUpdateDTO beerOrderUpdateDTO) {
        log.debug("Update beer order - in controller");
        return ResponseEntity.ok(beerOrderService.updateBeerOrder(beerOrderId, beerOrderUpdateDTO));
    }

    @DeleteMapping(BEER_ORDER_PATH_ID)
    public ResponseEntity<Void> deleteBeerOrder(@PathVariable UUID beerOrderId) {
        log.debug("Delete beer order - in controller");
        beerOrderService.deleteBeerOrder(beerOrderId);
        return ResponseEntity.noContent().build();
    }
}
