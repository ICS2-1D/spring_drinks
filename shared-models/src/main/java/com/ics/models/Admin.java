package com.ics.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "admins")
public class Admin implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    private long id;
    private String username;
    private String password;
    private LocalDateTime last_Login;
}
