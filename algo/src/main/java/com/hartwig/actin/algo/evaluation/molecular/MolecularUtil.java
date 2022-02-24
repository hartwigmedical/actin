package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.util.EvaluationFactory;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;

import org.jetbrains.annotations.NotNull;

final class MolecularUtil {

    private MolecularUtil() {
    }

    @NotNull
    public static Evaluation noMatchFound(@NotNull MolecularRecord molecular) {
//        EvaluationResult result = molecular.hasReliableQuality() ? EvaluationResult.FAIL : EvaluationResult.UNDETERMINED;

        EvaluationResult result = EvaluationResult.FAIL;
        return EvaluationFactory.create(result);
    }
}
