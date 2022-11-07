package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

public class HasCertainTumorMutationalLoad implements EvaluationFunction {

    @Nullable
    private final Integer minTumorMutationalLoad;
    private final Integer maxTumorMutationalLoad;

    public HasCertainTumorMutationalLoad(@Nullable final Integer minTumorMutationalLoad, final Integer maxTumorMutationalLoad) {
        this.minTumorMutationalLoad = minTumorMutationalLoad;
        this.maxTumorMutationalLoad = maxTumorMutationalLoad;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Integer tumorMutationalLoad = record.molecular().characteristics().tumorMutationalLoad();

        if (tumorMutationalLoad == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Unknown tumor mutational load (TML)")
                    .addFailGeneralMessages("Unknown TML")
                    .build();
        }

        // TODO: Add purity implementation

        boolean tumorMutationalLoadIsAllowed = false;
        if ((minTumorMutationalLoad == null && maxTumorMutationalLoad != null && tumorMutationalLoad <= maxTumorMutationalLoad) || (
                minTumorMutationalLoad != null && maxTumorMutationalLoad == null && tumorMutationalLoad >= minTumorMutationalLoad) || (
                minTumorMutationalLoad != null && maxTumorMutationalLoad != null && tumorMutationalLoad >= minTumorMutationalLoad
                        && tumorMutationalLoad <= maxTumorMutationalLoad)) {
            tumorMutationalLoadIsAllowed = true;
        }

        if (tumorMutationalLoadIsAllowed) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("TML of sample " + tumorMutationalLoad + " is within specified range")
                    .addPassGeneralMessages("Adequate TML")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("TML of sample " + tumorMutationalLoad + " is not within specified range")
                .addFailGeneralMessages("Inadequate TML")
                .build();
    }
}
