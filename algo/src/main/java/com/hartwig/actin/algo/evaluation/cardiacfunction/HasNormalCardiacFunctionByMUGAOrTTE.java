package com.hartwig.actin.algo.evaluation.cardiacfunction;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.NotNull;

public class HasNormalCardiacFunctionByMUGAOrTTE implements EvaluationFunction {

    HasNormalCardiacFunctionByMUGAOrTTE() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Double lvef = record.clinical().clinicalStatus().lvef();
        if (lvef != null && lvef < 0.5) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addWarnSpecificMessages("LVEF of " + lvef + " exceeds 50%")
                    .addWarnGeneralMessages("LVEF > 50%")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("Currently normal cardiac function by MUGA or TTE cannot be determined")
                .addUndeterminedGeneralMessages("Undetermined cardiac function by MUGA or TTE")
                .build();
    }
}
