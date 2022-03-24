package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;
import net.emustudio.edigen.nodes.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Detects unused disassembler formats
 */
public class DetectUnusedFormatsVisitor extends Visitor {
    private final Set<String> instructions = new HashSet<>();
    private final Set<String> parameters = new HashSet<>();

    private Rule currentRule;
    private final List<String> subrules = new ArrayList<>();

    private final List<String> currentFormatParameters = new ArrayList<>();

    @Override
    public void visit(Disassembler disassembler) throws SemanticException {
        super.visit(disassembler);

        // detects unused formats (missing formats are ok)
        if (!instructions.containsAll(parameters)) {
            parameters.removeAll(instructions);
            throw new SemanticException("Some disassembler formats are unreachable: " + parameters, disassembler);
        }
    }

    @Override
    public void visit(Rule rule) throws SemanticException {
        this.currentRule = rule;
        this.subrules.clear();
        super.visit(rule);
        storeInstructionImage();
    }

    @Override
    public void visit(Subrule subrule) throws SemanticException {
        subrules.add(subrule.getName());
    }

    @Override
    public void visit(Variant variant) throws SemanticException {
        if (!subrules.isEmpty()) {
            storeInstructionImage();
            subrules.clear();
        }
        super.visit(variant);
    }

    @Override
    public void visit(Format format) throws SemanticException {
        currentFormatParameters.clear();
        super.visit(format);
        storeCurrentFormatParameters();
    }

    @Override
    public void visit(Value value) throws SemanticException {
        currentFormatParameters.add(value.getName());
    }

    private void storeInstructionImage() {
        for (String ruleName : currentRule.getNames()) {
            List<String> instruction = new ArrayList<>();
            instruction.add(ruleName);
            instruction.addAll(subrules);
            instructions.add(instruction.toString());
        }
    }

    private void storeCurrentFormatParameters() {
        if (!currentFormatParameters.isEmpty()) {
            parameters.add(currentFormatParameters.toString());
        }
    }
}
