package com.hartwig.actin.algo.evaluation.molecular;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;

import org.jetbrains.annotations.NotNull;

final class PriorMolecularTestFunctions {

    private static final String PD_L1 = "PD-L1";
    private static final String IHC = "IHC";

    @NotNull
    public static List<PriorMolecularTest> allPDL1Tests(@NotNull List<PriorMolecularTest> priorMolecularTests,
            @NotNull String measureToFind) {
        return allIHCTestsStream(priorMolecularTests).filter(priorMolecularTest -> priorMolecularTest.item().equals(PD_L1)
                && measureToFind.equals(priorMolecularTest.measure())).collect(Collectors.toList());
    }

    @NotNull
    public static List<PriorMolecularTest> allIHCTestsForProtein(@NotNull List<PriorMolecularTest> priorMolecularTests,
            @NotNull String protein) {
        return allIHCTestsForProteinStream(priorMolecularTests, protein).collect(Collectors.toList());
    }

    @NotNull
    public static Stream<PriorMolecularTest> allIHCTestsForProteinStream(@NotNull List<PriorMolecularTest> priorMolecularTests,
            @NotNull String protein) {
        return allIHCTestsStream(priorMolecularTests).filter(priorMolecularTest -> protein.equals(priorMolecularTest.item()));
    }

    @NotNull
    private static Stream<PriorMolecularTest> allIHCTestsStream(@NotNull List<PriorMolecularTest> priorMolecularTests) {
        return priorMolecularTests.stream().filter(priorMolecularTest -> priorMolecularTest.test().equals(IHC));
    }
}
