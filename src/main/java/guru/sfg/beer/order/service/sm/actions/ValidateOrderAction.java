package guru.sfg.beer.order.service.sm.actions;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.services.BeerOrderManagerImpl;
import guru.sfg.beer.order.service.web.mappers.BeerOrderMapper;
import guru.sfg.brewery.model.events.ValidateOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class ValidateOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final JmsTemplate jmsTemplate;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderMapper beerOrderMapper;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {

        String beerOrder_id = (String) stateContext.getMessage().getHeaders().getOrDefault(BeerOrderManagerImpl.BEER_ORDER_ID_HEADER, -1L);
        BeerOrder beerOrder = beerOrderRepository.getOne(UUID.fromString(beerOrder_id));

        ValidateOrderRequest validateOrderRequest = ValidateOrderRequest.builder()
                .beerOrderDto(beerOrderMapper.beerOrderToDto(beerOrder))
                .build();

        jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER_QUEUE, validateOrderRequest);

        log.debug("ValidateOrderAction executed with beerOrderId: ", beerOrder_id);


    }
}
