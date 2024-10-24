package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.Driver
import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.Drivers
import com.hartwig.actin.datamodel.molecular.Fusion
import com.hartwig.actin.datamodel.molecular.GeneAlteration
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumberType

class MolecularDriversSummarizer private constructor(
    private val drivers: Drivers,
    private val evaluatedCohortsInterpreter: EvaluatedCohortsInterpreter
) {

    fun keyVariants(): List<String> {
        return drivers.variants.filter(::isKeyDriver).map { it.event }
    }

    fun keyAmplifiedGenes(): List<String> {
        return drivers.copyNumbers
            .asSequence()
            .filter { it.type.isGain }
            .filter(::isKeyDriver)
            .map { it.gene + if (it.type == CopyNumberType.PARTIAL_GAIN) " (partial)" else "" }
            .distinct()
            .toList()
    }

    fun keyDeletedGenes(): List<String> {
        return keyGenesForAlterations(drivers.copyNumbers.filter { it.type.isLoss })
    }

    fun keyHomozygouslyDisruptedGenes(): List<String> {
        return keyGenesForAlterations(drivers.homozygousDisruptions)
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
            .filter(evaluatedCohortsInterpreter::driverIsActionable)
    }

    companion object {
        fun fromMolecularDriversAndEvaluatedCohorts(
            drivers: Drivers,
            cohorts: List<EvaluatedCohort>
        ): MolecularDriversSummarizer {
            return MolecularDriversSummarizer(drivers, EvaluatedCohortsInterpreter.fromEvaluatedCohorts(cohorts))
        }

        private fun isKeyDriver(driver: Driver): Boolean {
            return driver.driverLikelihood == DriverLikelihood.HIGH && driver.isReportable
        }

        private fun <T> keyGenesForAlterations(geneAlterationStream: Iterable<T>): List<String> where T : GeneAlteration, T : Driver {
            return geneAlterationStream.filter(::isKeyDriver).map(GeneAlteration::gene).distinct()
        }
    }
}