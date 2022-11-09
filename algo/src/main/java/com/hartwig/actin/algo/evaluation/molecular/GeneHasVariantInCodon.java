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
import org.jetbrains.annotations.Nullable;

public class GeneHasVariantInCodon implements EvaluationFunction {

    @NotNull
    private final String gene;
    @NotNull
    private final List<String> codons;

    GeneHasVariantInCodon(@NotNull final String gene, @NotNull final List<String> codons) {
        this.gene = gene;
        this.codons = codons;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> canonicalReportableVariantMatches = Sets.newHashSet();
        Set<String> canonicalUnreportableVariantMatches = Sets.newHashSet();
        Set<String> canonicalCodonMatches = Sets.newHashSet();

        Set<String> reportableOtherVariantMatches = Sets.newHashSet();
        Set<String> reportableOtherCodonMatches = Sets.newHashSet();

        for (Variant variant : record.molecular().drivers().variants()) {
            if (variant.gene().equals(gene)) {
                for (String codon : codons) {
                    if (isCodonMatch(variant.canonicalImpact().affectedCodon(), codon)) {
                        canonicalCodonMatches.add(codon);
                        if (variant.isReportable()) {
                            canonicalReportableVariantMatches.add(MolecularEventFactory.variantEvent(variant));
                        } else {
                            canonicalUnreportableVariantMatches.add(MolecularEventFactory.variantEvent(variant));
                        }
                    }

                    if (variant.isReportable()) {
                        for (TranscriptImpact otherImpact : variant.otherImpacts()) {
                            if (isCodonMatch(otherImpact.affectedCodon(), codon)) {
                                reportableOtherVariantMatches.add(MolecularEventFactory.variantEvent(variant));
                                reportableOtherCodonMatches.add(codon);
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
                    .addPassSpecificMessages("Variant(s) in codon(s) " + Format.concat(canonicalCodonMatches) + " in gene " + gene
                            + " detected in canonical transcript")
                    .addPassGeneralMessages("Variant(s) in codon(s) " + Format.concat(canonicalCodonMatches) + " found in " + gene)
                    .build();
        } else if (!canonicalUnreportableVariantMatches.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(canonicalUnreportableVariantMatches)
                    .addWarnSpecificMessages("Variant(s) in codon(s) " + Format.concat(canonicalCodonMatches) + " in " + gene
                            + " detected in canonical transcript, but not considered reportable")
                    .addWarnGeneralMessages(
                            "Variant(s) in codon(s) " + Format.concat(canonicalCodonMatches) + " found in canonical transcript of gene "
                                    + gene)
                    .build();
        } else if (!reportableOtherCodonMatches.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(reportableOtherVariantMatches)
                    .addWarnSpecificMessages("Variant(s) in codon(s) " + Format.concat(reportableOtherCodonMatches) + " in " + gene
                            + " detected, but in non-canonical transcript")
                    .addWarnGeneralMessages(
                            "Variant(s) in codon(s) " + Format.concat(canonicalCodonMatches) + " found in non-canonical transcript of gene "
                                    + gene)
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No variants in codon(s) " + Format.concat(codons) + " detected in gene " + gene)
                .addFailGeneralMessages("No specific variants in codon(s) in " + gene + " detected")
                .build();
    }

    private static boolean isCodonMatch(@Nullable Integer affectedCodon, @NotNull String codonToMatch) {
        if (affectedCodon == null) {
            return false;
        }

        // TODO Make a more explicit datamodel.
        int codonIndexToMatch = Integer.parseInt(codonToMatch.substring(1));
        return codonIndexToMatch == affectedCodon;
    }
}