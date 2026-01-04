package guru.springframework.spring6restmvc.services;

import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.events.BeerCreatedEvent;
import guru.springframework.spring6restmvc.events.BeerDeletedEvent;
import guru.springframework.spring6restmvc.events.BeerPatchedEvent;
import guru.springframework.spring6restmvc.events.BeerUpdatedEvent;
import guru.springframework.spring6restmvc.mappers.BeerMapper;
import guru.springframework.spring6restmvc.repositories.BeerRepository;
import guru.springframework.spring6restmvcapi.models.BeerDTO;
import guru.springframework.spring6restmvcapi.models.BeerStyle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class BeerServiceJPA implements BeerService {

    private final BeerRepository beerRepository;
    private final BeerMapper beerMapper;
    private final CacheManager cacheManager;
    private final ApplicationEventPublisher applicationEventPublisher;

    private static final int DEFAULT_PAGE_SIZE = 25;
    private static final int DEFAULT_PAGE_NUMBER = 0;

    @Cacheable(cacheNames = "beerListCache")
    @Override
    public Page<BeerDTO> getAllBeers(String beerName, BeerStyle beerStyle, Boolean showInventory, Integer pageNumber, Integer pageSize) {
        log.info("Get All beers - in service");

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

    @Cacheable(cacheNames = "beerCache", key = "#id")
    @Override
    public Optional<BeerDTO> getBeerById(UUID id) {
        log.info("Get beer by id - in service");
        return beerRepository.findById(id).map(beerMapper::beerToBeerDTO);
    }

    @Override
    public BeerDTO saveBeer(BeerDTO beer) {
        Optional.ofNullable(cacheManager.getCache("beerListCache")).ifPresent(Cache::clear);
        val savedBeer = beerRepository.save(beerMapper.beerDtoToBeer(beer));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        applicationEventPublisher.publishEvent(new BeerCreatedEvent(savedBeer, authentication));

        return beerMapper.beerToBeerDTO(savedBeer);
    }

    @Override
    public Optional<BeerDTO> updateBeerById(UUID beerId, BeerDTO beer) {
        clearCache(beerId);
        return beerRepository.findById(beerId).map(foundBeer -> {
            foundBeer.setBeerName(beer.getBeerName());
            foundBeer.setBeerStyle(beer.getBeerStyle());
            foundBeer.setPrice(beer.getPrice());
            foundBeer.setUpc(beer.getUpc());
            foundBeer.setQuantityOnHand(beer.getQuantityOnHand());
            val updatedBeer = beerRepository.save(foundBeer);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            applicationEventPublisher.publishEvent(new BeerUpdatedEvent(updatedBeer, authentication));

            return beerMapper.beerToBeerDTO(updatedBeer);
        });
    }

    @Override
    public Boolean deleteBeerById(UUID beerId) {
        clearCache(beerId);

        if (beerRepository.existsById(beerId)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            applicationEventPublisher.publishEvent(new BeerDeletedEvent(Beer.builder().id(beerId).build(), authentication));
            beerRepository.deleteById(beerId);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Optional<BeerDTO> updateBeerPartiallyById(UUID beerId, BeerDTO beer) {

        clearCache(beerId);

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
                    val updatedBeer = beerRepository.save(foundBeer);

                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    applicationEventPublisher.publishEvent(new BeerPatchedEvent(updatedBeer, authentication));
                    return beerMapper.beerToBeerDTO(updatedBeer);
                }
        );
    }

    private void clearCache(UUID uuid) {
        Optional.ofNullable(cacheManager.getCache("beerListCache")).ifPresent(Cache::clear);
        Optional.ofNullable(cacheManager.getCache("beerCache")).ifPresent(cache -> cache.evict(uuid));
    }
}
