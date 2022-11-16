package com.hartwig.actin.molecular.interpretation;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.filter.GeneFilter;
import com.hartwig.actin.molecular.filter.GeneFilterFactory;

import org.jetbrains.annotations.NotNull;

public class MolecularInputChecker {

    private static final Set<String> VALID_PROTEIN_ENDINGS = Sets.newHashSet("del", "dup", "ins");

    @NotNull
    private final GeneFilter geneFilter;

    @NotNull
    public static MolecularInputChecker createAnyGeneValid() {
        return new MolecularInputChecker(GeneFilterFactory.createAlwaysValid());
    }

    public MolecularInputChecker(@NotNull final GeneFilter geneFilter) {
        this.geneFilter = geneFilter;
    }

    public boolean isGene(@NotNull String string) {
        return geneFilter.include(string);
    }

    public static boolean isHlaAllele(@NotNull String string) {
        int asterixIndex = string.indexOf("*");
        int semicolonIndex = string.indexOf(":");
        return asterixIndex == 1 && semicolonIndex > asterixIndex;
    }

    public static boolean isProteinImpact(@NotNull String string) {
        char first = string.charAt(0);
        boolean hasValidStart = Character.isUpperCase(first);

        char last = string.charAt(string.length() - 1);
        boolean hasValidEnd = hasSpecificValidProteinEnding(string) || Character.isUpperCase(last);

        String mid = string.substring(1, string.length() - 1);
        boolean hasValidMid = hasSpecificValidProteinEnding(string) || mid.contains("_") || isPositiveNumber(mid);

        return hasValidStart && hasValidEnd && hasValidMid;
    }

    private static boolean hasSpecificValidProteinEnding(@NotNull String string) {
        for (String validProteinEnding : VALID_PROTEIN_ENDINGS) {
            if (string.endsWith(validProteinEnding)) {
                return true;
            }
        }
        return false;
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
