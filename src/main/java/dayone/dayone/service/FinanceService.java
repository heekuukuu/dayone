package dayone.dayone.service;


import dayone.dayone.excepion.impl.NoCompanyException;
import dayone.dayone.model.Company;
import dayone.dayone.model.Dividend;
import dayone.dayone.model.ScrapedResult;
import dayone.dayone.model.constants.CacheKey;
import dayone.dayone.persist.CompanyRepository;
import dayone.dayone.persist.DividendRepository;
import dayone.dayone.persist.entity.CompanyEntity;
import dayone.dayone.persist.entity.DividendEntity;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor

public class FinanceService {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    //요청이 자주 들어오는가?
    // 자주 변경되는 데이터 인가?
   @Cacheable(key ="#companyName",value = CacheKey.KEY_FINANCE) //
   public ScrapedResult getDividendByCompanyName(String companyName){


   // 1.회사명 기준으로 회사정보 조회

    CompanyEntity company = this.companyRepository.findByName(companyName)
       .orElseThrow(() -> new NoCompanyException());

      // 2.조회된 회사 ID로 배당금 정보조회
       List<DividendEntity> dividedEntities = this.dividendRepository.findAllByCompanyId(company.getId());

      // 3.결과 조합후 반환
       List<Dividend> dividends = dividedEntities.stream()
               .map(e -> new Dividend(e.getDate(), e.getDividend()))
               .collect(Collectors.toList());


       return new ScrapedResult(new Company(company.getTicker(), company.getName()),
               dividends);
   }
}