package dayone.dayone.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class ScrapedResult {

    private Company company;


     private List<Dividend> dividends;

    public ScrapedResult() {this.dividends = new ArrayList<>();
}

//    private List<Dividend> dividendEntities;
//
//    public ScrapedResult() {
//        this.dividendEntities = new ArrayList<>();
//    }

}

