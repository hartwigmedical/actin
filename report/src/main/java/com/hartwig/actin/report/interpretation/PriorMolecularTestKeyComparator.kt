package com.hartwig.actin.report.interpretation;

import java.util.Comparator;

import org.jetbrains.annotations.NotNull;

public class PriorMolecularTestKeyComparator implements Comparator<PriorMolecularTestKey> {

    @Override
    public int compare(@NotNull PriorMolecularTestKey key1, @NotNull PriorMolecularTestKey key2) {
        int scoreTextCompare = key1.scoreText().compareTo(key2.scoreText());
        if (scoreTextCompare != 0) {
            return scoreTextCompare;
        }

        return key1.test().compareTo(key2.test());
    }
}
