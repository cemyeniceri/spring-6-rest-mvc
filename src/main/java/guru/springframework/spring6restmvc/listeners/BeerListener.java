package guru.springframework.spring6restmvc.listeners;

import guru.springframework.spring6restmvc.events.*;
import guru.springframework.spring6restmvc.mappers.BeerMapper;
import guru.springframework.spring6restmvc.repositories.BeerAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class BeerListener {

    private final BeerMapper beerMapper;
    private final BeerAuditRepository beerAuditRepository;

    @Async
    @EventListener
    public void listen(BeerEvent beerEvent) {

        val beerAudit = beerMapper.beerToBeerAudit(beerEvent.getBeer());
        String eventType;

        switch (beerEvent) {
            case BeerCreatedEvent beerCreatedEvent -> eventType = "BEER_CREATED";
            case BeerUpdatedEvent beerUpdatedEvent -> eventType = "BEER_UPDATED";
            case BeerPatchedEvent beerPatchedEvent -> eventType = "BEER_PATCHED";
            case BeerDeletedEvent beerDeletedEvent -> eventType = "BEER_DELETED";
            default -> eventType = "UNKNOWN";
        }

        beerAudit.setAuditEventType(eventType);

        if (beerEvent.getAuthentication() != null && beerEvent.getAuthentication().getName() != null) {
            beerAudit.setPrincipalName(beerEvent.getAuthentication().getName());
        }

        val savedBeerAudit = beerAuditRepository.save(beerAudit);
        log.debug("Beer Audit Saved: {}", savedBeerAudit.getId());
    }
}
