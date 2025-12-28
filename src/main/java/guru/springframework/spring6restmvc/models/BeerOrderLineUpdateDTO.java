package guru.springframework.spring6restmvc.models;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Data
public class BeerOrderLineUpdateDTO {

    private UUID id;

    @NotNull
    private UUID beerId;

    @Min(value = 1, message = "Quantity on hand must be greater than 0")
    private Integer orderQuantity;

    private Integer quantityAllocated;
}
