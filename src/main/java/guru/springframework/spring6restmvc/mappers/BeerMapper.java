package guru.springframework.spring6restmvc.mappers;

import guru.springframework.spring6restmvc.entity.Beer;
import guru.springframework.spring6restmvc.model.BeerDTO;
import org.mapstruct.Mapper;

@Mapper
public interface BeerMapper {
    Beer beerDtoToBeer(BeerDTO beerDTO);

    BeerDTO beerToBeerDTO(Beer beer);
}
