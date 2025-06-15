package com.ics.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Embeddable
public class Customer {
    private String name;
    private String phoneNumber;
}
