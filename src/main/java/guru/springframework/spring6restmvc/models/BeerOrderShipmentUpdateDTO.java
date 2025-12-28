package guru.springframework.spring6restmvc.models;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class BeerOrderShipmentUpdateDTO {

    @NotBlank
    private String trackingNumber;
}
