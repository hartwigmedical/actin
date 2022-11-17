package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.molecular.util.MolecularCharacteristicEvents;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HasTumorMutationalLoadWithinRange implements EvaluationFunction {

    private final int minTumorMutationalLoad;
    @Nullable
    private final Integer maxTumorMutationalLoad;

    HasTumorMutationalLoadWithinRange(final int minTumorMutationalLoad, @Nullable final Integer maxTumorMutationalLoad) {
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

        boolean meetsMinTumorLoad = tumorMutationalLoad >= minTumorMutationalLoad;
        boolean meetsMaxTumorLoad = maxTumorMutationalLoad == null || tumorMutationalLoad <= maxTumorMutationalLoad;
        boolean tumorMutationalLoadIsAllowed = meetsMinTumorLoad && meetsMaxTumorLoad;

        if (tumorMutationalLoadIsAllowed) {
            if (maxTumorMutationalLoad == null) {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addPassSpecificMessages("TML of sample " + tumorMutationalLoad + " is higher than requested minimal TML of "
                                + minTumorMutationalLoad)
                        .addPassGeneralMessages("Adequate TML")
                        .addInclusionMolecularEvents(MolecularCharacteristicEvents.HIGH_TUMOR_MUTATIONAL_LOAD)
                        .build();
            } else {
                return EvaluationFactory.unrecoverable()
                        .result(EvaluationResult.PASS)
                        .addPassSpecificMessages(
                                "TML of sample " + tumorMutationalLoad + " is between requested TML range of " + minTumorMutationalLoad
                                        + " - " + maxTumorMutationalLoad)
                        .addPassGeneralMessages("Adequate TML")
                        .addInclusionMolecularEvents(MolecularCharacteristicEvents.ADEQUATE_TUMOR_MUTATIONAL_LOAD)
                        .build();
            }
        }

        boolean tumorMutationalLoadIsAlmostAllowed = minTumorMutationalLoad - tumorMutationalLoad <= 5;
        boolean hasSufficientQuality = record.molecular().hasSufficientQuality();

        if (tumorMutationalLoadIsAlmostAllowed && !hasSufficientQuality) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addWarnSpecificMessages("TML of sample " + tumorMutationalLoad + " almost exceeds " + minTumorMutationalLoad
                            + " while data quality is insufficient (perhaps a few mutations are missed)")
                    .addWarnGeneralMessages("Inadequate TML")
                    .addInclusionMolecularEvents(MolecularCharacteristicEvents.ALMOST_SUFFICIENT_TUMOR_MUTATIONAL_LOAD)
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("TML of sample " + tumorMutationalLoad + " is not within specified range")
                .addFailGeneralMessages("Molecular requirements")
                .build();
    }
}
