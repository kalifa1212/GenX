package com.hforge.extractor;

import com.hforge.config.GeneratorOptions;
import com.hforge.exceptions.ExtractorException;
import com.hforge.util.CsvWriter;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Squelette commun des extracteurs : validation, lecture, écriture CSV.
 * Les sous-classes décrivent uniquement quoi lire et comment le mettre en colonnes.
 */
public abstract class AbstractExtractor<T> implements Extractor {

    private static final char SEPARATOR = ',';

    protected abstract String defaultOutputFile();

    protected abstract List<T> read(File input, GeneratorOptions options);

    protected abstract List<String> header();

    protected abstract List<String> row(T item);

    @Override
    public void extract(GeneratorOptions options) {

        File input = options.getInputFile();

        if (input == null) {
            throw new ExtractorException(
                    "Option --input manquante (fichier .osm.pbf).");
        }

        if (!input.isFile()) {
            throw new ExtractorException(
                    "Fichier introuvable : " + input.getAbsolutePath());
        }

        File output = options.getOutputFile() != null
                ? options.getOutputFile()
                : new File(defaultOutputFile());

        if (output.exists() && !options.isOverwrite() && !options.isDryRun()) {
            throw new ExtractorException(
                    "Le fichier existe déjà (--overwrite false) : "
                            + output.getAbsolutePath());
        }

        List<T> items = read(input, options);

        if (options.isDryRun()) {
            System.out.println("[dry-run] " + items.size()
                    + " élément(s) trouvé(s), aucun fichier écrit.");
            return;
        }

        try (CsvWriter csv = new CsvWriter(output, SEPARATOR)) {

            csv.writeRow(header());

            for (T item : items) {
                csv.writeRow(row(item));
            }

        } catch (IOException e) {
            throw new ExtractorException(
                    "Erreur écriture CSV : " + output.getAbsolutePath(), e);
        }

        System.out.println(items.size() + " élément(s) exporté(s) vers "
                + output.getAbsolutePath());
    }

}
