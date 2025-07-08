package com.ics.dtos;

import com.ics.models.Branch;
import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a consolidated report containing sales data from ALL branches.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsolidatedSalesReportDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private double grandTotalSales;

    private Map<Branch, SalesReportDto> salesByBranch;
}
