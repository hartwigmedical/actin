package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.interpretation.LabInterpretation;
import com.hartwig.actin.clinical.interpretation.LabInterpreter;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.jetbrains.annotations.NotNull;

public class HasSufficientAbsLeukocytes implements EvaluationFunction {

    private final double minLeukocytes;

    HasSufficientAbsLeukocytes(final double minLeukocytes) {
        this.minLeukocytes = minLeukocytes;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        LabInterpretation interpretation = LabInterpreter.interpret(record.clinical().labValues());

        LabValue leukocytes = interpretation.mostRecentValue(LabMeasurement.LEUKOCYTES_ABS);

        if (!LabValueEvaluation.existsWithExpectedUnit(leukocytes, LabMeasurement.LEUKOCYTES_ABS.expectedUnit())) {
            return Evaluation.UNDETERMINED;
        }

        return LabValueEvaluation.evaluateVersusMinValue(leukocytes.value(), leukocytes.comparator(), minLeukocytes);
    }
}
