package com.ics.models;

import java.io.Serializable;

public enum OrderStatus implements Serializable {
    PENDING,
    COMPLETED,
    CANCELLED,
}
