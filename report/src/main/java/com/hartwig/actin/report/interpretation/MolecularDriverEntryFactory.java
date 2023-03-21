package com.hartwig.actin.report.interpretation;

import java.util.List;
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

        return Stream.of(streamAndFilterDrivers(drivers.variants()).map(this::fromVariant),
                        streamAndFilterDrivers(drivers.copyNumbers()).map(this::fromCopyNumber),
                        streamAndFilterDrivers(drivers.homozygousDisruptions()).map(this::fromHomozygousDisruption),
                        streamAndFilterDrivers(drivers.disruptions()).map(this::fromDisruption),
                        streamAndFilterDrivers(drivers.fusions()).map(this::fromFusion),
                        streamAndFilterDrivers(drivers.viruses()).map(this::fromVirus))
                .flatMap(Function.identity())
                .sorted(new MolecularDriverEntryComparator());
    }

    private <T extends Driver> Stream<T> streamAndFilterDrivers(Set<T> drivers) {
        return drivers.stream()
                .filter(driver -> driver.isReportable() || !driver.evidence().externalEligibleTrials().isEmpty()
                        || trialsPerInclusionEvent.containsKey(driver.event()) || !driver.evidence().approvedTreatments().isEmpty());
    }

    private MolecularDriverEntry fromVariant(@NotNull Variant variant) {
        ImmutableMolecularDriverEntry.Builder entryBuilder = ImmutableMolecularDriverEntry.builder();

        String mutationTypeString = variant.isHotspot() ? "Hotspot" : "VUS";
        mutationTypeString = variant.isBiallelic() ? "Biallelic " + mutationTypeString : mutationTypeString;

        entryBuilder.driverType("Mutation (" + mutationTypeString + ")");

        double boundedVariantCopies = Math.max(0, Math.min(variant.variantCopyNumber(), variant.totalCopyNumber()));
        String variantCopyString =
                boundedVariantCopies < 1 ? Formats.singleDigitNumber(boundedVariantCopies) : Formats.noDigitNumber(boundedVariantCopies);

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
    }

    private MolecularDriverEntry fromCopyNumber(@NotNull CopyNumber copyNumber) {
        ImmutableMolecularDriverEntry.Builder entryBuilder = ImmutableMolecularDriverEntry.builder();

        entryBuilder.driverType(copyNumber.type().isGain() ? "Amplification" : "Loss");
        entryBuilder.driver(copyNumber.event() + ", " + copyNumber.minCopies() + " copies");
        entryBuilder.driverLikelihood(copyNumber.driverLikelihood());

        addActionability(entryBuilder, copyNumber);

        return entryBuilder.build();
    }

    private MolecularDriverEntry fromHomozygousDisruption(@NotNull HomozygousDisruption homozygousDisruption) {
        ImmutableMolecularDriverEntry.Builder entryBuilder = ImmutableMolecularDriverEntry.builder();
        entryBuilder.driverType("Disruption (homozygous)");
        entryBuilder.driver(homozygousDisruption.gene());
        entryBuilder.driverLikelihood(homozygousDisruption.driverLikelihood());

        addActionability(entryBuilder, homozygousDisruption);

        return entryBuilder.build();
    }

    @NotNull
    private MolecularDriverEntry fromDisruption(@NotNull Disruption disruption) {
        ImmutableMolecularDriverEntry.Builder entryBuilder = ImmutableMolecularDriverEntry.builder();
        entryBuilder.driverType("Disruption");
        String addon = Formats.singleDigitNumber(disruption.junctionCopyNumber()) + " disr. / "
                + Formats.singleDigitNumber(disruption.undisruptedCopyNumber()) + " undisr. copies";
        entryBuilder.driver(disruption.gene() + ", " + disruption.type() + " (" + addon + ")");
        entryBuilder.driverLikelihood(disruption.driverLikelihood());

        addActionability(entryBuilder, disruption);

        return entryBuilder.build();
    }

    @NotNull
    private MolecularDriverEntry fromFusion(@NotNull Fusion fusion) {
        ImmutableMolecularDriverEntry.Builder entryBuilder = ImmutableMolecularDriverEntry.builder();
        entryBuilder.driverType(fusion.driverType().display());
        entryBuilder.driver(fusion.event() + ", exon " + fusion.fusedExonUp() + " - exon " + fusion.fusedExonDown());
        entryBuilder.driverLikelihood(fusion.driverLikelihood());

        addActionability(entryBuilder, fusion);

        return entryBuilder.build();
    }

    @NotNull
    private MolecularDriverEntry fromVirus(@NotNull Virus virus) {
        ImmutableMolecularDriverEntry.Builder entryBuilder = ImmutableMolecularDriverEntry.builder();
        entryBuilder.driverType("Virus");
        entryBuilder.driver(virus.event() + ", " + virus.integrations() + " integrations detected");
        entryBuilder.driverLikelihood(virus.driverLikelihood());

        addActionability(entryBuilder, virus);

        return entryBuilder.build();
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
