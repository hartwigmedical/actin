package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HasSufficientLymphocytes implements LabEvaluationFunction {

    private static final Logger LOGGER = LogManager.getLogger(HasSufficientLymphocytes.class);

    private static final double CONVERSION_FACTOR = 1000;

    private final double minLymphocytes;
    @NotNull
    private final LabUnit targetUnit;

    HasSufficientLymphocytes(final double minLymphocytes, @NotNull final LabUnit targetUnit) {
        this.minLymphocytes = minLymphocytes;
        this.targetUnit = targetUnit;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record, @NotNull LabValue labValue) {
        LabUnit measuredUnit = LabUnit.fromString(labValue.unit());
        if (measuredUnit == null) {
            LOGGER.warn("Could not determine lab unit for '{}'", labValue);
            return ImmutableEvaluation.builder().result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedMessages("Could not determine lymphocyte lab unit for '" + labValue.code() + "'")
                    .build();
        }

        Double value = convertValue(labValue.value(), measuredUnit, targetUnit);
        if (value == null) {
            LOGGER.warn("Could not convert lymphocyte value from '{}' to '{}'", measuredUnit, targetUnit);
            return ImmutableEvaluation.builder().result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedMessages("Could not convert lymphocyte value from '" + measuredUnit + "' to '" + targetUnit + "'")
                    .build();
        }

        EvaluationResult result = LaboratoryUtil.evaluateVersusMinValue(value, labValue.comparator(), minLymphocytes);
        ImmutableEvaluation.Builder builder = ImmutableEvaluation.builder().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailMessages(labValue.code() + " is insufficient");
        } else if (result == EvaluationResult.UNDETERMINED) {
            builder.addUndeterminedMessages(labValue.code() + " sufficiency could not be evaluated");
        } else if (result.isPass()) {
            builder.addPassMessages(labValue.code() + " is sufficient");
        }
        return builder.build();
    }

    @Nullable
    private static Double convertValue(double value, @NotNull LabUnit measuredUnit, @NotNull LabUnit targetUnit) {
        if (measuredUnit == targetUnit) {
            return value;
        } else if (measuredUnit == LabUnit.BILLION_PER_LITER && targetUnit == LabUnit.CELLS_PER_MICROLITER) {
            return value * CONVERSION_FACTOR;
        } else if (measuredUnit == LabUnit.CELLS_PER_MICROLITER && targetUnit == LabUnit.BILLION_PER_LITER) {
            return value / CONVERSION_FACTOR;
        } else {
            return null;
        }
    }
}
