package com.ics.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * A generic request object to be sent from a client to the server.
 * All DTOs used as a payload must be Serializable.
 */

@Data
@AllArgsConstructor
public class Request implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L; // Recommended for Serializable classes

    private String type;
    private Object payload;
}