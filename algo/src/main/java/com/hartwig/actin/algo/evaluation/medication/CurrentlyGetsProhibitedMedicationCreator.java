package com.hartwig.actin.algo.evaluation.medication;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class CurrentlyGetsProhibitedMedicationCreator implements EvaluationFunction {

    CurrentlyGetsProhibitedMedicationCreator() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        // Currently cannot be determined
        return EvaluationFactory.create(EvaluationResult.UNDETERMINED);
    }
}
