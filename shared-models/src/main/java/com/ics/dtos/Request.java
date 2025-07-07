package com.ics.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
public class Request implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String type;
    private Object payload;
}