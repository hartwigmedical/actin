package com.hartwig.actin.algo.evaluation.laboratory;

import java.time.LocalDate;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
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
    @NotNull
    private final LocalDate minPassDate;

    public LabMeasurementEvaluator(@NotNull final LabMeasurement measurement, @NotNull final LabEvaluationFunction function,
            @NotNull final LocalDate minValidDate, @NotNull final LocalDate minPassDate) {
        this.measurement = measurement;
        this.function = function;
        this.minValidDate = minValidDate;
        this.minPassDate = minPassDate;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        LabInterpretation interpretation = LabInterpreter.interpret(record.clinical().labValues());

        LabValue mostRecent = interpretation.mostRecentValue(measurement);

        if (!isValid(mostRecent, measurement)) {
            ImmutableEvaluation.Builder builder = EvaluationFactory.recoverable().result(EvaluationResult.UNDETERMINED);

            if (mostRecent == null) {
                builder.addUndeterminedSpecificMessages("No measurement found for " + measurement.code());
            } else if (!mostRecent.unit().equals(measurement.defaultUnit())) {
                builder.addUndeterminedSpecificMessages("Unexpected unit specified for " + measurement.code() + ": " + mostRecent.unit());
            } else if (mostRecent.date().isBefore(minValidDate)) {
                builder.addUndeterminedSpecificMessages("Most recent measurement too old for " + measurement.code());
            }

            return builder.build();
        }

        Evaluation evaluation = function.evaluate(record, mostRecent);

        if (evaluation.result() == EvaluationResult.FAIL) {
            LabValue secondMostRecent = interpretation.secondMostRecentValue(measurement);
            if (isValid(secondMostRecent, measurement)) {
                Evaluation secondEvaluation = function.evaluate(record, secondMostRecent);
                if (secondEvaluation.result() == EvaluationResult.PASS) {
                    return EvaluationFactory.recoverable()
                            .result(EvaluationResult.WARN)
                            .addWarnSpecificMessages("Latest measurement fails for " + measurement.code() + ", but second-latest succeeded")
                            .build();
                }
            }
        }

        if (evaluation.result() == EvaluationResult.PASS && !mostRecent.date().isAfter(minPassDate)) {
            return EvaluationFactory.recoverable()
                    .result(EvaluationResult.WARN)
                    .addAllWarnSpecificMessages(appendPastMinPassDate(evaluation.passSpecificMessages()))
                    .build();
        }

        return evaluation;
    }

    private boolean isValid(@Nullable LabValue value, @NotNull LabMeasurement measurement) {
        return value != null && value.unit().equals(measurement.defaultUnit()) && !value.date().isBefore(minValidDate);
    }

    @NotNull
    private Set<String> appendPastMinPassDate(@NotNull Set<String> inputs) {
        Set<String> messages = Sets.newHashSet();
        for (String message : inputs) {
            messages.add(message + ", but measurement occurred before " + Format.date(minValidDate));
        }
        return messages;
    }
}
