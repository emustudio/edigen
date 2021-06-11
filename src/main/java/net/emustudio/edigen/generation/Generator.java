/*
 * Copyright (C) 2012 Matúš Sulír
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.emustudio.edigen.generation;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.misc.Template;

import java.io.*;

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
     * @param defaultTemplate the path in the JAR file to the template used if
     *        no specific template is configured
     * @param name the name of the package + class
     */
    protected Generator(String defaultTemplate, String name) {
        this.defaultTemplate = defaultTemplate;
        this.name = name;
    }
    
    /**
     * Returns the package name.
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
     * @param templateFile the template path
     */
    public void setTemplateFile(String templateFile) {
        this.templateFile = templateFile;
    }

    /**
     * Sets the directory to write the generated file to.
     * @param outputDirectory the output directory name
     */
    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
    
    /**
     * Generates the output file from the current AST.
     * @throws IOException when the file can not be read / written
     * @throws SemanticException when there is a semantic error in the input file
     */
    public void generate() throws IOException, SemanticException {
        BufferedReader templateReader = null;
        BufferedWriter outputWriter = null;
        
        try {
            templateReader = openTemplate();
            outputWriter = openOutput();
            
            Template template = new Template(templateReader, outputWriter);
            fillTemplate(template);
            template.write();
        } finally {
            if (templateReader != null)
                templateReader.close();
            
            if (outputWriter != null)
                outputWriter.close();
        }
    }

    /**
     * Sets the variables used in the template file.
     * @param template the template object
     * @throws SemanticException on template filling / code generation failure
     */
    protected void fillTemplate(Template template) throws SemanticException {
        template.setVariable("auto_gen_warning",
                "/* Auto-generated file. Do not modify. */");
    }
    
    /**
     * Opens the reader of the template file.
     * @return the template reader
     * @throws FileNotFoundException when the file can not be open
     */
    private BufferedReader openTemplate() throws FileNotFoundException {
        BufferedReader templateSource;
        
        if (templateFile != null) {
            templateSource = new BufferedReader(new FileReader(templateFile));
        } else {
            InputStream stream = getClass().getResourceAsStream(defaultTemplate);
            templateSource = new BufferedReader(new InputStreamReader(stream));
        }
        
        return templateSource;
    }
    
    /**
     * Opens the writer of the output file.
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
