package com.hartwig.actin.algo.evaluation.molecular;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;

import org.jetbrains.annotations.NotNull;

final class IHCFunctions {

    private IHCFunctions() {
    }

    @NotNull
    public static List<PriorMolecularTest> allIHCTestsForGene(@NotNull List<PriorMolecularTest> priorMolecularTests, @NotNull String gene) {
        List<PriorMolecularTest> filtered = Lists.newArrayList();
        for (PriorMolecularTest priorMolecularTest : priorMolecularTests) {
            if (priorMolecularTest.test().equals("IHC") && priorMolecularTest.item().equals(gene)) {
                filtered.add(priorMolecularTest);
            }
        }

        return filtered;
    }
}
