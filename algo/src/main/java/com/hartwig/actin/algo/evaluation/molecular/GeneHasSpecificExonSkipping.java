package com.hartwig.actin.algo.evaluation.molecular;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;

import org.jetbrains.annotations.NotNull;

public class GeneHasSpecificExonSkipping implements EvaluationFunction {

    @NotNull
    private final String gene;
    private final int exonToSkip;

    GeneHasSpecificExonSkipping(@NotNull final String gene, final int exonToSkip) {
        this.gene = gene;
        this.exonToSkip = exonToSkip;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasFusionSkippingExon = false;
        boolean hasSpliceVariantInExon = false;

        for (Fusion fusion : record.molecular().drivers().fusions()) {
            if (fusion.isReportable() && fusion.geneStart().equals(gene) && fusion.geneEnd().equals(gene)) {

            }
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("Specific exon skipping of a certain gene currently cannot be determined")
                .addUndeterminedGeneralMessages("Undetermined exon skipping in gene")
                .build();
    }
}
