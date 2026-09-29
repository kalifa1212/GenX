package com.hforge.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Field {

    private String name;

    private String type;

    private boolean nullable = true;

    private boolean unique;

    private boolean id;

    private boolean generated;

    private Integer length;

    private String defaultValue;

    private String description;



    // Getters et Setters

}