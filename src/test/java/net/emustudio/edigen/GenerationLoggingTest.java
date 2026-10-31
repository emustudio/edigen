/* SPDX-FileCopyrightText: 2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.edigen;

import net.emustudio.edigen.parser.ParseException;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.Assert.*;

/** Exercises generation diagnostics through the same entry point used by build plugins. */
public class GenerationLoggingTest {
    private static final String SPEC = "root instruction;\n"
            + "instruction = \"nop\": 0x00;\n"
            + "%%\n\"%s\" = instruction;\n";

    @Rule
    public TemporaryFolder temporary = new TemporaryFolder();

    @Test
    public void normalProgressIdentifiesInputAndCompletedFilesInOrder() throws Exception {
        Fixture fixture = fixture(SPEC);
        RecordingLog log = new RecordingLog(false);

        new Edigen(log).run(fixture.arguments());

        assertEquals(4, log.info.size());
        assertEquals("Generating decoder and disassembler from " + fixture.source, log.info.get(0));
        assertEquals("Wrote decoder example.Decoder to " + fixture.output.resolve("Decoder.java"), log.info.get(1));
        assertEquals("Wrote disassembler example.Disassembler to " + fixture.output.resolve("Disassembler.java"), log.info.get(2));
        assertTrue(log.info.get(3).matches("Generation completed in [0-9]+ ms"));
        assertTrue(log.warnings.isEmpty());
        assertTrue(log.debug.isEmpty());
        assertTrue(Files.size(fixture.output.resolve("Decoder.java")) > 0);
        assertTrue(Files.size(fixture.output.resolve("Disassembler.java")) > 0);
    }

    @Test
    public void hostDebugShowsPassesAndTemplatesWithoutImplicitAstDumps() throws Exception {
        Fixture fixture = fixture(SPEC);
        RecordingLog log = new RecordingLog(true);

        new Edigen(log).run(fixture.arguments());

        assertTrue(log.debug.stream().anyMatch(message -> message.startsWith("Parsed " + fixture.source + " in ")));
        int started = log.debug.indexOf("Running transformation ResolveNamesVisitor");
        int completed = indexStartingWith(log.debug, "Completed transformation ResolveNamesVisitor in ");
        assertTrue(started >= 0);
        assertTrue(completed > started);
        assertTrue(log.debug.contains("Generating example.Decoder using bundled template /Decoder.edt"));
        assertTrue(log.debug.contains("Generating example.Disassembler using bundled template /Disassembler.edt"));
        assertFalse(log.debug.stream().anyMatch(message -> message.startsWith("AST after ")));
    }

    @Test
    public void explicitAstDumpsAreLabeledAndDoNotChangeGeneratedSources() throws Exception {
        Fixture fixture = fixture(SPEC);
        new Edigen(new RecordingLog(false)).run(fixture.arguments());
        byte[] decoder = Files.readAllBytes(fixture.output.resolve("Decoder.java"));
        byte[] disassembler = Files.readAllBytes(fixture.output.resolve("Disassembler.java"));
        RecordingLog log = new RecordingLog(true);

        new Edigen(log).run(fixture.arguments("-d"));

        assertTrue(log.debug.stream().anyMatch(message -> message.startsWith("AST after ResolveNamesVisitor:\n")));
        assertTrue(log.debug.stream().anyMatch(message -> message.startsWith("AST after DetectUnreachableFormatsVisitor:\n")));
        assertArrayEquals(decoder, Files.readAllBytes(fixture.output.resolve("Decoder.java")));
        assertArrayEquals(disassembler, Files.readAllBytes(fixture.output.resolve("Disassembler.java")));
    }

    @Test
    public void missingFormatsProduceOneSortedActionableWarningAndContinue() throws Exception {
        Fixture fixture = fixture("root instruction;\n"
                + "instruction = \"z\": 0x02 z | \"a\": 0x01 a | \"nop\": 0x00;\n"
                + "z = z: z(8);\na = a: a(8);\n%%\n\"%s\" = instruction;\n");
        RecordingLog log = new RecordingLog(false);

        new Edigen(log).run(fixture.arguments());

        assertEquals(List.of(fixture.source + ": No disassembly format covers decoded field combinations "
                + "[a, instruction], [instruction, z]. Add matching formats in the disassembler section. "
                + "Generation will continue."), log.warnings);
        assertTrue(log.info.get(log.info.size() - 1).startsWith("Generation completed in "));
    }

