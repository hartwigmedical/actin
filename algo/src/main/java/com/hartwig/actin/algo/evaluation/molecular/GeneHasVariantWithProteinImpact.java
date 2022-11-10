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
import com.hartwig.actin.molecular.util.MolecularEventFactory;

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
        Set<String> canonicalReportableVariantMatches = Sets.newHashSet();
        Set<String> canonicalUnreportableVariantMatches = Sets.newHashSet();
        Set<String> canonicalProteinImpactMatches = Sets.newHashSet();

        Set<String> reportableOtherVariantMatches = Sets.newHashSet();
        Set<String> reportableOtherProteinImpactMatches = Sets.newHashSet();

        for (Variant variant : record.molecular().drivers().variants()) {
            if (variant.gene().equals(gene)) {
                String variantEvent = MolecularEventFactory.event(variant);
                for (String allowedProteinImpact : allowedProteinImpacts) {
                    if (variant.canonicalImpact().hgvsProteinImpact().equals(allowedProteinImpact)) {
                        canonicalProteinImpactMatches.add(allowedProteinImpact);
                        if (variant.isReportable()) {
                            canonicalReportableVariantMatches.add(variantEvent);
                        } else {
                            canonicalUnreportableVariantMatches.add(variantEvent);
                        }
                    }

                    if (variant.isReportable()) {
                        for (TranscriptImpact otherImpact : variant.otherImpacts()) {
                            if (otherImpact.hgvsProteinImpact().equals(allowedProteinImpact)) {
                                reportableOtherVariantMatches.add(variantEvent);
                                reportableOtherProteinImpactMatches.add(allowedProteinImpact);
                            }
                        }
                    }
                }
            }
        }

        if (!canonicalReportableVariantMatches.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addAllInclusionMolecularEvents(canonicalReportableVariantMatches)
                    .addPassSpecificMessages("Variant(s) " + Format.concat(canonicalProteinImpactMatches) + " in gene " + gene
                            + " detected in canonical transcript")
                    .addPassGeneralMessages(Format.concat(canonicalProteinImpactMatches) + " found in " + gene)
                    .build();
        } else if (!canonicalUnreportableVariantMatches.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(canonicalUnreportableVariantMatches)
                    .addWarnSpecificMessages("Variant(s) " + Format.concat(canonicalProteinImpactMatches) + " in " + gene
                            + " detected in canonical transcript, but are non-reportable")
                    .addWarnGeneralMessages(Format.concat(canonicalProteinImpactMatches) + " found in " + gene)
                    .build();
        } else if (!reportableOtherVariantMatches.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(reportableOtherVariantMatches)
                    .addWarnSpecificMessages("Variant(s) " + Format.concat(reportableOtherProteinImpactMatches) + " in " + gene
                            + " detected, but in non-canonical transcript")
                    .addWarnGeneralMessages(
                            Format.concat(reportableOtherProteinImpactMatches) + " found in non-canonical transcript of gene " + gene)
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("None of " + Format.concat(allowedProteinImpacts) + " detected in gene " + gene)
                .addFailGeneralMessages("No specific variants in " + gene + " detected")
                .build();
    }
}