package com.hartwig.actin.algo.evaluation.othercondition;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasPostOperativeBaselineContrastEnhancedMRIScan implements EvaluationFunction {

    HasPostOperativeBaselineContrastEnhancedMRIScan() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("Currently presence of post-operative baseline contrast enhancing MRI scan is undetermined")
                .addUndeterminedGeneralMessages("Undetermined presence post-operative baseline contrast enhancing MRI scan")
                .build();
    }
}