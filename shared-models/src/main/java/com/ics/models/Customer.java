package com.ics.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@Embeddable
public class Customer {
    private String customer_name;
    private String customer_phone_number;
}
