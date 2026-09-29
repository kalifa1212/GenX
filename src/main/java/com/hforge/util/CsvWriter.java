package com.hforge.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Écrivain CSV (RFC 4180) en UTF-8 avec BOM pour une ouverture correcte dans Excel.
 */
public class CsvWriter implements AutoCloseable {

    private static final String EOL = "\r\n";

    private final BufferedWriter writer;

    private final char separator;

    public CsvWriter(File file, char separator) throws IOException {

        File parent = file.getAbsoluteFile().getParentFile();

        if (parent != null) {
            parent.mkdirs();
        }

        this.separator = separator;

        this.writer = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file),
                        StandardCharsets.UTF_8
                )
        );

        // BOM UTF-8
        writer.write('\uFEFF');
    }

    public void writeRow(List<String> values) throws IOException {

        for (int i = 0; i < values.size(); i++) {

            if (i > 0) {
                writer.write(separator);
            }

            writer.write(escape(values.get(i)));
        }

        writer.write(EOL);
    }

    private String escape(String value) {

        if (value == null) {
            return "";
        }

        boolean mustQuote = value.indexOf(separator) >= 0
                || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0
                || value.indexOf('\r') >= 0;

        if (!mustQuote) {
            return value;
        }

        return '"' + value.replace("\"", "\"\"") + '"';
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

}
