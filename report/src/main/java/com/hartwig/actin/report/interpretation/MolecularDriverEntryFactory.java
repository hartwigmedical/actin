package com.hartwig.actin.report.interpretation;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Set;

import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.driver.Amplification;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.Driver;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.driver.Virus;
import com.hartwig.actin.molecular.datamodel.evidence.EvidenceEntry;
import com.hartwig.actin.molecular.datamodel.evidence.MolecularEvidence;
import com.hartwig.actin.report.pdf.util.Formats;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MolecularDriverEntryFactory {

    private static final DecimalFormat SINGLE_DIGIT_FORMAT = new DecimalFormat("#.#", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

    private MolecularDriverEntryFactory() {
    }

    @NotNull
    public static Set<MolecularDriverEntry> create(@NotNull MolecularDrivers drivers, @NotNull MolecularEvidence evidence) {
        Set<MolecularDriverEntry> entries = Sets.newTreeSet(new MolecularDriverEntryComparator());

        entries.addAll(fromVariants(drivers.variants(), evidence));
        entries.addAll(fromAmplifications(drivers.amplifications(), evidence));
        entries.addAll(fromLosses(drivers.losses(), evidence));
        entries.addAll(fromHomozygousDisruptions(drivers.homozygousDisruptions(), evidence));
        entries.addAll(fromDisruptions(drivers.disruptions(), evidence));
        entries.addAll(fromFusions(drivers.fusions(), evidence));
        entries.addAll(fromViruses(drivers.viruses(), evidence));

        return entries;
    }

    @NotNull
    private static Set<MolecularDriverEntry> fromVariants(@NotNull Set<Variant> variants, @NotNull MolecularEvidence evidence) {
        Set<MolecularDriverEntry> entries = Sets.newHashSet();

        for (Variant variant : variants) {
            ImmutableMolecularDriverEntry.Builder entryBuilder = ImmutableMolecularDriverEntry.builder();
            entryBuilder.driverType("Mutation (" + variant.driverType().display() + ")");

            double boundedVariantCopies = Math.max(0, Math.min(variant.variantCopyNumber(), variant.totalCopyNumber()));
            String variantCopyString = boundedVariantCopies < 1
                    ? Formats.singleDigitNumber(boundedVariantCopies)
                    : Formats.noDigitNumber(boundedVariantCopies);

            double boundedTotalCopies = Math.max(0, variant.totalCopyNumber());
            String totalCopyString =
                    boundedTotalCopies < 1 ? Formats.singleDigitNumber(boundedTotalCopies) : Formats.noDigitNumber(boundedTotalCopies);

            String driver = variant.gene() + " " + variant.impact() + " (" + variantCopyString + "/" + totalCopyString + " copies)";
            if (ClonalityInterpreter.isPotentiallySubclonal(variant)) {
                driver = driver + "*";
            }
            entryBuilder.driver(driver);
            entryBuilder.driverLikelihood(variant.driverLikelihood());

            addActionability(entryBuilder, variant, evidence);

            entries.add(entryBuilder.build());
        }

        return entries;
    }

    @NotNull
    private static Set<MolecularDriverEntry> fromAmplifications(@NotNull Set<Amplification> amplifications,
            @NotNull MolecularEvidence evidence) {
        Set<MolecularDriverEntry> entries = Sets.newHashSet();

        for (Amplification amplification : amplifications) {
            ImmutableMolecularDriverEntry.Builder entryBuilder = ImmutableMolecularDriverEntry.builder();

            String addon = amplification.isPartial() ? " (partial)" : Strings.EMPTY;
            entryBuilder.driverType("Amplification" + addon);
            entryBuilder.driver(amplification.gene() + " amp, " + amplification.copies() + " copies");
            entryBuilder.driverLikelihood(amplification.driverLikelihood());

            addActionability(entryBuilder, amplification, evidence);

            entries.add(entryBuilder.build());
        }

        return entries;
    }

    @NotNull
    private static Set<MolecularDriverEntry> fromLosses(@NotNull Set<Loss> losses, @NotNull MolecularEvidence evidence) {
        Set<MolecularDriverEntry> entries = Sets.newHashSet();

        for (Loss loss : losses) {
            ImmutableMolecularDriverEntry.Builder entryBuilder = ImmutableMolecularDriverEntry.builder();
            entryBuilder.driverType("Loss");
            entryBuilder.driver(loss.gene() + " del");
            entryBuilder.driverLikelihood(loss.driverLikelihood());

            addActionability(entryBuilder, loss, evidence);

            entries.add(entryBuilder.build());
        }

        return entries;
    }

    @NotNull
    private static Set<MolecularDriverEntry> fromHomozygousDisruptions(@NotNull Set<HomozygousDisruption> homozygousDisruptions,
            @NotNull MolecularEvidence evidence) {
        Set<MolecularDriverEntry> entries = Sets.newHashSet();

        for (HomozygousDisruption homozygousDisruption : homozygousDisruptions) {
            ImmutableMolecularDriverEntry.Builder entryBuilder = ImmutableMolecularDriverEntry.builder();
            entryBuilder.driverType("Homozygous disruption");
            entryBuilder.driver(homozygousDisruption.gene());
            entryBuilder.driverLikelihood(homozygousDisruption.driverLikelihood());

            addActionability(entryBuilder, homozygousDisruption, evidence);

            entries.add(entryBuilder.build());
        }

        return entries;
    }

    @NotNull
    private static Set<MolecularDriverEntry> fromDisruptions(@NotNull Set<Disruption> disruptions, @NotNull MolecularEvidence evidence) {
        Set<MolecularDriverEntry> entries = Sets.newHashSet();

        for (Disruption disruption : disruptions) {
            ImmutableMolecularDriverEntry.Builder entryBuilder = ImmutableMolecularDriverEntry.builder();
            entryBuilder.driverType("Non-homozygous disruption");
            String addon = Formats.singleDigitNumber(disruption.junctionCopyNumber()) + " disr. / " + Formats.singleDigitNumber(
                    disruption.undisruptedCopyNumber()) + " undisr. copies";
            entryBuilder.driver(disruption.gene() + ", " + disruption.type() + " (" + addon + ")");
            entryBuilder.driverLikelihood(disruption.driverLikelihood());

            addActionability(entryBuilder, disruption, evidence);

            entries.add(entryBuilder.build());
        }

        return entries;
    }

    @NotNull
    private static Set<MolecularDriverEntry> fromFusions(@NotNull Set<Fusion> fusions, @NotNull MolecularEvidence evidence) {
        Set<MolecularDriverEntry> entries = Sets.newHashSet();

        for (Fusion fusion : fusions) {
            ImmutableMolecularDriverEntry.Builder entryBuilder = ImmutableMolecularDriverEntry.builder();
            entryBuilder.driverType(fusion.driverType().display());
            String name = fusion.fiveGene() + "-" + fusion.threeGene() + " fusion";
            entryBuilder.driver(name + ", " + fusion.details());
            entryBuilder.driverLikelihood(fusion.driverLikelihood());

            addActionability(entryBuilder, fusion, evidence);

            entries.add(entryBuilder.build());
        }

        return entries;
    }

    @NotNull
    private static Set<MolecularDriverEntry> fromViruses(@NotNull Set<Virus> viruses, @NotNull MolecularEvidence evidence) {
        Set<MolecularDriverEntry> entries = Sets.newHashSet();

        for (Virus virus : viruses) {
            ImmutableMolecularDriverEntry.Builder entryBuilder = ImmutableMolecularDriverEntry.builder();
            entryBuilder.driverType("Virus");
            entryBuilder.driver(virus.name() + ", " + virus.integrations() + " integrations detected");
            entryBuilder.driverLikelihood(virus.driverLikelihood());

            addActionability(entryBuilder, virus, evidence);

            entries.add(entryBuilder.build());
        }

        return entries;
    }

    private static void addActionability(@NotNull ImmutableMolecularDriverEntry.Builder entryBuilder, @NotNull Driver driver,
            @NotNull MolecularEvidence evidence) {
        entryBuilder.actinTreatments(treatments(driver, evidence.actinTrials()));
        entryBuilder.externalTreatments(treatments(driver, evidence.externalTrials()));

        entryBuilder.bestResponsiveEvidence(bestResponsiveEvidence(driver, evidence));
        entryBuilder.bestResistanceEvidence(bestResistanceEvidence(driver, evidence));
    }

    @NotNull
    private static Set<String> treatments(@NotNull Driver driver, @NotNull Set<EvidenceEntry> evidences) {
        Set<String> treatments = Sets.newTreeSet(Ordering.natural());
        for (EvidenceEntry evidence : evidences) {
            if (evidence.event().equals(driver.event())) {
                treatments.add(evidence.treatment());
            }
        }
        return treatments;
    }

    @Nullable
    private static String bestResponsiveEvidence(@NotNull Driver driver, @NotNull MolecularEvidence evidence) {
        if (hasEvidence(driver, evidence.approvedEvidence())) {
            return "Approved";
        } else if (hasEvidence(driver, evidence.onLabelExperimentalEvidence()) || hasEvidence(driver,
                evidence.offLabelExperimentalEvidence())) {
            return "Experimental";
        } else if (hasEvidence(driver, evidence.preClinicalEvidence())) {
            return "Pre-clinical";
        }

        return null;
    }

    @Nullable
    private static String bestResistanceEvidence(@NotNull Driver driver, @NotNull MolecularEvidence evidence) {
        if (hasEvidence(driver, evidence.knownResistanceEvidence())) {
            return "Known";
        } else if (hasEvidence(driver, evidence.suspectResistanceEvidence())) {
            return "Suspect";
        }

        return null;
    }

    private static boolean hasEvidence(@NotNull Driver driver, @NotNull Iterable<EvidenceEntry> evidences) {
        for (EvidenceEntry evidence : evidences) {
            if (evidence.event().equals(driver.event())) {
                return true;
            }
        }
        return false;
    }
}
