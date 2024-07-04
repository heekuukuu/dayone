package dayone.dayone.scraper;

import dayone.dayone.model.Company;
import dayone.dayone.model.Dividend;
import dayone.dayone.model.ScrapedResult;
import dayone.dayone.model.constants.Month;
import dayone.dayone.persist.CompanyRepository;
import dayone.dayone.persist.DividendRepository;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Component
public class YahooFinanceScraper implements Scraper{

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private DividendRepository dividendRepository;

     private static final String STATISTICS_URL =
             "https://finance.yahoo.com/quote/%s/history/?frequency=1mo&period1=%d&period2=%d";
       // "https://finance.yahoo.com/quote/%s/history/?frequency=1mo&period1=%d&period2=%d";
 //  private static final String STATISTICS_URL = "https://finance.yahoo.com/quote/COKE/history/?frequency=1mo&period1=99153000&period2=1719415309";

    private static final String SUMMARY_URL =
            "https://finance.yahoo.com/quote/%s?p=%s";

    private  static final long START_TIME =86400; // 60 * 60 = 24


     @Override
    public ScrapedResult scrap(Company company) {
        var scrapResult = new ScrapedResult();
        scrapResult.setCompany(company);

            try {
                long now = System.currentTimeMillis() / 1000;

                String url = String.format(STATISTICS_URL, company.getTicker(), START_TIME, now);
                Connection connection = Jsoup.connect(url);
                Document document = connection.get();

                //Elements parsingDivs = document.getElementsByClass("table svelte-ewueuo");
                Elements parsingDivs = document.getElementsByAttributeValue("data-test", "historical-prices");
                Element tableEle = parsingDivs.get(0);// table 전체

                Element tbody = tableEle.children().get(1);

                List<Dividend> dividends = new ArrayList<>();
                for (Element e : tbody.children()) {
                    String txt = e.text();
                    if (!txt.endsWith("Dividend")) {
                        continue;
                    }
                    String[] splits = txt.split(" ");
                    int month = Month.strToNumber(splits[0]);
                    int day = Integer.valueOf(splits[1].replace(",", ""));
                    int year = Integer.valueOf(splits[2]);
                    String dividend = splits[3];

                    if (month < 0 ){
                        throw new RuntimeException("Unexpected Month enum value -> " + splits[0]);
                    }
                    dividends.add(new Dividend(LocalDateTime.of(year, month, day, 0, 0), dividend));

                }

                     scrapResult.setDividends(dividends);
            } catch (IOException e) {
                e.printStackTrace();
            }
        return scrapResult;
    }

      @Override
      public Company scrapCompanyByTicker(String ticker){
          String url = String.format(SUMMARY_URL, ticker, ticker);

          try {
              Document document = Jsoup.connect(url).get();
              Element titleEle = document.getElementsByTag("h1").get(0);
              String title = titleEle.text().split(" - ")[1].trim();
              //데이터 특성에따라 조작
              return new Company(ticker, title);
          } catch (IOException e) {
            e.printStackTrace();
          }

          return null;
      }
}


