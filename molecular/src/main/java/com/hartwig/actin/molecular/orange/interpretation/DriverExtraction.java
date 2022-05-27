package com.hartwig.actin.molecular.orange.interpretation;

import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.driver.Amplification;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.FusionDriverType;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableAmplification;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableDisruption;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableFusion;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableHomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableLoss;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableMolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableVariant;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableVirus;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.driver.VariantDriverType;
import com.hartwig.actin.molecular.datamodel.driver.Virus;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.GainLossInterpretation;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleGainLoss;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.purple.VariantHotspot;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterEntry;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterRecord;
import com.hartwig.actin.molecular.orange.util.AminoAcid;
import com.hartwig.actin.molecular.orange.util.EventFormatter;
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
        return ImmutableMolecularDrivers.builder()
                .variants(extractVariants(record.purple()))
                .amplifications(extractAmplifications(record.purple()))
                .losses(extractLosses(record.purple()))
                .homozygousDisruptions(extractHomozygousDisruptions(record.linx()))
                .disruptions(extractDisruptions(record.linx()))
                .fusions(extractFusions(record.linx()))
                .viruses(extractViruses(record.virusInterpreter()))
                .build();
    }

    @NotNull
    private static Set<Variant> extractVariants(@NotNull PurpleRecord purple) {
        Set<Variant> variants = Sets.newTreeSet(new VariantComparator());
        for (PurpleVariant variant : purple.variants()) {
            String impact = extractImpact(variant);
            variants.add(ImmutableVariant.builder()
                    .event(variant.gene() + " " + EventFormatter.format(impact))
                    .driverLikelihood(interpretDriverLikelihood(variant))
                    .gene(variant.gene())
                    .impact(impact)
                    .variantCopyNumber(keep3Digits(variant.alleleCopyNumber()))
                    .totalCopyNumber(keep3Digits(variant.totalCopyNumber()))
                    .driverType(extractVariantDriverType(variant))
                    .clonalLikelihood(keep3Digits(variant.clonalLikelihood()))
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
    @VisibleForTesting
    static VariantDriverType extractVariantDriverType(@NotNull PurpleVariant variant) {
        if (variant.hotspot() == VariantHotspot.HOTSPOT) {
            return VariantDriverType.HOTSPOT;
        } else if (variant.biallelic()) {
            return VariantDriverType.BIALLELIC;
        } else {
            return VariantDriverType.VUS;
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
                        .event(gainLoss.gene() + " " + EventFormatter.GAIN_EVENT)
                        .driverLikelihood(isPartial ? DriverLikelihood.MEDIUM : DriverLikelihood.HIGH)
                        .gene(gainLoss.gene())
                        .isPartial(isPartial)
                        .copies(gainLoss.minCopies())
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
                        .event(gainLoss.gene() + " " + EventFormatter.LOSS_EVENT)
                        .driverLikelihood(DriverLikelihood.HIGH)
                        .gene(gainLoss.gene())
                        .isPartial(gainLoss.interpretation() == GainLossInterpretation.PARTIAL_LOSS)
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
                    .event(homozygous + " " + EventFormatter.HOM_DISRUPTION_EVENT)
                    .driverLikelihood(DriverLikelihood.HIGH)
                    .gene(homozygous)
                    .build());
        }
        return homozygousDisruptions;
    }

    @NotNull
    private static Set<Disruption> extractDisruptions(@NotNull LinxRecord linx) {
        Set<Disruption> disruptions = Sets.newTreeSet(new DisruptionComparator());
        for (LinxDisruption disruption : linx.disruptions()) {
            disruptions.add(ImmutableDisruption.builder()
                    .event(disruption.gene() + " " + EventFormatter.DISRUPTION_EVENT)
                    .driverLikelihood(DriverLikelihood.LOW)
                    .gene(disruption.gene())
                    .type(disruption.type())
                    .junctionCopyNumber(keep3Digits(disruption.junctionCopyNumber()))
                    .undisruptedCopyNumber(keep3Digits(disruption.undisruptedCopyNumber()))
                    .range(disruption.range())
                    .build());
        }
        return disruptions;
    }

    @NotNull
    private static Set<Fusion> extractFusions(@NotNull LinxRecord linx) {
        Set<Fusion> fusions = Sets.newTreeSet(new FusionComparator());
        for (LinxFusion fusion : linx.fusions()) {
            fusions.add(ImmutableFusion.builder()
                    .event(fusion.geneStart() + "-" + fusion.geneEnd() + " fusion")
                    .driverLikelihood(extractFusionDriverLikelihood(fusion))
                    .fiveGene(fusion.geneStart())
                    .threeGene(fusion.geneEnd())
                    .details(fusion.geneContextStart() + " -> " + fusion.geneContextEnd())
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
            String event = virus.interpretation() != null ? virus.interpretation() + " positive" : Strings.EMPTY;
            viruses.add(ImmutableVirus.builder()
                    .event(event)
                    .driverLikelihood(extractVirusDriverLikelihood(virus))
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

    @VisibleForTesting
    static double keep3Digits(double input) {
        return Math.round(input * 1000) / 1000D;
    }
}
