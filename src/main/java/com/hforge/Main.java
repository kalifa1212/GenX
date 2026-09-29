package com.hforge;

import com.hforge.config.CommandLineParser;
import com.hforge.config.GeneratorConfig;
import com.hforge.config.GeneratorOptions;
import com.hforge.exceptions.ParserException;
import com.hforge.extractor.MosqueExtractor;
import com.hforge.generator.*;
import com.hforge.generator.engine.FreeMarkerTemplateEngine;
import com.hforge.generator.engine.TemplateEngine;
import com.hforge.parser.YamlParser;

import java.util.Set;


public class Main {

    public static void main(String[] args) {

        CommandLineParser cli = new CommandLineParser();

        GeneratorOptions options = cli.parse(args);

        Set<String> types = options.getGenerators();

        // ---------------- Extracteurs (aucun YAML requis) ----------------

        if (types.contains("mosque")) {

            new MosqueExtractor()
                    .extract(options);

        }

        // ---------------- Générateurs de code (YAML requis) ----------------

        boolean needsConfig = types.contains("all")
                || types.contains("entity")
                || types.contains("repository")
                || types.contains("dto")
                || types.contains("enums")
                || types.contains("controller")
                || types.contains("controllerImpl")
                || types.contains("service")
                || types.contains("serviceImpl");


        if (!needsConfig) {
            return;
        }

        if (options.getConfigFile() == null) {
            throw new ParserException("Option --config manquante.");
        }

        GeneratorConfig config =
                new YamlParser()
                        .parse(options.getConfigFile());

        TemplateEngine engine =
                new FreeMarkerTemplateEngine();

        if(types.contains("all")
                || types.contains("entity")){

            new EntityGenerator(engine)
                    .generate(config);

        }

        if(types.contains("all")
                || types.contains("repository")){

            new RepositoryGenerator(engine)
                    .generate(config);

        }

        if(types.contains("all")
                || types.contains("dto")){

            new DtoGenerator(engine)
                    .generate(config);

        }
        if(types.contains("all")
                || types.contains("enums")){

            new EnumsGenerator(engine)
                    .generate(config);

        }
        //TODO adding new futture
        if(types.contains("all")
                || types.contains("controller")){

            new ControllerGenerator(engine)
                    .generate(config);

        }
        if(types.contains("all")
                || types.contains("controllerImpl")){

            new ControllerImplGenerator(engine)
                    .generate(config);

        }
        if(types.contains("all")
                || types.contains("service")){

            new ServiceGenerator(engine)
                    .generate(config);

        }
        if(types.contains("all")
                || types.contains("serviceImpl")){

            new ServiceImplGenerator(engine)
                    .generate(config);

        }


    }

}
