package com.hforge.generator.engine;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class FreeMarkerTemplateEngine implements TemplateEngine {


    private final Configuration configuration;


    public FreeMarkerTemplateEngine() {

        configuration = new Configuration(
                Configuration.VERSION_2_3_34
        );

        configuration.setClassLoaderForTemplateLoading(
                getClass().getClassLoader(),
                "templates"
        );

        configuration.setDefaultEncoding("UTF-8");

    }


    @Override
    public void render(
            String templateName,
            Map<String, Object> model,
            String outputFile
    ) {


        try {


            Template template =
                    configuration.getTemplate(templateName);


            File file =
                    new File(outputFile);


            File parent =
                    file.getParentFile();


            if(parent != null){

                parent.mkdirs();

            }


            try(Writer writer =
                        new FileWriter(file)){


                template.process(
                        model,
                        writer
                );

            }


        } catch(IOException | TemplateException e){

            throw new RuntimeException(
                    "Erreur génération fichier : "
                            + outputFile,
                    e
            );

        }

    }
}