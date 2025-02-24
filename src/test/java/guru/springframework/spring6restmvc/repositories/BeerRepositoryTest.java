package guru.springframework.spring6restmvc.repositories;

import guru.springframework.spring6restmvc.bootstrap.BootstrapData;
import guru.springframework.spring6restmvc.entity.Beer;
import guru.springframework.spring6restmvc.model.BeerStyle;
import guru.springframework.spring6restmvc.services.BeerCsvServiceImpl;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Import({BootstrapData.class, BeerCsvServiceImpl.class})
class BeerRepositoryTest {

    @Autowired
    BeerRepository beerRepository;

    @Test
    void testGetBeerListByName() {
        Page<Beer> ipaList = beerRepository.findAllByBeerNameIsLike("%IPA%", null);
        assertThat(ipaList.getContent().size()).isEqualTo(336);
    }

    @Test
    void testGetBeerListByStyle() {
        Page<Beer> ipaList = beerRepository.findAllByBeerStyle(BeerStyle.IPA, null);
        assertThat(ipaList.getContent().size()).isEqualTo(548);
    }

    @Test
    void testSaveBeer() {
        Beer savedBeer = beerRepository.save(Beer.builder()
                .beerName("My Beer")
                .beerStyle(BeerStyle.LAGER)
                .upc("123456789")
                .price(new BigDecimal("12.98"))
                .build());

        beerRepository.flush();
        assertThat(savedBeer).isNotNull();
        assertThat(savedBeer.getId()).isNotNull();
        assertThat(savedBeer.getBeerName()).isEqualTo("My Beer");
    }

    @Test
    void testSaveBeerWhenBeerNameTooLong() {
        assertThrows(ConstraintViolationException.class, () -> {
                    beerRepository.save(Beer.builder()
                            .beerName("000000000000000000000000000000000000000000000000001")
                            .beerStyle(BeerStyle.LAGER)
                            .upc("123456789")
                            .price(new BigDecimal("12.98"))
                            .build());

                    beerRepository.flush();
                }
        );
    }
}

