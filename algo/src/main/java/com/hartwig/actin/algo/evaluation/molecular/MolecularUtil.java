package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;

import org.jetbrains.annotations.NotNull;

final class MolecularUtil {

    private MolecularUtil() {
    }

    @NotNull
    public static EvaluationResult noMatchFound(@NotNull MolecularRecord molecular) {
        return molecular.hasReliableQuality() ? EvaluationResult.FAIL : EvaluationResult.UNDETERMINED;
    }
}
