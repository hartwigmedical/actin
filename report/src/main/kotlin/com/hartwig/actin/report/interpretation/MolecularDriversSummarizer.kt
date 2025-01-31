package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.driver.Driver
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.GeneAlteration
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType

class MolecularDriversSummarizer private constructor(
    private val drivers: Drivers,
    private val interpretedCohortsSummarizer: InterpretedCohortsSummarizer
) {

    fun keyVariants(): List<String> {
        return drivers.variants.filter(::isKeyDriver).map { it.event }
    }

    fun keyAmplifiedGenes(): List<String> {
        return drivers.copyNumbers
            .asSequence()
            .filter { copyNumber -> copyNumber.canonicalImpact.type.isGain || copyNumber.otherImpacts.any { it.type.isGain } }
            .filter(::isKeyDriver)
            .map {
                it.gene + if (it.canonicalImpact.type == CopyNumberType.PARTIAL_GAIN) " (partial)" else "" +
                        if (it.canonicalImpact.type == CopyNumberType.NONE) " (alt transcript)" else ""
            }
            .distinct()
            .toList()
    }

    fun keyDeletedGenes(): List<String> {
        return drivers.copyNumbers
            .asSequence()
            .filter { copyNumber -> copyNumber.canonicalImpact.type.isLoss || copyNumber.otherImpacts.any { it.type.isLoss } }
            .filter(::isKeyDriver)
            .map { it.gene + if (it.canonicalImpact.type == CopyNumberType.NONE) " (alt transcript)" else "" }
            .distinct()
            .toList()
    }

    fun keyHomozygouslyDisruptedGenes(): List<String> {
        return drivers.homozygousDisruptions.filter(::isKeyDriver).map(GeneAlteration::gene).distinct()
    }

    fun keyFusionEvents(): List<String> {
        return drivers.fusions.filter(::isKeyDriver).map(Fusion::event).distinct()
    }

    fun keyVirusEvents(): List<String> {
        return drivers.viruses
            .filter(::isKeyDriver)
            .map { String.format("%s (%s int. detected)", it.event, it.integrations) }
            .distinct()
    }

    fun actionableEventsThatAreNotKeyDrivers(): List<Driver> {
        val nonDisruptionDrivers = listOf(
            drivers.variants,
            drivers.copyNumbers,
            drivers.fusions,
            drivers.homozygousDisruptions,
            drivers.viruses
        ).flatten().filterNot(::isKeyDriver)
        return (nonDisruptionDrivers + drivers.disruptions.toList())
            .filter(interpretedCohortsSummarizer::driverIsActionable)
    }

    private fun isKeyDriver(driver: Driver): Boolean {
        return driver.driverLikelihood == DriverLikelihood.HIGH && driver.isReportable
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