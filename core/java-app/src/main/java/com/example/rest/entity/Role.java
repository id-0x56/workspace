package com.example.rest.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "roles")
public class Role implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", nullable = false, unique = true, length = 32)
    private String name;

    @JsonBackReference
    @ManyToMany(mappedBy = "roles")
    private List<User> users;

    protected Role() {}

    public Role(String name, List<User> users) {
        this.name = name;
        this.users = users;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getUsers() {
        return this.users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        // if (object == null || this.getClass() != object.getClass()) {
        //     return false;
        // }

        // Role role = (Role) object;

        if (!(object instanceof Role role)) {
            return false;
        }

        return Objects.equals(this.getId(), role.getId())
            && Objects.equals(this.getName(), role.getName())
            && Objects.equals(this.getUsers(), role.getUsers());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            this.getId(),
            this.getName(),
            this.getUsers()
        );
    }
}
