package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.Driver
import com.hartwig.actin.datamodel.molecular.Fusion
import com.hartwig.actin.datamodel.molecular.GeneAlteration
import com.hartwig.actin.datamodel.molecular.ProteinEffect
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
        with(molecularDriversInterpreter) {
            return listOf(
                filteredVariants().map(::fromVariant),
                filteredCopyNumbers().flatMap(::fromCopyNumber),
                filteredHomozygousDisruptions().map(::fromHomozygousDisruption),
                filteredDisruptions().map(::fromDisruption),
                filteredFusions().map(::fromFusion),
                filteredViruses().map(::fromVirus)
            )
                .flatten()
                .sortedWith(MolecularDriverEntryComparator())
        }
    }

    private fun fromVariant(variant: Variant): MolecularDriverEntry {
        val biallelicIndicator = if (variant.extendedVariantDetails?.isBiallelic == true) "Biallelic " else ""
        val mutationTypeString = if (variant.isHotspot) "Hotspot" else "VUS"
        val driverType = "Mutation ($biallelicIndicator$mutationTypeString)"

        val variantAndTotalCopies = variant.extendedVariantDetails?.let { details ->
            listOf(min(details.variantCopyNumber, details.totalCopyNumber), details.totalCopyNumber)
        } ?: listOf(0.0, 0.0)
        val (variantCopyString, totalCopyString) = variantAndTotalCopies.map(::formatCopyNumberString)

        val subClonalIndicator = if (ClonalityInterpreter.isPotentiallySubclonal(variant)) "*" else ""
        val name = "${variant.event} ($variantCopyString/$totalCopyString copies)$subClonalIndicator"

        return driverEntryForGeneAlteration(driverType, name, variant)
    }

    private fun formatCopyNumberString(copyNumber: Double): String {
        val boundedCopyNumber = copyNumber.coerceAtLeast(0.0)
        return if (boundedCopyNumber < 1) Formats.singleDigitNumber(boundedCopyNumber) else Formats.noDigitNumber(boundedCopyNumber)
    }

    private fun fromCopyNumber(copyNumber: CopyNumber): List<MolecularDriverEntry> {
        val canonicalDriverType = getDriverType(copyNumber.canonicalImpact.type)
        val canonicalName = "${copyNumber.event}, ${copyNumber.canonicalImpact.minCopies} copies"

        val entries = mutableListOf(driverEntryForGeneAlteration(canonicalDriverType, canonicalName, copyNumber))
        entries.addAll(copyNumber.otherImpacts.map { impact ->
            val otherDriverType = getDriverType(impact.type)
            val otherName = "${copyNumber.event} (alt), ${impact.minCopies} copies"
            driverEntryForGeneAlteration(otherDriverType, otherName, copyNumber)
        })
        return entries
    }

    private fun getDriverType(type: CopyNumberType): String {
        return when (type) {
            CopyNumberType.FULL_GAIN, CopyNumberType.PARTIAL_GAIN -> "Amplification"
            CopyNumberType.LOSS -> "Loss"
            CopyNumberType.NONE -> "Copy Number"
        }
    }

    private fun fromHomozygousDisruption(homozygousDisruption: HomozygousDisruption): MolecularDriverEntry {
        return driverEntryForGeneAlteration("Disruption (homozygous)", homozygousDisruption.gene, homozygousDisruption)
    }

    private fun fromDisruption(disruption: Disruption): MolecularDriverEntry {
        val disruptionCopyNumber = Formats.singleDigitNumber(disruption.junctionCopyNumber)
        val undisruptedCopyNumber = Formats.singleDigitNumber(disruption.undisruptedCopyNumber)
        val name = "${disruption.gene}, ${disruption.type} ($disruptionCopyNumber disr. / $undisruptedCopyNumber undisr. copies)"

        return driverEntryForGeneAlteration("Disruption", name, disruption)
    }

    private fun fromFusion(fusion: Fusion): MolecularDriverEntry {
        val name = if (fusion.fusedExonUp == null || fusion.fusedExonDown == null) {
            fusion.event
        } else {
            "${fusion.event}, exon ${fusion.fusedExonUp} - exon ${fusion.fusedExonDown}"
        }
        return driverEntry(fusion.driverType.display(), name, fusion, fusion.proteinEffect)
    }

    private fun fromVirus(virus: Virus): MolecularDriverEntry {
        val name = "${virus.event}, ${virus.integrations} integrations detected"
        return driverEntry("Virus", name, virus)
    }

    private fun <T> driverEntryForGeneAlteration(
        driverType: String, name: String, geneAlteration: T
    ): MolecularDriverEntry where T : Driver, T : GeneAlteration {
        return driverEntry(driverType, name, geneAlteration, geneAlteration.proteinEffect)
    }

    private fun driverEntry(
        driverType: String, name: String, driver: Driver, proteinEffect: ProteinEffect? = null
    ): MolecularDriverEntry {
        return MolecularDriverEntry(
            driverType = driverType,
            description = name,
            event = driver.event,
            driverLikelihood = driver.driverLikelihood,
            evidenceTier = driver.evidenceTier(),
            proteinEffect = proteinEffect,
            actinTrials = molecularDriversInterpreter.trialsForDriver(driver).toSet(),
            externalTrials = driver.evidence.externalEligibleTrials,
            bestResponsiveEvidence = bestResponsiveEvidence(driver),
            bestResistanceEvidence = bestResistanceEvidence(driver)
        )
    }

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