package com.hartwig.actin.molecular.interpretation;

import java.util.Set;

import com.google.common.collect.Sets;

import org.jetbrains.annotations.NotNull;

public class MolecularInputChecker {

    private final boolean anyGeneIsValid;
    @NotNull
    private final Set<String> allowedGenes;

    @NotNull
    public static MolecularInputChecker createAnyGeneValid() {
        return new MolecularInputChecker(true, Sets.newHashSet());
    }

    @NotNull
    public static MolecularInputChecker createSpecificGenesValid(@NotNull Set<String> allowedGenes) {
        return new MolecularInputChecker(false, allowedGenes);
    }

    public MolecularInputChecker(final boolean anyGeneIsValid, @NotNull final Set<String> allowedGenes) {
        this.anyGeneIsValid = anyGeneIsValid;
        this.allowedGenes = allowedGenes;
    }

    public boolean isGene(@NotNull String string) {
        return anyGeneIsValid || allowedGenes.contains(string);
    }

    public static boolean isHlaAllele(@NotNull String string) {
        int asterixIndex = string.indexOf("*");
        int semicolonIndex = string.indexOf(":");
        return asterixIndex == 1 && semicolonIndex > asterixIndex;
    }

    public static boolean isProteinImpact(@NotNull String string) {
        char first = string.charAt(0);
        char last = string.charAt(string.length() - 1);
        String codon = string.substring(1, string.length() - 1);

        return Character.isUpperCase(first) && Character.isUpperCase(last) && isPositiveNumber(codon);
    }

    public static boolean isCodon(@NotNull String string) {
        char first = string.charAt(0);
        String codon = string.substring(1);

        return Character.isUpperCase(first) && isPositiveNumber(codon);
    }

    private static boolean isPositiveNumber(@NotNull String codon) {
        try {
            return Integer.parseInt(codon) > 0;
        } catch (NumberFormatException exception) {
            return false;
        }
    }
}
