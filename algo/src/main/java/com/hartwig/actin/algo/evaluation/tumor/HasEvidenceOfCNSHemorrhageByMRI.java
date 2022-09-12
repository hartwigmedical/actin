package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasEvidenceOfCNSHemorrhageByMRI implements EvaluationFunction {

    HasEvidenceOfCNSHemorrhageByMRI() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("Currently it not determined if there is evidence of CNS hemorrhage on MRI scan")
                .addUndeterminedGeneralMessages("Undetermined CNS hemorrhage by MRI")
                .build();
    }

}
