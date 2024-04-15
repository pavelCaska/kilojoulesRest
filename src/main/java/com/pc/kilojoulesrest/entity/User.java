package com.pc.kilojoulesrest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;

    private String roles;

    @ToString.Exclude
    @OneToMany(mappedBy = "user")
    private Set<Meal> meals;

    @ToString.Exclude
    @OneToMany(mappedBy = "user")
    private Set<Journal> journals;

}
