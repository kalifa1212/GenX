package com.hforge.config;

import com.hforge.model.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Data
@NoArgsConstructor
public class GeneratorConfig {

    private Project project;

    private List<Entity> entities = new ArrayList<>();

    private List<Service> services = new ArrayList<>();

    private List<Dto> dtos = new ArrayList<>();
    private List<Enums> enums = new ArrayList<>();
    private List<Validators> validators = new ArrayList<>();
    private List<Mapper> mappers = new ArrayList<>();

}