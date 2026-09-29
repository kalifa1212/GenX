package ${project.basePackage}.service;

import ${project.basePackage}.dto.${entity.name}Dto;

import java.util.List;

public interface ${entity.name}Service {

${entity.name}Dto save(${entity.name}Dto dto);

${entity.name}Dto update(${entity.name}Dto dto);

void delete(Long id);

${entity.name}Dto findById(Long id);

List<${entity.name}Dto> findAll();

}