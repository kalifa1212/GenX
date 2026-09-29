package com.hforge.parser;

import com.hforge.config.GeneratorConfig;
import com.hforge.model.Project;

import java.io.File;

public interface Parser {

    GeneratorConfig parse(File file);

}