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
                String variantEvent = MolecularEventFactory.event(variant);
                for (String allowedCodingImpact : allowedCodingImpacts) {
                    if (variant.canonicalImpact().hgvsCodingImpact().equals(allowedCodingImpact)) {
                        canonicalCodingImpactMatches.add(allowedCodingImpact);
                        if (variant.isReportable()) {
                            canonicalReportableVariantMatches.add(variantEvent);
                        } else {
                            canonicalUnreportableVariantMatches.add(variantEvent);
                        }
                    }

                    if (variant.isReportable()) {
                        for (TranscriptImpact otherImpact : variant.otherImpacts()) {
                            if (otherImpact.hgvsCodingImpact().equals(allowedCodingImpact)) {
                                reportableOtherVariantMatches.add(variantEvent);
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
        } else if (!canonicalUnreportableVariantMatches.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(canonicalUnreportableVariantMatches)
                    .addWarnSpecificMessages("Variant(s) " + Format.concat(reportableOtherCodingImpactMatches) + " in " + gene
                            + " detected in canonical transcript, but considered non-reportable")
                    .addWarnGeneralMessages(Format.concat(canonicalCodingImpactMatches) + " found in " + gene)
                    .build();
        } else if (!reportableOtherVariantMatches.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(reportableOtherVariantMatches)
                    .addWarnSpecificMessages("Variant(s) " + Format.concat(reportableOtherCodingImpactMatches) + " in " + gene
                            + " detected, but in non-canonical transcript")
                    .addWarnGeneralMessages(Format.concat(reportableOtherCodingImpactMatches) + " found in non-canonical transcript of " + gene)
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("None of " + Format.concat(allowedCodingImpacts) + " detected in gene " + gene)
                .addFailGeneralMessages("No specific variants in " + gene + " detected")
                .build();
    }
}