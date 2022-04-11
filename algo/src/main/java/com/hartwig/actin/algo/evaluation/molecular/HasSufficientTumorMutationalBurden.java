package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
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
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages("No tumor mutational burden is known")
                    .build();
        }

        EvaluationResult result =
                Double.compare(tumorMutationalBurden, minTumorMutationalBurden) >= 0 ? EvaluationResult.PASS : EvaluationResult.FAIL;

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable().result(result);
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Tumor mutational burden does not exceed " + minTumorMutationalBurden);
            builder.addFailGeneralMessages("Molecular requirements");
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Tumor mutational burden exceeds " + minTumorMutationalBurden);
            builder.addPassGeneralMessages("Molecular requirements");
        }

        return builder.build();
    }
}
