package com.hartwig.actin.algo.evaluation.molecular;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.molecular.datamodel.driver.CodingEffect;
import com.hartwig.actin.molecular.datamodel.driver.CopyNumber;
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.GeneRole;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect;
import com.hartwig.actin.molecular.datamodel.driver.Variant;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GeneIsInactivated implements EvaluationFunction {

    private static final double CLONAL_CUTOFF = 0.5;

    static final Set<CodingEffect> INACTIVATING_CODING_EFFECTS = Sets.newHashSet();

    static {
        INACTIVATING_CODING_EFFECTS.add(CodingEffect.NONSENSE_OR_FRAMESHIFT);
        INACTIVATING_CODING_EFFECTS.add(CodingEffect.MISSENSE);
        INACTIVATING_CODING_EFFECTS.add(CodingEffect.SPLICE);
    }

    @NotNull
    private final String gene;

    GeneIsInactivated(@NotNull final String gene) {
        this.gene = gene;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Set<String> inactivationEventsThatQualify = Sets.newHashSet();
        Set<String> inactivationEventsThatAreUnreportable = Sets.newHashSet();
        Set<String> inactivationEventsNoTSG = Sets.newHashSet();
        Set<String> inactivationEventsGainOfFunction = Sets.newHashSet();

        for (HomozygousDisruption homozygousDisruption : record.molecular().drivers().homozygousDisruptions()) {
            if (homozygousDisruption.gene().equals(gene)) {
                boolean isGainOfFunction = homozygousDisruption.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION
                        || homozygousDisruption.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION_PREDICTED;
                if (!homozygousDisruption.isReportable()) {
                    inactivationEventsThatAreUnreportable.add(homozygousDisruption.event());
                } else if (homozygousDisruption.geneRole() == GeneRole.ONCO || homozygousDisruption.geneRole() == GeneRole.UNKNOWN) {
                    inactivationEventsNoTSG.add(homozygousDisruption.event());
                } else if (isGainOfFunction) {
                    inactivationEventsGainOfFunction.add(homozygousDisruption.event());
                } else {
                    inactivationEventsThatQualify.add(homozygousDisruption.event());
                }
            }
        }

        for (CopyNumber copyNumber : record.molecular().drivers().copyNumbers()) {
            if (copyNumber.type() == CopyNumberType.LOSS && copyNumber.gene().equals(gene)) {
                boolean isGainOfFunction = copyNumber.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION
                        || copyNumber.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION_PREDICTED;
                if (!copyNumber.isReportable()) {
                    inactivationEventsThatAreUnreportable.add(copyNumber.event());
                } else if (copyNumber.geneRole() == GeneRole.ONCO || copyNumber.geneRole() == GeneRole.UNKNOWN) {
                    inactivationEventsNoTSG.add(copyNumber.event());
                } else if (isGainOfFunction) {
                    inactivationEventsGainOfFunction.add(copyNumber.event());
                } else {
                    inactivationEventsThatQualify.add(copyNumber.event());
                }
            }
        }
        Set<String> reportableNonDriverBiallelicVariantsOther = Sets.newHashSet();
        Set<String> reportableNonDriverNonBiallelicVariantsOther = Sets.newHashSet();
        Set<String> inactivationHighDriverNonBiallelicVariants = Sets.newHashSet();
        Set<String> inactivationSubclonalVariants = Sets.newHashSet();
        List<String> eventsThatMayBeTransPhased = Lists.newArrayList();
        Set<Integer> evaluatedPhaseGroups = Sets.newHashSet();

        Boolean hasHighMutationalLoad = record.molecular().characteristics().hasHighTumorMutationalLoad();
        Boolean isMicrosatelliteUnstable = record.molecular().characteristics().isMicrosatelliteUnstable();

        for (Variant variant : record.molecular().drivers().variants()) {
            if (variant.gene().equals(gene) && INACTIVATING_CODING_EFFECTS.contains(variant.canonicalImpact().codingEffect())) {
                if (!variant.isReportable()) {
                    inactivationEventsThatAreUnreportable.add(variant.event());
                } else {
                    Integer phaseGroup = null;
                    if (phaseGroup == null || !evaluatedPhaseGroups.contains(phaseGroup)) {
                        evaluatedPhaseGroups.add(phaseGroup);
                        eventsThatMayBeTransPhased.add(variant.event());
                    }

                    if (variant.driverLikelihood() == DriverLikelihood.HIGH) {
                        boolean isGainOfFunction = variant.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION
                                || variant.proteinEffect() == ProteinEffect.GAIN_OF_FUNCTION_PREDICTED;

                        if (variant.geneRole() == GeneRole.ONCO || variant.geneRole() == GeneRole.UNKNOWN) {
                            inactivationEventsNoTSG.add(variant.event());
                        } else if (!variant.isBiallelic()) {
                            inactivationHighDriverNonBiallelicVariants.add(variant.event());
                        } else if (isGainOfFunction) {
                            inactivationEventsGainOfFunction.add(variant.event());
                        } else if (variant.clonalLikelihood() < CLONAL_CUTOFF) {
                            inactivationSubclonalVariants.add(variant.event());
                        } else {
                            inactivationEventsThatQualify.add(variant.event());
                        }
                    } else if (hasHighMutationalLoad == null || !hasHighMutationalLoad || isMicrosatelliteUnstable == null
                            || !isMicrosatelliteUnstable) {
                        if (variant.isBiallelic()) {
                            reportableNonDriverBiallelicVariantsOther.add(variant.event());
                        } else {
                            reportableNonDriverNonBiallelicVariantsOther.add(variant.event());
                        }
                    }
                }
            }
        }

        Set<Integer> evaluatedClusterGroups = Sets.newHashSet();
        for (Disruption disruption : record.molecular().drivers().disruptions()) {
            if (disruption.gene().equals(gene) && disruption.isReportable()) {
                if (!evaluatedClusterGroups.contains(disruption.clusterGroup())) {
                    evaluatedClusterGroups.add(disruption.clusterGroup());
                    eventsThatMayBeTransPhased.add(disruption.event());
                }
            }
        }

        if (!inactivationEventsThatQualify.isEmpty()) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addAllInclusionMolecularEvents(inactivationEventsThatQualify)
                    .addPassSpecificMessages(
                            "Inactivation event(s) detected for gene " + gene + ": " + Format.concat(inactivationEventsThatQualify))
                    .addPassGeneralMessages(gene + " inactivation")
                    .build();
        }

        Evaluation potentialWarnEvaluation = evaluatePotentialWarns(inactivationEventsThatAreUnreportable,
                inactivationEventsNoTSG,
                inactivationEventsGainOfFunction,
                inactivationHighDriverNonBiallelicVariants,
                inactivationSubclonalVariants,
                reportableNonDriverBiallelicVariantsOther,
                reportableNonDriverNonBiallelicVariantsOther,
                eventsThatMayBeTransPhased);

        if (potentialWarnEvaluation != null) {
            return potentialWarnEvaluation;
        }

        return EvaluationFactory.unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("No inactivation event(s) detected for gene " + gene)
                .addFailGeneralMessages("No " + gene + " inactivation")
                .build();
    }

    @Nullable
    private Evaluation evaluatePotentialWarns(@NotNull Set<String> inactivationEventsThatAreUnreportable,
            @NotNull Set<String> inactivationEventsNoTSG, @NotNull Set<String> inactivationEventsGainOfFunction,
            @NotNull Set<String> inactivationHighDriverNonBiallelicVariants, @NotNull Set<String> inactivationSubclonalVariants,
            @NotNull Set<String> reportableNonDriverBiallelicVariantsOther,
            @NotNull Set<String> reportableNonDriverNonBiallelicVariantsOther, @NotNull List<String> eventsThatMayBeTransPhased) {
        Set<String> warnEvents = Sets.newHashSet();
        Set<String> warnSpecificMessages = Sets.newHashSet();
        Set<String> warnGeneralMessages = Sets.newHashSet();

        if (!inactivationEventsThatAreUnreportable.isEmpty()) {
            warnEvents.addAll(inactivationEventsThatAreUnreportable);
            warnSpecificMessages.add(
                    "Inactivation events detected for gene " + gene + ": " + Format.concat(inactivationEventsThatAreUnreportable)
                            + ", but considered non-reportable");
            warnGeneralMessages.add(gene + " potential inactivation but not reportable");
        }

        if (!inactivationEventsNoTSG.isEmpty()) {
            warnEvents.addAll(inactivationEventsNoTSG);
            warnSpecificMessages.add("Inactivation events detected for gene " + gene + ": " + Format.concat(inactivationEventsNoTSG)
                    + " but gene is not annotated with gene role TSG");
            warnGeneralMessages.add(gene + " potential inactivation but gene role not TSG");
        }

        if (!inactivationEventsGainOfFunction.isEmpty()) {
            warnEvents.addAll(inactivationEventsGainOfFunction);
            warnSpecificMessages.add("Inactivation events detected for " + gene + ": " + Format.concat(inactivationEventsGainOfFunction)
                    + " but no events annotated as having gain-of-function impact");
            warnGeneralMessages.add(gene + " potential inactivation but event annotated with gain-of-function protein impact");
        }

        if (!inactivationHighDriverNonBiallelicVariants.isEmpty() && !(eventsThatMayBeTransPhased.size() > 1)) {
            warnEvents.addAll(inactivationHighDriverNonBiallelicVariants);
            warnSpecificMessages.add(
                    "Inactivation event(s) detected for " + gene + ": " + Format.concat(inactivationHighDriverNonBiallelicVariants)
                            + " but event(s) are not biallelic");
            warnGeneralMessages.add(gene + " potential inactivation but not biallelic");
        }

        if (!inactivationSubclonalVariants.isEmpty()) {
            warnEvents.addAll(inactivationSubclonalVariants);
            warnSpecificMessages.add("Inactivation event(s) detected for " + gene + ": " + Format.concat(inactivationSubclonalVariants)
                    + " but subclonal likelihood > " + Format.percentage(1 - CLONAL_CUTOFF));
            warnGeneralMessages.add(gene + " potentially inactivating event(s) " + Format.concat(inactivationSubclonalVariants)
                    + " but subclonal likelihood > " + Format.percentage(1 - CLONAL_CUTOFF));
        }

        if (!reportableNonDriverBiallelicVariantsOther.isEmpty()) {
            warnEvents.addAll(reportableNonDriverBiallelicVariantsOther);
            warnSpecificMessages.add(
                    "Potential inactivation events detected for " + gene + ": " + Format.concat(reportableNonDriverBiallelicVariantsOther)
                            + " but event(s) are not of high driver likelihood");
            warnGeneralMessages.add("Variant(s) " + Format.concat(reportableNonDriverBiallelicVariantsOther) + " of low driver likelihood");
        }

        if (!reportableNonDriverNonBiallelicVariantsOther.isEmpty()) {
            warnEvents.addAll(reportableNonDriverNonBiallelicVariantsOther);
            warnSpecificMessages.add("Potential inactivation events detected for " + gene + ": " + Format.concat(
                    reportableNonDriverNonBiallelicVariantsOther) + " but event(s) are not biallelic and not of high driver likelihood");
            warnGeneralMessages.add("Variant(s) " + Format.concat(reportableNonDriverNonBiallelicVariantsOther)
                    + " not biallelic and no high driver likelihood");
        }

        if (eventsThatMayBeTransPhased.size() > 1) {
            warnEvents.addAll(eventsThatMayBeTransPhased);
            warnSpecificMessages.add("Multiple events detected for " + gene + ": " + Format.concat(eventsThatMayBeTransPhased)
                    + " that potentially together inactivate the gene?");
            warnGeneralMessages.add(gene + " potential inactivation if considering multiple events");
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
