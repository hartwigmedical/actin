package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.clinical.datamodel.LabUnit;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class HasLimitedCreatinine implements LabEvaluationFunction {

    private static final Logger LOGGER = LogManager.getLogger(HasLimitedCreatinine.class);

    private final double maxCreatinineMgPerDL;

    HasLimitedCreatinine(final double maxCreatinineMgPerDL) {
        this.maxCreatinineMgPerDL = maxCreatinineMgPerDL;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record, @NotNull LabValue labValue) {
        double convertedValue;

        if (labValue.unit() == LabUnit.MILLIGRAMS_PER_DECILITER) {
            convertedValue = labValue.value();
        } else if (labValue.unit() == LabUnit.MICROMOLES_PER_LITER) {
            convertedValue = labValue.value() / 88.42;
        } else {
            LOGGER.warn("Could not resolve creatinine unit: '{}'", labValue.unit());
            return ImmutableEvaluation.builder()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedMessages("Could not determine creatinine unit '" + labValue.unit() + "'")
                    .build();
        }

        EvaluationResult result = LabEvaluation.evaluateVersusMaxValue(convertedValue, labValue.comparator(), maxCreatinineMgPerDL);
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
