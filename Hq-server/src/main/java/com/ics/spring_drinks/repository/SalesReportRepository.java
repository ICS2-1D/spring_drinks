package com.ics.spring_drinks.repository;

import com.ics.dtos.RawSalesRow;
import com.ics.dtos.SalesReportDto;
import com.ics.models.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesReportRepository extends JpaRepository<OrderItem, Long> {
    @Query(value = """
    SELECT 
        d.drink_name AS drinkName,
        SUM(oi.quantity) AS totalUnitsSold,
        SUM(oi.total_price) AS totalSales
    FROM payments p
    JOIN orders o ON p.order_id = o.order_id
    JOIN order_items oi ON o.order_id = oi.order_id
    JOIN drinks d ON oi.drink_id = d.id
    WHERE p.payment_status = 'SUCCESS'
    GROUP BY d.drink_name
    ORDER BY totalSales DESC
    """, nativeQuery = true)
    List<RawSalesRow> getSalesData();

}

