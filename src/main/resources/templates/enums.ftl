package ${project.basePackage}.enums;

public enum ${enum.name} {

<#list enum.values as value>
    ${value}<#if value_has_next>,<#else>;</#if>
</#list>

}