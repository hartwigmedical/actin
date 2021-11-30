package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.interpretation.LabInterpretation;
import com.hartwig.actin.clinical.interpretation.LabInterpreter;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.jetbrains.annotations.NotNull;

public class HasSufficientThrombocytes implements EvaluationFunction {

    private final double minThrombocytes;

    HasSufficientThrombocytes(final double minThrombocytes) {
        this.minThrombocytes = minThrombocytes;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        LabInterpretation interpretation = LabInterpreter.interpret(record.clinical().labValues());

        LabValue thrombocytes = interpretation.mostRecentValue(LabMeasurement.THROMBOCYTES_ABS);

        if (thrombocytes == null) {
            return Evaluation.UNDETERMINED;
        }

        return LabValueEvaluation.evaluateOnMinimalValue(thrombocytes.value(), thrombocytes.comparator(), minThrombocytes);
    }
}
