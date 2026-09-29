package com.hforge.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Entity {

    private String name;

    private String tableName;

    private String description;

    private List<Field> fields = new ArrayList<>();

    // Getters et Setters

}