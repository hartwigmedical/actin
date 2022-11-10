package com.hartwig.actin.molecular.orange.interpretation;

import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.driver.Amplification;
import com.hartwig.actin.molecular.datamodel.driver.CodingContext;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.FusionDriverType;
import com.hartwig.actin.molecular.datamodel.driver.GeneRole;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableAmplification;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableDisruption;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableFusion;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableHomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableLoss;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableMolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableTranscriptImpact;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableVariant;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableVirus;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect;
import com.hartwig.actin.molecular.datamodel.driver.RegionType;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.driver.VariantType;
import com.hartwig.actin.molecular.datamodel.driver.Virus;
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableActionableEvidence;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.GainLossInterpretation;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleGainLoss;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterEntry;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterRecord;
import com.hartwig.actin.molecular.orange.util.AminoAcid;
import com.hartwig.actin.molecular.sort.driver.CopyNumberComparator;
import com.hartwig.actin.molecular.sort.driver.DisruptionComparator;
import com.hartwig.actin.molecular.sort.driver.FusionComparator;
import com.hartwig.actin.molecular.sort.driver.HomozygousDisruptionComparator;
import com.hartwig.actin.molecular.sort.driver.VariantComparator;
import com.hartwig.actin.molecular.sort.driver.VirusComparator;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

final class DriverExtraction {

    static final String UPSTREAM_GENE_VARIANT = "upstream_gene_variant";

    private DriverExtraction() {
    }

    @NotNull
    public static MolecularDrivers extract(@NotNull OrangeRecord record) {
        // In case purple contains no tumor cells, we wipe all drivers.
        if (!record.purple().containsTumorCells()) {
            return ImmutableMolecularDrivers.builder().build();
        }

        Set<Loss> losses = extractLosses(record.purple());

        return ImmutableMolecularDrivers.builder()
                .variants(extractVariants(record.purple()))
                .amplifications(extractAmplifications(record.purple()))
                .losses(losses)
                .homozygousDisruptions(extractHomozygousDisruptions(record.linx()))
                .disruptions(extractDisruptions(record.linx(), losses))
                .fusions(extractFusions(record.linx()))
                .viruses(extractViruses(record.virusInterpreter()))
                .build();
    }

    @NotNull
    private static Set<Variant> extractVariants(@NotNull PurpleRecord purple) {
        Set<Variant> variants = Sets.newTreeSet(new VariantComparator());
        for (PurpleVariant variant : purple.variants()) {
            variants.add(ImmutableVariant.builder()
                    .isReportable(true)
                    .event(Strings.EMPTY)
                    .driverLikelihood(interpretDriverLikelihood(variant))
                    .evidence(createEmptyEvidence())
                    .gene(variant.gene())
                    .geneRole(GeneRole.UNKNOWN)
                    .proteinEffect(ProteinEffect.UNKNOWN)
                    .isAssociatedWithDrugResistance(null)
                    .type(VariantType.SNV)
                    .variantCopyNumber(keep3Digits(variant.alleleCopyNumber()))
                    .totalCopyNumber(keep3Digits(variant.totalCopyNumber()))
                    .isBiallelic(false)
                    .isHotspot(false)
                    .clonalLikelihood(keep3Digits(variant.clonalLikelihood()))
                    .canonicalImpact(ImmutableTranscriptImpact.builder()
                            .transcriptId(Strings.EMPTY)
                            .isSpliceRegion(false)
                            .hgvsCodingImpact(Strings.EMPTY)
                            .hgvsProteinImpact(Strings.EMPTY)
                            .build())
                    .otherImpacts(Sets.newHashSet())
                    .build());
        }
        return variants;
    }

    @NotNull
    @VisibleForTesting
    static String extractImpact(@NotNull PurpleVariant variant) {
        if (!variant.hgvsProteinImpact().isEmpty()) {
            return AminoAcid.forceSingleLetterAminoAcids(variant.hgvsProteinImpact());
        } else if (!variant.hgvsCodingImpact().isEmpty()) {
            return variant.hgvsCodingImpact();
        } else if (variant.effect().equals(UPSTREAM_GENE_VARIANT)) {
            return "upstream";
        } else {
            return variant.effect();
        }
    }

