package com.hartwig.actin.algo.evaluation.molecular;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.molecular.datamodel.driver.CodingEffect;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.Variant;

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
        Set<String> fusionSkippingEvents = Sets.newHashSet();
        for (Fusion fusion : record.molecular().drivers().fusions()) {
            if (fusion.isReportable() && fusion.geneStart().equals(gene) && fusion.geneEnd().equals(gene)
                    && fusion.fusedExonUp() == exonToSkip - 1 && fusion.fusedExonDown() == exonToSkip + 1) {
                fusionSkippingEvents.add(fusion.event());
            }
        }

        Set<String> exonSplicingVariants = Sets.newHashSet();
        for (Variant variant : record.molecular().drivers().variants()) {
            boolean isCanonicalSplice =
                    variant.canonicalImpact().codingEffect() == CodingEffect.SPLICE || variant.canonicalImpact().isSpliceRegion();
            Integer canonicalExonAffected = variant.canonicalImpact().affectedExon();
            boolean isCanonicalExonAffected = canonicalExonAffected != null && canonicalExonAffected == exonToSkip;

            if (variant.isReportable() && variant.gene().equals(gene) && isCanonicalExonAffected && isCanonicalSplice) {
                exonSplicingVariants.add(variant.event());
            }
        }

        if (!fusionSkippingEvents.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addAllInclusionMolecularEvents(fusionSkippingEvents)
                    .addPassSpecificMessages(
                            "Exon " + exonToSkip + " skipped in gene " + gene + " due to " + Format.concat(fusionSkippingEvents))
                    .addPassGeneralMessages(gene + " exon " + exonToSkip + " skipping")
                    .build();
        }

        if (!exonSplicingVariants.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(exonSplicingVariants)
                    .addWarnSpecificMessages(
                            "Exon " + exonToSkip + " may be skipped in gene " + gene + " due to " + Format.concat(exonSplicingVariants))
                    .addWarnGeneralMessages("Potential " + gene + " exon " + exonToSkip + " skipping")
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No " + gene + " exon " + exonToSkip + " skipping")
                .addFailGeneralMessages("Molecular requirements")
                .build();
    }
}
