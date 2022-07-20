package com.hartwig.actin.algo.evaluation.cardiacfunction;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.ECG;

import org.jetbrains.annotations.NotNull;

public class HasSufficientQTCF implements EvaluationFunction {

    private final double minQTCF;

    HasSufficientQTCF(final double minQTCF) {
        this.minQTCF = minQTCF;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        ECG ecg = record.clinical().clinicalStatus().ecg();
        if (!QTCFFunctions.hasQTCF(ecg)) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("No measurement found for QTCF")
                    .build();
        }

        Integer value = ecg.qtcfValue();
        String unit = ecg.qtcfUnit();

        if (!QTCFFunctions.isExpectedQTCFUnit(unit)) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("QTCF measure not in '" + QTCFFunctions.EXPECTED_QTCF_UNIT + "': " + unit)
                    .build();
        }

        EvaluationResult result = Double.compare(value, minQTCF) >= 0 ? EvaluationResult.PASS : EvaluationResult.FAIL;
        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("QTCF of " + value + " " + unit + " below threshold of " + minQTCF);
            builder.addFailGeneralMessages("QTCF requirements");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("QTCF of " + value + " " + unit + " above threshold of " + minQTCF);
            builder.addPassGeneralMessages("QTCF requirements");
        }

        return builder.build();

    }
}