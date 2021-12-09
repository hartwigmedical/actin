package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.interpretation.LabInterpretation;
import com.hartwig.actin.clinical.interpretation.LabInterpreter;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.jetbrains.annotations.NotNull;

public class HasLimitedLabValueULN implements EvaluationFunction {

    @NotNull
    private final LabMeasurement measurement;
    private final double maxULN;

    HasLimitedLabValueULN(@NotNull final LabMeasurement measurement, final double maxULN) {
        this.measurement = measurement;
        this.maxULN = maxULN;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        LabInterpretation interpretation = LabInterpreter.interpret(record.clinical().labValues());

        LabValue value = interpretation.mostRecentValue(measurement);

        if (!LaboratoryUtil.existsWithExpectedUnit(value, measurement.expectedUnit())) {
            return Evaluation.UNDETERMINED;
        }

        return LaboratoryUtil.evaluateVersusMaxULN(value, maxULN);
    }
}
