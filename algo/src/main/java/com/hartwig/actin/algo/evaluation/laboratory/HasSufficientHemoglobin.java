package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HasSufficientHemoglobin implements LabEvaluationFunction {

    private static final Logger LOGGER = LogManager.getLogger(HasSufficientHemoglobin.class);

    private static final double G_PER_DL_TO_MMOL_PER_L_CONVERSION_FACTOR = 0.6206;

    private final double minHemoglobin;
    @NotNull
    private final LabUnit targetUnit;

    public HasSufficientHemoglobin(final double minHemoglobin, @NotNull final LabUnit targetUnit) {
        this.minHemoglobin = minHemoglobin;
        this.targetUnit = targetUnit;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record, @NotNull LabValue labValue) {
        LabUnit measuredUnit = LabUnit.fromString(labValue.unit());
        if (measuredUnit == null) {
            LOGGER.warn("Could not determine lab unit for '{}'", labValue);
            return EvaluationFactory.create(EvaluationResult.UNDETERMINED);
        }

        Double value = convertValue(labValue.value(), measuredUnit, targetUnit);
        if (value == null) {
            LOGGER.warn("Could not convert value from '{}' to '{}'", measuredUnit, targetUnit);
            return EvaluationFactory.create(EvaluationResult.UNDETERMINED);
        }

        return LaboratoryUtil.evaluateVersusMinValue(value, labValue.comparator(), minHemoglobin);
    }

    @Nullable
    private static Double convertValue(double value, @NotNull LabUnit measuredUnit, @NotNull LabUnit targetUnit) {
        if (measuredUnit == targetUnit) {
            return value;
        } else if (measuredUnit == LabUnit.G_PER_DL && targetUnit == LabUnit.MMOL_PER_L) {
            return value * G_PER_DL_TO_MMOL_PER_L_CONVERSION_FACTOR;
        } else if (measuredUnit == LabUnit.MMOL_PER_L && targetUnit == LabUnit.G_PER_DL) {
            return value / G_PER_DL_TO_MMOL_PER_L_CONVERSION_FACTOR;
        } else {
            return null;
        }
    }
}
