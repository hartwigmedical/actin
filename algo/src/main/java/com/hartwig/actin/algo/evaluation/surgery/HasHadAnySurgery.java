package com.hartwig.actin.algo.evaluation.surgery;

import java.time.LocalDate;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.Surgery;

import org.jetbrains.annotations.NotNull;

public class HasHadAnySurgery implements EvaluationFunction {

    @NotNull
    private final LocalDate minDate;

    HasHadAnySurgery(@NotNull final LocalDate minDate) {
        this.minDate = minDate;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasHadRecentSurgery = false;
        for (Surgery surgery : record.clinical().surgeries()) {
            if (!surgery.endDate().isBefore(minDate)) {
                hasHadRecentSurgery = true;
            }
        }

        EvaluationResult result = hasHadRecentSurgery ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient has had no recent surgeries");
            builder.addFailGeneralMessages("No recent surgeries");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient has had at least one recent surgery");
            builder.addPassGeneralMessages("Recent surgeries");
        }

        return builder.build();
    }
}
