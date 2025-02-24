package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.entity.Beer;
import guru.springframework.spring6restmvc.mappers.BeerMapper;
import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.model.BeerStyle;
import guru.springframework.spring6restmvc.repositories.BeerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

@Service
@Primary
@RequiredArgsConstructor
public class BeerServiceJPA implements BeerService {

    private final BeerRepository beerRepository;
    private final BeerMapper beerMapper;

    private static final int DEFAULT_PAGE_SIZE = 25;
    private static final int DEFAULT_PAGE_NUMBER = 0;

    @Override
    public Page<BeerDTO> getAllBeers(String beerName, BeerStyle beerStyle, Boolean showInventory, Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize);

        Page<Beer> beerPage;
        if (StringUtils.hasText(beerName) && beerStyle == null) {
            beerPage = listBeersByName(beerName, pageRequest);
        } else if (!StringUtils.hasText(beerName) && beerStyle != null) {
            beerPage = listBeersByStyle(beerStyle, pageRequest);
        } else if (StringUtils.hasText(beerName) && beerStyle != null) {
            beerPage = listBeersByNameAndStyle(beerName, beerStyle, pageRequest);
        } else {
            beerPage = beerRepository.findAll(pageRequest);
        }

        if (showInventory != null && !showInventory) {
            beerPage.forEach(beer -> beer.setQuantityOnHand(null));
        }
        return beerPage.map(beerMapper::beerToBeerDTO);
    }

    private PageRequest buildPageRequest(Integer pageNumber, Integer pageSize) {
        int queryPageSize = pageSize == null ? DEFAULT_PAGE_SIZE : (pageSize > 1000 ? 1000 : pageSize);
        int queryPageNumber;
        if (pageNumber != null && pageNumber > 0) {
            queryPageNumber = pageNumber - 1;
        } else {
            queryPageNumber = DEFAULT_PAGE_NUMBER;
        }

        Sort sortByBeerName = Sort.by(Sort.Order.asc("beerName"));
        return PageRequest.of(queryPageNumber, queryPageSize, sortByBeerName);
    }

    private Page<Beer> listBeersByNameAndStyle(String beerName, BeerStyle beerStyle, PageRequest pageRequest) {
        return beerRepository.findAllByBeerNameIsLikeAndBeerStyle("%" + beerName + "%", beerStyle, pageRequest);
    }

    private Page<Beer> listBeersByStyle(BeerStyle beerStyle, PageRequest pageRequest) {
        return beerRepository.findAllByBeerStyle(beerStyle, pageRequest);
    }

    private Page<Beer> listBeersByName(String beerName, PageRequest pageRequest) {
        return beerRepository.findAllByBeerNameIsLike("%" + beerName + "%", pageRequest);
    }

    @Override
    public Optional<BeerDTO> getBeerById(UUID id) {
        return beerRepository.findById(id).map(beerMapper::beerToBeerDTO);
    }

    @Override
    public BeerDTO saveBeer(BeerDTO beer) {
        return beerMapper.beerToBeerDTO(beerRepository.save(beerMapper.beerDtoToBeer(beer)));
    }

    @Override
    public Optional<BeerDTO> updateBeerById(UUID beerId, BeerDTO beer) {
        return beerRepository.findById(beerId).map(foundBeer -> {
            foundBeer.setBeerName(beer.getBeerName());
            foundBeer.setBeerStyle(beer.getBeerStyle());
            foundBeer.setPrice(beer.getPrice());
            foundBeer.setUpc(beer.getUpc());
            foundBeer.setQuantityOnHand(beer.getQuantityOnHand());
            return beerMapper.beerToBeerDTO(beerRepository.save(foundBeer));
        });
    }

    @Override
    public Boolean deleteBeerById(UUID beerId) {
        if (beerRepository.existsById(beerId)) {
            beerRepository.deleteById(beerId);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Optional<BeerDTO> updateBeerPartiallyById(UUID beerId, BeerDTO beer) {

        return beerRepository.findById(beerId).map(foundBeer -> {
                    if (StringUtils.hasText(beer.getBeerName())) {
                        foundBeer.setBeerName(beer.getBeerName());
                    }
                    if (beer.getBeerStyle() != null) {
                        foundBeer.setBeerStyle(beer.getBeerStyle());
                    }
                    if (StringUtils.hasText(beer.getUpc())) {
                        foundBeer.setUpc(beer.getUpc());
                    }
                    if (beer.getPrice() != null) {
                        foundBeer.setPrice(beer.getPrice());
                    }
                    if (beer.getQuantityOnHand() != null) {
                        foundBeer.setQuantityOnHand(beer.getQuantityOnHand());
                    }

                    return beerMapper.beerToBeerDTO(beerRepository.save(foundBeer));
                }
        );
    }
}
