package com.hartwig.actin.algo.evaluation.molecular;

import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.molecular.datamodel.driver.TranscriptImpact;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.driver.VariantType;
import com.hartwig.actin.treatment.input.datamodel.VariantTypeInput;

import org.jetbrains.annotations.NotNull;

public class GeneHasVariantInExonRangeOfType implements EvaluationFunction {

    @NotNull
    private final String gene;
    private final int minExon;
    private final int maxExon;
    @Nullable
    private final VariantTypeInput requiredVariantType;

    GeneHasVariantInExonRangeOfType(@NotNull final String gene, final int minExon, final int maxExon,
            @Nullable final VariantTypeInput requiredVariantType) {
        this.gene = gene;
        this.minExon = minExon;
        this.maxExon = maxExon;
        this.requiredVariantType = requiredVariantType;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<VariantType> allowedVariantTypes = determineAllowedVariantTypes(requiredVariantType);

        Set<String> canonicalReportableVariantMatches = Sets.newHashSet();
        Set<String> canonicalUnreportableVariantMatches = Sets.newHashSet();
        Set<String> reportableOtherVariantMatches = Sets.newHashSet();

        for (Variant variant : record.molecular().drivers().variants()) {
            if (variant.gene().equals(gene) && allowedVariantTypes.contains(variant.type())) {
                if (hasEffectInExonRange(variant.canonicalImpact().affectedExon(), minExon, maxExon)) {
                    if (variant.isReportable()) {
                        canonicalReportableVariantMatches.add(variant.event());
                    } else {
                        canonicalUnreportableVariantMatches.add(variant.event());
                    }
                }

                if (variant.isReportable()) {
                    for (TranscriptImpact otherImpact : variant.otherImpacts()) {
                        if (hasEffectInExonRange(otherImpact.affectedExon(), minExon, maxExon)) {
                            reportableOtherVariantMatches.add(variant.event());
                        }
                    }
                }
            }
        }

        if (!canonicalReportableVariantMatches.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addAllInclusionMolecularEvents(canonicalReportableVariantMatches)
                    .addPassSpecificMessages("Variant(s) in exon range " + minExon + " - " + maxExon + " in gene " + gene
                            + " of adequate type detected in canonical transcript")
                    .addPassGeneralMessages("Adequate variant(s) found in " + gene)
                    .build();
        }

        Evaluation potentialWarnEvaluation = evaluatePotentialWarns(canonicalUnreportableVariantMatches, reportableOtherVariantMatches);

        if (potentialWarnEvaluation != null) {
            return potentialWarnEvaluation;
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No adequate variant in exon range " + minExon + " - " + maxExon + " detected in gene " + gene)
                .addFailGeneralMessages("Molecular requirements")
                .build();
    }

    private static boolean hasEffectInExonRange(@Nullable Integer affectedExon, int minExon, int maxExon) {
        return affectedExon != null && affectedExon >= minExon && affectedExon <= maxExon;
    }

    @NotNull
    private static Set<VariantType> determineAllowedVariantTypes(@Nullable VariantTypeInput requiredVariantType) {
        if (requiredVariantType == null) {
            return Sets.newHashSet(VariantType.values());
        }

        switch (requiredVariantType) {
            case SNV: {
                return Sets.newHashSet(VariantType.SNV);
            }
            case MNV: {
                return Sets.newHashSet(VariantType.MNV);
            }
            case INSERT: {
                return Sets.newHashSet(VariantType.INSERT);
            }
            case DELETE: {
                return Sets.newHashSet(VariantType.DELETE);
            }
            case INDEL: {
                return Sets.newHashSet(VariantType.INSERT, VariantType.DELETE);
            }
            default: {
                throw new IllegalStateException("Could not map required variant type: " + requiredVariantType);
            }
        }
    }

    @Nullable
    private Evaluation evaluatePotentialWarns(@NotNull Set<String> canonicalUnreportableVariantMatches,
            @NotNull Set<String> reportableOtherVariantMatches) {
        Set<String> warnEvents = Sets.newHashSet();
        Set<String> warnSpecificMessages = Sets.newHashSet();
        Set<String> warnGeneralMessages = Sets.newHashSet();

        if (!canonicalUnreportableVariantMatches.isEmpty()) {
            warnEvents.addAll(canonicalUnreportableVariantMatches);
            warnSpecificMessages.add("Variant(s) in exon range " + minExon + " - " + maxExon + " in gene " + gene
                    + " of adequate type detected in canonical transcript, but considered non-reportable");
            warnGeneralMessages.add("Adequate variant(s) found in " + gene);
        }

        if (!reportableOtherVariantMatches.isEmpty()) {
            warnEvents.addAll(reportableOtherVariantMatches);
            warnSpecificMessages.add("Variant(s) in exon range " + minExon + " - " + maxExon + " in gene " + gene
                    + " of adequate type detected, but in non-canonical transcript");
            warnGeneralMessages.add("Adequate variant(s) found in non-canonical transcript of gene " + gene);
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
