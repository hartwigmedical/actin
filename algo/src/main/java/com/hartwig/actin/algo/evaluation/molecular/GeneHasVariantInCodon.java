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
import org.jetbrains.annotations.Nullable;

public class GeneHasVariantInCodon implements EvaluationFunction {

    private static final double CLONAL_CUTOFF = 0.5;
    private static final double CLONAL_CUTOFF_PERCENTAGE = CLONAL_CUTOFF * 100;

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
        Set<String> canonicalReportableSubclonalVariantMatches = Sets.newHashSet();
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
                            if (variant.clonalLikelihood() < CLONAL_CUTOFF) {
                                canonicalReportableSubclonalVariantMatches.add(variant.event());
                            } else {
                                canonicalReportableVariantMatches.add(variant.event());
                            }
                        } else {
                            canonicalUnreportableVariantMatches.add(variant.event());
                        }
                    }

                    if (variant.isReportable()) {
                        for (TranscriptImpact otherImpact : variant.otherImpacts()) {
                            if (isCodonMatch(otherImpact.affectedCodon(), codon)) {
                                reportableOtherVariantMatches.add(variant.event());
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
        }

        Evaluation potentialWarnEvaluation = evaluatePotentialWarns(canonicalReportableSubclonalVariantMatches,
                canonicalUnreportableVariantMatches,
                canonicalCodonMatches,
                reportableOtherVariantMatches,
                reportableOtherCodonMatches);

        if (potentialWarnEvaluation != null) {
            return potentialWarnEvaluation;
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

        int codonIndexToMatch = Integer.parseInt(codonToMatch.substring(1));
        return codonIndexToMatch == affectedCodon;
    }

    @Nullable
    private Evaluation evaluatePotentialWarns(@NotNull Set<String> canonicalReportableSubclonalVariantMatches,
            @NotNull Set<String> canonicalUnreportableVariantMatches, @NotNull Set<String> canonicalCodonMatches,
            @NotNull Set<String> reportableOtherVariantMatches, @NotNull Set<String> reportableOtherCodonMatches) {
        Set<String> warnEvents = Sets.newHashSet();
        Set<String> warnSpecificMessages = Sets.newHashSet();
        Set<String> warnGeneralMessages = Sets.newHashSet();

        if (!canonicalReportableSubclonalVariantMatches.isEmpty()) {
            warnEvents.addAll(canonicalReportableSubclonalVariantMatches);
            warnSpecificMessages.add("Variant(s) in codon(s) " + Format.concat(canonicalReportableSubclonalVariantMatches) + " in " + gene
                    + " detected in canonical transcript, but subclonal likelihood of >" + CLONAL_CUTOFF_PERCENTAGE + "%");
            warnGeneralMessages.add(
                    "Variant(s) in codon(s) " + Format.concat(canonicalReportableSubclonalVariantMatches) + " found in " + gene
                            + " but subclonal likelihood of >" + CLONAL_CUTOFF_PERCENTAGE + "%");
        }

        if (!canonicalUnreportableVariantMatches.isEmpty()) {
            warnEvents.addAll(canonicalUnreportableVariantMatches);
            warnSpecificMessages.add("Variant(s) in codon(s) " + Format.concat(canonicalCodonMatches) + " in " + gene
                    + " detected in canonical transcript, but not considered reportable");
            warnGeneralMessages.add(
                    "Variant(s) in codon(s) " + Format.concat(canonicalCodonMatches) + " found in canonical transcript of gene " + gene);
        }

        if (!reportableOtherVariantMatches.isEmpty()) {
            warnEvents.addAll(reportableOtherVariantMatches);
            warnSpecificMessages.add("Variant(s) in codon(s) " + Format.concat(reportableOtherCodonMatches) + " in " + gene
                    + " detected, but in non-canonical transcript");
            warnGeneralMessages.add(
                    "Variant(s) in codon(s) " + Format.concat(canonicalCodonMatches) + " found in non-canonical transcript of gene "
                            + gene);
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