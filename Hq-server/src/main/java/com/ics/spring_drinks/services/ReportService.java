package com.ics.spring_drinks.services;

import com.ics.dtos.SalesReportDto;
import com.ics.dtos.RawSalesRow;
import com.ics.models.Branch;
import com.ics.spring_drinks.repository.SalesReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final SalesReportRepository salesReportRepository;

    /**
     * Builds a consolidated sales report for all branches.
     * @return A DTO containing the grand total and a map of reports for each branch.
     */
    public SalesReportDto buildConsolidatedReport() {
        List<RawSalesRow> allSalesData = salesReportRepository.getSalesData();
        double grandTotal = allSalesData.stream().mapToDouble(RawSalesRow::getTotalSales).sum();

        // Group raw sales data by branch
        Map<Branch, List<RawSalesRow>> salesGroupedByBranch = allSalesData.stream()
                .collect(Collectors.groupingBy(row -> Branch.valueOf(row.getBranch())));

        Map<Branch, SalesReportDto> finalReportMap = new EnumMap<>(Branch.class);

        // For each branch, create an individual SalesReportDto
        salesGroupedByBranch.forEach((branch, rows) -> {
            double branchTotal = rows.stream().mapToDouble(RawSalesRow::getTotalSales).sum();
            Map<String, SalesReportDto.DrinkSale> drinksSold = new LinkedHashMap<>();
            for (RawSalesRow row : rows) {
                drinksSold.put(row.getDrinkName(), new SalesReportDto.DrinkSale(row.getTotalUnitsSold(), row.getTotalSales()));
            }
            finalReportMap.put(branch, new SalesReportDto(branchTotal, drinksSold));
        });

        return new SalesReportDto(grandTotal, finalReportMap);
    }

    /**
     * Builds a sales report for a single, specified branch.
     * @param branch The branch to generate the report for.
     * @return A standard SalesReportDto for the given branch.
     */
    public SalesReportDto buildSalesReportForBranch(Branch branch) {
        // This assumes a repository method that can filter by branch.
        // If not available, you could filter the full list as shown in the consolidated report.
        List<RawSalesRow> branchSalesData = salesReportRepository.getSalesDataByBranch(branch);

        double totalSales = 0;
        Map<String, SalesReportDto.DrinkSale> drinksSold = new LinkedHashMap<>();

        for (RawSalesRow row : branchSalesData) {
            drinksSold.put(row.getDrinkName(), new SalesReportDto.DrinkSale(row.getTotalUnitsSold(), row.getTotalSales()));
            totalSales += row.getTotalSales();
        }

        return new SalesReportDto(totalSales, drinksSold);
    }
}
