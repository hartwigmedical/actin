package com.hartwig.actin.report.interpretation;

import java.util.Set;

import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.molecular.datamodel.driver.Amplification;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.Driver;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.driver.Virus;
import com.hartwig.actin.report.pdf.util.Formats;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MolecularDriverEntryFactory {

    private MolecularDriverEntryFactory() {
    }

    @NotNull
    public static Set<MolecularDriverEntry> create(@NotNull TreatmentMatch treatmentMatch, @NotNull MolecularDrivers drivers) {
        Set<MolecularDriverEntry> entries = Sets.newTreeSet(new MolecularDriverEntryComparator());

        entries.addAll(fromVariants(treatmentMatch, drivers.variants()));
        entries.addAll(fromAmplifications(treatmentMatch, drivers.amplifications()));
        entries.addAll(fromLosses(treatmentMatch, drivers.losses()));
        entries.addAll(fromHomozygousDisruptions(treatmentMatch, drivers.homozygousDisruptions()));
        entries.addAll(fromDisruptions(treatmentMatch, drivers.disruptions()));
        entries.addAll(fromFusions(treatmentMatch, drivers.fusions()));
        entries.addAll(fromViruses(treatmentMatch, drivers.viruses()));

        return entries;
    }

    @NotNull
    private static Set<MolecularDriverEntry> fromVariants(@NotNull TreatmentMatch treatmentMatch, @NotNull Set<Variant> variants) {
        Set<MolecularDriverEntry> entries = Sets.newHashSet();

        for (Variant variant : variants) {
            ImmutableMolecularDriverEntry.Builder entryBuilder = ImmutableMolecularDriverEntry.builder();
            // TODO Implement
            entryBuilder.driverType("Mutation (TODO type))");

            double boundedVariantCopies = Math.max(0, Math.min(variant.variantCopyNumber(), variant.totalCopyNumber()));
            String variantCopyString = boundedVariantCopies < 1
                    ? Formats.singleDigitNumber(boundedVariantCopies)
                    : Formats.noDigitNumber(boundedVariantCopies);

            double boundedTotalCopies = Math.max(0, variant.totalCopyNumber());
            String totalCopyString =
                    boundedTotalCopies < 1 ? Formats.singleDigitNumber(boundedTotalCopies) : Formats.noDigitNumber(boundedTotalCopies);

            // TODO Add Impact
            String driver = variant.gene() + " <impact> (" + variantCopyString + "/" + totalCopyString + " copies)";
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
    private static Set<MolecularDriverEntry> fromAmplifications(@NotNull TreatmentMatch treatmentMatch,
            @NotNull Set<Amplification> amplifications) {
        Set<MolecularDriverEntry> entries = Sets.newHashSet();

        for (Amplification amplification : amplifications) {
            ImmutableMolecularDriverEntry.Builder entryBuilder = ImmutableMolecularDriverEntry.builder();

            // TODO Replace partial
            String addon = Strings.EMPTY; //amplification.isPartial() ? " (partial)" : Strings.EMPTY;
            entryBuilder.driverType("Amplification" + addon);
            entryBuilder.driver(amplification.gene() + " amp, " + amplification.minCopies() + " copies");
            entryBuilder.driverLikelihood(amplification.driverLikelihood());

            addActionability(entryBuilder, amplification);

            entries.add(entryBuilder.build());
        }

        return entries;
    }

    @NotNull
    private static Set<MolecularDriverEntry> fromLosses(@NotNull TreatmentMatch treatmentMatch, @NotNull Set<Loss> losses) {
        Set<MolecularDriverEntry> entries = Sets.newHashSet();

        for (Loss loss : losses) {
            ImmutableMolecularDriverEntry.Builder entryBuilder = ImmutableMolecularDriverEntry.builder();
            entryBuilder.driverType("Loss");
            entryBuilder.driver(loss.gene() + " del");
            entryBuilder.driverLikelihood(loss.driverLikelihood());

            addActionability(entryBuilder, loss);

            entries.add(entryBuilder.build());
        }

        return entries;
    }

    @NotNull
    private static Set<MolecularDriverEntry> fromHomozygousDisruptions(@NotNull TreatmentMatch treatmentMatch,
            @NotNull Set<HomozygousDisruption> homozygousDisruptions) {
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
    private static Set<MolecularDriverEntry> fromDisruptions(@NotNull TreatmentMatch treatmentMatch, @NotNull Set<Disruption> disruptions) {
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
    private static Set<MolecularDriverEntry> fromFusions(@NotNull TreatmentMatch treatmentMatch, @NotNull Set<Fusion> fusions) {
        Set<MolecularDriverEntry> entries = Sets.newHashSet();

        for (Fusion fusion : fusions) {
            ImmutableMolecularDriverEntry.Builder entryBuilder = ImmutableMolecularDriverEntry.builder();
            entryBuilder.driverType(fusion.driverType().display());
            String name = fusion.geneStart() + "-" + fusion.geneEnd() + " fusion";
            //            entryBuilder.driver(name + ", " + fusion.details());
            entryBuilder.driver(name);
            entryBuilder.driverLikelihood(fusion.driverLikelihood());

            addActionability(entryBuilder, fusion);

            entries.add(entryBuilder.build());
        }

        return entries;
    }

    @NotNull
    private static Set<MolecularDriverEntry> fromViruses(@NotNull TreatmentMatch treatmentMatch, @NotNull Set<Virus> viruses) {
        Set<MolecularDriverEntry> entries = Sets.newHashSet();

        for (Virus virus : viruses) {
            ImmutableMolecularDriverEntry.Builder entryBuilder = ImmutableMolecularDriverEntry.builder();
            entryBuilder.driverType("Virus");
            entryBuilder.driver(virus.name() + ", " + virus.integrations() + " integrations detected");
            entryBuilder.driverLikelihood(virus.driverLikelihood());

            addActionability(entryBuilder, virus);

            entries.add(entryBuilder.build());
        }

        return entries;
    }

    private static void addActionability(@NotNull ImmutableMolecularDriverEntry.Builder entryBuilder, @NotNull Driver driver) {
        entryBuilder.actinTrials(inclusiveActinTrials(driver));
        entryBuilder.externalTrials(externalTrials(driver));

        entryBuilder.bestResponsiveEvidence(bestResponsiveEvidence(driver));
        entryBuilder.bestResistanceEvidence(bestResistanceEvidence(driver));
    }

    @NotNull
    private static Set<String> inclusiveActinTrials(@NotNull Driver driver) {
        Set<String> trials = Sets.newTreeSet(Ordering.natural());
        // TODO figure out how to implement
        //        for (ActinTrialEvidence evidence : actinTrials) {
        //            if (evidence.isInclusionCriterion() && evidence.event().equals(driver.event())) {
        //                trials.add(evidence.trialAcronym());
        //            }
        //        }
        return trials;
    }

    @NotNull
    private static Set<String> externalTrials(@NotNull Driver driver) {
        Set<String> trials = Sets.newTreeSet(Ordering.natural());
        // TODO Implement
        return trials;
    }

    @Nullable
    private static String bestResponsiveEvidence(@NotNull Driver driver) {
        // TODO Implement
        //        if (hasEvidence(driver, evidence.approvedEvidence())) {
        //            return "Approved";
        //        } else if (hasEvidence(driver, evidence.onLabelExperimentalEvidence()) || hasEvidence(driver,
        //                evidence.offLabelExperimentalEvidence())) {
        //            return "Experimental";
        //        } else if (hasEvidence(driver, evidence.preClinicalEvidence())) {
        //            return "Pre-clinical";
        //        }

        return null;
    }

    @Nullable
    private static String bestResistanceEvidence(@NotNull Driver driver) {
        // TODO Implement
        //        if (hasEvidence(driver, evidence.knownResistanceEvidence())) {
        //            return "Known";
        //        } else if (hasEvidence(driver, evidence.suspectResistanceEvidence())) {
        //            return "Suspect";
        //        }

        return null;
    }
}
