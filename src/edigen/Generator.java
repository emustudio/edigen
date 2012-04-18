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
package edigen;

import edigen.tree.TreeNode;
import edigen.util.Template;
import java.io.*;

/**
 * A generator transforms an abstract syntax tree into an output language.
 * @author Matúš Sulír
 */
public abstract class Generator {
    
    private String defaultTemplate;
    private String className;
    private String templateFile;
    private String packageName;
    private String outputDirectory;
    private PrintStream debugStream;

    /**
     * Constructs the part of the generator.
     * @param defaultTemplate the path in the JAR file to the template used if
     *        no specific template is configured
     */
    protected Generator(String defaultTemplate, String className) {
        this.defaultTemplate = defaultTemplate;
        this.className = className;
    }
    
    /**
     * Transforms the AST to the form suitable for code generation.
     * @throws SemanticException when a semantic error occurs
     */
    protected abstract void transform() throws SemanticException;
    
    /**
     * Sets the template file to use instead of the default one.
     * @param templateFile the template path
     */
    public void setTemplateFile(String templateFile) {
        this.templateFile = templateFile;
    }

    /**
     * Returns the name of the package to which the generated class will belong.
     * @return the package name (using the dot notation)
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Sets the package to which the generated class will belong.
     * @param packageName the package name (using the dot notation)
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Sets the directory to write the generated file to.
     * @param outputDirectory the output directory name
     */
    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
    
    /**
     * Sets the stream where the debugging information will be printed.
     * 
     * Setting this to null disables debugging.
     * @param debugStream the debug stream
     */
    public void setDebugStream(PrintStream debugStream) {
        this.debugStream = debugStream;
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
     */
    protected void fillTemplate(Template template) {
        template.setVariable("auto_gen_warning",
                "/* Auto-generated file. Do not modify. */");
        
        if (packageName != null)
            template.setVariable("package_spec", "package " + packageName + ";");
        else
            template.setVariable("package_spec", "");
    }
    
    /**
     * Dumps the current AST if debugging is turned on.
     * @param rootNode the node where to start dumping
     */
    protected void dump(TreeNode rootNode) {
        if (debugStream != null)
            rootNode.dump(debugStream);
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
        String outputFile = className + ".java";
        File outputPath;

        if (outputDirectory != null)
            outputPath = new File(outputDirectory, outputFile);
        else
            outputPath = new File(outputFile);
        
        return new BufferedWriter(new FileWriter(outputPath));
    }
}
