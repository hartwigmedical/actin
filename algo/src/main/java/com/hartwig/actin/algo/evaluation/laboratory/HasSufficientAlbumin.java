package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class HasSufficientAlbumin implements LabEvaluationFunction {

    private static final Logger LOGGER = LogManager.getLogger(HasSufficientAlbumin.class);

    private final double minAlbuminGPerDL;

    HasSufficientAlbumin(final double minAlbuminGPerDL) {
        this.minAlbuminGPerDL = minAlbuminGPerDL;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record, @NotNull LabValue labValue) {
        double convertedValue;

        LabUnit labUnit = LabUnit.fromString(labValue.unit());
        if (labUnit == LabUnit.GRAM_PER_DECILITER) {
            convertedValue = labValue.value();
        } else if (labUnit == LabUnit.GRAM_PER_LITER) {
            convertedValue = labValue.value() / 10;
        } else {
            LOGGER.warn("Could not resolve albumin unit: '{}'", labValue.unit());
            return ImmutableEvaluation.builder().result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedMessages("Could not determine albumin unit '" + labValue.unit() + "'")
                    .build();
        }

        EvaluationResult result = LaboratoryUtil.evaluateVersusMinValue(convertedValue, labValue.comparator(), minAlbuminGPerDL);
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
}
