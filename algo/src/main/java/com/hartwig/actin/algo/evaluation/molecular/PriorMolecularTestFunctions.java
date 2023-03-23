package com.hartwig.actin.algo.evaluation.molecular;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;

import org.jetbrains.annotations.NotNull;

final class PriorMolecularTestFunctions {

    @NotNull
    public static List<PriorMolecularTest> allPDL1Tests(@NotNull List<PriorMolecularTest> priorMolecularTests,
            @NotNull String measureToFind) {
        return allIHCTestsStream(priorMolecularTests).filter(priorMolecularTest -> priorMolecularTest.item().equals("PD-L1")
                && measureToFind.equals(priorMolecularTest.measure())).collect(Collectors.toList());
    }

    @NotNull
    public static List<PriorMolecularTest> allIHCTestsForItem(@NotNull List<PriorMolecularTest> priorMolecularTests, @NotNull String item) {
        return allIHCTestsForItemStream(priorMolecularTests, item).collect(Collectors.toList());
    }

    @NotNull
    public static Stream<PriorMolecularTest> allIHCTestsForItemStream(@NotNull List<PriorMolecularTest> priorMolecularTests, @NotNull String item) {
        return allIHCTestsStream(priorMolecularTests).filter(priorMolecularTest -> item.equals(priorMolecularTest.item()));
    }

    @NotNull
    private static Stream<PriorMolecularTest> allIHCTestsStream(@NotNull List<PriorMolecularTest> priorMolecularTests) {
        return priorMolecularTests.stream().filter(priorMolecularTest -> priorMolecularTest.test().equals("IHC"));
    }
}
