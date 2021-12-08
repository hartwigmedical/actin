package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.interpretation.LabInterpretation;
import com.hartwig.actin.clinical.interpretation.LabInterpreter;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.jetbrains.annotations.NotNull;

public class HasSufficientLabValue implements EvaluationFunction {

    @NotNull
    private final LabMeasurement measurement;
    private final double minValue;

    public HasSufficientLabValue(@NotNull final LabMeasurement measurement, final double minValue) {
        this.measurement = measurement;
        this.minValue = minValue;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        LabInterpretation interpretation = LabInterpreter.interpret(record.clinical().labValues());

        LabValue labValue = interpretation.mostRecentValue(measurement);

        if (!LabValueEvaluation.existsWithExpectedUnit(labValue, measurement.expectedUnit())) {
            return Evaluation.UNDETERMINED;
        }

        return LabValueEvaluation.evaluateVersusMinValue(labValue.value(), labValue.comparator(), minValue);
    }
}
