package com.hartwig.actin.algo.evaluation.tumor;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.molecular.datamodel.ExperimentType;

import org.jetbrains.annotations.NotNull;

public class CanProvideSampleForFFPEAnalysis implements EvaluationFunction {

    CanProvideSampleForFFPEAnalysis() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        if (record.molecular().type() != ExperimentType.WGS) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Can't determine whether patient can provide sample for FFPE analysis without WGS")
                    .addUndeterminedGeneralMessages("FFPE analysis")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.PASS)
                .addPassSpecificMessages("It is assumed that patient can provide sample for FFPE analysis (presence of WGS analysis)")
                .addPassGeneralMessages("FFPE analysis")
                .build();
    }
}
