package com.hartwig.actin.report.interpretation;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.driver.CopyNumber;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.Driver;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.driver.Virus;
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence;
import com.hartwig.actin.report.pdf.util.Formats;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MolecularDriverEntryFactory {

    private static final String RESPONSIVE_EVIDENCE_APPROVED_TREATMENTS = "Approved";

    @NotNull
    private final Multimap<String, String> trialsPerInclusionEvent;

    @NotNull
    public static MolecularDriverEntryFactory fromEvaluatedCohorts(@NotNull List<EvaluatedCohort> evaluatedCohorts) {
        Multimap<String, String> trialsPerInclusionEvent = ArrayListMultimap.create();
        for (EvaluatedCohort evaluatedCohort : evaluatedCohorts) {
            if (evaluatedCohort.isPotentiallyEligible() && evaluatedCohort.isOpen()) {
                for (String molecularEvent : evaluatedCohort.molecularEvents()) {
                    trialsPerInclusionEvent.put(molecularEvent, evaluatedCohort.acronym());
                }
            }
        }
        return new MolecularDriverEntryFactory(trialsPerInclusionEvent);
    }

    private MolecularDriverEntryFactory(@NotNull final Multimap<String, String> trialsPerInclusionEvent) {
        this.trialsPerInclusionEvent = trialsPerInclusionEvent;
    }

    @NotNull
    public Stream<MolecularDriverEntry> create(@NotNull MolecularRecord molecular) {
        MolecularDrivers drivers = molecular.drivers();

        return Stream.of(fromVariants(drivers.variants()),
                        fromCopyNumbers(drivers.copyNumbers()),
                        fromHomozygousDisruptions(drivers.homozygousDisruptions()),
                        fromDisruptions(drivers.disruptions()),
                        fromFusions(drivers.fusions()),
                        fromViruses(drivers.viruses()))
                .flatMap(Function.identity())
                .filter(entry -> entry.driverLikelihood() != null || !entry.externalTrials().isEmpty() || !entry.actinTrials().isEmpty()
                        || Objects.equals(entry.bestResponsiveEvidence(), RESPONSIVE_EVIDENCE_APPROVED_TREATMENTS))
                .sorted(new MolecularDriverEntryComparator());
    }

    private Stream<MolecularDriverEntry> fromVariants(@NotNull Set<Variant> variants) {
        return variants.stream().filter(Driver::isReportable).map(variant -> {
            ImmutableMolecularDriverEntry.Builder entryBuilder = ImmutableMolecularDriverEntry.builder();

            String mutationTypeString = variant.isHotspot() ? "Hotspot" : "VUS";
            mutationTypeString = variant.isBiallelic() ? "Biallelic " + mutationTypeString : mutationTypeString;

            entryBuilder.driverType("Mutation (" + mutationTypeString + ")");

            double boundedVariantCopies = Math.max(0, Math.min(variant.variantCopyNumber(), variant.totalCopyNumber()));
            String variantCopyString = boundedVariantCopies < 1
                    ? Formats.singleDigitNumber(boundedVariantCopies)
                    : Formats.noDigitNumber(boundedVariantCopies);

            double boundedTotalCopies = Math.max(0, variant.totalCopyNumber());
            String totalCopyString =
                    boundedTotalCopies < 1 ? Formats.singleDigitNumber(boundedTotalCopies) : Formats.noDigitNumber(boundedTotalCopies);

            String driver = variant.event() + " (" + variantCopyString + "/" + totalCopyString + " copies)";
            if (ClonalityInterpreter.isPotentiallySubclonal(variant)) {
                driver = driver + "*";
            }
            entryBuilder.driver(driver);
            entryBuilder.driverLikelihood(variant.driverLikelihood());

            addActionability(entryBuilder, variant);

            return entryBuilder.build();
        });
    }

    private Stream<MolecularDriverEntry> fromCopyNumbers(@NotNull Set<CopyNumber> copyNumbers) {
        return copyNumbers.stream().filter(Driver::isReportable).map(copyNumber -> {
            ImmutableMolecularDriverEntry.Builder entryBuilder = ImmutableMolecularDriverEntry.builder();

            entryBuilder.driverType(copyNumber.type().isGain() ? "Amplification" : "Loss");
            entryBuilder.driver(copyNumber.event() + ", " + copyNumber.minCopies() + " copies");
            entryBuilder.driverLikelihood(copyNumber.driverLikelihood());

            addActionability(entryBuilder, copyNumber);

            return entryBuilder.build();
        });
    }

    private Stream<MolecularDriverEntry> fromHomozygousDisruptions(@NotNull Set<HomozygousDisruption> homozygousDisruptions) {
        return homozygousDisruptions.stream().filter(Driver::isReportable).map(homozygousDisruption -> {
            ImmutableMolecularDriverEntry.Builder entryBuilder = ImmutableMolecularDriverEntry.builder();
            entryBuilder.driverType("Disruption (homozygous)");
            entryBuilder.driver(homozygousDisruption.gene());
            entryBuilder.driverLikelihood(homozygousDisruption.driverLikelihood());

            addActionability(entryBuilder, homozygousDisruption);

            return entryBuilder.build();
        });
    }

    @NotNull
    private Stream<MolecularDriverEntry> fromDisruptions(@NotNull Set<Disruption> disruptions) {
        return disruptions.stream().filter(Driver::isReportable).map(disruption -> {
            ImmutableMolecularDriverEntry.Builder entryBuilder = ImmutableMolecularDriverEntry.builder();
            entryBuilder.driverType("Disruption");
            String addon = Formats.singleDigitNumber(disruption.junctionCopyNumber()) + " disr. / "
                    + Formats.singleDigitNumber(disruption.undisruptedCopyNumber()) + " undisr. copies";
            entryBuilder.driver(disruption.gene() + ", " + disruption.type() + " (" + addon + ")");
            entryBuilder.driverLikelihood(disruption.driverLikelihood());

            addActionability(entryBuilder, disruption);

            return entryBuilder.build();
        });
    }

    @NotNull
    private Stream<MolecularDriverEntry> fromFusions(@NotNull Set<Fusion> fusions) {
        return fusions.stream().filter(Driver::isReportable).map(fusion -> {
            ImmutableMolecularDriverEntry.Builder entryBuilder = ImmutableMolecularDriverEntry.builder();
            entryBuilder.driverType(fusion.driverType().display());
            entryBuilder.driver(fusion.event() + ", exon " + fusion.fusedExonUp() + " - exon " + fusion.fusedExonDown());
            entryBuilder.driverLikelihood(fusion.driverLikelihood());

            addActionability(entryBuilder, fusion);

            return entryBuilder.build();
        });
    }

    @NotNull
    private Stream<MolecularDriverEntry> fromViruses(@NotNull Set<Virus> viruses) {
        return viruses.stream().map(virus -> {
            ImmutableMolecularDriverEntry.Builder entryBuilder = ImmutableMolecularDriverEntry.builder();
            entryBuilder.driverType("Virus");
            entryBuilder.driver(virus.event() + ", " + virus.integrations() + " integrations detected");
            entryBuilder.driverLikelihood(virus.driverLikelihood());

            addActionability(entryBuilder, virus);

            return entryBuilder.build();
        });
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
            return RESPONSIVE_EVIDENCE_APPROVED_TREATMENTS;
        } else if (!evidence.onLabelExperimentalTreatments().isEmpty()) {
            return "On-label experimental";
        } else if (!evidence.offLabelExperimentalTreatments().isEmpty()) {
            return "Off-label experimental";
        } else if (!evidence.preClinicalTreatments().isEmpty()) {
            return "Pre-clinical";
        }

        return null;
    }

    @Nullable
    private static String bestResistanceEvidence(@NotNull Driver driver) {
        ActionableEvidence evidence = driver.evidence();
        if ((!evidence.knownResistantTreatments().isEmpty())) {
            return "Known resistance";
        } else if (!evidence.suspectResistantTreatments().isEmpty()) {
            return "Suspect resistance";
        }

        return null;
    }
}
