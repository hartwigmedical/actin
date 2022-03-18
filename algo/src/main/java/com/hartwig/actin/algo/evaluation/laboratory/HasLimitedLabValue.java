package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.clinical.datamodel.LabUnit;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.jetbrains.annotations.NotNull;

public class HasLimitedLabValue implements LabEvaluationFunction {

    private final double maxValue;
    @NotNull
    private final LabMeasurement measurement;
    @NotNull
    private final LabUnit targetUnit;

    public HasLimitedLabValue(final double maxValue, @NotNull final LabMeasurement measurement, @NotNull final LabUnit targetUnit) {
        this.maxValue = maxValue;
        this.measurement = measurement;
        this.targetUnit = targetUnit;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record, @NotNull LabValue labValue) {
        Double convertedValue = LabUnitConverter.convert(measurement, labValue, targetUnit);

        if (convertedValue == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("Could not convert value for " + labValue.code() + " to " + targetUnit.display())
                    .build();
        }

        EvaluationResult result = LabEvaluation.evaluateVersusMaxValue(convertedValue, labValue.comparator(), maxValue);
        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages(labValue.code() + " is insufficient");
        } else if (result == EvaluationResult.UNDETERMINED) {
            builder.addUndeterminedSpecificMessages(labValue.code() + " sufficiency could not be evaluated");
            builder.addUndeterminedGeneralMessages("Lab evaluation undetermined");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages(labValue.code() + " is sufficient");
        }

        return builder.build();
    }
}
