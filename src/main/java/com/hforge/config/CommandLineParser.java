package com.hforge.config;

import java.io.File;
import java.util.Arrays;

public class CommandLineParser {

    public GeneratorOptions parse(String[] args) {

        GeneratorOptions options = new GeneratorOptions();

        for (int i = 0; i < args.length; i++) {

            switch (args[i]) {

                case "--config" -> options.setConfigFile(
                        new File(args[++i])
                );

                case "--type" ->
                        options.getGenerators().addAll(
                                Arrays.asList(args[++i].split(","))
                        );

                case "--input" -> options.setInputFile(
                        new File(args[++i])
                );

                case "--output" -> options.setOutputFile(
                        new File(args[++i])
                );

                case "--no-bbox" ->
                        options.setBboxFilter(false);

                case "--verbose" ->
                        options.setVerbose(true);

                case "--dry-run" ->
                        options.setDryRun(true);

                case "--overwrite" ->
                        options.setOverwrite(Boolean.parseBoolean(args[++i]));
            }

        }

        if (options.getGenerators().isEmpty()) {
            options.getGenerators().add("all");
        }

        return options;

    }

}
