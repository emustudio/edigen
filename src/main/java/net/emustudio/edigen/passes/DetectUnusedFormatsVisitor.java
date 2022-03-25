package net.emustudio.edigen.passes;

import net.emustudio.edigen.SemanticException;
import net.emustudio.edigen.Visitor;
import net.emustudio.edigen.nodes.*;

import java.util.*;

/**
 * Detects unused disassembler formats
 */
public class DetectUnusedFormatsVisitor extends Visitor {
    private final Set<Set<String>> reachablePaths = new HashSet<>();
    private final Set<Set<String>> requestedPaths = new HashSet<>();

    private Rule currentRule;
    private Set<Rule> currentPath;
    private Set<String> currentParameters;

    @Override
    public void visit(Disassembler disassembler) throws SemanticException {
        super.visit(disassembler);

        System.out.println(reachablePaths);

        // detects unused formats (missing formats are ok)
        if (!reachablePaths.containsAll(requestedPaths)) {
            requestedPaths.removeAll(reachablePaths);
            throw new SemanticException("Some disassembler formats are unreachable: " + requestedPaths, disassembler);
        }
    }

    @Override
    public void visit(Rule rule) throws SemanticException {
        this.currentRule = rule;
        if (currentRule.isRoot()) {
            // we want to have full set of keys (collect full path from root), not just partial ones.
            // There is just one root rule
            currentPath = new HashSet<>();
            super.visit(rule);
        }
    }

    @Override
    public void visit(Variant variant) throws SemanticException {
        if (variant.returns()) {
            currentPath.add(currentRule);
        }
        Set<Rule> oldReachablePath = new HashSet<>(currentPath);

        variant.acceptChildren(this);

        if (variant.returns()) {
            storeCurrentPath();
        }
        currentPath = oldReachablePath;
    }

    @Override
    public void visit(Subrule subrule) throws SemanticException {
        Rule subruleRule = subrule.getRule();
        if (subruleRule != null && subruleRule != currentRule) {
            Rule oldCurrentRule = currentRule;
            currentRule = subruleRule;
            subruleRule.acceptChildren(this);
            currentRule = oldCurrentRule;
        }
    }

    @Override
    public void visit(Format format) throws SemanticException {
        currentParameters = new HashSet<>();
        super.visit(format);
        storeCurrentFormatParameters();
    }

    @Override
    public void visit(Value value) throws SemanticException {
        currentParameters.add(value.getName());
    }

    private void storeCurrentPath() {
        List<Set<String>> pathVariations = pathCombinations(
                new ArrayList<>(currentPath), new ArrayList<>(), new HashSet<>()
        );
        reachablePaths.addAll(pathVariations);
    }

    private List<Set<String>> pathCombinations(List<Rule> path, List<Set<String>> result, Set<String> pathVariation) {
        if (path.isEmpty()) {
            List<Set<String>> finalResult = new ArrayList<>(result);
            finalResult.add(pathVariation);
            return finalResult;
        }
        Rule rule = path.get(0);
        List<Rule> pathTail = path.subList(1, path.size());

        List<Set<String>> newResult = new ArrayList<>(result);
        for (String name : rule.getNames()) {
            Set<String> newPathVariation = new HashSet<>(pathVariation);
            newPathVariation.add(name);
            newResult.addAll(pathCombinations(pathTail, newResult, newPathVariation));
        }
        return newResult;
    }

    private void storeCurrentFormatParameters() {
        if (!currentParameters.isEmpty()) {
            requestedPaths.add(currentParameters);
        }
    }
}
