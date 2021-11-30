package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.interpretation.LabInterpretation;
import com.hartwig.actin.clinical.interpretation.LabInterpreter;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.jetbrains.annotations.NotNull;

public class HasLimitedCreatinineULN implements EvaluationFunction {

    private final double maxCreatinineULN;

    HasLimitedCreatinineULN(final double maxCreatinineULN) {
        this.maxCreatinineULN = maxCreatinineULN;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        LabInterpretation interpretation = LabInterpreter.interpret(record.clinical().labValues());

        LabValue creatinine = interpretation.mostRecentValue(LabMeasurement.CREATININE);

        if (creatinine == null) {
            return Evaluation.UNDETERMINED;
        }

        Double upperLimit = creatinine.refLimitUp();
        if (upperLimit == null) {
            return Evaluation.UNDETERMINED;
        }

        double maxCreatinine = upperLimit * maxCreatinineULN;
        return LabValueEvaluation.evaluateVersusMaxValue(creatinine.value(), creatinine.comparator(), maxCreatinine);
    }
}
