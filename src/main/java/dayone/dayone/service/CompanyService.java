package dayone.dayone.service;


import dayone.dayone.excepion.impl.NoCompanyException;
import dayone.dayone.model.Company;
import dayone.dayone.model.ScrapedResult;
import dayone.dayone.persist.CompanyRepository;
import dayone.dayone.persist.DividendRepository;
import dayone.dayone.persist.entity.CompanyEntity;
import dayone.dayone.persist.entity.DividendEntity;
import dayone.dayone.scraper.Scraper;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import org.apache.commons.collections4.Trie;
import java.util.List;
import java.util.stream.Collectors;

import static org.jsoup.select.Collector.collect;

@AllArgsConstructor
@Service

public class CompanyService {


    private final Trie trie;
    private final Scraper yahooFinanceScraper;
    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    public Company save(String ticker) {
     boolean exists = this.companyRepository.existsByTicker(ticker);
       if (exists){
           throw new RuntimeException("already exists ticker ->" + ticker);
       }
     return this.storeCompanyAndDividend(ticker);
    }

    public Page<CompanyEntity> getAllCompany(Pageable pageable){
        return this.companyRepository.findAll(pageable);

    }


    private Company storeCompanyAndDividend(String ticker){
      //ticker 를 기준으로 회사를 스크래핑
       Company company = this.yahooFinanceScraper.scrapCompanyByTicker(ticker);
        if (ObjectUtils.isEmpty(company)){
            throw new RuntimeException("failed to scrap ticker -> " + ticker);
        }

      // 해당회사가 존재할경우. 회사의배당금 정보를 스크래핑
       ScrapedResult scrapedResult= this.yahooFinanceScraper.scrap(company);

        //스크래핑 결과
      CompanyEntity companyEntity = this.companyRepository.save(new CompanyEntity(company));

      List<DividendEntity>  dividedEntities = scrapedResult.getDividends().stream()
                .map(e -> new DividendEntity(companyEntity.getId(), e))
                .collect(Collectors.toList());

        this.dividendRepository.saveAll(dividedEntities);
        return company;
    }

    public List<String> getCompanyNamesByKeyword(String keyword) {
        Pageable limit = PageRequest.of(0, 10);
        Page<CompanyEntity> companyEntities = this.companyRepository.findByNameStartingWithIgnoreCase(keyword, limit);
        return companyEntities.stream()
                .map(CompanyEntity::getName)
                .collect(Collectors.toList());
    }

    public void addAutocompleteKeyword(String keyword) {
        this.trie.put(keyword, null);
    }

    public List<String> autocomplete(String keyword) {
        return (List<String>) this.trie.prefixMap(keyword).keySet().stream()
                .collect(Collectors.toList());
    }

    public void deleteAutocompleteKeyword(String keyword) {
        this.trie.remove(keyword);
    }

    public String deleteCompany(String ticker) {
      var company = this.companyRepository.findByTicker(ticker)
        .orElseThrow(() -> new NoCompanyException());

      this.dividendRepository.deleteAllByCompanyId(company.getId());
      this.companyRepository.delete(company);

      this.deleteAutocompleteKeyword(company.getName());
      return company.getName();
    }
}