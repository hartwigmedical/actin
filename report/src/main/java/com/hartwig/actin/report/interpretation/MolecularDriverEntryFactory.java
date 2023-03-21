package com.hartwig.actin.report.interpretation;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.driver.CopyNumber;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.Driver;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.driver.Virus;
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence;
import com.hartwig.actin.report.pdf.util.Formats;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MolecularDriverEntryFactory {

    private final MolecularDriversDetails molecularDriversDetails;

    public MolecularDriverEntryFactory(MolecularDriversDetails molecularDriversDetails) {
        this.molecularDriversDetails = molecularDriversDetails;
    }

    @NotNull
    public Stream<MolecularDriverEntry> create() {
        return Stream.of(molecularDriversDetails.filteredVariants().map(this::fromVariant),
                        molecularDriversDetails.filteredCopyNumbers().map(this::fromCopyNumber),
                        molecularDriversDetails.filteredHomozygousDisruptions().map(this::fromHomozygousDisruption),
                        molecularDriversDetails.filteredDisruptions().map(this::fromDisruption),
                        molecularDriversDetails.filteredFusions().map(this::fromFusion),
                        molecularDriversDetails.filteredViruses().map(this::fromVirus))
                .flatMap(Function.identity())
                .sorted(new MolecularDriverEntryComparator());
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
        entryBuilder.actinTrials(molecularDriversDetails.trialsForDriver(driver));
        entryBuilder.externalTrials(externalTrials(driver));

        entryBuilder.bestResponsiveEvidence(bestResponsiveEvidence(driver));
        entryBuilder.bestResistanceEvidence(bestResistanceEvidence(driver));
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
