package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.Driver
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.GeneAlteration
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidenceCategories.approved
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidenceCategories.experimental
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidenceCategories.knownResistant
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidenceCategories.preclinical
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidenceCategories.suspectResistant
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.Disruption
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.driver.Virus
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
        val mutationTypeString = formatMutationType(variant)
        val driverType = "Mutation ($mutationTypeString)"

        val variantAndTotalCopies = variant.extendedVariantDetails?.let { details ->
            listOf(min(details.variantCopyNumber, details.totalCopyNumber), details.totalCopyNumber)
        } ?: listOf(0.0, 0.0)
        val (variantCopyString, totalCopyString) = variantAndTotalCopies.map(::formatCopyNumberString)

        val subClonalIndicator = if (ClonalityInterpreter.isPotentiallySubclonal(variant)) "*" else ""
        val name = "${variant.event} ($variantCopyString/$totalCopyString copies)$subClonalIndicator"

        return driverEntryForGeneAlteration(driverType, name, variant)
    }

    private fun formatMutationType(variant: Variant): String {
        return when {
            variant.isHotspot && variant.proteinEffect == ProteinEffect.UNKNOWN -> {
                "Hotspot with unknown protein effect"
            }

            variant.isHotspot && isNoEffect(variant) -> {
                "Hotspot with no protein effect"
            }

            isNoEffect(variant) -> {
                "No protein effect"
            }

            variant.geneRole == GeneRole.ONCO && isGainOfFunction(variant) -> {
                "Gain of function"
            }

            variant.geneRole == GeneRole.TSG && (variant.canonicalImpact.codingEffect == CodingEffect.NONSENSE_OR_FRAMESHIFT ||
                    isLossOfFunction(variant)) && (variant.extendedVariantDetails?.isBiallelic == true) -> {
                "Loss of function, biallelic"
            }

            variant.geneRole == GeneRole.TSG &&
                    (variant.canonicalImpact.codingEffect == CodingEffect.NONSENSE_OR_FRAMESHIFT || isLossOfFunction(variant)) -> {
                "Loss of function"
            }

            (variant.geneRole == GeneRole.UNKNOWN || variant.geneRole == GeneRole.BOTH || variant.geneRole == GeneRole.TSG)
                    && variant.isHotspot && (variant.extendedVariantDetails?.isBiallelic == true) -> {
                "Hotspot, biallelic"
            }

            variant.isHotspot -> {
                "Hotspot"
            }

            (variant.geneRole == GeneRole.UNKNOWN || variant.geneRole == GeneRole.BOTH || variant.geneRole == GeneRole.TSG) &&
                    (variant.extendedVariantDetails?.isBiallelic == true) -> {
                "No known hotspot, biallelic"
            }

            else -> {
                "No known hotspot"
            }
        }
    }

    private fun isNoEffect(variant: Variant) =
        variant.proteinEffect in listOf(ProteinEffect.NO_EFFECT, ProteinEffect.NO_EFFECT_PREDICTED)

    private fun isGainOfFunction(variant: Variant) =
        variant.proteinEffect in listOf(ProteinEffect.GAIN_OF_FUNCTION, ProteinEffect.GAIN_OF_FUNCTION_PREDICTED)

    private fun isLossOfFunction(variant: Variant) =
        variant.proteinEffect in listOf(ProteinEffect.LOSS_OF_FUNCTION, ProteinEffect.LOSS_OF_FUNCTION_PREDICTED)

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
            CopyNumberType.DEL -> "Deletion"
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
        return driverEntry(fusion.driverType.display(), fusion.event, fusion, fusion.proteinEffect)
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