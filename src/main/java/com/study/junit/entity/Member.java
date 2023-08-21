package com.study.junit.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(of = {"id", "name"})
@Entity
public class Member {
    @Id
    private Long id;
    private String name;
}
