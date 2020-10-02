package guru.sfg.beer.order.service.services.testcomponets;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.brewery.model.events.AllocateOrderRequest;
import guru.sfg.brewery.model.events.AllocateOrderResult;
import guru.sfg.brewery.model.events.ValidateOrderRequest;
import guru.sfg.brewery.model.events.ValidateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * Created by jt on 2/15/20.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderAllocateListener {
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_QUEUE)
    public void list(AllocateOrderRequest request) {

        boolean pendingInventory = false;
        boolean allocationError = false;

        if (request.getBeerOrderDto().getCustomerRef().equals("partial-allocation")) {
            pendingInventory = true;
        }
        if (request.getBeerOrderDto().getCustomerRef().equals("fail-allocation")) {
            allocationError = true;
        }

        request.getBeerOrderDto().getBeerOrderLines().forEach(beerOrderLineDto -> {
            beerOrderLineDto.setQuantityAllocated(beerOrderLineDto.getOrderQuantity());
        });

        AllocateOrderResult.AllocateOrderResultBuilder builder = AllocateOrderResult.builder();
        builder.beerOrderDto(request.getBeerOrderDto());
        builder.pendingInventory(pendingInventory);
        builder.allocationError(allocationError);


        log.debug("Allocate Listener recibiendo beerOrder: {}", request.getBeerOrderDto().getId());

        jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE,
                builder.build());

    }
}
