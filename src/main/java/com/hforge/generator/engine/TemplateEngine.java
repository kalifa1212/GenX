package com.hforge.generator.engine;

import java.util.Map;

public interface TemplateEngine {

    void render(String template,
                Map<String,Object> model,
                String outputFile);

}