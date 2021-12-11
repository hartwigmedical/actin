package com.hartwig.actin.algo.evaluation.surgery;

import java.time.LocalDate;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.Surgery;

import org.jetbrains.annotations.NotNull;

public class HasHadRecentSurgery implements EvaluationFunction {

    @NotNull
    private final LocalDate minDate;

    HasHadRecentSurgery(@NotNull final LocalDate minDate) {
        this.minDate = minDate;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        for (Surgery surgery : record.clinical().surgeries()) {
            if (minDate.isBefore(surgery.endDate())) {
                return Evaluation.PASS;
            }
        }

        return Evaluation.FAIL;
    }
}
