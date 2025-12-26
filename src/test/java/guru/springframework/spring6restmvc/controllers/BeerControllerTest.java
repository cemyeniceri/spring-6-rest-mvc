package guru.springframework.spring6restmvc.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6restmvc.configs.SpringSecurityConfig;
import guru.springframework.spring6restmvc.models.BeerDTO;
import guru.springframework.spring6restmvc.services.BeerService;
import guru.springframework.spring6restmvc.services.BeerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static guru.springframework.spring6restmvc.controllers.BeerController.BEER_PATH;
import static guru.springframework.spring6restmvc.controllers.BeerController.BEER_PATH_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BeerController.class)
@Import(SpringSecurityConfig.class)
class BeerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    BeerService beerService;

    @Autowired
    ObjectMapper objectMapper;

    @Captor
    ArgumentCaptor<UUID> captorUUID;

    @Captor
    ArgumentCaptor<BeerDTO> captorBeer;

    BeerServiceImpl beerServiceImpl;

    public static final SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtRequestPostProcessor = jwt().jwt(jwt ->
            jwt.claims(claims -> {
                                claims.put("scope", "message-read");
                                claims.put("scope", "message-write");
                            }
                    )
                    .subject("messaging-client")
                    .notBefore(Instant.now().minusSeconds(5L)));

    @BeforeEach
    void setUp() {
        beerServiceImpl = new BeerServiceImpl();
    }

    @Test
    void testGetBeerById() throws Exception {
        BeerDTO testBeer = beerServiceImpl.getAllBeers(null, null, false, 1, 25).getContent().getFirst();

        given(beerService.getBeerById(testBeer.getId())).willReturn(Optional.of(testBeer));

        mockMvc.perform(get(BEER_PATH_ID, testBeer.getId())
                        .with(jwtRequestPostProcessor)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testBeer.getId().toString())))
                .andExpect(jsonPath("$.beerName", is(testBeer.getBeerName())));
    }

    @Test
    void tesGetBeerByIdNotFound() throws Exception {
        given(beerService.getBeerById(any())).willReturn(Optional.empty());
        mockMvc.perform(get(BEER_PATH_ID, UUID.randomUUID())
                        .with(jwtRequestPostProcessor)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllBeers() throws Exception {
        Page<BeerDTO> testBeers = beerServiceImpl.getAllBeers(any(), any(), any(), any(), any());

        given(beerService.getAllBeers(null, null, false, 1, 25)).willReturn(testBeers);

        mockMvc.perform(get(BEER_PATH)
                        .with(jwtRequestPostProcessor)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(3)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testCreateNewBeer() throws Exception {

        BeerDTO beer = beerServiceImpl.getAllBeers(null, null, false, 1, 25).getContent().getFirst();
        beer.setId(null);

        given(beerService.saveBeer(any(BeerDTO.class))).willReturn(beerServiceImpl.getAllBeers(null, null, false, 1, 25).getContent().get(1));

        mockMvc.perform(post(BEER_PATH)
                        .with(jwtRequestPostProcessor)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beer)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    void testUpdateBeerById() throws Exception {
        BeerDTO testBeer = beerServiceImpl.getAllBeers(null, null, false, 1, 25).getContent().getFirst();
        given(beerService.updateBeerById(any(UUID.class), any(BeerDTO.class))).willReturn(Optional.of(testBeer));

        mockMvc.perform(put(BEER_PATH_ID, testBeer.getId())
                        .with(jwtRequestPostProcessor)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBeer)))
                .andExpect(status().isNoContent());

        verify(beerService, times(1)).updateBeerById(any(UUID.class), any(BeerDTO.class));
    }

    @Test
    void testDeleteBeer() throws Exception {
        BeerDTO testBeer = beerServiceImpl.getAllBeers(null, null, false, 1, 25).getContent().getFirst();
        given(beerService.deleteBeerById(any(UUID.class))).willReturn(true);
        mockMvc.perform(delete(BEER_PATH_ID, testBeer.getId())
                        .with(jwtRequestPostProcessor)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(beerService, times(1)).deleteBeerById(captorUUID.capture());
        assertThat(testBeer.getId()).isEqualTo(captorUUID.getValue());
    }

    @Test
    void testPatchBeer() throws Exception {
        BeerDTO testBeer = beerServiceImpl.getAllBeers(null, null, false, 1, 25).getContent().getFirst();
        Map<String, Object> beerMap = new HashMap<>();
        beerMap.put("beerName", "UpdatedBeerName");
        given(beerService.updateBeerPartiallyById(any(UUID.class), any(BeerDTO.class))).willReturn(Optional.of(testBeer));
        mockMvc.perform(patch(BEER_PATH_ID, testBeer.getId())
                        .with(jwtRequestPostProcessor)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerMap))
                )
                .andExpect(status().isNoContent());
        verify(beerService, times(1)).updateBeerPartiallyById(captorUUID.capture(), captorBeer.capture());
        assertThat(testBeer.getId()).isEqualTo(captorUUID.getValue());
        assertThat(beerMap.get("beerName")).isEqualTo(captorBeer.getValue().getBeerName());
    }

    @Test
    void testCreateNewBeerWhenRequiredFieldsAreMissing() throws Exception {
        BeerDTO beerDTO = BeerDTO.builder().build();
        given(beerService.saveBeer(any(BeerDTO.class))).willReturn(beerServiceImpl.getAllBeers(null, null, false, 1, 25).getContent().get(1));

        mockMvc.perform(post(BEER_PATH)
                        .with(jwtRequestPostProcessor)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(6)));
        verify(beerService, never()).saveBeer(any(BeerDTO.class));
    }

    @Test
    void testUpdateBeerByIdWhenNameBlank() throws Exception {
        BeerDTO testBeer = beerServiceImpl.getAllBeers(null, null, false, 1, 25).getContent().getFirst();
        testBeer.setBeerName("");

        mockMvc.perform(put(BEER_PATH_ID, testBeer.getId())
                        .with(jwtRequestPostProcessor)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBeer)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(1)));

        verify(beerService, never()).updateBeerById(any(UUID.class), any(BeerDTO.class));
    }
}