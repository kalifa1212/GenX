package com.hforge.generator;

import com.hforge.config.GeneratorConfig;
import com.hforge.generator.engine.TemplateEngine;
import com.hforge.model.Entity;
import com.hforge.model.Enums;

import java.util.HashMap;
import java.util.Map;

public class EnumsGenerator extends AbstractGenerator{
    public EnumsGenerator(TemplateEngine templateEngine) {
        super(templateEngine);
    }

    @Override
    public void generate(GeneratorConfig config) {

        for(Enums enums : config.getEnums()) {

            Map<String,Object> model = new HashMap<>();

            model.put("project", config.getProject());
            model.put("enum", enums);

            String output =
                    config.getProject().getOutputDirectory()
                            + "/enums/"
                            + enums.getName()
                            + ".java";

            templateEngine.render(
                    "enums.ftl",
                    model,
                    output
            );

        }

    }
}
