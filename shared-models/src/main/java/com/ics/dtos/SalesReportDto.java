package com.ics.dtos;

import com.ics.models.Branch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesReportDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private double grandTotalSales;
    private Map<Branch, SalesReportDto> salesByBranch;

}
