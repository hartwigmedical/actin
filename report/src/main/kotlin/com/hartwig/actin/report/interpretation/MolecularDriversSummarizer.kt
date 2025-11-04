package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.driver.Driver
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.GeneAlteration

class MolecularDriversSummarizer private constructor(
    private val drivers: Drivers,
    private val interpretedCohortsSummarizer: InterpretedCohortsSummarizer
) {

    fun keyVariantEvents(): List<String> {
        val highDriverVariants = drivers.variants.filter(::isReportableHighDriver)
        val variantsAssociatedWithDrugResistance = drivers.variants.filter { it.isReportable && it.isAssociatedWithDrugResistance == true }
        return (highDriverVariants + variantsAssociatedWithDrugResistance).toSet().map { it.eventDisplay() }.sorted()
    }

    fun otherVariantEvents(): List<String> =
        drivers.variants.asSequence().filter { it.isReportable }.map { it.eventDisplay() }.filterNot { it in keyVariantEvents() }.toSet()
            .sorted().toList()

    fun keyAmplifiedGeneEvents(): List<String> =
        drivers.copyNumbers
            .asSequence()
            .filter { copyNumber -> copyNumber.canonicalImpact.type.isGain || copyNumber.otherImpacts.any { it.type.isGain } }
            .filter(::isReportableHighDriver)
            .map { it.eventDisplay() }
            .distinct()
            .toList()

    fun keyDeletedGeneEvents(): List<String> =
        drivers.copyNumbers
            .asSequence()
            .filter { copyNumber -> copyNumber.canonicalImpact.type.isDeletion || copyNumber.otherImpacts.any { it.type.isDeletion } }
            .filter(::isReportableHighDriver)
            .map { it.eventDisplay() }
            .distinct()
            .toList()

    fun keyHomozygouslyDisruptedGenes(): List<String> =
        drivers.homozygousDisruptions.filter(::isReportableHighDriver).map(GeneAlteration::gene).distinct()

    fun keyFusionEvents(): List<String> =
        drivers.fusions.filter(::isReportableHighDriver).map(Fusion::eventDisplay).distinct()

    fun keyVirusEvents(): List<String> =
        drivers.viruses.filter(::isReportableHighDriver).map { it.eventDisplay() }.distinct()

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

    private fun isReportableHighDriver(driver: Driver): Boolean =
        driver.driverLikelihood == DriverLikelihood.HIGH && driver.isReportable

    companion object {
        fun fromMolecularDriversAndEvaluatedCohorts(
            drivers: Drivers,
            cohorts: List<InterpretedCohort>
        ): MolecularDriversSummarizer {
            return MolecularDriversSummarizer(drivers, InterpretedCohortsSummarizer.fromCohorts(cohorts))
        }

        private fun <T : Driver> List<T>.matchingLikelihood(useHighDrivers: Boolean) =
            this.filter { (it.driverLikelihood == DriverLikelihood.HIGH) == useHighDrivers }

        fun filterDriversByDriverLikelihood(drivers: Drivers, useHighDrivers: Boolean): Drivers {
            return with(drivers) {
                Drivers(
                    variants = variants.matchingLikelihood(useHighDrivers),
                    copyNumbers = copyNumbers.matchingLikelihood(useHighDrivers),
                    homozygousDisruptions = homozygousDisruptions.matchingLikelihood(useHighDrivers),
                    disruptions = disruptions.matchingLikelihood(useHighDrivers),
                    fusions = fusions.matchingLikelihood(useHighDrivers),
                    viruses = viruses.matchingLikelihood(useHighDrivers)
                )
            }
        }
    }
}