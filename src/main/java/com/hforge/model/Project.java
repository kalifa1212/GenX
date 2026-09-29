package com.hforge.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Project {
    private String name;

    private String groupId;

    private String artifactId;

    private String version;

    private String basePackage;

    private String outputDirectory;

    private List<Entity> entities = new ArrayList<>();

}