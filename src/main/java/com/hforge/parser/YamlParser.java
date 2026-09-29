package com.hforge.parser;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.hforge.config.GeneratorConfig;
import com.hforge.exceptions.ParserException;
import java.io.File;
import java.io.IOException;

public class YamlParser implements Parser {
    private final ObjectMapper mapper;

    public YamlParser() {

        mapper = new ObjectMapper(new YAMLFactory());

        mapper.findAndRegisterModules();

        mapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false
        );
    }

    @Override
    public GeneratorConfig parse(File file) {

        if (file == null) {
            throw new ParserException("Le fichier YAML est null.");
        }

        if (!file.exists()) {
            throw new ParserException("Fichier introuvable : " + file.getAbsolutePath());
        }

        try {

            return mapper.readValue(file, GeneratorConfig.class);

        } catch (IOException e) {

            throw new ParserException(
                    "Erreur lors de la lecture du fichier YAML.",
                    e
            );

        }
    }

}