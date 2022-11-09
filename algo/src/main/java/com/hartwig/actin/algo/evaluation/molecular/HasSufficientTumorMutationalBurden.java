package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

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
                    .build();
        } else if (tumorMutationalBurdenIsAlmostAllowed && !hasSufficientQuality) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addWarnSpecificMessages("TMB of sample " + tumorMutationalBurden + " almost exceeds " + minTumorMutationalBurden
                            + " while data quality is insufficient")
                    .addWarnGeneralMessages("Inadequate TMB")
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
