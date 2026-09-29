package com.hforge.generator;


import com.hforge.generator.engine.TemplateEngine;
import com.hforge.model.Project;

import java.io.File;
import java.util.Map;

public abstract class AbstractGenerator implements Generator {

    protected final TemplateEngine templateEngine;

    protected AbstractGenerator(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }
    protected void generateFile(
            String template,
            Map<String, Object> model,
            String outputPath
    ) {

        templateEngine.render(
                template,
                model,
                outputPath
        );

    }


    protected String buildPath(
            Project project,
            String folder,
            String fileName
    ) {

        return project.getOutputDirectory()
                + File.separator
                + folder
                + File.separator
                + fileName;

    }


}