    @NotNull
    @VisibleForTesting
    static DriverLikelihood interpretDriverLikelihood(@NotNull PurpleVariant variant) {
        if (variant.driverLikelihood() >= 0.8) {
            return DriverLikelihood.HIGH;
        } else if (variant.driverLikelihood() >= 0.2) {
            return DriverLikelihood.MEDIUM;
        } else {
            return DriverLikelihood.LOW;
        }
    }

    @NotNull
    private static Set<Amplification> extractAmplifications(@NotNull PurpleRecord purple) {
        Set<Amplification> amplifications = Sets.newTreeSet(new CopyNumberComparator());
        for (PurpleGainLoss gainLoss : purple.gainsLosses()) {
            if (gainLoss.interpretation() == GainLossInterpretation.PARTIAL_GAIN
                    || gainLoss.interpretation() == GainLossInterpretation.FULL_GAIN) {
                boolean isPartial = gainLoss.interpretation() == GainLossInterpretation.PARTIAL_GAIN;
                amplifications.add(ImmutableAmplification.builder()
                        .isReportable(true)
                        .event(Strings.EMPTY)
                        .driverLikelihood(isPartial ? DriverLikelihood.MEDIUM : DriverLikelihood.HIGH)
                        .evidence(createEmptyEvidence())
                        .gene(gainLoss.gene())
                        .geneRole(GeneRole.UNKNOWN)
                        .proteinEffect(ProteinEffect.UNKNOWN)
                        .isAssociatedWithDrugResistance(null)
                        .minCopies(gainLoss.minCopies())
                        .maxCopies(0)
                        .build());
            }
        }
        return amplifications;
    }

    @NotNull
    private static Set<Loss> extractLosses(@NotNull PurpleRecord purple) {
        Set<Loss> losses = Sets.newTreeSet(new CopyNumberComparator());
        for (PurpleGainLoss gainLoss : purple.gainsLosses()) {
            if (gainLoss.interpretation() == GainLossInterpretation.PARTIAL_LOSS
                    || gainLoss.interpretation() == GainLossInterpretation.FULL_LOSS) {
                losses.add(ImmutableLoss.builder()
                        .isReportable(true)
                        .event(Strings.EMPTY)
                        .driverLikelihood(DriverLikelihood.HIGH)
                        .evidence(createEmptyEvidence())
                        .gene(gainLoss.gene())
                        .geneRole(GeneRole.UNKNOWN)
                        .proteinEffect(ProteinEffect.UNKNOWN)
                        .isAssociatedWithDrugResistance(null)
                        .minCopies(gainLoss.minCopies())
                        .maxCopies(0)
                        .build());
            }
        }
        return losses;
    }

    @NotNull
    private static Set<HomozygousDisruption> extractHomozygousDisruptions(@NotNull LinxRecord linx) {
        Set<HomozygousDisruption> homozygousDisruptions = Sets.newTreeSet(new HomozygousDisruptionComparator());
        for (String homozygous : linx.homozygousDisruptedGenes()) {
            homozygousDisruptions.add(ImmutableHomozygousDisruption.builder()
                    .isReportable(true)
                    .event(Strings.EMPTY)
                    .driverLikelihood(DriverLikelihood.HIGH)
                    .evidence(createEmptyEvidence())
                    .gene(homozygous)
                    .geneRole(GeneRole.UNKNOWN)
                    .proteinEffect(ProteinEffect.UNKNOWN)
                    .isAssociatedWithDrugResistance(null)
                    .build());
        }
        return homozygousDisruptions;
    }

    @NotNull
    private static Set<Disruption> extractDisruptions(@NotNull LinxRecord linx, @NotNull Set<Loss> losses) {
        Set<Disruption> disruptions = Sets.newTreeSet(new DisruptionComparator());
        for (LinxDisruption disruption : linx.disruptions()) {
            // TODO: Linx should already filter or flag disruptions that are lost.
            if (include(disruption, losses)) {
                disruptions.add(ImmutableDisruption.builder()
                        .isReportable(true)
                        .event(Strings.EMPTY)
                        .driverLikelihood(DriverLikelihood.LOW)
                        .evidence(createEmptyEvidence())
                        .gene(disruption.gene())
                        .geneRole(GeneRole.UNKNOWN)
                        .proteinEffect(ProteinEffect.UNKNOWN)
                        .isAssociatedWithDrugResistance(null)
                        .type(disruption.type())
                        .junctionCopyNumber(keep3Digits(disruption.junctionCopyNumber()))
                        .undisruptedCopyNumber(keep3Digits(disruption.undisruptedCopyNumber()))
                        .regionType(RegionType.INTRONIC)
                        .codingContext(CodingContext.NON_CODING)
                        .range(disruption.range())
                        .build());
            }
        }
        return disruptions;
    }

