package net.emustudio.edigen.passes;

import net.emustudio.edigen.misc.BitSequence;
import net.emustudio.edigen.nodes.Mask;
import net.emustudio.edigen.nodes.Pattern;
import net.emustudio.edigen.nodes.TreeNode;
import net.emustudio.edigen.nodes.Variant;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class PassUtils {

    public static String mkString(int length, char c) {
        char[] data = new char[length];
        Arrays.fill(data, c);
        return String.valueOf(data);
    }

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

    public static Variant createVariantWithMask(String bits) {
        Variant variant = new Variant();
        Mask mask = new Mask(BitSequence.fromBinary(bits));
        variant.addChild(mask);
        return variant;
    }

    public static Variant createVariantWithMaskAndPattern(String maskBits, String patternBits) {
        Variant variant = new Variant();
        Mask mask = new Mask(BitSequence.fromBinary(maskBits));
        variant.addChild(mask);
        Pattern pattern = new Pattern(BitSequence.fromBinary(patternBits));
        variant.addChild(pattern);
        return variant;
    }
}
