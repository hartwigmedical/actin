package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.interpretation.LabInterpretation;
import com.hartwig.actin.clinical.interpretation.LabInterpreter;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HasSufficientHemoglobin implements EvaluationFunction {

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
    public Evaluation evaluate(@NotNull PatientRecord record) {
        LabInterpretation interpretation = LabInterpreter.interpret(record.clinical().labValues());

        LabValue hemoglobin = interpretation.mostRecentValue(LabMeasurement.HEMOGLOBIN);

        if (hemoglobin == null) {
            return Evaluation.UNDETERMINED;
        }

        LabUnit measuredUnit = determineUnit(hemoglobin);
        if (measuredUnit == null) {
            LOGGER.warn("Could not determine lab unit for {}", hemoglobin);
            return Evaluation.UNDETERMINED;
        }

        Double value = convertValue(hemoglobin.value(), measuredUnit, targetUnit);
        if (value == null) {
            LOGGER.warn("Could not convert value from {} to {}", measuredUnit, targetUnit);
            return Evaluation.UNDETERMINED;
        }

        return Double.compare(value, minHemoglobin) < 0 ? Evaluation.FAIL : Evaluation.PASS;
    }

    @Nullable
    private static LabUnit determineUnit(@NotNull LabValue hemoglobin) {
        switch (hemoglobin.unit().toLowerCase()) {
            case "mmol/l": {
                return LabUnit.MMOL_PER_L;
            }
            case "g/dl": {
                return LabUnit.G_PER_DL;
            }
            default: {
                return null;
            }
        }
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
