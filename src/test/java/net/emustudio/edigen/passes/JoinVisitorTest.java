package net.emustudio.edigen.passes;

public class JoinVisitorTest {

    // subrule without length does not generate mask/pattern

    // subrule without pre-pattern generates false mask & false pattern of the subrule length

    // subrule with pre-pattern generates true mask of the pre-pattern & pre-pattern bits; the rest of the length - false mask&pattern

    // constant (pattern) generates true mask & constant bits

    // if pre-pattern of subrule is longer than length - throw exception



}
