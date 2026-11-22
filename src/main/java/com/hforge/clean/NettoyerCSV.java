package com.hforge.clean;

import com.hforge.config.GeneratorOptions;
import com.hforge.exceptions.ExtractorException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NettoyerCSV {

     static Path fichierEntree ;
    public NettoyerCSV(GeneratorOptions options) {
        File input = options.getInputFile();

        if (input == null) {
            throw new ExtractorException(
                    "Option --input manquante (fichier .osm.pbf).");
        }

        if (!input.isFile()) {
            throw new ExtractorException(
                    "Fichier introuvable : " + input.getAbsolutePath());
        }
        this.fichierEntree = Path.of(input.getPath());
    }

    public  void nettoyer() throws IOException {



        Path fichierSortie = Paths.get("mosquees_nettoyees.csv");

        // Colonnes à supprimer
        Set<String> colonnesASupprimer = Set.of(
                "osm_type","osm_id","name_fr","name_en","name_ar",
                "alt_name","denomination","building","street","house_number",
                "postcode","phone","website","opening_hours","wikidata","osm_url"
        );

        try (
                Reader reader = Files.newBufferedReader(
                        fichierEntree, StandardCharsets.UTF_8
                );
                CSVParser parser = CSVFormat.DEFAULT.builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .get()
                        .parse(reader);

                Writer writer = Files.newBufferedWriter(
                        fichierSortie, StandardCharsets.UTF_8
                );
                CSVPrinter printer = new CSVPrinter(
                        writer, CSVFormat.DEFAULT
                )
        ) {
            // Garder uniquement les headers souhaités
            List<String> headers = new ArrayList<>();

            for (String header : parser.getHeaderNames()) {
                if (!colonnesASupprimer.contains(header)) {
                    headers.add(header);
                }
            }

            // Écrire le nouvel en-tête
            printer.printRecord(headers);

            // Parcourir chaque ligne
            for (CSVRecord record : parser) {
                List<String> valeurs = new ArrayList<>();

                for (String header : headers) {
                    valeurs.add(record.get(header));
                }

                printer.printRecord(valeurs);
            }
        }

        System.out.println(
                "CSV nettoyé : " + fichierSortie.toAbsolutePath()
        );
    }
}
