package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.controllers.NotFoundException;
import guru.springframework.spring6restmvc.entities.BeerOrder;
import guru.springframework.spring6restmvc.entities.BeerOrderLine;
import guru.springframework.spring6restmvc.entities.BeerOrderShipment;
import guru.springframework.spring6restmvc.entities.Customer;
import guru.springframework.spring6restmvc.mappers.BeerOrderMapper;
import guru.springframework.spring6restmvc.models.BeerOrderCreateDTO;
import guru.springframework.spring6restmvc.models.BeerOrderDTO;
import guru.springframework.spring6restmvc.models.BeerOrderUpdateDTO;
import guru.springframework.spring6restmvc.repositories.BeerOrderRepository;
import guru.springframework.spring6restmvc.repositories.BeerRepository;
import guru.springframework.spring6restmvc.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BeerOrderServiceJPA implements BeerOrderService {

    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderMapper beerOrderMapper;
    private final CustomerRepository customerRepository;
    private final BeerRepository beerRepository;

    @Override
    public Page<BeerOrderDTO> getAllBeerOrders(Integer pageNumber, Integer pageSize) {
        if (pageNumber == null || pageNumber < 0) {
            pageNumber = 0;
        }

        if (pageSize == null || pageSize < 1) {
            pageSize = 25;
        }

        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        return beerOrderRepository.findAll(pageRequest).map(beerOrderMapper::beerOrderToBeerOrderDto);
    }

    @Override
    public Optional<BeerOrderDTO> getBeerOrderById(UUID id) {
        return beerOrderRepository.findById(id).map(beerOrderMapper::beerOrderToBeerOrderDto);
    }

    @Override
    public BeerOrder createBeerOrder(BeerOrderCreateDTO beerOrderCreateDTO) {
        Customer customer = customerRepository.findById(beerOrderCreateDTO.getCustomerId()).orElseThrow(NotFoundException::new);
        Set<BeerOrderLine> beerOrderLines = new HashSet<>();
        beerOrderCreateDTO.getBeerOrderLines().forEach(beerOrderLine -> {
            beerOrderLines.add(BeerOrderLine.builder()
                    .beer(beerRepository.findById(beerOrderLine.getBeerId()).orElseThrow(NotFoundException::new))
                    .orderQuantity(beerOrderLine.getOrderQuantity())
                    .build());
        });

        return beerOrderRepository.save(BeerOrder.builder()
                .customer(customer)
                .customerRef(beerOrderCreateDTO.getCustomerRef())
                .beerOrderLines(beerOrderLines)
                .build());
    }

    @Override
    public BeerOrderDTO updateBeerOrder(UUID beerOrderId, BeerOrderUpdateDTO beerOrderUpdateDTO) {
        BeerOrder beerOrder = beerOrderRepository.findById(beerOrderId).orElseThrow(NotFoundException::new);
        beerOrder.setCustomer(customerRepository.findById(beerOrderUpdateDTO.getCustomerId()).orElseThrow(NotFoundException::new));
        beerOrder.setCustomerRef(beerOrderUpdateDTO.getCustomerRef());

        beerOrderUpdateDTO.getBeerOrderLines().forEach(beerOrderLine -> {
            if (beerOrderLine.getBeerId() != null) {
                val foundLine = beerOrder.getBeerOrderLines().stream()
                        .filter(beerOrderLine1 -> beerOrderLine1.getId().equals(beerOrderLine.getId()))
                        .findFirst().orElseThrow(NotFoundException::new);

                foundLine.setBeer(beerRepository.findById(beerOrderLine.getBeerId()).orElseThrow(NotFoundException::new));
                foundLine.setOrderQuantity(beerOrderLine.getOrderQuantity());
                foundLine.setQuantityAllocated(beerOrderLine.getQuantityAllocated());
            } else {
                beerOrder.getBeerOrderLines().add(BeerOrderLine.builder()
                        .beer(beerRepository.findById(beerOrderLine.getBeerId()).orElseThrow(NotFoundException::new))
                        .orderQuantity(beerOrderLine.getOrderQuantity())
                        .quantityAllocated(beerOrderLine.getQuantityAllocated()).build());
            }
        });

        if (beerOrderUpdateDTO.getBeerOrderShipment() != null && beerOrderUpdateDTO.getBeerOrderShipment().getTrackingNumber() != null) {
            if (beerOrder.getBeerOrderShipment() == null) {
                beerOrder.setBeerOrderShipment(BeerOrderShipment.builder().trackingNumber(beerOrderUpdateDTO.getBeerOrderShipment().getTrackingNumber()).build());
            } else {
                beerOrder.getBeerOrderShipment().setTrackingNumber(beerOrderUpdateDTO.getBeerOrderShipment().getTrackingNumber());
            }
        }

        return beerOrderMapper.beerOrderToBeerOrderDto(beerOrderRepository.save(beerOrder));
    }

    @Override
    public void deleteBeerOrder(UUID beerOrderId) {
        BeerOrder beerOrder = beerOrderRepository.findById(beerOrderId).orElseThrow(NotFoundException::new);
        beerOrderRepository.delete(beerOrder);
    }
}
