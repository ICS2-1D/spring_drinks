package com.ics.dtos;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the sales report for a SINGLE branch.
 * Contains the total sales for the branch and a map of drinks sold.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesReportDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private double totalSales;
    private Map<String, DrinkSale> drinksSold;

    /**
     * Inner class to hold details about a single type of drink sold.
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DrinkSale implements Serializable, Comparable<DrinkSale> {
        @Serial
        private static final long serialVersionUID = 1L;
        private int quantity;
        private double totalPrice;

        @Override
        public int compareTo(DrinkSale other) {
            return Double.compare(other.totalPrice, this.totalPrice); // For descending order
        }
    }
}
