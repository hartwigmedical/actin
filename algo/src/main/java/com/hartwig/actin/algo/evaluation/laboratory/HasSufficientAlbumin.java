package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
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
        if (labValue.unit().equals(LabUnit.G_PER_DL.display())) {
            convertedValue = labValue.value();
        } else if (labValue.unit().equals(LabUnit.G_PER_L.display())) {
            convertedValue = labValue.value() / 10;
        } else {
            LOGGER.warn("Could not resolve albumin unit: '{}'", labValue.unit());
            return EvaluationFactory.create(EvaluationResult.UNDETERMINED);
        }

        return LaboratoryUtil.evaluateVersusMinValue(labValue.code(), convertedValue, labValue.comparator(), minAlbuminGPerDL);
    }
}
