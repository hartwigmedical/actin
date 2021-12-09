package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.interpretation.LabInterpretation;
import com.hartwig.actin.clinical.interpretation.LabInterpreter;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.jetbrains.annotations.NotNull;

public class HasLabValueWithinRef implements EvaluationFunction {

    @NotNull
    private final LabMeasurement measurement;

    HasLabValueWithinRef(@NotNull final LabMeasurement measurement) {
        this.measurement = measurement;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        LabInterpretation interpretation = LabInterpreter.interpret(record.clinical().labValues());

        LabValue value = interpretation.mostRecentValue(measurement);

        if (!LabValueEvaluation.existsWithExpectedUnit(value, measurement.expectedUnit())) {
            return Evaluation.UNDETERMINED;
        }

        Boolean isOutsideRef = value.isOutsideRef();
        if (isOutsideRef == null) {
            return Evaluation.UNDETERMINED;
        }

        return isOutsideRef ? Evaluation.FAIL : Evaluation.PASS;
    }
}
