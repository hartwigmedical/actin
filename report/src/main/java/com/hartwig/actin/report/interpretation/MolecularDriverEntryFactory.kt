package com.hartwig.actin.report.interpretation

import com.google.common.collect.Ordering
import com.google.common.collect.Sets
import com.hartwig.actin.molecular.datamodel.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.driver.Disruption
import com.hartwig.actin.molecular.datamodel.driver.Driver
import com.hartwig.actin.molecular.datamodel.driver.Fusion
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption
import com.hartwig.actin.molecular.datamodel.driver.Variant
import com.hartwig.actin.molecular.datamodel.driver.Virus
import com.hartwig.actin.report.pdf.util.Formats
import java.util.function.Function
import java.util.stream.Stream

class MolecularDriverEntryFactory(private val molecularDriversInterpreter: MolecularDriversInterpreter) {
    fun create(): Stream<MolecularDriverEntry?> {
        return Stream.of(
            molecularDriversInterpreter.filteredVariants().map { variant: Variant -> fromVariant(variant) },
            molecularDriversInterpreter.filteredCopyNumbers().map { copyNumber: CopyNumber -> fromCopyNumber(copyNumber) },
            molecularDriversInterpreter.filteredHomozygousDisruptions()
                .map { homozygousDisruption: HomozygousDisruption -> fromHomozygousDisruption(homozygousDisruption) },
            molecularDriversInterpreter.filteredDisruptions().map { disruption: Disruption -> fromDisruption(disruption) },
            molecularDriversInterpreter.filteredFusions().map { fusion: Fusion -> fromFusion(fusion) },
            molecularDriversInterpreter.filteredViruses().map { virus: Virus -> fromVirus(virus) })
            .flatMap(Function.identity())
            .sorted(MolecularDriverEntryComparator())
    }

    private fun fromVariant(variant: Variant): MolecularDriverEntry {
        val entryBuilder = ImmutableMolecularDriverEntry.builder()
        var mutationTypeString = if (variant.isHotspot) "Hotspot" else "VUS"
        mutationTypeString = if (variant.isBiallelic) "Biallelic $mutationTypeString" else mutationTypeString
        entryBuilder.driverType("Mutation ($mutationTypeString)")
        val boundedVariantCopies = Math.max(0.0, Math.min(variant.variantCopyNumber(), variant.totalCopyNumber()))
        val variantCopyString =
            if (boundedVariantCopies < 1) Formats.singleDigitNumber(boundedVariantCopies) else Formats.noDigitNumber(boundedVariantCopies)
        val boundedTotalCopies = Math.max(0.0, variant.totalCopyNumber())
        val totalCopyString =
            if (boundedTotalCopies < 1) Formats.singleDigitNumber(boundedTotalCopies) else Formats.noDigitNumber(boundedTotalCopies)
        var driver = variant.event() + " (" + variantCopyString + "/" + totalCopyString + " copies)"
        if (ClonalityInterpreter.isPotentiallySubclonal(variant)) {
            driver = "$driver*"
        }
        entryBuilder.driver(driver)
        entryBuilder.driverLikelihood(variant.driverLikelihood())
        addActionability(entryBuilder, variant)
        return entryBuilder.build()
    }

    private fun fromCopyNumber(copyNumber: CopyNumber): MolecularDriverEntry {
        val entryBuilder = ImmutableMolecularDriverEntry.builder()
        entryBuilder.driverType(if (copyNumber.type().isGain) "Amplification" else "Loss")
        entryBuilder.driver(copyNumber.event() + ", " + copyNumber.minCopies() + " copies")
        entryBuilder.driverLikelihood(copyNumber.driverLikelihood())
        addActionability(entryBuilder, copyNumber)
        return entryBuilder.build()
    }

    private fun fromHomozygousDisruption(homozygousDisruption: HomozygousDisruption): MolecularDriverEntry {
        val entryBuilder = ImmutableMolecularDriverEntry.builder()
        entryBuilder.driverType("Disruption (homozygous)")
        entryBuilder.driver(homozygousDisruption.gene())
        entryBuilder.driverLikelihood(homozygousDisruption.driverLikelihood())
        addActionability(entryBuilder, homozygousDisruption)
        return entryBuilder.build()
    }

    private fun fromDisruption(disruption: Disruption): MolecularDriverEntry {
        val entryBuilder = ImmutableMolecularDriverEntry.builder()
        entryBuilder.driverType("Disruption")
        val addon = (Formats.singleDigitNumber(disruption.junctionCopyNumber()) + " disr. / "
                + Formats.singleDigitNumber(disruption.undisruptedCopyNumber()) + " undisr. copies")
        entryBuilder.driver(disruption.gene() + ", " + disruption.type() + " (" + addon + ")")
        entryBuilder.driverLikelihood(disruption.driverLikelihood())
        addActionability(entryBuilder, disruption)
        return entryBuilder.build()
    }

    private fun fromFusion(fusion: Fusion): MolecularDriverEntry {
        val entryBuilder = ImmutableMolecularDriverEntry.builder()
        entryBuilder.driverType(fusion.driverType().display())
        entryBuilder.driver(fusion.event() + ", exon " + fusion.fusedExonUp() + " - exon " + fusion.fusedExonDown())
        entryBuilder.driverLikelihood(fusion.driverLikelihood())
        addActionability(entryBuilder, fusion)
        return entryBuilder.build()
    }

    private fun fromVirus(virus: Virus): MolecularDriverEntry {
        val entryBuilder = ImmutableMolecularDriverEntry.builder()
        entryBuilder.driverType("Virus")
        entryBuilder.driver(virus.event() + ", " + virus.integrations() + " integrations detected")
        entryBuilder.driverLikelihood(virus.driverLikelihood())
        addActionability(entryBuilder, virus)
        return entryBuilder.build()
    }

    private fun addActionability(entryBuilder: ImmutableMolecularDriverEntry.Builder, driver: Driver) {
        entryBuilder.actinTrials(molecularDriversInterpreter.trialsForDriver(driver))
        entryBuilder.externalTrials(externalTrials(driver))
        entryBuilder.bestResponsiveEvidence(bestResponsiveEvidence(driver))
        entryBuilder.bestResistanceEvidence(bestResistanceEvidence(driver))
    }

    companion object {
        private fun externalTrials(driver: Driver): Set<String> {
            val trials: MutableSet<String> = Sets.newTreeSet(Ordering.natural())
            trials.addAll(driver.evidence().externalEligibleTrials())
            return trials
        }

        private fun bestResponsiveEvidence(driver: Driver): String? {
            val evidence = driver.evidence()
            if (!evidence.approvedTreatments().isEmpty()) {
                return "Approved"
            } else if (!evidence.onLabelExperimentalTreatments().isEmpty()) {
                return "On-label experimental"
            } else if (!evidence.offLabelExperimentalTreatments().isEmpty()) {
                return "Off-label experimental"
            } else if (!evidence.preClinicalTreatments().isEmpty()) {
                return "Pre-clinical"
            }
            return null
        }

        private fun bestResistanceEvidence(driver: Driver): String? {
            val evidence = driver.evidence()
            if (!evidence.knownResistantTreatments().isEmpty()) {
                return "Known resistance"
            } else if (!evidence.suspectResistantTreatments().isEmpty()) {
                return "Suspect resistance"
            }
            return null
        }
    }
}