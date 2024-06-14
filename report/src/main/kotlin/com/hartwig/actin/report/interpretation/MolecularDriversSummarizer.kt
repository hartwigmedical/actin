package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.Driver
import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.Drivers
import com.hartwig.actin.molecular.datamodel.Fusion
import com.hartwig.actin.molecular.datamodel.GeneAlteration
import com.hartwig.actin.molecular.datamodel.orange.driver.CopyNumberType

class MolecularDriversSummarizer private constructor(
    private val drivers: Drivers,
    private val evaluatedCohortsInterpreter: EvaluatedCohortsInterpreter
) {
    fun keyGenesWithVariants(): List<String> {
        return keyGenesForAlterations(drivers.variants)
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

    fun actionableEventsThatAreNotKeyDrivers(): List<String> {
        val nonDisruptionDrivers = listOf(
            drivers.variants,
            drivers.copyNumbers,
            drivers.fusions,
            drivers.homozygousDisruptions,
            drivers.viruses
        ).flatten().filterNot(::isKeyDriver)
        return (nonDisruptionDrivers + drivers.disruptions.toList())
            .filter(evaluatedCohortsInterpreter::driverIsActionable)
            .map(Driver::event)
            .distinct()
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