package com.ics.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
public class Response implements Serializable {
    @Serial
    private static final long serialVersionUID = 2L;
    private Status status;
    private Object data;
    private String message;

    public enum Status {
        SUCCESS,
        ERROR,
        UNAUTHENTICATED
    }
}

