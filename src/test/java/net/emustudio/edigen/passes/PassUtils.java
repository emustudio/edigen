package net.emustudio.edigen.passes;

import net.emustudio.edigen.nodes.Mask;
import net.emustudio.edigen.nodes.Pattern;
import net.emustudio.edigen.nodes.TreeNode;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class PassUtils {

    public static List<String> findMaskStrings(TreeNode tree) {
        return findMasks(tree).stream().map(n -> n.getBits().toString()).collect(Collectors.toList());
    }

    public static List<Mask> findMasks(TreeNode tree) {
        List<Mask> masks = new LinkedList<>();
        if (tree instanceof Mask) {
            masks.add((Mask)tree);
        }
        tree.getChildren().forEach(node -> {
            if (node instanceof Mask) {
                masks.add(((Mask)node));
            } else {
                node.getChildren().forEach(n -> masks.addAll(findMasks(n)));
            }
        });
        return masks;
    }

    public static Mask findMask(TreeNode tree) {
        return findMasks(tree).get(0);
    }

    public static Pattern findPattern(TreeNode tree) {
        AtomicReference<Pattern> pattern = new AtomicReference<>();
        tree.getChildren().forEach(node -> {
            if (node instanceof Pattern) {
                pattern.set((Pattern)node);
            }
        });
        return pattern.get();
    }
}
