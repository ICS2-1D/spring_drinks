package com.ics.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@RequiredArgsConstructor
@Embeddable
public class Customer implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String customer_name;
    private String customer_phone_number;
}
