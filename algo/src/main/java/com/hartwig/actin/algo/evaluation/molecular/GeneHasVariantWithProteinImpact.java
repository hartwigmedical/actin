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
        Set<String> canonicalProteinImpactMatches = Sets.newHashSet();
        Set<String> otherProteinImpactMatches = Sets.newHashSet();

        for (Variant variant : record.molecular().drivers().variants()) {
            if (variant.gene().equals(gene)) {
                for (String allowedProteinImpact : allowedProteinImpacts) {
                    if (variant.canonicalImpact().proteinImpact().equals(allowedProteinImpact)) {
                        canonicalProteinImpactMatches.add(allowedProteinImpact);
                    }

                    for (TranscriptImpact otherImpact : variant.otherImpacts()) {
                        if (otherImpact.proteinImpact().equals(allowedProteinImpact)) {
                            otherProteinImpactMatches.add(allowedProteinImpact);
                        }
                    }
                }
            }
        }

        if (!canonicalProteinImpactMatches.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addAllInclusionMolecularEvents(toInclusionEvents(canonicalProteinImpactMatches))
                    .addPassSpecificMessages("Variant(s) " + Format.concat(canonicalProteinImpactMatches) + " in gene " + gene
                            + " detected in canonical transcript")
                    .addPassGeneralMessages(Format.concat(canonicalProteinImpactMatches) + " found in " + gene)
                    .build();
        } else if (!otherProteinImpactMatches.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(toInclusionEvents(otherProteinImpactMatches))
                    .addWarnSpecificMessages("Variant(s) " + Format.concat(otherProteinImpactMatches) + " in " + gene
                            + " detected, but in non-canonical transcript")
                    .addWarnGeneralMessages(Format.concat(otherProteinImpactMatches) + " found in non-canonical transcript of gene " + gene)
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("None of " + Format.concat(allowedProteinImpacts) + " detected in gene " + gene)
                .addFailGeneralMessages("No specific variants in " + gene + " detected")
                .build();
    }

    @NotNull
    private Set<String> toInclusionEvents(@NotNull Set<String> proteinImpacts) {
        Set<String> inclusionEvents = Sets.newHashSet();
        for (String proteinImpact : proteinImpacts) {
            inclusionEvents.add(gene + " " + proteinImpact);
        }
        return inclusionEvents;
    }
}