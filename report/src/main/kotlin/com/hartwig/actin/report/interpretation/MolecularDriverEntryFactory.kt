package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.driver.Disruption
import com.hartwig.actin.molecular.datamodel.driver.Driver
import com.hartwig.actin.molecular.datamodel.driver.Fusion
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption
import com.hartwig.actin.molecular.datamodel.driver.Variant
import com.hartwig.actin.molecular.datamodel.driver.Virus
import com.hartwig.actin.report.pdf.util.Formats
import kotlin.math.min

class MolecularDriverEntryFactory(private val molecularDriversInterpreter: MolecularDriversInterpreter) {
    fun create(): List<MolecularDriverEntry> {
        return listOf(
            molecularDriversInterpreter.filteredVariants().map { variant: Variant -> fromVariant(variant) },
            molecularDriversInterpreter.filteredCopyNumbers().map { copyNumber: CopyNumber -> fromCopyNumber(copyNumber) },
            molecularDriversInterpreter.filteredHomozygousDisruptions()
                .map { homozygousDisruption: HomozygousDisruption -> fromHomozygousDisruption(homozygousDisruption) },
            molecularDriversInterpreter.filteredDisruptions().map { disruption: Disruption -> fromDisruption(disruption) },
            molecularDriversInterpreter.filteredFusions().map { fusion: Fusion -> fromFusion(fusion) },
            molecularDriversInterpreter.filteredViruses().map { virus: Virus -> fromVirus(virus) }
        )
            .flatten()
            .sortedWith(MolecularDriverEntryComparator())
    }

    private fun fromVariant(variant: Variant): MolecularDriverEntry {
        val biallelicIndicator = if (variant.isBiallelic) "Biallelic " else ""
        val mutationTypeString = if (variant.isHotspot) "Hotspot" else "VUS"
        val driverType = "Mutation ($biallelicIndicator$mutationTypeString)"

        val boundedVariantCopies = min(variant.variantCopyNumber, variant.totalCopyNumber).coerceAtLeast(0.0)
        val variantCopyString =
            if (boundedVariantCopies < 1) Formats.singleDigitNumber(boundedVariantCopies) else Formats.noDigitNumber(boundedVariantCopies)
        val boundedTotalCopies = variant.totalCopyNumber.coerceAtLeast(0.0)
        val totalCopyString =
            if (boundedTotalCopies < 1) Formats.singleDigitNumber(boundedTotalCopies) else Formats.noDigitNumber(boundedTotalCopies)
        val subClonalIndicator = if (ClonalityInterpreter.isPotentiallySubclonal(variant)) "*" else ""
        val name = "${variant.event} ($variantCopyString/$totalCopyString copies)$subClonalIndicator"

        return driverEntry(driverType, name, variant)
    }

    private fun fromCopyNumber(copyNumber: CopyNumber): MolecularDriverEntry {
        val driverType = when (copyNumber.type) {
            CopyNumberType.FULL_GAIN, CopyNumberType.PARTIAL_GAIN -> "Amplification"
            CopyNumberType.LOSS -> "Loss"
            CopyNumberType.NONE -> "None"
        }
        val name = copyNumber.event + ", " + copyNumber.minCopies + " copies"
        return driverEntry(driverType, name, copyNumber)
    }

    private fun fromHomozygousDisruption(homozygousDisruption: HomozygousDisruption): MolecularDriverEntry {
        return driverEntry("Disruption (homozygous)", homozygousDisruption.gene, homozygousDisruption)
    }

    private fun fromDisruption(disruption: Disruption): MolecularDriverEntry {
        val disruptionCopyNumber = Formats.singleDigitNumber(disruption.junctionCopyNumber)
        val undisruptedCopyNumber = Formats.singleDigitNumber(disruption.undisruptedCopyNumber)
        val name = "${disruption.gene}, ${disruption.type} ($disruptionCopyNumber disr. / $undisruptedCopyNumber undisr. copies)"

        return driverEntry("Disruption", name, disruption)
    }

    private fun fromFusion(fusion: Fusion): MolecularDriverEntry {
        val name = fusion.event + ", exon " + fusion.fusedExonUp + " - exon " + fusion.fusedExonDown
        return driverEntry(fusion.driverType.display(), name, fusion)
    }

    private fun fromVirus(virus: Virus): MolecularDriverEntry {
        val name = virus.event + ", " + virus.integrations + " integrations detected"
        return driverEntry("Virus", name, virus)
    }

    private fun driverEntry(driverType: String, name: String, driver: Driver): MolecularDriverEntry {
        return MolecularDriverEntry(
            driverType = driverType,
            driver = name,
            driverLikelihood = driver.driverLikelihood,
            actinTrials = molecularDriversInterpreter.trialsForDriver(driver).toSet(),
            externalTrials = driver.evidence.externalEligibleTrials,
            bestResponsiveEvidence = bestResponsiveEvidence(driver),
            bestResistanceEvidence = bestResistanceEvidence(driver)
        )
    }

    companion object {
        private fun bestResponsiveEvidence(driver: Driver): String? {
            val evidence = driver.evidence
            return when {
                evidence.approvedTreatments.isNotEmpty() -> {
                    "Approved"
                }

                evidence.onLabelExperimentalTreatments.isNotEmpty() -> {
                    "On-label experimental"
                }

                evidence.offLabelExperimentalTreatments.isNotEmpty() -> {
                    "Off-label experimental"
                }

                evidence.preClinicalTreatments.isNotEmpty() -> {
                    "Pre-clinical"
                }

                else -> null
            }
        }

        private fun bestResistanceEvidence(driver: Driver): String? {
            val evidence = driver.evidence
            return when {
                evidence.knownResistantTreatments.isNotEmpty() -> {
                    "Known resistance"
                }

                evidence.suspectResistantTreatments.isNotEmpty() -> {
                    "Suspect resistance"
                }

                else -> null
            }
        }
    }
}