    @Test
    public void parseFailureRetainsSourceDiagnosticAndCauseWithoutSuccess() throws Exception {
        Fixture fixture = fixture("root instruction;\ninstruction = ;\n%%\n\"%s\" = instruction;\n");
        RecordingLog log = new RecordingLog(false);

        ParseException failure = assertThrows(ParseException.class, () -> new Edigen(log).run(fixture.arguments()));

        assertTrue(failure.getMessage().startsWith("Cannot parse specification " + fixture.source + ": "));
        assertTrue(failure.getMessage().contains("line 2"));
        assertTrue(failure.getCause() instanceof ParseException);
        assertEquals(1, log.info.size());
        assertFalse(Files.exists(fixture.output.resolve("Decoder.java")));
    }

    @Test
    public void unexpectedCharacterRetainsParserLocationAndCause() throws Exception {
        Fixture fixture = fixture("root instruction;\ninstruction = @;\n%%\n\"%s\" = instruction;\n");
        RecordingLog log = new RecordingLog(false);

        ParseException failure = assertThrows(ParseException.class, () -> new Edigen(log).run(fixture.arguments()));

        assertTrue(failure.getMessage().startsWith("Cannot parse specification " + fixture.source + ": "));
        assertTrue(failure.getMessage().contains("line 2, column 15"));
        assertTrue(failure.getCause() instanceof ParseException);
        assertTrue(failure.getCause().getMessage().contains("@"));
        assertEquals(1, log.info.size());
    }

    @Test
    public void ignoreUnusedRulesOmitsTheSkippedPassFromDebugMessages() throws Exception {
        Fixture fixture = fixture("root instruction;\ninstruction = \"nop\": 0x00;\n"
                + "unused = \"unused\": 0xFF;\n%%\n\"%s\" = instruction;\n");
        RecordingLog strict = new RecordingLog(true);
        SemanticException failure = assertThrows(SemanticException.class,
                () -> new Edigen(strict).run(fixture.arguments()));
        assertTrue(failure.getMessage().contains("Transformation DetectUnusedRulesVisitor failed"));
        RecordingLog ignored = new RecordingLog(true);

        new Edigen(ignored).run(fixture.arguments("-iu"));

        assertFalse(ignored.debug.stream().anyMatch(message -> message.contains("DetectUnusedRulesVisitor")));
        assertTrue(ignored.debug.contains("Running transformation SemanticCheckVisitor"));
        assertTrue(ignored.info.get(ignored.info.size() - 1).startsWith("Generation completed in "));
    }

    @Test
    public void semanticFailureNamesThePassAndPreservesOriginalLine() throws Exception {
        Fixture fixture = fixture("root instruction;\ninstruction = missing;\n%%\n\"%s\" = instruction;\n");
        RecordingLog log = new RecordingLog(true);

        SemanticException failure = assertThrows(SemanticException.class, () -> new Edigen(log).run(fixture.arguments()));

        assertTrue(failure.getMessage().startsWith(fixture.source + ": Transformation ResolveNamesVisitor failed: "));
        assertTrue(failure.getMessage().contains("Line 2"));
        assertTrue(failure.getCause() instanceof SemanticException);
        assertTrue(log.debug.contains("Running transformation ResolveNamesVisitor"));
        assertEquals(-1, indexStartingWith(log.debug, "Completed transformation ResolveNamesVisitor"));
        assertEquals(1, log.info.size());
    }

    @Test
    public void readFailureNamesTheSpecificationAndPreservesCause() throws Exception {
        Fixture fixture = fixture(SPEC);
        Files.delete(fixture.source);
        RecordingLog log = new RecordingLog(false);

        IOException failure = assertThrows(IOException.class, () -> new Edigen(log).run(fixture.arguments()));

        assertTrue(failure.getMessage().startsWith("Cannot read specification " + fixture.source + ": "));
        assertTrue(failure.getCause() instanceof IOException);
        assertEquals(1, log.info.size());
    }

