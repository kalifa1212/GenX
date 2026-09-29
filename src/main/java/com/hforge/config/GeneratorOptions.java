package com.hforge.config;

import lombok.Data;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

@Data
public class GeneratorOptions {

    private File configFile;

    private Set<String> generators = new HashSet<>();

    private boolean overwrite = true;

    private boolean verbose = false;

    private boolean dryRun = false;

    // --- Extracteurs (ex : --type mosque) ---

    /** Fichier source .osm.pbf */
    private File inputFile;

    /** Fichier CSV de sortie (valeur par défaut définie par l'extracteur) */
    private File outputFile;

    /** Limiter les résultats au rectangle englobant du Cameroun */
    private boolean bboxFilter = true;

}
