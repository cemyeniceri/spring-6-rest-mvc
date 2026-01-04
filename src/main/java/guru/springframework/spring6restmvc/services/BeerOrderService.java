package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.entities.BeerOrder;
import guru.springframework.spring6restmvcapi.models.BeerOrderCreateDTO;
import guru.springframework.spring6restmvcapi.models.BeerOrderDTO;
import guru.springframework.spring6restmvcapi.models.BeerOrderUpdateDTO;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface BeerOrderService {

    Page<BeerOrderDTO> getAllBeerOrders(Integer pageNumber, Integer pageSize);

    Optional<BeerOrderDTO> getBeerOrderById(UUID id);

    BeerOrder createBeerOrder(BeerOrderCreateDTO beerOrderCreateDTO);

    BeerOrderDTO updateBeerOrder(UUID beerOrderId, BeerOrderUpdateDTO beerOrderUpdateDTO);

    void deleteBeerOrder(UUID beerOrderId);
}
