
package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.dto.TotalReturnsDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.naming.spi.DirStateFactory.Result;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {

  private RestTemplate restTemplate;

  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }


  //TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  //    Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  //    into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  //    clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest

  //CHECKSTYLE:OFF


  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {
        String uri =buildUri(symbol, from, to);
        RestTemplate rest=new RestTemplate();
        TiingoCandle[] c=rest.getForObject(uri, TiingoCandle[].class);
         return Arrays.asList(c);
  }


  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String token=getToken();
    String uri = String.format("https://api.tiingo.com/tiingo/daily/%s/prices?startDate=%s&endDate=%s&token=%s",
        symbol, startDate, endDate, token);
    return uri;
  }


  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate) {
      List<AnnualizedReturn> annualizedReturns = new ArrayList<>();
      // String token = getToken();
      for (PortfolioTrade trade : portfolioTrades) {
          try {
              // Fetch candles (price data) from Tiingo for the trade
              List<Candle> candles = getStockQuote(trade.getSymbol(), trade.getPurchaseDate(), endDate);
              // Calculate buy and sell prices from candles
              Double buyPrice = getOpeningPriceOnStartDate(candles);
              Double sellPrice = getClosingPriceOnEndDate(candles);
              // Calculate annualized return
              AnnualizedReturn annualizedReturn = calculateAnnualizedReturns(endDate, trade, buyPrice, sellPrice);
              // Add to list
              annualizedReturns.add(annualizedReturn);
          } catch (JsonProcessingException e) {
              // Handle the exception (e.g., log it and continue)
              e.printStackTrace();
          }
      }
      // Step 3: Sort annualized returns in descending order based on annualized return
      annualizedReturns.sort(getComparator());
      return annualizedReturns;
  }
  

  private AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate, PortfolioTrade trade,
      Double buyPrice, Double sellPrice) {
        Double totalReturn=(sellPrice-buyPrice)/buyPrice;
        long daysBetween = ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate);
        double yearsBetween = daysBetween / 365.24;
        Double annualizedReturn = Math.pow((1 + totalReturn), (1 / yearsBetween)) - 1;
        return new AnnualizedReturn(trade.getSymbol(),annualizedReturn,totalReturn);
  }


  private Double getClosingPriceOnEndDate(List<Candle> candles) {
    if(candles!=null&&!candles.isEmpty())
    {
     return candles.get(candles.size()-1).getClose();
    }
     return 0.0;
  }


  private Double getOpeningPriceOnStartDate(List<Candle> candles) {
    if(candles!=null&&!candles.isEmpty())
    {
     return candles.get(0).getOpen();
    }
     return 0.0;
  }

  public static String getToken() {
    return "4f155bd4fb3dce68309f523620aadf2384bba707";
  }
}
