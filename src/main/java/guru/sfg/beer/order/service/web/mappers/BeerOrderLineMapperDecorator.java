package guru.sfg.beer.order.service.web.mappers;

import guru.sfg.beer.order.service.domain.BeerOrderLine;
import guru.sfg.beer.order.service.services.Beer.BeerService;
import guru.sfg.beer.order.service.services.model.Beer;
import guru.sfg.brewery.model.BeerOrderLineDto;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BeerOrderLineMapperDecorator implements BeerOrderLineMapper {

    private BeerOrderLineMapper beerOrderLineMapper;

    private BeerService beerService;

    @Autowired
    public void setBeerOrderLineMapper(BeerOrderLineMapper beerOrderLineMapper) {
        this.beerOrderLineMapper = beerOrderLineMapper;
    }
    @Autowired
    public void setBeerService(BeerService beerService) {
        this.beerService = beerService;
    }

    @Override
    public BeerOrderLineDto beerOrderLineToDto(BeerOrderLine line) {
        Beer beerByUpc = beerService.getBeerByUpc(line.getUpc());
        BeerOrderLineDto beerOrderLineDto = beerOrderLineMapper.beerOrderLineToDto(line);
        beerOrderLineDto.setBeerName(beerByUpc.getBeerName());
        beerOrderLineDto.setBeerStyle(beerByUpc.getBeerStyle());
        beerOrderLineDto.setPrice(beerByUpc.getPrice());
        beerOrderLineDto.setBeerId(beerByUpc.getId());
        return beerOrderLineDto;
    }

    @Override
    public BeerOrderLine dtoToBeerOrderLine(BeerOrderLineDto dto) {
        return beerOrderLineMapper.dtoToBeerOrderLine(dto);
    }
}
