package com.ics.dtos;


import com.ics.models.Branch;
import lombok.Data;
import lombok.RequiredArgsConstructor;


import java.util.List;

@Data
@RequiredArgsConstructor
public class OrderRequest {
    private Long customerId;
    private Branch branch;
    private List<OrderItemRequest> items;
    private String CustomerName;
    private String CustomerPhoneNumber;
}

