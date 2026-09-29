package com.hforge.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Service {
    private String name;

    private String entity;

    private List<String> methods = new ArrayList<>();
}