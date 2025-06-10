package com.ics.distributed_drinks_system.dtos;


import com.ics.distributed_drinks_system.models.Branch;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {

    private Long customerId;

    private Branch branch;

    private List<OrderItemRequest> items;
}

