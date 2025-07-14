package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.Driver
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.GeneAlteration

class MolecularDriversSummarizer private constructor(
    private val drivers: Drivers,
    private val interpretedCohortsSummarizer: InterpretedCohortsSummarizer
) {

    fun keyVariants(): List<String> {
        val highDriverVariants = drivers.variants.filter(::isReportableHighDriver)
        val variantsAssociatedWithDrugResistance = drivers.variants.filter { it.isReportable && it.isAssociatedWithDrugResistance == true }
        return (highDriverVariants + variantsAssociatedWithDrugResistance).toSet().map { it.event }.sorted()
    }

    fun otherVariants(): List<String> {
        val otherVariants = drivers.variants.filterNot { it.event in keyVariants() }
        return otherVariants.toSet().map { it.event }.sorted()
    }

    fun keyAmplifiedGenes(): List<String> {
        return drivers.copyNumbers
            .asSequence()
            .filter { copyNumber -> copyNumber.canonicalImpact.type.isGain || copyNumber.otherImpacts.any { it.type.isGain } }
            .filter(::isReportableHighDriver)
            .map { it.gene + annotateCopyNumber(it.canonicalImpact.minCopies, it.canonicalImpact.maxCopies, it.canonicalImpact.type) }
            .distinct()
            .toList()
    }

    fun keyDeletedGenes(): List<String> {
        return drivers.copyNumbers
            .asSequence()
            .filter { copyNumber -> copyNumber.canonicalImpact.type.isDeletion || copyNumber.otherImpacts.any { it.type.isDeletion } }
            .filter(::isReportableHighDriver)
            .map { it.gene + if (it.canonicalImpact.type == CopyNumberType.NONE) " (alt transcript)" else "" }
            .distinct()
            .toList()
    }

    fun keyHomozygouslyDisruptedGenes(): List<String> {
        return drivers.homozygousDisruptions.filter(::isReportableHighDriver).map(GeneAlteration::gene).distinct()
    }

    fun keyFusionEvents(): List<String> {
        return drivers.fusions.filter(::isReportableHighDriver).map(Fusion::event).distinct()
    }

    fun keyVirusEvents(): List<String> {
        return drivers.viruses
            .filter(::isReportableHighDriver)
            .map { it -> it.event + (it.integrations?.let { " ($it int. detected)" } ?: "") }
            .distinct()
    }

    fun actionableEventsThatAreNotKeyDrivers(): List<Driver> {
        val nonDisruptionDrivers = listOf(
            drivers.variants,
            drivers.copyNumbers,
            drivers.fusions,
            drivers.homozygousDisruptions,
            drivers.viruses
        ).flatten().filterNot(::isReportableHighDriver)
        return (nonDisruptionDrivers + drivers.disruptions.toList())
            .filter(interpretedCohortsSummarizer::driverIsActionable)
    }

    private fun isReportableHighDriver(driver: Driver): Boolean {
        return driver.driverLikelihood == DriverLikelihood.HIGH && driver.isReportable
    }

    private fun annotateCopyNumber(minCopies: Int?, maxCopies: Int?, impactType: CopyNumberType): String {
        return when (impactType) {
            CopyNumberType.FULL_GAIN -> minCopies?.let { " $minCopies copies" } ?: ""
            CopyNumberType.PARTIAL_GAIN -> maxCopies?.let { " $maxCopies copies (partial)" } ?: " (partial)"
            CopyNumberType.NONE -> " (alt transcript)"
            else -> ""
        }
    }

    companion object {
        fun fromMolecularDriversAndEvaluatedCohorts(
            drivers: Drivers,
            cohorts: List<InterpretedCohort>
        ): MolecularDriversSummarizer {
            return MolecularDriversSummarizer(drivers, InterpretedCohortsSummarizer.fromCohorts(cohorts))
        }
    }
}