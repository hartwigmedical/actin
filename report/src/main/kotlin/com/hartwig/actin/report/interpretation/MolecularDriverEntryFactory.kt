package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.Disruption
import com.hartwig.actin.datamodel.molecular.driver.Driver
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.GeneAlteration
import com.hartwig.actin.datamodel.molecular.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.Virus
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidenceCategories.approved
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidenceCategories.experimental
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidenceCategories.knownResistant
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidenceCategories.preclinical
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidenceCategories.suspectResistant
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
        val name =
            "${variant.event} (${variant.canonicalImpact.hgvsCodingImpact}), $variantCopyString/$totalCopyString copies$subClonalIndicator"

        return driverEntryForGeneAlteration(driverType, name, variant)
    }

    private fun formatCopyNumberString(copyNumber: Double): String {
        val boundedCopyNumber = copyNumber.coerceAtLeast(0.0)
        return if (boundedCopyNumber < 1) Formats.forcedSingleDigitNumber(boundedCopyNumber) else Formats.noDigitNumber(boundedCopyNumber)
    }

    private fun fromCopyNumber(copyNumber: CopyNumber): List<MolecularDriverEntry> {
        val entries = mutableListOf<MolecularDriverEntry>()
        if (copyNumber.canonicalImpact.type != CopyNumberType.NONE || molecularDriversInterpreter.copyNumberIsActionable(copyNumber)) {
            val canonicalDriverType = getDriverType(copyNumber.canonicalImpact.type)
            val canonicalName = "${copyNumber.event}, ${copyNumber.canonicalImpact.minCopies} copies"
            entries.add(driverEntryForGeneAlteration(canonicalDriverType, canonicalName, copyNumber))
        }

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
            externalTrials = driver.evidence.eligibleTrials,
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