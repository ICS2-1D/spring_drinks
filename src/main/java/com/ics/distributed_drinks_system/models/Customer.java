package com.ics.distributed_drinks_system.models;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Customer {
    private String name;
    private String phoneNumber;
}
