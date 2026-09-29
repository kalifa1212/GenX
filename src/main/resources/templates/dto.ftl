package ${project.basePackage}.dto;

import lombok.Data;

@Data
public class ${entity.name}Dto {

<#list entity.fields as field>

    private ${field.type} ${field.name};

</#list>

}