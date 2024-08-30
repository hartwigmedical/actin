package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.Driver
import com.hartwig.actin.datamodel.molecular.Fusion
import com.hartwig.actin.datamodel.molecular.Variant
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidenceCategories.approved
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidenceCategories.experimental
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidenceCategories.knownResistant
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidenceCategories.preclinical
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidenceCategories.suspectResistant
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.orange.driver.Disruption
import com.hartwig.actin.datamodel.molecular.orange.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.orange.driver.Virus
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
        val biallelicIndicator = if (variant.extendedVariantDetails?.isBiallelic == true) "Biallelic " else ""
        val mutationTypeString = if (variant.isHotspot) "Hotspot" else "VUS"
        val driverType = "Mutation ($biallelicIndicator$mutationTypeString)"

        val boundedVariantCopies =
            variant.extendedVariantDetails?.let { min(it.variantCopyNumber, it.totalCopyNumber).coerceAtLeast(0.0) } ?: 0.0
        val variantCopyString =
            if (boundedVariantCopies < 1) Formats.singleDigitNumber(boundedVariantCopies) else Formats.noDigitNumber(boundedVariantCopies)
        val boundedTotalCopies = variant.extendedVariantDetails?.totalCopyNumber?.coerceAtLeast(0.0) ?: 0.0
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
            CopyNumberType.NONE -> "Copy Number"
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
        val name =
            "${fusion.event}, exon ${fusion.extendedFusionOrThrow().fusedExonUp} - exon ${fusion.extendedFusionOrThrow().fusedExonDown}"
        return driverEntry(fusion.driverType.display(), name, fusion)
    }

    private fun fromVirus(virus: Virus): MolecularDriverEntry {
        val name = "${virus.event}, ${virus.integrations} integrations detected"
        return driverEntry("Virus", name, virus)
    }

    private fun driverEntry(driverType: String, name: String, driver: Driver): MolecularDriverEntry {
        return MolecularDriverEntry(
            driverType = driverType,
            displayedName = name,
            eventName = driver.event,
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
                approved(evidence.treatmentEvidence).isNotEmpty() -> {
                    "Approved"
                }

                experimental(evidence.treatmentEvidence, true).isNotEmpty() -> {
                    "On-label experimental"
                }

                experimental(evidence.treatmentEvidence, false).isNotEmpty() -> {
                    "Off-label experimental"
                }

                preclinical(evidence.treatmentEvidence).isNotEmpty() -> {
                    "Pre-clinical"
                }

                else -> null
            }
        }

        private fun bestResistanceEvidence(driver: Driver): String? {
            val evidence = driver.evidence
            return when {
                knownResistant(evidence.treatmentEvidence).isNotEmpty() -> {
                    "Known resistance"
                }

                suspectResistant(evidence.treatmentEvidence).isNotEmpty() -> {
                    "Suspect resistance"
                }

                else -> null
            }
        }
    }
}