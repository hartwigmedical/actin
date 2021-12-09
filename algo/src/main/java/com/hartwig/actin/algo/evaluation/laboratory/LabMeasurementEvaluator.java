package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.interpretation.LabInterpretation;
import com.hartwig.actin.clinical.interpretation.LabInterpreter;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.jetbrains.annotations.NotNull;

public class LabMeasurementEvaluator implements EvaluationFunction {

    @NotNull
    private final LabMeasurement measurement;
    @NotNull
    private final LabEvaluationFunction function;

    LabMeasurementEvaluator(@NotNull final LabMeasurement measurement, @NotNull final LabEvaluationFunction function) {
        this.measurement = measurement;
        this.function = function;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        LabInterpretation interpretation = LabInterpreter.interpret(record.clinical().labValues());

        LabValue mostRecent = interpretation.mostRecentValue(measurement);

        if (!LabValueEvaluation.existsWithExpectedUnit(mostRecent, measurement.expectedUnit())) {
            return Evaluation.UNDETERMINED;
        }

        Evaluation evaluation = function.evaluate(mostRecent);

        if (evaluation == Evaluation.FAIL) {
            LabValue secondMostRecent = interpretation.secondMostRecentValue(measurement);
            if (LabValueEvaluation.existsWithExpectedUnit(mostRecent, measurement.expectedUnit())) {
                Evaluation secondEvaluation = function.evaluate(secondMostRecent);
                if (secondEvaluation == Evaluation.PASS) {
                    return Evaluation.UNDETERMINED;
                }
            }
        }

        return evaluation;
    }
}