package com.hartwig.actin.algo.evaluation.molecular;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;

import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.molecular.datamodel.driver.TranscriptImpact;
import com.hartwig.actin.molecular.datamodel.driver.Variant;

import org.jetbrains.annotations.NotNull;

public class GeneHasVariantWithProteinImpact implements EvaluationFunction {

    @NotNull
    private final String gene;
    @NotNull
    private final List<String> allowedProteinImpacts;

    GeneHasVariantWithProteinImpact(@NotNull final String gene, @NotNull final List<String> allowedProteinImpacts) {
        this.gene = gene;
        this.allowedProteinImpacts = allowedProteinImpacts;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> proteinImpactsCanonicalFound = Sets.newHashSet();
        Set<String> proteinImpactsOtherFound = Sets.newHashSet();

        for (Variant variant : record.molecular().drivers().variants()) {
            if (variant.gene().equals(gene)) {
                for (String allowedProteinImpact : allowedProteinImpacts) {
                    boolean proteinImpactOtherMatch = false;
                    for (TranscriptImpact otherImpact : variant.otherImpacts()) {
                        if (otherImpact.proteinImpact().equals(allowedProteinImpact)) {
                            proteinImpactOtherMatch = true;
                        }
                    }

                    if (variant.canonicalImpact().proteinImpact().equals(allowedProteinImpact)) {
                        proteinImpactsCanonicalFound.add(allowedProteinImpact);
                    }

                    if (proteinImpactOtherMatch) {
                        proteinImpactsOtherFound.add(allowedProteinImpact);
                    }
                }
            }
        }

        if (!proteinImpactsCanonicalFound.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Variant(s) " + Format.concat(proteinImpactsCanonicalFound) + " in gene " + gene
                            + " detected in canonical transcript")
                    .addPassGeneralMessages(Format.concat(proteinImpactsCanonicalFound) + " found in " + gene)
                    .build();
        } else if (!proteinImpactsOtherFound.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addWarnSpecificMessages("Variant(s) " + Format.concat(proteinImpactsCanonicalFound) + " in " + gene
                            + " detected, but in non-canonical transcript")
                    .addWarnGeneralMessages(
                            Format.concat(proteinImpactsCanonicalFound) + " found in non-canonical transcript of gene " + gene)
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("None of " + Format.concat(allowedProteinImpacts) + " detected in gene " + gene)
                .addFailGeneralMessages("No specific variants in " + gene + " detected")
                .build();
    }
}