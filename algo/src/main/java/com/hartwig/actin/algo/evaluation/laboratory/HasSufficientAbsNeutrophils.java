package com.hartwig.actin.algo.evaluation.laboratory;

import com.google.common.annotations.VisibleForTesting;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.interpretation.LabInterpretation;
import com.hartwig.actin.clinical.interpretation.LabInterpreter;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HasSufficientAbsNeutrophils implements EvaluationFunction {

    private final double minNeutrophils;

    HasSufficientAbsNeutrophils(final double minNeutrophils) {
        this.minNeutrophils = minNeutrophils;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        LabInterpretation interpretation = LabInterpreter.interpret(record.clinical().labValues());

        LabValue neutrophils1 = interpretation.mostRecentValue(LabMeasurement.NEUTROPHILS_ABS);
        LabValue neutrophils2 = interpretation.mostRecentValue(LabMeasurement.NEUTROPHILS_ABS_EDA);

        LabValue best = pickBest(neutrophils1, neutrophils2);
        if (best == null || !(best.unit().equals(LabMeasurement.NEUTROPHILS_ABS.expectedUnit()) || best.unit()
                .equals(LabMeasurement.NEUTROPHILS_ABS_EDA.expectedUnit()))) {
            return Evaluation.UNDETERMINED;
        }

        return LabValueEvaluation.evaluateVersusMinValue(best.value(), best.comparator(), minNeutrophils);
    }

    @Nullable
    @VisibleForTesting
    static LabValue pickBest(@Nullable LabValue neutrophils1, @Nullable LabValue neutrophils2) {
        if (neutrophils1 == null) {
            return neutrophils2;
        } else if (neutrophils2 == null) {
            return neutrophils1;
        } else {
            return Double.compare(neutrophils1.value(), neutrophils2.value()) >= 0 ? neutrophils1 : neutrophils2;
        }
    }
}
