package com.hartwig.actin.algo.evaluation.molecular;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

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
        Set<String> canonicalReportableVariantMatches = Sets.newHashSet();
        Set<String> canonicalUnreportableVariantMatches = Sets.newHashSet();
        Set<String> canonicalProteinImpactMatches = Sets.newHashSet();

        Set<String> reportableOtherVariantMatches = Sets.newHashSet();
        Set<String> reportableOtherProteinImpactMatches = Sets.newHashSet();

        for (Variant variant : record.molecular().drivers().variants()) {
            if (variant.gene().equals(gene)) {
                for (String allowedProteinImpact : allowedProteinImpacts) {
                    if (variant.canonicalImpact().hgvsProteinImpact().equals(allowedProteinImpact)) {
                        canonicalProteinImpactMatches.add(allowedProteinImpact);
                        if (variant.isReportable()) {
                            canonicalReportableVariantMatches.add(variant.event());
                        } else {
                            canonicalUnreportableVariantMatches.add(variant.event());
                        }
                    }

                    if (variant.isReportable()) {
                        for (TranscriptImpact otherImpact : variant.otherImpacts()) {
                            if (otherImpact.hgvsProteinImpact().equals(allowedProteinImpact)) {
                                reportableOtherVariantMatches.add(variant.event());
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
        }

        Evaluation potentialWarnEvaluation = evaluatePotentialWarns(canonicalUnreportableVariantMatches,
                canonicalProteinImpactMatches,
                reportableOtherVariantMatches,
                reportableOtherProteinImpactMatches);

        if (potentialWarnEvaluation != null) {
            return potentialWarnEvaluation;
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("None of " + Format.concat(allowedProteinImpacts) + " detected in gene " + gene)
                .addFailGeneralMessages("No specific variants in " + gene + " detected")
                .build();
    }

    @Nullable
    private Evaluation evaluatePotentialWarns(@NotNull Set<String> canonicalUnreportableVariantMatches,
            @NotNull Set<String> canonicalProteinImpactMatches, @NotNull Set<String> reportableOtherVariantMatches,
            @NotNull Set<String> reportableOtherProteinImpactMatches) {
        Set<String> warnEvents = Sets.newHashSet();
        Set<String> warnSpecificMessages = Sets.newHashSet();
        Set<String> warnGeneralMessages = Sets.newHashSet();

        if (!canonicalUnreportableVariantMatches.isEmpty()) {
            warnEvents.addAll(canonicalUnreportableVariantMatches);
            warnSpecificMessages.add("Variant(s) " + Format.concat(canonicalProteinImpactMatches) + " in " + gene
                    + " detected in canonical transcript, but are non-reportable");
            warnGeneralMessages.add(Format.concat(canonicalProteinImpactMatches) + " found in " + gene);
        }

        if (!reportableOtherVariantMatches.isEmpty()) {
            warnEvents.addAll(reportableOtherVariantMatches);
            warnSpecificMessages.add("Variant(s) " + Format.concat(reportableOtherProteinImpactMatches) + " in " + gene
                    + " detected, but in non-canonical transcript");
            warnGeneralMessages.add(
                    Format.concat(reportableOtherProteinImpactMatches) + " found in non-canonical transcript of gene " + gene);
        }

        if (!warnEvents.isEmpty() && !warnSpecificMessages.isEmpty() && !warnGeneralMessages.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(warnEvents)
                    .addAllWarnSpecificMessages(warnSpecificMessages)
                    .addAllWarnGeneralMessages(warnGeneralMessages)
                    .build();
        }

        return null;
    }
}