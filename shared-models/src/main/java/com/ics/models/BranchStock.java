package com.ics.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "branch_inventory")
@Data
@NoArgsConstructor
public class BranchInventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "branch", nullable = false)
    private Branch branch;

    @Column(name = "drink_id")
    private Long drinkId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "low_stock_threshold", nullable = false)
    private int lowStockThreshold;

    @Column(name = "last_updated")
    private java.sql.Timestamp lastUpdated;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "drink_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Drink drink;
}