    private static boolean include(@NotNull LinxDisruption disruption, @NotNull Set<Loss> losses) {
        return !disruption.type().equalsIgnoreCase("del") || !isLost(losses, disruption.gene());
    }

    private static boolean isLost(@NotNull Set<Loss> losses, @NotNull String gene) {
        for (Loss loss : losses) {
            if (loss.gene().equals(gene)) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    private static Set<Fusion> extractFusions(@NotNull LinxRecord linx) {
        Set<Fusion> fusions = Sets.newTreeSet(new FusionComparator());
        for (LinxFusion fusion : linx.fusions()) {
            fusions.add(ImmutableFusion.builder()
                    .isReportable(true)
                    .event(Strings.EMPTY)
                    .driverLikelihood(extractFusionDriverLikelihood(fusion))
                    .evidence(createEmptyEvidence())
                    .geneStart(fusion.geneStart())
                    .geneTranscriptStart(Strings.EMPTY)
                    .geneContextStart(Strings.EMPTY)
                    .fusedExonUp(0)
                    .geneEnd(fusion.geneEnd())
                    .geneTranscriptEnd(Strings.EMPTY)
                    .geneContextEnd(Strings.EMPTY)
                    .fusedExonDown(0)
                    .proteinEffect(ProteinEffect.UNKNOWN)
                    .isAssociatedWithDrugResistance(null)
                    .driverType(extractFusionDriverType(fusion))
                    .build());
        }
        return fusions;
    }

    @NotNull
    @VisibleForTesting
    static FusionDriverType extractFusionDriverType(@NotNull LinxFusion fusion) {
        switch (fusion.type()) {
            case PROMISCUOUS_3:
            case PROMISCUOUS_5:
            case PROMISCUOUS_BOTH:
            case IG_PROMISCUOUS: {
                return FusionDriverType.PROMISCUOUS;
            }
            case KNOWN_PAIR:
            case IG_KNOWN_PAIR:
            case EXON_DEL_DUP: {
                return FusionDriverType.KNOWN;
            }
            default: {
                throw new IllegalStateException("Cannot determine fusion driver type for fusion of type: " + fusion.type());
            }
        }
    }

    @NotNull
    @VisibleForTesting
    static DriverLikelihood extractFusionDriverLikelihood(@NotNull LinxFusion fusion) {
        switch (fusion.driverLikelihood()) {
            case HIGH: {
                return DriverLikelihood.HIGH;
            }
            case LOW: {
                return DriverLikelihood.LOW;
            }
            default: {
                throw new IllegalStateException(
                        "Cannot determine driver likelihood type for fusion driver likelihood: " + fusion.driverLikelihood());
            }
        }
    }

    @NotNull
    private static Set<Virus> extractViruses(@NotNull VirusInterpreterRecord virusInterpreter) {
        Set<Virus> viruses = Sets.newTreeSet(new VirusComparator());
        for (VirusInterpreterEntry virus : virusInterpreter.entries()) {
            viruses.add(ImmutableVirus.builder()
                    .isReportable(true)
                    .event(Strings.EMPTY)
                    .driverLikelihood(extractVirusDriverLikelihood(virus))
                    .evidence(createEmptyEvidence())
                    .name(virus.name())
                    .integrations(virus.integrations())
                    .build());
        }
        return viruses;
    }

    @NotNull
    @VisibleForTesting
    static DriverLikelihood extractVirusDriverLikelihood(@NotNull VirusInterpreterEntry virus) {
        switch (virus.driverLikelihood()) {
            case HIGH: {
                return DriverLikelihood.HIGH;
            }
            case LOW:
            case UNKNOWN: {
                return DriverLikelihood.LOW;
            }
            default: {
                throw new IllegalStateException(
                        "Cannot determine driver likelihood type for virus driver likelihood: " + virus.driverLikelihood());
            }
        }
    }

    @NotNull
    private static ActionableEvidence createEmptyEvidence() {
        return ImmutableActionableEvidence.builder().build();
    }

    @VisibleForTesting
    static double keep3Digits(double input) {
        return Math.round(input * 1000) / 1000D;
    }
}
