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
import com.hartwig.actin.molecular.util.MolecularEventFactory;
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
        Set<String> reportableOtherMatchVariants = Sets.newHashSet();

        for (Variant variant : record.molecular().drivers().variants()) {
            if (variant.gene().equals(gene) && allowedVariantTypes.contains(variant.type())) {
                String variantEvent = MolecularEventFactory.event(variant);
                if (hasEffectInExonRange(variant.canonicalImpact().affectedExon(), minExon, maxExon)) {
                    if (variant.isReportable()) {
                        canonicalReportableVariantMatches.add(variantEvent);
                    } else {
                        canonicalUnreportableVariantMatches.add(variantEvent);
                    }
                }

                if (variant.isReportable()) {
                    for (TranscriptImpact otherImpact : variant.otherImpacts()) {
                        if (hasEffectInExonRange(otherImpact.affectedExon(), minExon, maxExon)) {
                            reportableOtherMatchVariants.add(variantEvent);
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
        } else if (!canonicalUnreportableVariantMatches.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(canonicalUnreportableVariantMatches)
                    .addWarnSpecificMessages("Variant(s) in exon range " + minExon + " - " + maxExon + " in gene " + gene
                            + " of adequate type detected in canonical transcript, but considered non-reportable")
                    .addWarnGeneralMessages("Adequate variant(s) found in " + gene)
                    .build();
        } else if (!reportableOtherMatchVariants.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.WARN)
                    .addAllInclusionMolecularEvents(reportableOtherMatchVariants)
                    .addWarnSpecificMessages("Variant(s) in exon range " + minExon + " - " + maxExon + " in gene " + gene
                            + " of adequate type detected, but in non-canonical transcript")
                    .addWarnGeneralMessages("Adequate variant(s) found in non-canonical transcript of gene " + gene)
                    .build();
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No adequate variant in exon range " + minExon + " - " + maxExon + " detected in gene " + gene)
                .addFailGeneralMessages("No specific variants in " + gene + " detected")
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
}
