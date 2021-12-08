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

public class HasSufficientAlbumin implements EvaluationFunction {

    private static final Logger LOGGER = LogManager.getLogger(HasSufficientAlbumin.class);

    private final double minAlbuminGPerDL;

    HasSufficientAlbumin(final double minAlbuminGPerDL) {
        this.minAlbuminGPerDL = minAlbuminGPerDL;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        LabInterpretation interpretation = LabInterpreter.interpret(record.clinical().labValues());

        LabValue albumin = interpretation.mostRecentValue(LabMeasurement.ALBUMIN);

        if (albumin == null) {
            return Evaluation.UNDETERMINED;
        }

        double convertedValue;
        if (albumin.unit().equals(LabUnit.G_PER_DL.display())) {
            convertedValue = albumin.value();
        } else if (albumin.unit().equals(LabUnit.G_PER_L.display())) {
            convertedValue = albumin.value() / 10;
        } else {
            LOGGER.warn("Could not resolve albumin unit: '{}'", albumin.unit());
            return Evaluation.UNDETERMINED;
        }

        return LabValueEvaluation.evaluateVersusMinValue(convertedValue, albumin.comparator(), minAlbuminGPerDL);
    }
}
