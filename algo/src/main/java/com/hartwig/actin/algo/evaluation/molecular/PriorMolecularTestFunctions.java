package com.hartwig.actin.algo.evaluation.molecular;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;

import org.jetbrains.annotations.NotNull;

final class PriorMolecularTestFunctions {

    private static final String LARGER_THAN = ">";
    private static final String LARGER_THAN_OR_EQUAL = ">=";
    private static final String SMALLER_THAN = "<";
    private static final String SMALLER_THAN_OR_EQUAL = "<=";

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

    @NotNull
    public static EvaluationResult evaluateVersusMinValue(double value, @NotNull String comparator, double minValue) {
        if (canBeDetermined(value, comparator, minValue)) {
            return EvaluationResult.UNDETERMINED;
        }

        return Double.compare(value, minValue) >= 0 ? EvaluationResult.PASS : EvaluationResult.FAIL;
    }

    @NotNull
    public static EvaluationResult evaluateVersusMaxValue(double value, @NotNull String comparator, double maxValue) {
        if (canBeDetermined(value, comparator, maxValue)) {
            return EvaluationResult.UNDETERMINED;
        }

        return Double.compare(value, maxValue) <= 0 ? EvaluationResult.PASS : EvaluationResult.FAIL;
    }

    private static boolean canBeDetermined(double value, @NotNull String comparator, double refValue) {
        switch (comparator) {
            case LARGER_THAN: {
                return value > refValue;
            } case LARGER_THAN_OR_EQUAL: {
                return value >= refValue;
            } case SMALLER_THAN: {
                return value < refValue;
            } case SMALLER_THAN_OR_EQUAL: {
                return value <= refValue;
            }
            default: {
                return true;
            }
        }
    }
}
