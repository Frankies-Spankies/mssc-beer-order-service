package guru.sfg.beer.order.service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import guru.sfg.beer.order.service.domain.*;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.repositories.CustomerRepository;
import guru.sfg.beer.order.service.services.Beer.BeerServiceImpl;
import guru.sfg.brewery.model.BeerDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.github.jenspiegsa.wiremockextension.ManagedWireMockServer.with;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@Slf4j
@TestPropertySource(properties = "app.scheduling.enable=false")
@ExtendWith(WireMockExtension.class)
@SpringBootTest
public class BeerOrderManagerImplIT {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    WireMockServer wireMockServer;

    @SpyBean
    BeerOrderManager beerOrderManager;

    @Autowired
    BeerOrderRepository beerOrderRepository;


    @Autowired
    CustomerRepository customerRepository;

    Customer testCustomer;

    UUID beerId = UUID.randomUUID();

    @TestConfiguration
    static class RestTemplateBuilderProvider {
        @Bean(destroyMethod = "stop")
        public WireMockServer wireMockServer() {
            WireMockServer server = with(wireMockConfig().port(8083));
            server.start();
            return server;
        }
    }

    @BeforeEach
    void setUp() {
        testCustomer = customerRepository.save(Customer.builder()
                .customerName("Test Customer")
                .build());
    }



    /**
    *En este caso se ocupa wiremock y no mockito, ya que se hace un Integration test, por lo que no queremos "simular" una respuesta
    *por parte de este servicio, sin embargo si se nescesita una respuesta de un servicio externo en esta prueba
    */
    @DirtiesContext
    @RepeatedTest(10)
    void testNewToAllocated() throws JsonProcessingException {
        log.debug("*Start*");
        BeerDto mockBeerDto = BeerDto.builder().upc("12345").build();

        wireMockServer.stubFor(
                get(BeerServiceImpl.BEER_SERVICE+"12345")
                        .willReturn(okJson(objectMapper.writeValueAsString(mockBeerDto)))
        );

        BeerOrder beerOrder = createBeerOrder();

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);


        await().untilAsserted(() -> {
            //verify(beerOrderManager).sendBeerOrderEvent(any(),eq(BeerOrderEventEnum.ALLOCATE_ORDER));
            verify(beerOrderManager).beerOrderAllocationPassed(any());
        });

        BeerOrder savedBeerOrder2 = beerOrderRepository.findById(savedBeerOrder.getId()).get();

        savedBeerOrder2.getBeerOrderLines().forEach(line -> {
            assertEquals(line.getOrderQuantity(), line.getQuantityAllocated());
        });




/*        Thread.sleep(1000);
        verify(beerOrderManager).processValidationResult(any(),any());

        BeerOrder savedBeerOrder2 = beerOrderRepository.findById(savedBeerOrder.getId()).get();

        assertNotNull(savedBeerOrder);
        assertEquals(BeerOrderStatusEnum.ALLOCATED, savedBeerOrder2.getOrderStatus());*/
    }

    @Test
    void testNewToPickedUp() throws JsonProcessingException {
        BeerDto beerDto = BeerDto.builder().id(beerId).upc("12345").build();

        wireMockServer.stubFor(get(BeerServiceImpl.BEER_SERVICE + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

        BeerOrder beerOrder = createBeerOrder();

        BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.ALLOCATED, foundOrder.getOrderStatus());
        });

        beerOrderManager.beerOrderPickedUp(savedBeerOrder.getId());

        await().untilAsserted(() -> {
            BeerOrder foundOrder = beerOrderRepository.findById(beerOrder.getId()).get();
            assertEquals(BeerOrderStatusEnum.PICKED_UP, foundOrder.getOrderStatus());
        });

        BeerOrder pickedUpOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();

        assertEquals(BeerOrderStatusEnum.PICKED_UP, pickedUpOrder.getOrderStatus());
    }

    public BeerOrder createBeerOrder(){
        BeerOrder beerOrder = BeerOrder.builder()
                .customer(testCustomer)
                .build();

        Set<BeerOrderLine> lines = new HashSet<>();
        lines.add(BeerOrderLine.builder()
                .beerId(beerId)
                .upc("12345")
                .orderQuantity(1)
                .beerOrder(beerOrder)
                .build());

        log.debug("Order lines started: ");
        lines.forEach(beerOrderLine -> log.debug("beerOrderLine getOrderQuantity(): {}, getQuantityAllocated(): {}", beerOrderLine.getOrderQuantity(), beerOrderLine.getQuantityAllocated()));

        beerOrder.setBeerOrderLines(lines);

        return beerOrder;
    }
}
