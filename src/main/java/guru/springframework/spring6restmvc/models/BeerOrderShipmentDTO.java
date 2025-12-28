package guru.springframework.spring6restmvc.models;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.util.UUID;

@Builder
@Data
public class BeerOrderShipmentDTO {

    private UUID id;

    private Long version;

    @NotBlank
    private String trackingNumber;

    private Timestamp createDate;

    private Timestamp updateDate;
}
