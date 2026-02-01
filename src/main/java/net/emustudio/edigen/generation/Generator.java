/* SPDX-FileCopyrightText: 2011-2026 Matúš Sulír, Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen.generation;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.misc.Template;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * An output code generator.
 */
public abstract class Generator {

    private final String defaultTemplate;
    private final String name;
    private String templateFile;
    private String outputDirectory;

    /**
     * Constructs the part of the generator.
     *
     * @param defaultTemplate the path in the JAR file to the template used if
     *                        no specific template is configured
     * @param name            the name of the package + class
     */
    protected Generator(String defaultTemplate, String name) {
        this.defaultTemplate = defaultTemplate;
        this.name = name;
    }

    /**
     * Returns the package name.
     *
     * @return the package name (using the dot notation)
     */
    public String getPackageName() {
        int dotIndex = name.lastIndexOf('.');

        if (dotIndex == -1)
            return "edigen.cpu";
        else
            return name.substring(0, dotIndex);
    }

    /**
     * Returns the class name (without the package name).
     *
     * @return the class name
     */
    public String getClassName() {
        int dotIndex = name.lastIndexOf('.');

        if (dotIndex == -1)
            return name;
        else
            return name.substring(dotIndex + 1);
    }

    /**
     * Sets the template file to use instead of the default one.
     *
     * @param templateFile the template path
     */
    public void setTemplateFile(String templateFile) {
        this.templateFile = templateFile;
    }

    /**
     * Sets the directory to write the generated file to.
     *
     * @param outputDirectory the output directory name
     */
    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    /**
     * Generates the output file from the current AST.
     *
     * @throws IOException       when the file can not be read / written
     * @throws SemanticException when there is a semantic error in the input file
     */
    public void generate() throws IOException, SemanticException {
        try (BufferedReader templateReader = openTemplate(); BufferedWriter outputWriter = openOutput()) {
            Template template = new Template(templateReader, outputWriter);
            fillTemplate(template);
            template.write();
        }
    }

    /**
     * Sets the variables used in the template file.
     *
     * @param template the template object
     * @throws SemanticException on template filling / code generation failure
     */
    protected void fillTemplate(Template template) throws SemanticException {
        template.setVariable("auto_gen_warning",
                "/* Auto-generated file. Do not modify. */");
    }

    /**
     * Opens the reader of the template file.
     *
     * @return the template reader
     * @throws FileNotFoundException when the file can not be open
     */
    private BufferedReader openTemplate() throws IOException {
        BufferedReader templateSource;

        if (templateFile != null) {
            templateSource = Files.newBufferedReader(new File(templateFile).toPath(), StandardCharsets.UTF_8);
        } else {
            InputStream stream = getClass().getResourceAsStream(defaultTemplate);
            templateSource = new BufferedReader(new InputStreamReader(stream));
        }

        return templateSource;
    }

    /**
     * Opens the writer of the output file.
     *
     * @return the file writer
     * @throws IOException when the file can not be open for writing
     */
    private BufferedWriter openOutput() throws IOException {
        String outputFile = getClassName() + ".java";
        File outputPath;

        if (outputDirectory != null)
            outputPath = new File(outputDirectory, outputFile);
        else
            outputPath = new File(outputFile);

        return new BufferedWriter(new FileWriter(outputPath));
    }
}
