package in.chester.moneymanager.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FilterDTO {

    private LocalDate startDate;
    private LocalDate endDate;
    private String keyword;
    private String type;
    private String sortField; // date, amount, name
    private String sortOrder; // asc, desc
}
