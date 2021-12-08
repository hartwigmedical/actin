package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.interpretation.LabInterpretation;
import com.hartwig.actin.clinical.interpretation.LabInterpreter;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.jetbrains.annotations.NotNull;

public class HasSufficientAbsLeukocytesLLN implements EvaluationFunction {

    private final double minLeukocytesLLN;

    HasSufficientAbsLeukocytesLLN(final double minLeukocytesLLN) {
        this.minLeukocytesLLN = minLeukocytesLLN;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        LabInterpretation interpretation = LabInterpreter.interpret(record.clinical().labValues());

        LabValue leukocytes = interpretation.mostRecentValue(LabMeasurement.LEUKOCYTES_ABS);

        if (!LabValueEvaluation.existsWithExpectedUnit(leukocytes, LabMeasurement.LEUKOCYTES_ABS.expectedUnit())) {
            return Evaluation.UNDETERMINED;
        }

        Double lowerLimit = leukocytes.refLimitLow();
        if (lowerLimit == null) {
            return Evaluation.UNDETERMINED;
        }

        double minLeukocytes = lowerLimit * minLeukocytesLLN;
        return LabValueEvaluation.evaluateVersusMinValue(leukocytes.value(), leukocytes.comparator(), minLeukocytes);
    }
}
