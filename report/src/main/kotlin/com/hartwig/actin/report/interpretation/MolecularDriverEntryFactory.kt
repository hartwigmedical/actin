package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.Disruption
import com.hartwig.actin.datamodel.molecular.driver.Driver
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.FusionDriverType
import com.hartwig.actin.datamodel.molecular.driver.GeneAlteration
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.HomozygousDisruption
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TranscriptCopyNumberImpact
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.Virus
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidenceCategories.approved
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidenceCategories.experimental
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidenceCategories.knownResistant
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidenceCategories.preclinical
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidenceCategories.suspectResistant
import com.hartwig.actin.report.interpretation.DriverDisplayFunctions.eventDisplay
import com.hartwig.actin.report.pdf.util.Formats
import kotlin.math.min

class MolecularDriverEntryFactory(private val molecularDriversInterpreter: MolecularDriversInterpreter) {

    private val variantsGroupedByGene = molecularDriversInterpreter.filteredVariants().groupBy { it.gene }

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
        val driverLikelihood = if (variantsGroupedByGene[variant.gene]?.any { it.isCancerAssociatedVariant } == true) {
            ""
        } else {
            variant.driverLikelihood?.let { ", " + formatDriverLikelihood(it) } ?: ""
        }

        val driverType = "Mutation ($mutationTypeString$driverLikelihood)"

        val variantAndTotalCopies =
            variant.variantCopyNumber?.let { vcn -> variant.totalCopyNumber?.let { cn -> listOf(min(vcn, cn), cn) } } ?: listOf(0.0, 0.0)
        val (variantCopyString, totalCopyString) = variantAndTotalCopies.map(::formatCopyNumberString)

        val subClonalIndicator = if (ClonalityInterpreter.isPotentiallySubclonal(variant)) "*" else ""
        val name = "${variant.eventDisplay()} ($variantCopyString/$totalCopyString copies)$subClonalIndicator"

