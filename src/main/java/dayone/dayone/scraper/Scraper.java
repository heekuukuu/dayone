package dayone.dayone.scraper;

import dayone.dayone.model.ScrapedResult;
import dayone.dayone.model.Company;

public interface Scraper {
    Company scrapCompanyByTicker(String ticker);

    ScrapedResult scrap(Company company);




}