    @Test
    public void secondOutputFailureKeepsOnlyTheFirstCompletedFileMessage() throws Exception {
        Fixture fixture = fixture(SPEC);
        Files.createDirectory(fixture.output.resolve("Disassembler.java"));
        RecordingLog log = new RecordingLog(false);

        IOException failure = assertThrows(IOException.class, () -> new Edigen(log).run(fixture.arguments()));

        assertTrue(failure.getMessage().startsWith("Cannot generate disassembler example.Disassembler at "
                + fixture.output.resolve("Disassembler.java") + " using bundled template /Disassembler.edt: "));
        assertTrue(failure.getCause() instanceof IOException);
        assertEquals(2, log.info.size());
        assertTrue(log.info.get(1).startsWith("Wrote decoder example.Decoder to "));
    }

    @Test
    public void closeFailureNeverAnnouncesTheFileAsWritten() throws Exception {
        Assume.assumeTrue(Files.exists(Path.of("/dev/full")));
        Fixture fixture = fixture(SPEC);
        Path template = temporary.newFile("tiny.edt").toPath();
        Files.writeString(template, "// tiny buffered output\n");
        Files.createSymbolicLink(fixture.output.resolve("Decoder.java"), Path.of("/dev/full"));
        RecordingLog log = new RecordingLog(false);

        IOException failure = assertThrows(IOException.class,
                () -> new Edigen(log).run(fixture.arguments("-dt", template.toString())));

        assertTrue(failure.getMessage().startsWith("Cannot generate decoder example.Decoder at "
                + fixture.output.resolve("Decoder.java") + " using custom template " + template + ": "));
        assertTrue(failure.getCause() instanceof IOException);
        assertEquals(1, log.info.size());
    }

    @Test
    public void customTemplateSelectionAndMissingTemplateIncludeActualPath() throws Exception {
        Fixture fixture = fixture(SPEC);
        Path template = temporary.newFile("custom.edt").toPath();
        try (InputStream bundled = getClass().getResourceAsStream("/Decoder.edt")) {
            Files.write(template, bundled.readAllBytes());
        }
        RecordingLog log = new RecordingLog(true);
        new Edigen(log).run(fixture.arguments("-dt", template.toString()));
        assertTrue(log.debug.contains("Generating example.Decoder using custom template " + template));

        Files.delete(template);
        RecordingLog failedLog = new RecordingLog(true);
        IOException failure = assertThrows(IOException.class,
                () -> new Edigen(failedLog).run(fixture.arguments("-dt", template.toString())));

        assertTrue(failure.getMessage().startsWith("Cannot read decoder template " + template + ": "));
        assertTrue(failure.getCause() instanceof IOException);
        assertEquals(1, failedLog.info.size());
    }

    @Test
    public void malformedUtf8TemplateFailureIdentifiesTemplateAndDestination() throws Exception {
        Fixture fixture = fixture(SPEC);
        Path template = temporary.newFile("malformed.edt").toPath();
        Files.write(template, new byte[]{(byte) 0xC3, 0x28});
        RecordingLog log = new RecordingLog(false);

        IOException failure = assertThrows(IOException.class,
                () -> new Edigen(log).run(fixture.arguments("-dt", template.toString())));

        assertTrue(failure.getMessage(), failure.getMessage().contains(template.toString()));
        assertTrue(failure.getMessage(), failure.getMessage().contains(fixture.output.resolve("Decoder.java").toString()));
        assertTrue(failure.getCause() instanceof IOException);
        assertEquals(1, log.info.size());
    }

