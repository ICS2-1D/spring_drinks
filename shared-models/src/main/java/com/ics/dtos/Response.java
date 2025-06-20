package com.ics.dtos;



import java.io.Serializable;

/**
 * A generic response object sent from the server back to the client.
 */
public class Response implements Serializable {
    private static final long serialVersionUID = 2L;

    private Status status;
    private Object data;
    private String message;

    public enum Status {
        SUCCESS,
        ERROR,
        UNAUTHENTICATED
    }

    // Constructors, getters, and setters

    public Response(Status status, Object data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

