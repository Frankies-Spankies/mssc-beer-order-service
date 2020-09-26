package guru.sfg.beer.order.service.services.Beer;

import guru.sfg.beer.order.service.services.model.Beer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@ConfigurationProperties(prefix = "beer.service")
@Component
public class BeerServiceImpl implements BeerService {

    private final RestTemplate restTemplate;
    public static final String BEER_SERVICE="/api/v1/beerUpc/";
    private String host;

    public BeerServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public Beer getBeerByUpc(String upc) {
        String url=host+BEER_SERVICE+upc;
        Beer forObject = restTemplate.getForObject(url, Beer.class, false);
        return forObject;
    }
}
