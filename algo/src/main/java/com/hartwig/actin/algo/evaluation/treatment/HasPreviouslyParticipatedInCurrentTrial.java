package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import org.jetbrains.annotations.NotNull;

//TODO: Update according to README
public class HasPreviouslyParticipatedInCurrentTrial implements EvaluationFunction {

    HasPreviouslyParticipatedInCurrentTrial() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("Trial participation in current trial currently cannot be evaluated")
                .addUndeterminedGeneralMessages("Undetermined if participated previously in current trial")
                .build();
    }
}