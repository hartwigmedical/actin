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

public class GeneHasVariantWithCodingImpact implements EvaluationFunction {

    @NotNull
    private final String gene;
    @NotNull
    private final List<String> allowedCodingImpacts;

    GeneHasVariantWithCodingImpact(@NotNull final String gene, @NotNull final List<String> allowedCodingImpacts) {
        this.gene = gene;
        this.allowedCodingImpacts = allowedCodingImpacts;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> canonicalReportableVariantMatches = Sets.newHashSet();
        Set<String> canonicalUnreportableVariantMatches = Sets.newHashSet();
        Set<String> canonicalCodingImpactMatches = Sets.newHashSet();

        Set<String> reportableOtherVariantMatches = Sets.newHashSet();
        Set<String> reportableOtherCodingImpactMatches = Sets.newHashSet();

        for (Variant variant : record.molecular().drivers().variants()) {
            if (variant.gene().equals(gene)) {
                for (String allowedCodingImpact : allowedCodingImpacts) {
                    if (variant.canonicalImpact().hgvsCodingImpact().equals(allowedCodingImpact)) {
                        canonicalCodingImpactMatches.add(allowedCodingImpact);
                        if (variant.isReportable()) {
                            canonicalReportableVariantMatches.add(variant.event());
                        } else {
                            canonicalUnreportableVariantMatches.add(variant.event());
                        }
                    }

                    if (variant.isReportable()) {
                        for (TranscriptImpact otherImpact : variant.otherImpacts()) {
                            if (otherImpact.hgvsCodingImpact().equals(allowedCodingImpact)) {
                                reportableOtherVariantMatches.add(variant.event());
                                reportableOtherCodingImpactMatches.add(allowedCodingImpact);
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
                    .addPassSpecificMessages("Variant(s) " + Format.concat(canonicalCodingImpactMatches) + " in gene " + gene
                            + " detected in canonical transcript")
                    .addPassGeneralMessages(Format.concat(canonicalCodingImpactMatches) + " found in " + gene)
                    .build();
        }

        Evaluation potentialWarnEvaluation = evaluatePotentialWarns(canonicalUnreportableVariantMatches,
                canonicalCodingImpactMatches,
                reportableOtherVariantMatches,
                reportableOtherCodingImpactMatches);

        if (potentialWarnEvaluation != null) {
            return potentialWarnEvaluation;
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("None of " + Format.concat(allowedCodingImpacts) + " detected in gene " + gene)
                .addFailGeneralMessages("No specific variants in " + gene + " detected")
                .build();
    }

    @Nullable
    private Evaluation evaluatePotentialWarns(@NotNull Set<String> canonicalUnreportableVariantMatches,
            @NotNull Set<String> canonicalCodingImpactMatches, @NotNull Set<String> reportableOtherVariantMatches,
            @NotNull Set<String> reportableOtherCodingImpactMatches) {
        Set<String> warnEvents = Sets.newHashSet();
        Set<String> warnSpecificMessages = Sets.newHashSet();
        Set<String> warnGeneralMessages = Sets.newHashSet();

        if (!canonicalUnreportableVariantMatches.isEmpty()) {
            warnEvents.addAll(canonicalUnreportableVariantMatches);
            warnSpecificMessages.add("Variant(s) " + Format.concat(canonicalCodingImpactMatches) + " in " + gene
                    + " detected in canonical transcript, but considered non-reportable");
            warnGeneralMessages.add(Format.concat(canonicalCodingImpactMatches) + " found in " + gene);
        }

        if (!reportableOtherVariantMatches.isEmpty()) {
            warnEvents.addAll(reportableOtherVariantMatches);
            warnSpecificMessages.add("Variant(s) " + Format.concat(reportableOtherCodingImpactMatches) + " in " + gene
                    + " detected, but in non-canonical transcript");
            warnGeneralMessages.add(Format.concat(reportableOtherCodingImpactMatches) + " found in non-canonical transcript of " + gene);
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