package com.hartwig.actin.algo.evaluation.molecular;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;

import org.jetbrains.annotations.NotNull;

final class PriorMolecularTestFunctions {

    private PriorMolecularTestFunctions() {
    }

    @NotNull
    public static List<PriorMolecularTest> allPDL1Tests(@NotNull List<PriorMolecularTest> priorMolecularTests,
            @NotNull String measureToFind) {
        List<PriorMolecularTest> filtered = Lists.newArrayList();
        for (PriorMolecularTest priorMolecularTest : allIHCTests(priorMolecularTests)) {
            String measure = priorMolecularTest.measure();
            if (priorMolecularTest.item().equals("PD-L1") && measure != null && measure.equals(measureToFind)) {
                filtered.add(priorMolecularTest);
            }
        }

        return filtered;
    }

    @NotNull
    public static List<PriorMolecularTest> allIHCTestsForGene(@NotNull List<PriorMolecularTest> priorMolecularTests, @NotNull String gene) {
        List<PriorMolecularTest> filtered = Lists.newArrayList();
        for (PriorMolecularTest priorMolecularTest : allIHCTests(priorMolecularTests)) {
            if (priorMolecularTest.item().equals(gene)) {
                filtered.add(priorMolecularTest);
            }
        }

        return filtered;
    }

    @NotNull
    private static List<PriorMolecularTest> allIHCTests(@NotNull List<PriorMolecularTest> priorMolecularTests) {
        List<PriorMolecularTest> filtered = Lists.newArrayList();
        for (PriorMolecularTest priorMolecularTest : priorMolecularTests) {
            if (priorMolecularTest.test().equals("IHC")) {
                filtered.add(priorMolecularTest);
            }
        }

        return filtered;
    }
}
