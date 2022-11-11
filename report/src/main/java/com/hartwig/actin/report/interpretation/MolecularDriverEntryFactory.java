package com.hartwig.actin.report.interpretation;

import java.util.Set;

import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.driver.Amplification;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.Driver;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.driver.Virus;
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence;
import com.hartwig.actin.report.pdf.util.Formats;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MolecularDriverEntryFactory {

    @NotNull
    private final Multimap<String, String> trialsPerInclusionEvent;

    public MolecularDriverEntryFactory(@NotNull final Multimap<String, String> trialsPerInclusionEvent) {
        this.trialsPerInclusionEvent = trialsPerInclusionEvent;
    }

    @NotNull
    public Set<MolecularDriverEntry> create(@NotNull MolecularRecord molecular) {
        Set<MolecularDriverEntry> entries = Sets.newTreeSet(new MolecularDriverEntryComparator());

        MolecularDrivers drivers = molecular.drivers();
        entries.addAll(fromVariants(drivers.variants()));
        entries.addAll(fromAmplifications(molecular.characteristics().ploidy(), drivers.amplifications()));
        entries.addAll(fromLosses(drivers.losses()));
        entries.addAll(fromHomozygousDisruptions(drivers.homozygousDisruptions()));
        entries.addAll(fromDisruptions(drivers.disruptions()));
        entries.addAll(fromFusions(drivers.fusions()));
        entries.addAll(fromViruses(drivers.viruses()));

        return entries;
    }

    @NotNull
    private Set<MolecularDriverEntry> fromVariants(@NotNull Set<Variant> variants) {
        Set<MolecularDriverEntry> entries = Sets.newHashSet();

        for (Variant variant : variants) {
            ImmutableMolecularDriverEntry.Builder entryBuilder = ImmutableMolecularDriverEntry.builder();

            String mutationTypeString = variant.isHotspot() ? "Hotspot" : "VUS";
            mutationTypeString = variant.isBiallelic() ? "Biallelic " + mutationTypeString : mutationTypeString;

            entryBuilder.driverType("Mutation (" + mutationTypeString + "))");

            double boundedVariantCopies = Math.max(0, Math.min(variant.variantCopyNumber(), variant.totalCopyNumber()));
            String variantCopyString = boundedVariantCopies < 1
                    ? Formats.singleDigitNumber(boundedVariantCopies)
                    : Formats.noDigitNumber(boundedVariantCopies);

            double boundedTotalCopies = Math.max(0, variant.totalCopyNumber());
            String totalCopyString =
                    boundedTotalCopies < 1 ? Formats.singleDigitNumber(boundedTotalCopies) : Formats.noDigitNumber(boundedTotalCopies);

            String driver = variant.event() + ", " + variantCopyString + "/" + totalCopyString + " copies)";
            if (ClonalityInterpreter.isPotentiallySubclonal(variant)) {
                driver = driver + "*";
            }
            entryBuilder.driver(driver);
            entryBuilder.driverLikelihood(variant.driverLikelihood());

            addActionability(entryBuilder, variant);

            entries.add(entryBuilder.build());
        }

        return entries;
    }

    @NotNull
    private Set<MolecularDriverEntry> fromAmplifications(@Nullable Double ploidy, @NotNull Set<Amplification> amplifications) {
        Set<MolecularDriverEntry> entries = Sets.newHashSet();

        for (Amplification amplification : amplifications) {
            ImmutableMolecularDriverEntry.Builder entryBuilder = ImmutableMolecularDriverEntry.builder();

            String addon = ploidy != null && amplification.minCopies() < 3 * ploidy ? " (partial)" : Strings.EMPTY;
            entryBuilder.driverType("Amplification" + addon);
            entryBuilder.driver(amplification.event() + ", " + amplification.minCopies() + " copies");
            entryBuilder.driverLikelihood(amplification.driverLikelihood());

            addActionability(entryBuilder, amplification);

            entries.add(entryBuilder.build());
        }

        return entries;
    }

    @NotNull
    private Set<MolecularDriverEntry> fromLosses(@NotNull Set<Loss> losses) {
        Set<MolecularDriverEntry> entries = Sets.newHashSet();

        for (Loss loss : losses) {
            ImmutableMolecularDriverEntry.Builder entryBuilder = ImmutableMolecularDriverEntry.builder();
            entryBuilder.driverType("Loss");
            entryBuilder.driver(loss.event());
            entryBuilder.driverLikelihood(loss.driverLikelihood());

            addActionability(entryBuilder, loss);

            entries.add(entryBuilder.build());
        }

        return entries;
    }

    @NotNull
    private Set<MolecularDriverEntry> fromHomozygousDisruptions(@NotNull Set<HomozygousDisruption> homozygousDisruptions) {
        Set<MolecularDriverEntry> entries = Sets.newHashSet();

        for (HomozygousDisruption homozygousDisruption : homozygousDisruptions) {
            ImmutableMolecularDriverEntry.Builder entryBuilder = ImmutableMolecularDriverEntry.builder();
            entryBuilder.driverType("Disruption (homozygous)");
            entryBuilder.driver(homozygousDisruption.gene());
            entryBuilder.driverLikelihood(homozygousDisruption.driverLikelihood());

            addActionability(entryBuilder, homozygousDisruption);

            entries.add(entryBuilder.build());
        }

        return entries;
    }

    @NotNull
    private Set<MolecularDriverEntry> fromDisruptions(@NotNull Set<Disruption> disruptions) {
        Set<MolecularDriverEntry> entries = Sets.newHashSet();

        for (Disruption disruption : disruptions) {
            ImmutableMolecularDriverEntry.Builder entryBuilder = ImmutableMolecularDriverEntry.builder();
            entryBuilder.driverType("Disruption");
            String addon = Formats.singleDigitNumber(disruption.junctionCopyNumber()) + " disr. / "
                    + Formats.singleDigitNumber(disruption.undisruptedCopyNumber()) + " undisr. copies";
            entryBuilder.driver(disruption.gene() + ", " + disruption.type() + " (" + addon + ")");
            entryBuilder.driverLikelihood(disruption.driverLikelihood());

            addActionability(entryBuilder, disruption);

            entries.add(entryBuilder.build());
        }

        return entries;
    }

    @NotNull
    private Set<MolecularDriverEntry> fromFusions(@NotNull Set<Fusion> fusions) {
        Set<MolecularDriverEntry> entries = Sets.newHashSet();

        for (Fusion fusion : fusions) {
            ImmutableMolecularDriverEntry.Builder entryBuilder = ImmutableMolecularDriverEntry.builder();
            entryBuilder.driverType(fusion.driverType().display());
            entryBuilder.driver(fusion.event() + ", exon " + fusion.fusedExonUp() + " to exon " + fusion.fusedExonDown());
            entryBuilder.driverLikelihood(fusion.driverLikelihood());

            addActionability(entryBuilder, fusion);

            entries.add(entryBuilder.build());
        }

        return entries;
    }

    @NotNull
    private Set<MolecularDriverEntry> fromViruses(@NotNull Set<Virus> viruses) {
        Set<MolecularDriverEntry> entries = Sets.newHashSet();

        for (Virus virus : viruses) {
            ImmutableMolecularDriverEntry.Builder entryBuilder = ImmutableMolecularDriverEntry.builder();
            entryBuilder.driverType("Virus");
            entryBuilder.driver(virus.event() + ", " + virus.integrations() + " integrations detected");
            entryBuilder.driverLikelihood(virus.driverLikelihood());

            addActionability(entryBuilder, virus);

            entries.add(entryBuilder.build());
        }

        return entries;
    }

    private void addActionability(@NotNull ImmutableMolecularDriverEntry.Builder entryBuilder, @NotNull Driver driver) {
        entryBuilder.actinTrials(inclusiveActinTrials(driver));
        entryBuilder.externalTrials(externalTrials(driver));

        entryBuilder.bestResponsiveEvidence(bestResponsiveEvidence(driver));
        entryBuilder.bestResistanceEvidence(bestResistanceEvidence(driver));
    }

    @NotNull
    private Set<String> inclusiveActinTrials(@NotNull Driver driver) {
        Set<String> trials = Sets.newTreeSet(Ordering.natural());

        if (trialsPerInclusionEvent.containsKey(driver.event())) {
            trials.addAll(trialsPerInclusionEvent.get(driver.event()));
        }

        return trials;
    }

    @NotNull
    private static Set<String> externalTrials(@NotNull Driver driver) {
        Set<String> trials = Sets.newTreeSet(Ordering.natural());

        trials.addAll(driver.evidence().externalEligibleTrials());

        return trials;
    }

    @Nullable
    private static String bestResponsiveEvidence(@NotNull Driver driver) {
        ActionableEvidence evidence = driver.evidence();
        if (!evidence.approvedTreatments().isEmpty()) {
            return "Approved";
        } else if (!evidence.onLabelExperimentalTreatments().isEmpty() || !evidence.offLabelExperimentalTreatments().isEmpty()) {
            return "Experimental";
        } else if (!evidence.preClinicalTreatments().isEmpty()) {
            return "Pre-clinical";
        }

        return null;
    }

    @Nullable
    private static String bestResistanceEvidence(@NotNull Driver driver) {
        ActionableEvidence evidence = driver.evidence();
        if ((!evidence.knownResistantTreatments().isEmpty())) {
            return "Known";
        } else if (!evidence.suspectResistantTreatments().isEmpty()) {
            return "Suspect";
        }

        return null;
    }
}
