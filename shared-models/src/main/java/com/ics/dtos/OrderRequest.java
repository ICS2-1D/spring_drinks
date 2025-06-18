package com.ics.dtos;


import com.ics.models.Branch;
import lombok.Data;
import lombok.RequiredArgsConstructor;


import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@RequiredArgsConstructor
public class OrderRequest  implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long customerId;
    private Branch branch;
    private List<OrderItemRequest> items;
    private String CustomerName;
    private String CustomerPhoneNumber;
}

