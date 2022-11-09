package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.molecular.util.MolecularEventFactory;

import org.jetbrains.annotations.NotNull;

public class HasSufficientTumorMutationalBurden implements EvaluationFunction {

    private final double minTumorMutationalBurden;

    HasSufficientTumorMutationalBurden(final double minTumorMutationalBurden) {
        this.minTumorMutationalBurden = minTumorMutationalBurden;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Double tumorMutationalBurden = record.molecular().characteristics().tumorMutationalBurden();
        if (tumorMutationalBurden == null) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("Unknown tumor mutational burden (TMB)")
                    .addFailGeneralMessages("Unknown TMB")
                    .build();
        }

        boolean tumorMutationalBurdenIsAllowed = tumorMutationalBurden >= minTumorMutationalBurden;
        boolean tumorMutationalBurdenIsAlmostAllowed = minTumorMutationalBurden - tumorMutationalBurden <= 5;
        boolean hasSufficientQuality = record.molecular().hasSufficientQuality();

        if (tumorMutationalBurdenIsAllowed) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("TMB of sample " + tumorMutationalBurden + " is sufficient")
                    .addPassGeneralMessages("Adequate TMB")
                    .addInclusionMolecularEvents(MolecularEventFactory.HIGH_TUMOR_MUTATIONAL_BURDEN)
                    .build();
        } else if (tumorMutationalBurdenIsAlmostAllowed && !hasSufficientQuality) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addWarnSpecificMessages("TMB of sample " + tumorMutationalBurden + " almost exceeds " + minTumorMutationalBurden
                            + " while data quality is insufficient (perhaps a few mutations are missed)")
                    .addWarnGeneralMessages("Inadequate TMB")
                    .addInclusionMolecularEvents(MolecularEventFactory.ALMOST_SUFFICIENT_TUMOR_MUTATIONAL_BURDEN)
                    .build();
        } else {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages("TMB of sample " + tumorMutationalBurden + " is not within specified range")
                    .addFailGeneralMessages("Inadequate TMB")
                    .build();

        }
    }
}
