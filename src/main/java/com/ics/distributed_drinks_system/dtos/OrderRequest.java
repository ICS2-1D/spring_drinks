package com.ics.distributed_drinks_system.dtos;


import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class OrderRequest {
    private int customerId;
    private int branchId;
    private List<OrderItemRequest> items;
}

