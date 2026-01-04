package guru.springframework.spring6restmvc.controllers;

import guru.springframework.spring6restmvc.services.BeerService;
import guru.springframework.spring6restmvcapi.models.BeerDTO;
import guru.springframework.spring6restmvcapi.models.BeerStyle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
public class BeerController {

    public static final String BEER_PATH = "/api/v1/beer";
    public static final String BEER_PATH_ID = BEER_PATH + "/{beerId}";
    private final BeerService beerService;

    @GetMapping(BEER_PATH)
    public Page<BeerDTO> listBeers(@RequestParam(required = false) String beerName,
                                   @RequestParam(required = false) BeerStyle beerStyle,
                                   @RequestParam(required = false) Boolean showInventory,
                                   @RequestParam(required = false) Integer pageNumber,
                                   @RequestParam(required = false) Integer pageSize) {
        log.debug("List beers - in controller");
        return beerService.getAllBeers(beerName, beerStyle, showInventory, pageNumber, pageSize);
    }

    @GetMapping(BEER_PATH_ID)
    public BeerDTO getBeerById(@PathVariable UUID beerId) {
        log.debug("Get beer by id - in controller");
        return beerService.getBeerById(beerId).orElseThrow(NotFoundException::new);
    }

    @PostMapping(BEER_PATH)
    public ResponseEntity<HttpHeaders> createBeer(@Validated @RequestBody BeerDTO beer) {
        BeerDTO savedBeer = beerService.saveBeer(beer);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Location", "/api/v1/beer/" + savedBeer.getId().toString());
        return new ResponseEntity<>(responseHeaders, HttpStatus.CREATED);
    }

    @PutMapping(BEER_PATH_ID)
    public ResponseEntity<HttpHeaders> updateBeerById(@PathVariable UUID beerId, @Validated @RequestBody BeerDTO beer) {
        Optional<BeerDTO> updateBeer = beerService.updateBeerById(beerId, beer);
        if (updateBeer.isEmpty()) {
            throw new NotFoundException();
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @DeleteMapping(BEER_PATH_ID)
    public ResponseEntity<HttpHeaders> deleteBeer(@PathVariable UUID beerId) {
        Boolean result = beerService.deleteBeerById(beerId);
        if (result) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            throw new NotFoundException();
        }
    }

    @PatchMapping(BEER_PATH_ID)
    public ResponseEntity<HttpHeaders> updateBeerPartially(@PathVariable UUID beerId, @RequestBody BeerDTO beer) {
        if (beerService.updateBeerPartiallyById(beerId, beer).isEmpty()) {
            throw new NotFoundException();
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }
}
