package com.ics.spring_drinks.repository;

import com.ics.dtos.RawSalesRow;
import com.ics.models.Branch;
import com.ics.models.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesReportRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Retrieves sales data aggregated by drink and branch. This is used for the
     * consolidated report covering all branches.
     * The key change is adding o.branch to the SELECT and GROUP BY clauses.
     *
     * @return A list of RawSalesRow objects, each including the branch.
     */
    @Query(value = """
    SELECT
        d.drink_name AS drinkName,
        o.branch AS branch,
        SUM(oi.quantity) AS totalUnitsSold,
        SUM(oi.total_price) AS totalSales
    FROM payments p
    JOIN orders o ON p.order_id = o.order_id
    JOIN order_items oi ON o.order_id = oi.order_id
    JOIN drinks d ON oi.drink_id = d.id
    WHERE p.payment_status = 'SUCCESS'
    GROUP BY o.branch, d.drink_name
    ORDER BY o.branch, totalSales DESC
    """, nativeQuery = true)
    List<RawSalesRow> getSalesData();


    /**
     * Retrieves sales data for a single, specified branch, aggregated by drink.
     * This is used for generating a report for a specific branch.
     *
     * @param branch The branch to filter the sales data by.
     * @return A list of RawSalesRow objects for the specified branch.
     */
    @Query(value = """
    SELECT
        d.drink_name AS drinkName,
        o.branch AS branch,
        SUM(oi.quantity) AS totalUnitsSold,
        SUM(oi.total_price) AS totalSales
    FROM payments p
    JOIN orders o ON p.order_id = o.order_id
    JOIN order_items oi ON o.order_id = oi.order_id
    JOIN drinks d ON oi.drink_id = d.id
    WHERE p.payment_status = 'SUCCESS'
    AND o.branch = :#{#branch.name()}
    GROUP BY d.drink_name
    ORDER BY totalSales DESC
    """, nativeQuery = true)
    List<RawSalesRow> getSalesDataByBranch(@Param("branch") Branch branch);
}