    @Test
    public void consoleFiltersDebugLazilyAndKeepsInvocationsIndependent() {
        ByteArrayOutputStream quietBytes = new ByteArrayOutputStream();
        ConsoleGenerationLog quiet = new ConsoleGenerationLog(new PrintStream(quietBytes), false);
        quiet.debug(() -> { throw new AssertionError("Disabled debug message was rendered"); });
        quiet.info("completed");
        quiet.warn("missing format");

        ByteArrayOutputStream debugBytes = new ByteArrayOutputStream();
        ConsoleGenerationLog debug = new ConsoleGenerationLog(new PrintStream(debugBytes), true);
        debug.debug(() -> "pass detail");
        quiet.debug(() -> { throw new AssertionError("Another invocation enabled quiet logging"); });

        assertEquals("INFO  completed" + System.lineSeparator() + "WARN  missing format" + System.lineSeparator(),
                quietBytes.toString(StandardCharsets.UTF_8));
        assertEquals("DEBUG pass detail" + System.lineSeparator(), debugBytes.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void cliKeepsHelpOnStdoutAndReportsFailuresOnceOnStderr() throws Exception {
        CliResult help = cli();
        assertEquals(0, help.exit);
        assertTrue(help.stdout.startsWith("Usage:"));
        assertEquals("", help.stderr);

        Fixture fixture = fixture(SPEC);
        CliResult success = cli(fixture.arguments("-d"));
        assertEquals(0, success.exit);
        assertEquals("", success.stdout);
        assertTrue(success.stderr.contains("INFO  Wrote decoder example.Decoder to "));
        assertTrue(success.stderr.contains("DEBUG AST after ResolveNamesVisitor:"));

        Files.delete(fixture.source);
        CliResult failure = cli(fixture.arguments());
        assertEquals(1, failure.exit);
        assertEquals("", failure.stdout);
        assertEquals(1, failure.stderr.lines().filter(line -> line.startsWith("ERROR ")).count());
        assertTrue(failure.stderr.contains("ERROR Cannot read specification " + fixture.source));
        assertFalse(failure.stderr.contains("Generation completed"));
    }

    private Fixture fixture(String specification) throws IOException {
        Path root = temporary.newFolder().toPath().toAbsolutePath();
        Path source = root.resolve("cpu.eds");
        Files.writeString(source, specification);
        return new Fixture(source, Files.createDirectory(root.resolve("generated")));
    }

    private CliResult cli(String... arguments) throws Exception {
        List<String> command = new ArrayList<>(List.of(
                Path.of(System.getProperty("java.home"), "bin", "java").toString(), "-cp",
                System.getProperty("edigen.test.classpath", System.getProperty("java.class.path")),
                Edigen.class.getName()));
        command.addAll(Arrays.asList(arguments));
        Path stdout = temporary.newFile().toPath();
        Path stderr = temporary.newFile().toPath();
        Process process = new ProcessBuilder(command).redirectOutput(stdout.toFile()).redirectError(stderr.toFile()).start();
        if (!process.waitFor(15, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            fail("Edigen CLI did not finish");
        }
        return new CliResult(process.exitValue(), Files.readString(stdout), Files.readString(stderr));
    }

    private static int indexStartingWith(List<String> messages, String prefix) {
        for (int index = 0; index < messages.size(); index++) {
            if (messages.get(index).startsWith(prefix)) return index;
        }
        return -1;
    }

    private static final class Fixture {
        final Path source;
        final Path output;

        Fixture(Path source, Path output) {
            this.source = source;
            this.output = output;
        }

        String[] arguments(String... options) {
            List<String> arguments = new ArrayList<>(List.of(source.toString(), "example.Decoder", "example.Disassembler",
                    "-do", output.toString(), "-ao", output.toString()));
            arguments.addAll(Arrays.asList(options));
            return arguments.toArray(new String[0]);
        }
    }

    private static final class RecordingLog implements GenerationLog {
        final List<String> info = new ArrayList<>();
        final List<String> warnings = new ArrayList<>();
        final List<String> debug = new ArrayList<>();
        final boolean debugEnabled;

        RecordingLog(boolean debugEnabled) { this.debugEnabled = debugEnabled; }
        @Override public void info(String message) { info.add(message); }
        @Override public void warn(String message) { warnings.add(message); }
        @Override public void debug(Supplier<String> message) { if (debugEnabled) debug.add(message.get()); }
    }

    private static final class CliResult {
        final int exit;
        final String stdout;
        final String stderr;

        CliResult(int exit, String stdout, String stderr) {
            this.exit = exit;
            this.stdout = stdout;
            this.stderr = stderr;
        }
    }
}
