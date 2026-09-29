package com.hforge.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Enums {
    private String name;
    private List<String> values = new ArrayList<>();
}
