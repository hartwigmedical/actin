package com.hartwig.actin.molecular.interpretation;

import java.util.Set;

import org.jetbrains.annotations.NotNull;

public class MolecularInputChecker {

    @NotNull
    private final Set<String> validGenes;

    public MolecularInputChecker(@NotNull final Set<String> validGenes) {
        this.validGenes = validGenes;
    }

    public boolean isGene(@NotNull String string) {
        // TODO Implement
        return true;
    }

    public static boolean isHlaAllele(@NotNull String string) {
        // Expected format "A*02:01"
        int asterixIndex = string.indexOf("*");
        int semicolonIndex = string.indexOf(":");
        return asterixIndex == 1 && semicolonIndex > asterixIndex;
    }

    public static boolean isProteinImpact(@NotNull String string) {
        char first = string.charAt(0);
        char last = string.charAt(string.length() - 1);
        String codon = string.substring(1, string.length() - 1);

        return Character.isUpperCase(first) && Character.isUpperCase(last) && isInteger(codon);
    }

    private static boolean isInteger(@NotNull String codon) {
        try {
            Integer.parseInt(codon);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }
}
