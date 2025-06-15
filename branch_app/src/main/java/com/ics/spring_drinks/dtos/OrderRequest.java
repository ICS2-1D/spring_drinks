package com.ics.spring_drinks.dtos;


import com.ics.spring_drinks.models.Branch;
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

