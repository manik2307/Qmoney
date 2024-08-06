  package com.crio.warmup.stock;
import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;
public class PortfolioManagerApplication {
  // Read the JSON file and return the list of symbols
  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    File filename = resolveFileFromResources(args[0]);
    ObjectMapper objectMapper = getObjectMapper();
    PortfolioTrade[] trades = objectMapper.readValue(filename, PortfolioTrade[].class);
    List<String> symbols = new ArrayList<>();
    for (PortfolioTrade trade : trades) {
      symbols.add(trade.getSymbol());
    }
    return symbols;
  }
  // Print JSON object to logger
  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }
  // Resolve file from resources
  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }
  // Get ObjectMapper with JavaTimeModule registered
  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }
  // TODO: CRIO_TASK_MODULE_JSON_PARSING
  //  Follow the instructions provided in the task documentation and fill up the correct values for
  //  the variables provided. First value is provided for your reference.
  //  A. Put a breakpoint on the first line inside mainReadFile() which says
  //    return Collections.emptyList();
  //  B. Then Debug the test #mainReadFile provided in PortfoliomanagerApplicationTest.java
  //  following the instructions to run the test.
  //  Once you are able to run the test, perform following tasks and record the output as a
  //  String in the function below.
  //  Use this link to see how to evaluate expressions -
  //  https://code.visualstudio.com/docs/editor/debugging#_data-inspection
  //  1. evaluate the value of "args[0]" and set the value
  //     to the variable named valueOfArgument0 (This is implemented for your reference.)
  //  2. In the same window, evaluate the value of expression below and set it
  //  to resultOfResolveFilePathArgs0
  //     expression ==> resolveFileFromResources(args[0])
  //  3. In the same window, evaluate the value of expression below and set it
  //  to toStringOfObjectMapper.
  //  You might see some garbage numbers in the output. Dont worry, its expected.
  //    expression ==> getObjectMapper().toString()
  //  4. Now Go to the debug window and open stack trace. Put the name of the function you see at
  //  second place from top to variable functionNameFromTestFileInStackTrace
  //  5. In the same window, you will see the line number of the function in the stack trace window.
  //  assign the same to lineNumberFromTestFileInStackTrace
  //  Once you are done with above, just run the corresponding test and
  //  make sure its working as expected. use below command to do the same.
  //  ./gradlew test --tests PortfolioManagerApplicationTest.testDebugValues
  public static List<String> debugOutputs() {
      String valueOfArgument0 = "trades.json";
      String resultOfResolveFilePathArgs0 = "/home/crio-user/workspace/maniksingla6262-ME_QMONEY_V2/qmoney/bin/main/trades.json";
      String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@446293d";
      String functionNameFromTestFileInStackTrace = "PortfolioManagerApplication.mainReadFile(String[])";
      String lineNumberFromTestFileInStackTrace = "";
    return Arrays.asList(valueOfArgument0, resultOfResolveFilePathArgs0, toStringOfObjectMapper, functionNameFromTestFileInStackTrace, lineNumberFromTestFileInStackTrace);
  }
  // Read quotes from the API and return the sorted list of symbols
  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    ObjectMapper objectMapper = getObjectMapper();
    List<PortfolioTrade> trades = Arrays.asList(objectMapper.readValue(resolveFileFromResources(args[0]), PortfolioTrade[].class));
    List<TotalReturnsDto> sortedByValue = mainReadQuotesHelper(args, trades);
    Collections.sort(sortedByValue, Comparator.comparing(TotalReturnsDto::getClosingPrice));
    List<String> stocks = new ArrayList<>();
    for (TotalReturnsDto trd : sortedByValue) {
      stocks.add(trd.getSymbol());
    }
    return stocks;
  }
  // Helper method to read quotes from the API
  private static List<TotalReturnsDto> mainReadQuotesHelper(String[] args, List<PortfolioTrade> trades) {
    RestTemplate restTemplate = new RestTemplate();
    String token = "4f155bd4fb3dce68309f523620aadf2384bba707";
    List<TotalReturnsDto> tests = new ArrayList<>();
    for (PortfolioTrade trade : trades) {
      String uri = prepareUrl(trade,LocalDate.parse(args[1]), token);
      try {
        TiingoCandle[] results = restTemplate.getForObject(uri, TiingoCandle[].class);
        if (results != null && results.length > 0) {
            tests.add(new TotalReturnsDto(trade.getSymbol(), results[results.length - 1].getClose()));
        } else {
            throw new RuntimeException("Invalid or empty response for stock: " + trade.getSymbol());
        }
    } catch (Exception e) {
        throw new RuntimeException("Error fetching data for stock: " + trade.getSymbol(), e);
    }
    }
    return tests;
  }
  // Prepare URL for the API request
  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
    String uri = String.format("https://api.tiingo.com/tiingo/daily/%s/prices?startDate=%s&endDate=%s&token=%s",
        trade.getSymbol(), trade.getPurchaseDate(), endDate, token);
    return uri;
  }
  // Read trades from JSON file
  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
    ObjectMapper objectMapper = getObjectMapper();
    PortfolioTrade[] trades = objectMapper.readValue(resolveFileFromResources(filename), PortfolioTrade[].class);
    return Arrays.asList(trades);
  }
  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Now that you have the list of PortfolioTrade and their data, calculate annualized returns
  //  for the stocks provided in the Json.
  //  Use the function you just wrote #calculateAnnualizedReturns.
  //  Return the list of AnnualizedReturns sorted by annualizedReturns in descending order.
  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.
 // TODO:
  //  Ensure all tests are passing using below command
  //  ./gradlew test --tests ModuleThreeRefactorTest
  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    if(candles!=null&&!candles.isEmpty())
    {
     return candles.get(0).getOpen();
    }
     return 0.0;
  }
 public static Double getClosingPriceOnEndDate(List<Candle> candles) {
    if(candles!=null&&!candles.isEmpty())
    {
     return candles.get(candles.size()-1).getClose();
    }
     return 0.0;
  }
  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
    String uri =prepareUrl(trade, endDate, token);
    RestTemplate rest=new RestTemplate();
    TiingoCandle[] c=rest.getForObject(uri, TiingoCandle[].class);
     return Arrays.asList(c);
  }
  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException {
       // Step 1: Read portfolio trades from JSON file
    List<PortfolioTrade> trades = readTradesFromJson(args[0]);
    LocalDate endDate = LocalDate.parse(args[1]);
    // Step 2: Prepare to fetch closing prices for each trade
    List<AnnualizedReturn> annualizedReturns = new ArrayList<>();
    String token = getToken();
    for (PortfolioTrade trade : trades) {
        // Fetch candles (price data) from Tiingo for the trade
        List<Candle> candles = fetchCandles(trade, endDate, token);
        // Calculate buy and sell prices from candles
        Double buyPrice = getOpeningPriceOnStartDate(candles);
        Double sellPrice = getClosingPriceOnEndDate(candles);
        // Calculate annualized return
        AnnualizedReturn annualizedReturn = calculateAnnualizedReturns(endDate, trade, buyPrice, sellPrice);
        // Add to list
        annualizedReturns.add(annualizedReturn);
    }
    // Step 3: Sort annualized returns in descending order based on annualized return
    annualizedReturns.sort(Comparator.comparingDouble(AnnualizedReturn::getAnnualizedReturn).reversed());
    return annualizedReturns;
  }
  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Return the populated list of AnnualizedReturn for all stocks.
  //  Annualized returns should be calculated in two steps:
  //   1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
  //      1.1 Store the same as totalReturns
  //   2. Calculate extrapolated annualized returns by scaling the same in years span.
  //      The formula is:
  //      annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  //      2.1 Store the same as annualized_returns
  //  Test the same using below specified command. The build should be successful.
  //     ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn
  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {
      Double totalReturn=(sellPrice-buyPrice)/buyPrice;
      long daysBetween = ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate);
      double yearsBetween = daysBetween / 365.24;
      Double annualizedReturn = Math.pow((1 + totalReturn), (1 / yearsBetween)) - 1;
      return new AnnualizedReturn(trade.getSymbol(),annualizedReturn,totalReturn);
  }
  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());
  //  printJsonObject(mainReadFile(args));
    printJsonObject(mainCalculateSingleReturn(args));
    printJsonObject(mainCalculateReturnsAfterRefactor(args));
  }
  public static String getToken() {
    return "4f155bd4fb3dce68309f523620aadf2384bba707";
  }
  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Once you are done with the implementation inside PortfolioManagerImpl and
  //  PortfolioManagerFactory, create PortfolioManager using PortfolioManagerFactory.
  //  Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  //  call the newly implemented method in PortfolioManager to calculate the annualized returns.

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.
  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
        String file = args[0];
        LocalDate endDate = LocalDate.parse(args[1]);
        
        List<PortfolioTrade> trades = readTradesFromJson(file);
        
        // Create an instance of RestTemplate
        RestTemplate restTemplate = new RestTemplate();
        
        // Create an instance of PortfolioManager using PortfolioManagerFactory
        PortfolioManager portfolioManager = PortfolioManagerFactory.getPortfolioManager(restTemplate);
        
        // Call the instance method calculateAnnualizedReturn
        return portfolioManager.calculateAnnualizedReturn(trades, endDate);
  }
  private static String readFileAsString(String file) {
    return null;
  }
}


