package com.ics.dtos;


import java.io.Serializable;

/**
 * A generic request object to be sent from a client to the server.
 * All DTOs used as a payload must be Serializable.
 */
public class Request implements Serializable {
    private static final long serialVersionUID = 1L; // Recommended for Serializable classes

    private String type;
    private Object payload;

    // Constructors, getters, and setters

    public Request(String type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}