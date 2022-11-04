package com.hartwig.actin.algo.evaluation.molecular;

import javax.annotation.Nullable;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.treatment.input.datamodel.VariantTypeInput;

import org.jetbrains.annotations.NotNull;

//TODO: Implement
public class GeneHasVariantInExonRangeOfType implements EvaluationFunction {

    @NotNull
    private final String gene;
    private final int minExon;
    private final int maxExon;
    @Nullable
    private final VariantTypeInput requiredVariantType;

    GeneHasVariantInExonRangeOfType(@NotNull final String gene, final int minExon, final int maxExon,
            @Nullable final VariantTypeInput requiredVariantType) {
        this.gene = gene;
        this.minExon = minExon;
        this.maxExon = maxExon;
        this.requiredVariantType = requiredVariantType;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No variant in required exon range detected in gene " + gene)
                .addFailGeneralMessages("Molecular requirements")
                .build();
    }
}
