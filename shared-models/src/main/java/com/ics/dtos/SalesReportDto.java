package com.ics.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesReportDto  implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private double totalSales;
    private Map<String, DrinkSale> drinksSold;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DrinkSale   implements Serializable{
        @Serial
        private static final long serialVersionUID = 1L;
        private int quantity;
        private double totalPrice;
    }
}

