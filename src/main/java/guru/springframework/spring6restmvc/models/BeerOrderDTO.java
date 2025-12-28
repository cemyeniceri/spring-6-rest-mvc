package guru.springframework.spring6restmvc.models;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;

@Builder
@Data
public class BeerOrderDTO {

    private UUID id;

    private Long version;

    private Timestamp createDate;

    private Timestamp updateDate;

    private String customerRef;

    private CustomerDTO customer;

    private Set<BeerOrderLineDTO> beerOrderLines;

    private BeerOrderShipmentDTO beerOrderShipment;
}
