package com.hartwig.actin.algo.evaluation.laboratory;

import java.time.LocalDate;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.interpretation.LabInterpretation;
import com.hartwig.actin.clinical.interpretation.LabInterpreter;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LabMeasurementEvaluator implements EvaluationFunction {

    @NotNull
    private final LabMeasurement measurement;
    @NotNull
    private final LabEvaluationFunction function;
    @NotNull
    private final LocalDate minValidDate;

    LabMeasurementEvaluator(@NotNull final LabMeasurement measurement, @NotNull final LabEvaluationFunction function,
            @NotNull final LocalDate minValidDate) {
        this.measurement = measurement;
        this.function = function;
        this.minValidDate = minValidDate;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        LabInterpretation interpretation = LabInterpreter.interpret(record.clinical().labValues());

        LabValue mostRecent = interpretation.mostRecentValue(measurement);

        if (!isValid(mostRecent, measurement)) {
            return Evaluation.UNDETERMINED;
        }

        Evaluation evaluation = function.evaluate(record, mostRecent);

        if (evaluation == Evaluation.FAIL) {
            LabValue secondMostRecent = interpretation.secondMostRecentValue(measurement);
            if (isValid(mostRecent, measurement)) {
                Evaluation secondEvaluation = function.evaluate(record, secondMostRecent);
                if (secondEvaluation == Evaluation.PASS) {
                    return Evaluation.UNDETERMINED;
                }
            }
        }

        return evaluation;
    }

    private boolean isValid(@Nullable LabValue value, @NotNull LabMeasurement measurement) {
        return value != null && value.unit().equals(measurement.expectedUnit()) && !value.date().isBefore(minValidDate);
    }
}
