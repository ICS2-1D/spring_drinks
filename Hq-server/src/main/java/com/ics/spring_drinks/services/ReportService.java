package com.ics.spring_drinks.services;


import com.ics.dtos.RawSalesRow;
import com.ics.dtos.SalesReportDto;
import com.ics.spring_drinks.repository.SalesReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final SalesReportRepository salesReportRepository;

    public SalesReportDto buildSalesReport() {
        List<RawSalesRow> rawResults = salesReportRepository.getSalesData();
        double totalSales = 0;
        Map<String, SalesReportDto.DrinkSale> drinksSold = new LinkedHashMap<>();

        for (RawSalesRow row : rawResults) {
            String drinkName = row.getDrinkName();
            int quantity = row.getTotalUnitsSold();
            double sales = row.getTotalSales();

            drinksSold.put(drinkName, new SalesReportDto.DrinkSale(quantity, sales));
            totalSales += sales;
        }

        return new SalesReportDto(totalSales, drinksSold);
    }

}
