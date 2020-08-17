package guru.sfg.beer.order.service.services.Beer;

import guru.sfg.beer.order.service.services.model.Beer;

public interface BeerService {

    Beer getBeerByUpc(String upc);
}
