package com.study.junit.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString(of = {"id", "name"})
@Entity
public class Member {
    @Id
    private Long id;
    private String name;
}
