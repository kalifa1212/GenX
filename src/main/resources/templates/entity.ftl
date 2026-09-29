package ${project.basePackage}.entity;

public class ${entity.name} {

<#list entity.fields as field>

    private ${field.type} ${field.name};

</#list>

}