package guru.sfg.beer.order.service.services.listeners;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.services.BeerOrderManager;
import guru.sfg.brewery.model.events.ValidateOrderRequest;
import guru.sfg.brewery.model.events.ValidateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class ValidationResultListener {

    private final BeerOrderManager beerOrderManager;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE)
    void listen(ValidateOrderResult validateOrderResult){
        log.debug("Received Validation Result beerOrderId {}, is valid: {}", validateOrderResult.getOrderId(), validateOrderResult.getIsValid());
        beerOrderManager.processValidationResult(validateOrderResult.getOrderId(), validateOrderResult.getIsValid());
    }
}