        return driverEntryForGeneAlteration(driverType, name, variant)
    }

    private fun formatMutationType(variant: Variant): String {
        return when {
            variant.isCancerAssociatedVariant && variant.proteinEffect == ProteinEffect.UNKNOWN -> {
                "cancer-associated variant with unknown protein effect"
            }

            variant.isCancerAssociatedVariant && isNoEffect(variant) -> "cancer-associated variant with no protein effect"

            isNoEffect(variant) -> "no protein effect"

            variant.geneRole == GeneRole.ONCO && isGainOfFunction(variant) -> "gain of function"

            variant.geneRole == GeneRole.TSG && isLossOfFunction(variant) && (variant.isBiallelic == true) -> "loss of function, biallelic"

            variant.geneRole == GeneRole.TSG && isLossOfFunction(variant) -> "loss of function"

            (variant.geneRole == GeneRole.UNKNOWN || variant.geneRole == GeneRole.BOTH || variant.geneRole == GeneRole.TSG)
                    && variant.isCancerAssociatedVariant && (variant.isBiallelic == true) -> {
                "cancer-associated variant, biallelic"
            }

            variant.isCancerAssociatedVariant -> "cancer-associated variant"

            (variant.geneRole == GeneRole.UNKNOWN || variant.geneRole == GeneRole.BOTH || variant.geneRole == GeneRole.TSG) &&
                    (variant.isBiallelic == true) -> {
                "no known cancer-associated variant, biallelic"
            }

            else -> "no known cancer-associated variant, not biallelic"
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
        val driverTypes = if (copyNumber.canonicalImpact.type != CopyNumberType.NONE ||
            molecularDriversInterpreter.copyNumberIsActionable(copyNumber)
        ) {
            listOf(getDriverType(copyNumber.canonicalImpact.type, copyNumber.otherImpacts))
        } else {
            copyNumber.otherImpacts.map { impact -> getDriverType(impact.type, null) }
        }
        return driverTypes.map { driverEntryForGeneAlteration(it, copyNumber.eventDisplay(), copyNumber) }
    }

    private fun getDriverType(type: CopyNumberType, impacts: Set<TranscriptCopyNumberImpact>?): String {
        return when {
            type.isGain || impacts?.any { it.type.isGain } == true -> "Amplification"
            type.isDeletion || impacts?.any { it.type.isDeletion } == true -> "Deletion"
            else -> "Copy Number"
        }
    }

    private fun fromHomozygousDisruption(homozygousDisruption: HomozygousDisruption): MolecularDriverEntry {
        return driverEntryForGeneAlteration("Disruption (homozygous)", homozygousDisruption.gene, homozygousDisruption)
    }

    private fun fromDisruption(disruption: Disruption): MolecularDriverEntry {
        val disruptionCopyNumber = Formats.singleDigitNumber(disruption.junctionCopyNumber)
        val undisruptedCopyNumber = Formats.singleDigitNumber(disruption.undisruptedCopyNumber)
        val name = "${disruption.gene}, ${disruption.type} ($disruptionCopyNumber disr. / $undisruptedCopyNumber undisr. copies)"

        return driverEntryForGeneAlteration("Disruption (not biallelic)", name, disruption)
    }

    private fun fromFusion(fusion: Fusion): MolecularDriverEntry {
        val driverType = fusion.driverType.display()
        val driverlikelihood = (fusion.driverLikelihood?.let { " (${formatDriverLikelihood(it)})" } ?: "")

        val knownList = listOf(FusionDriverType.KNOWN_PAIR, FusionDriverType.KNOWN_PAIR_IG, FusionDriverType.KNOWN_PAIR_DEL_DUP)
        val combined = if (fusion.driverType in knownList) driverType else driverType + driverlikelihood

        val fusionWithDomainInfo = if (!fusion.domainsKept.isNullOrEmpty() || !fusion.domainsLost.isNullOrEmpty()) {
            "${fusion.event}\nDomain(s) kept: ${fusion.domainsKept?.joinToString(", ") ?: ""}\nDomain(s) lost: ${
                fusion.domainsLost?.joinToString(
                    ", "
                ) ?: ""
            }"
        } else fusion.event

        return driverEntry(combined, fusionWithDomainInfo, fusion, fusion.proteinEffect)
    }

    private fun fromVirus(virus: Virus): MolecularDriverEntry {
        val driverType = "Virus" + (virus.driverLikelihood?.let { " (${formatDriverLikelihood(it)})" } ?: "")
        return driverEntry(driverType, virus.eventDisplay(), virus)
    }

    private fun <T> driverEntryForGeneAlteration(
        driverType: String, name: String, geneAlteration: T
    ): MolecularDriverEntry where T : Driver, T : GeneAlteration {
        return driverEntry(driverType, name, geneAlteration, geneAlteration.proteinEffect)
    }

    private fun driverEntry(driverType: String, name: String, driver: Driver, proteinEffect: ProteinEffect? = null): MolecularDriverEntry {
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

    private fun formatDriverLikelihood(driverLikelihood: DriverLikelihood?): String {
        return driverLikelihood?.let { it.toString().lowercase() + " driver" } ?: ""
    }

    private fun bestResponsiveEvidence(driver: Driver): String? {
        val evidence = driver.evidence
        return when {
            approved(evidence.treatmentEvidence).isNotEmpty() -> "Approved"
            experimental(evidence.treatmentEvidence, true).isNotEmpty() -> "On-label experimental"
            experimental(evidence.treatmentEvidence, false).isNotEmpty() -> "Off-label experimental"
            preclinical(evidence.treatmentEvidence).isNotEmpty() -> "Pre-clinical"
            else -> null
        }
    }

    private fun bestResistanceEvidence(driver: Driver): String? {
        val evidence = driver.evidence
        return when {
            knownResistant(evidence.treatmentEvidence).isNotEmpty() -> "Known resistance"
            suspectResistant(evidence.treatmentEvidence).isNotEmpty() -> "Suspect resistance"
            else -> null
        }
    }
}