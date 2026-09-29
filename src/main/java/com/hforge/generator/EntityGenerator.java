package com.hforge.generator;

import com.hforge.config.GeneratorConfig;
import com.hforge.generator.engine.TemplateEngine;
import com.hforge.model.Entity;

import java.util.HashMap;
import java.util.Map;

public class EntityGenerator extends AbstractGenerator {

    public EntityGenerator(TemplateEngine templateEngine) {
        super(templateEngine);
    }

    @Override
    public void generate(GeneratorConfig config) {

        for(Entity entity : config.getEntities()) {

            Map<String,Object> model = new HashMap<>();

            model.put("project", config.getProject());
            model.put("entity", entity);

            String output =
                    config.getProject().getOutputDirectory()
                            + "/entity/"
                            + entity.getName()
                            + ".java";

            templateEngine.render(
                    "entity.ftl",
                    model,
                    output
            );

        }

    }

}