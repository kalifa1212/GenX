package ${project.basePackage}.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ${project.basePackage}.entity.${entity.name};

public interface ${entity.name}Repository
extends JpaRepository<${entity.name}, UUID>{

}